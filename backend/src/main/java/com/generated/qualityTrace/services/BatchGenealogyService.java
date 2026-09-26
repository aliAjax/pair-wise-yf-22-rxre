package com.generated.qualityTrace.services;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.GenealogyAction;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.BatchGenealogyDtoFactory;
import com.generated.qualityTrace.models.BatchTransform;
import com.generated.qualityTrace.models.BatchTransformLine;
import com.generated.qualityTrace.repositories.BatchGenealogyRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.types.BatchGenealogyPayload;
import com.generated.qualityTrace.types.BatchGenealogyPayload.BatchQty;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.validators.BatchGenealogyValidator;

/**
 * 批次谱系服务：拆分/合并登记（数量平衡 + 回环拒绝 + bizNo 幂等），
 * 以及按批号查询上游工单、原料批和下游批次。
 */
@Service
public class BatchGenealogyService {
  private static final Logger log = LoggerFactory.getLogger(BatchGenealogyService.class);

  private final BatchGenealogyRepository repo;
  private final ProductBatchRepository batchRepo;

  public BatchGenealogyService(BatchGenealogyRepository repo, ProductBatchRepository batchRepo) {
    this.repo = repo;
    this.batchRepo = batchRepo;
  }

  /** 拆分登记：父批 -> 多个子批，子批合计必须等于父批数量。 */
  public synchronized Map<String, Object> split(BatchGenealogyPayload p) {
    String bizNo = BatchGenealogyValidator.requireBizNo(p == null ? null : p.bizNo());
    Optional<BatchTransform> replay = repo.findByBizNo(bizNo);
    if (replay.isPresent()) {
      log.info(LogTemplates.GENEALOGY_REPLAY, bizNo);
      return BatchGenealogyDtoFactory.transformResult(replay.get());
    }
    String parent = BatchGenealogyValidator.requireBatchNo(p.parentBatchNo(), "parentBatchNo");
    long parentQty = BatchGenealogyValidator.requirePositive(p.parentQuantity(), "parentQuantity");
    List<BatchQty> children = BatchGenealogyValidator.requireNonEmpty(p.children(), "children");
    BatchGenealogyValidator.requireNoDuplicates(children);
    assertRegistryQuantity(parent, parentQty);

    long sum = 0;
    for (BatchQty c : children) {
      String childNo = BatchGenealogyValidator.requireBatchNo(c == null ? null : c.batchNo(), "children.batchNo");
      long q = BatchGenealogyValidator.requirePositive(c == null ? null : c.quantity(), "children.quantity");
      rejectCycle(childNo, parent);
      sum = Math.addExact(sum, q);
    }
    if (sum != parentQty) {
      throw new BatchGenealogyException(ErrorCodes.QUANTITY_MISMATCH,
          ErrorMessages.QUANTITY_MISMATCH + ": children total " + sum
              + " != parent batch " + parent + " quantity " + parentQty);
    }

    BatchTransform t = newTransform(bizNo, GenealogyAction.SPLIT, parent, parentQty);
    for (BatchQty c : children) {
      t.lines.add(newLine(bizNo, t.action, parent, c.batchNo().trim(), c.quantity()));
    }
    repo.save(t);
    for (BatchQty c : children) {
      registerDerived(c.batchNo().trim(), c.quantity(), parent, "SPLIT");
    }
    log.info(LogTemplates.GENEALOGY_SPLIT, bizNo, parent, children.size());
    return BatchGenealogyDtoFactory.transformResult(t);
  }

  /** 合并登记：多个来源批 -> 一个合并批，合并量必须等于各来源之和。 */
  public synchronized Map<String, Object> merge(BatchGenealogyPayload p) {
    String bizNo = BatchGenealogyValidator.requireBizNo(p == null ? null : p.bizNo());
    Optional<BatchTransform> replay = repo.findByBizNo(bizNo);
    if (replay.isPresent()) {
      log.info(LogTemplates.GENEALOGY_REPLAY, bizNo);
      return BatchGenealogyDtoFactory.transformResult(replay.get());
    }
    String target = BatchGenealogyValidator.requireBatchNo(p.targetBatchNo(), "targetBatchNo");
    long targetQty = BatchGenealogyValidator.requirePositive(p.targetQuantity(), "targetQuantity");
    List<BatchQty> sources = BatchGenealogyValidator.requireNonEmpty(p.sources(), "sources");
    BatchGenealogyValidator.requireNoDuplicates(sources);
    assertRegistryQuantity(target, targetQty);

    long sum = 0;
    for (BatchQty s : sources) {
      String srcNo = BatchGenealogyValidator.requireBatchNo(s == null ? null : s.batchNo(), "sources.batchNo");
      long q = BatchGenealogyValidator.requirePositive(s == null ? null : s.quantity(), "sources.quantity");
      rejectCycle(target, srcNo);
      sum = Math.addExact(sum, q);
    }
    if (sum != targetQty) {
      throw new BatchGenealogyException(ErrorCodes.QUANTITY_MISMATCH,
          ErrorMessages.QUANTITY_MISMATCH + ": sources total " + sum
              + " != merged batch " + target + " quantity " + targetQty);
    }

    BatchTransform t = newTransform(bizNo, GenealogyAction.MERGE, target, targetQty);
    for (BatchQty s : sources) {
      t.lines.add(newLine(bizNo, t.action, s.batchNo().trim(), target, s.quantity()));
    }
    repo.save(t);
    registerDerived(target, targetQty, null, "MERGED");
    log.info(LogTemplates.GENEALOGY_MERGE, bizNo, target, sources.size());
    return BatchGenealogyDtoFactory.transformResult(t);
  }

  /** 追溯查询：任一产品批号 -> 上游工单、原料批、下游批次。 */
  public Map<String, Object> trace(String batchNo) {
    String bn = BatchGenealogyValidator.requireBatchNo(batchNo, "batchNo");
    Map<String, Object> self = batchRepo.findByBatchNo(bn).orElse(null);

    Set<String> workOrders = new TreeSet<>();
    Set<String> materialLots = new TreeSet<>();
    collectRegistry(bn, workOrders, materialLots);

    List<Map<String, Object>> upstream = new ArrayList<>();
    Set<String> visitedUp = new HashSet<>();
    visitedUp.add(bn);
    Deque<String> upQueue = new ArrayDeque<>();
    upQueue.add(bn);
    while (!upQueue.isEmpty()) {
      String cur = upQueue.poll();
      for (BatchTransformLine l : repo.findLinesByTarget(cur)) {
        if (visitedUp.add(l.sourceBatchNo)) {
          upstream.add(BatchGenealogyDtoFactory.traceNode(
              l.sourceBatchNo, l.quantity, batchRepo.findByBatchNo(l.sourceBatchNo).orElse(null)));
          collectRegistry(l.sourceBatchNo, workOrders, materialLots);
          upQueue.add(l.sourceBatchNo);
        }
      }
    }

    List<Map<String, Object>> downstream = new ArrayList<>();
    Set<String> visitedDown = new HashSet<>();
    visitedDown.add(bn);
    Deque<String> downQueue = new ArrayDeque<>();
    downQueue.add(bn);
    while (!downQueue.isEmpty()) {
      String cur = downQueue.poll();
      for (BatchTransformLine l : repo.findLinesBySource(cur)) {
        if (visitedDown.add(l.targetBatchNo)) {
          downstream.add(BatchGenealogyDtoFactory.traceNode(
              l.targetBatchNo, l.quantity, batchRepo.findByBatchNo(l.targetBatchNo).orElse(null)));
          downQueue.add(l.targetBatchNo);
        }
      }
    }

    log.info(LogTemplates.GENEALOGY_TRACE, bn, upstream.size(), downstream.size());
    return BatchGenealogyDtoFactory.traceResult(
        bn, self, upstream, List.copyOf(workOrders), List.copyOf(materialLots), downstream);
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (BatchTransform t : repo.findAll()) {
      out.add(BatchGenealogyDtoFactory.transformResult(t));
    }
    return out;
  }

  /** 新增 from -> to 的边之前，若 to 已经能从 from 沿谱系到达（来源绕回原批）则拒绝。 */
  private void rejectCycle(String from, String to) {
    if (from.equals(to)) {
      throw new BatchGenealogyException(ErrorCodes.GENEALOGY_CYCLE,
          ErrorMessages.GENEALOGY_CYCLE + ": batch " + from + " refers to itself");
    }
    if (reachable(from, to)) {
      throw new BatchGenealogyException(ErrorCodes.GENEALOGY_CYCLE,
          ErrorMessages.GENEALOGY_CYCLE + ": " + from + " is already upstream of " + to);
    }
  }

  /** 沿 来源->去向 方向深度优先，判断 from 是否能到达 to。 */
  private boolean reachable(String from, String to) {
    Deque<String> stack = new ArrayDeque<>();
    Set<String> visited = new HashSet<>();
    stack.push(from);
    while (!stack.isEmpty()) {
      String cur = stack.pop();
      if (cur.equals(to)) {
        return true;
      }
      if (!visited.add(cur)) {
        continue;
      }
      for (BatchTransformLine l : repo.findLinesBySource(cur)) {
        stack.push(l.targetBatchNo);
      }
    }
    return false;
  }

  /** 已入册批次的登记数量必须与申报数量一致，防止张冠李戴。 */
  private void assertRegistryQuantity(String batchNo, long declared) {
    Optional<Map<String, Object>> row = batchRepo.findByBatchNo(batchNo);
    if (row.isPresent() && row.get().get("quantity") instanceof Number n && n.longValue() != declared) {
      throw new BatchGenealogyException(ErrorCodes.QUANTITY_MISMATCH,
          ErrorMessages.QUANTITY_MISMATCH + ": batch " + batchNo
              + " registered quantity " + n.longValue() + " != declared " + declared);
    }
  }

  /** 拆分/合并产生的新批次入册：拆分子批继承父批工单与原料批。 */
  private void registerDerived(String batchNo, long quantity, String parentBatchNo, String status) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("batchNo", batchNo);
    row.put("quantity", quantity);
    if (parentBatchNo != null) {
      Optional<Map<String, Object>> parent = batchRepo.findByBatchNo(parentBatchNo);
      if (parent.isPresent()) {
        if (parent.get().get("workOrderId") != null) {
          row.put("workOrderId", parent.get().get("workOrderId"));
        }
        if (parent.get().get("materialLotNo") != null) {
          row.put("materialLotNo", parent.get().get("materialLotNo"));
        }
      }
    }
    row.put("producedAt", Formatters.nowIso());
    row.put("batchStatus", status);
    batchRepo.registerIfAbsent(row);
  }

  private void collectRegistry(String batchNo, Set<String> workOrders, Set<String> materialLots) {
    Optional<Map<String, Object>> row = batchRepo.findByBatchNo(batchNo);
    if (row.isEmpty()) {
      return;
    }
    Object wo = row.get().get("workOrderId");
    Object lot = row.get().get("materialLotNo");
    if (wo != null) {
      workOrders.add(String.valueOf(wo));
    }
    if (lot != null) {
      materialLots.add(String.valueOf(lot));
    }
  }

  private BatchTransform newTransform(String bizNo, GenealogyAction action, String anchorBatchNo, long totalQuantity) {
    BatchTransform t = new BatchTransform();
    t.bizNo = bizNo;
    t.action = action.name();
    t.anchorBatchNo = anchorBatchNo;
    t.totalQuantity = totalQuantity;
    t.createdAt = Formatters.nowIso();
    return t;
  }

  private BatchTransformLine newLine(String bizNo, String action, String source, String target, long quantity) {
    BatchTransformLine l = new BatchTransformLine();
    l.bizNo = bizNo;
    l.action = action;
    l.sourceBatchNo = source;
    l.targetBatchNo = target;
    l.quantity = quantity;
    return l;
  }
}
