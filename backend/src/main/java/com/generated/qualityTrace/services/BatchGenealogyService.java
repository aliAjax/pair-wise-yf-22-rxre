package com.generated.qualityTrace.services;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.BatchStatus;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.GenealogyEventType;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.BatchGenealogyDtoFactory;
import com.generated.qualityTrace.constructors.WorkOrderDtoFactory;
import com.generated.qualityTrace.exceptions.BatchNotFoundException;
import com.generated.qualityTrace.exceptions.GenealogyRejectionException;
import com.generated.qualityTrace.models.BatchGenealogyEvent;
import com.generated.qualityTrace.models.BatchGenealogyLink;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.repositories.BatchGenealogyRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import com.generated.qualityTrace.types.BatchGenealogyPayload;
import com.generated.qualityTrace.types.BatchGenealogyResult;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.validators.BatchGenealogyValidator;

@Service
public class BatchGenealogyService {
  private static final Logger log = LoggerFactory.getLogger(BatchGenealogyService.class);

  private final BatchGenealogyRepository genealogyRepo;
  private final ProductBatchRepository batchRepo;
  private final WorkOrderRepository workOrderRepo;

  public BatchGenealogyService(BatchGenealogyRepository genealogyRepo,
                               ProductBatchRepository batchRepo,
                               WorkOrderRepository workOrderRepo) {
    this.genealogyRepo = genealogyRepo;
    this.batchRepo = batchRepo;
    this.workOrderRepo = workOrderRepo;
  }

  public synchronized BatchGenealogyResult registerSplit(BatchGenealogyPayload.SplitRequest req) {
    Optional<BatchGenealogyResult> replay = replayIfRegistered(req == null ? null : req.docNo());
    if (replay.isPresent()) return replay.get();

    BatchGenealogyValidator.validateSplit(req);
    ProductBatch parent = batchRepo.findByBatchNo(req.parentBatchNo())
        .orElseThrow(() -> new BatchNotFoundException(req.parentBatchNo()));
    ensureNotConsumed(parent);

    int sum = req.children().stream().mapToInt(c -> c.quantity().intValue()).sum();
    if (sum != parent.quantity.intValue()) {
      throw rejection(ErrorCodes.GENEALOGY_QUANTITY_MISMATCH,
          "children sum " + sum + " != parent batch " + parent.batchNo + " quantity " + parent.quantity);
    }
    for (BatchGenealogyPayload.SplitChild child : req.children()) {
      if (wouldCreateCycle(parent.batchNo, child.batchNo())) {
        throw cycle(parent.batchNo, child.batchNo());
      }
      if (batchRepo.findByBatchNo(child.batchNo()).isPresent()) {
        throw rejection(ErrorCodes.BATCH_ALREADY_EXISTS,
            String.format(ErrorMessages.BATCH_ALREADY_EXISTS, child.batchNo()));
      }
    }

    String now = Formatters.now();
    BatchGenealogyEvent event = genealogyRepo.saveEvent(new BatchGenealogyEvent(
        null, req.docNo(), GenealogyEventType.SPLIT.name(), parent.batchNo, sum, now));
    List<BatchGenealogyLink> links = new ArrayList<>();
    for (BatchGenealogyPayload.SplitChild child : req.children()) {
      links.add(genealogyRepo.saveLink(new BatchGenealogyLink(
          null, req.docNo(), parent.batchNo, child.batchNo(), child.quantity(), now)));
      batchRepo.save(new ProductBatch(null, child.batchNo(), parent.workOrderId,
          child.quantity(), parent.materialLotNo, now, BatchStatus.READY));
    }
    parent.batchStatus = BatchStatus.SPLIT;

    Map<String, Object> body = BatchGenealogyDtoFactory.splitReceipt(event, links);
    event.resultSnapshot = body;
    log.info(String.format(LogTemplates.GENEALOGY_SPLIT,
        Formatters.docRef(req.docNo()), Formatters.batchRef(parent.batchNo), links.size(), sum));
    return new BatchGenealogyResult(body, false);
  }

  public synchronized BatchGenealogyResult registerMerge(BatchGenealogyPayload.MergeRequest req) {
    Optional<BatchGenealogyResult> replay = replayIfRegistered(req == null ? null : req.docNo());
    if (replay.isPresent()) return replay.get();

    BatchGenealogyValidator.validateMerge(req);
    List<ProductBatch> sources = new ArrayList<>();
    for (BatchGenealogyPayload.MergeSource s : req.sources()) {
      ProductBatch b = batchRepo.findByBatchNo(s.batchNo())
          .orElseThrow(() -> new BatchNotFoundException(s.batchNo()));
      ensureNotConsumed(b);
      if (s.quantity().intValue() != b.quantity.intValue()) {
        throw rejection(ErrorCodes.GENEALOGY_QUANTITY_MISMATCH,
            "source " + s.batchNo() + " declared " + s.quantity() + " != registered " + b.quantity);
      }
      sources.add(b);
    }
    int sum = sources.stream().mapToInt(b -> b.quantity.intValue()).sum();
    if (sum != req.mergeQuantity().intValue()) {
      throw rejection(ErrorCodes.GENEALOGY_QUANTITY_MISMATCH,
          "merge quantity " + req.mergeQuantity() + " != sources sum " + sum);
    }
    for (BatchGenealogyPayload.MergeSource s : req.sources()) {
      if (wouldCreateCycle(s.batchNo(), req.targetBatchNo())) {
        throw cycle(s.batchNo(), req.targetBatchNo());
      }
    }

    String now = Formatters.now();
    Optional<ProductBatch> targetOpt = batchRepo.findByBatchNo(req.targetBatchNo());
    ProductBatch target;
    if (targetOpt.isPresent()) {
      target = targetOpt.get();
      ensureNotConsumed(target);
      target.quantity = target.quantity + req.mergeQuantity();
    } else {
      String lots = sources.stream().map(b -> b.materialLotNo).filter(Objects::nonNull)
          .distinct().collect(Collectors.joining("+"));
      target = batchRepo.save(new ProductBatch(null, req.targetBatchNo(), null,
          req.mergeQuantity(), lots.isEmpty() ? null : lots, now, BatchStatus.READY));
    }
    for (ProductBatch b : sources) b.batchStatus = BatchStatus.MERGED;

    BatchGenealogyEvent event = genealogyRepo.saveEvent(new BatchGenealogyEvent(
        null, req.docNo(), GenealogyEventType.MERGE.name(), target.batchNo, req.mergeQuantity(), now));
    List<BatchGenealogyLink> links = new ArrayList<>();
    for (BatchGenealogyPayload.MergeSource s : req.sources()) {
      links.add(genealogyRepo.saveLink(new BatchGenealogyLink(
          null, req.docNo(), s.batchNo(), target.batchNo, s.quantity(), now)));
    }

    Map<String, Object> body = BatchGenealogyDtoFactory.mergeReceipt(event, links);
    event.resultSnapshot = body;
    log.info(String.format(LogTemplates.GENEALOGY_MERGE,
        Formatters.docRef(req.docNo()), Formatters.batchRef(target.batchNo), links.size(), req.mergeQuantity()));
    return new BatchGenealogyResult(body, false);
  }

  public Map<String, Object> trace(String batchNo) {
    ProductBatch batch = batchRepo.findByBatchNo(batchNo)
        .orElseThrow(() -> new BatchNotFoundException(batchNo));
    List<ProductBatch> upstream = collectUpstream(batchNo).stream()
        .map(batchRepo::findByBatchNo).flatMap(Optional::stream).toList();
    List<ProductBatch> downstream = collectDownstream(batchNo).stream()
        .map(batchRepo::findByBatchNo).flatMap(Optional::stream).toList();
    List<Map<String, Object>> workOrders = Stream.concat(Stream.of(batch), upstream.stream())
        .map(b -> b.workOrderId).filter(Objects::nonNull).distinct()
        .map(workOrderRepo::findById).flatMap(Optional::stream)
        .map(WorkOrderDtoFactory::toMap).toList();
    List<String> materialLots = Stream.concat(Stream.of(batch), upstream.stream())
        .map(b -> b.materialLotNo).filter(Objects::nonNull).distinct().toList();
    log.info(String.format(LogTemplates.GENEALOGY_TRACE, batchNo, upstream.size(), downstream.size()));
    return BatchGenealogyDtoFactory.trace(batch, upstream, workOrders, materialLots, downstream);
  }

  public List<Map<String, Object>> listEvents() {
    return genealogyRepo.findAllEvents().stream().map(BatchGenealogyDtoFactory::eventToMap).toList();
  }

  public List<Map<String, Object>> listLinks() {
    return genealogyRepo.findAllLinks().stream().map(BatchGenealogyDtoFactory::linkToMap).toList();
  }

  private Optional<BatchGenealogyResult> replayIfRegistered(String docNo) {
    if (docNo == null || docNo.isBlank()) return Optional.empty();
    return genealogyRepo.findEventByDocNo(docNo).map(e -> {
      log.info(String.format(LogTemplates.GENEALOGY_REPLAY, Formatters.docRef(docNo)));
      return new BatchGenealogyResult(e.resultSnapshot, true);
    });
  }

  private boolean wouldCreateCycle(String sourceBatchNo, String targetBatchNo) {
    if (sourceBatchNo.equals(targetBatchNo)) return true;
    Set<String> visited = new LinkedHashSet<>();
    Deque<String> stack = new ArrayDeque<>();
    stack.push(targetBatchNo);
    while (!stack.isEmpty()) {
      String cur = stack.pop();
      for (BatchGenealogyLink l : genealogyRepo.findLinksBySource(cur)) {
        if (l.targetBatchNo.equals(sourceBatchNo)) return true;
        if (visited.add(l.targetBatchNo)) stack.push(l.targetBatchNo);
      }
    }
    return false;
  }

  private Set<String> collectUpstream(String batchNo) {
    Set<String> visited = new LinkedHashSet<>();
    Deque<String> stack = new ArrayDeque<>();
    stack.push(batchNo);
    while (!stack.isEmpty()) {
      String cur = stack.pop();
      for (BatchGenealogyLink l : genealogyRepo.findLinksByTarget(cur)) {
        if (visited.add(l.sourceBatchNo)) stack.push(l.sourceBatchNo);
      }
    }
    return visited;
  }

  private Set<String> collectDownstream(String batchNo) {
    Set<String> visited = new LinkedHashSet<>();
    Deque<String> stack = new ArrayDeque<>();
    stack.push(batchNo);
    while (!stack.isEmpty()) {
      String cur = stack.pop();
      for (BatchGenealogyLink l : genealogyRepo.findLinksBySource(cur)) {
        if (visited.add(l.targetBatchNo)) stack.push(l.targetBatchNo);
      }
    }
    return visited;
  }

  private void ensureNotConsumed(ProductBatch b) {
    if (BatchStatus.SPLIT.equals(b.batchStatus) || BatchStatus.MERGED.equals(b.batchStatus)) {
      throw rejection(ErrorCodes.GENEALOGY_BATCH_CONSUMED,
          String.format(ErrorMessages.GENEALOGY_BATCH_CONSUMED, b.batchNo, b.batchStatus));
    }
  }

  private GenealogyRejectionException cycle(String sourceBatchNo, String targetBatchNo) {
    return rejection(ErrorCodes.GENEALOGY_CYCLE_DETECTED,
        String.format(ErrorMessages.GENEALOGY_CYCLE_DETECTED, sourceBatchNo, targetBatchNo));
  }

  private GenealogyRejectionException rejection(String code, String message) {
    return new GenealogyRejectionException(code, message);
  }
}
