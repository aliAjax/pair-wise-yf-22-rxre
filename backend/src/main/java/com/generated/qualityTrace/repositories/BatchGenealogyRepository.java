package com.generated.qualityTrace.repositories;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.models.BatchGenealogyEvent;
import com.generated.qualityTrace.models.BatchGenealogyLink;

@Repository
public class BatchGenealogyRepository {
  private final List<BatchGenealogyEvent> events = new CopyOnWriteArrayList<>();
  private final List<BatchGenealogyLink> links = new CopyOnWriteArrayList<>();
  private final AtomicLong eventIds = new AtomicLong(1000);
  private final AtomicLong linkIds = new AtomicLong(1000);

  public Optional<BatchGenealogyEvent> findEventByDocNo(String docNo) {
    return events.stream().filter(e -> e.docNo.equals(docNo)).findFirst();
  }

  public BatchGenealogyEvent saveEvent(BatchGenealogyEvent e) {
    if (e.id == null) e.id = eventIds.incrementAndGet();
    events.add(e);
    return e;
  }

  public BatchGenealogyLink saveLink(BatchGenealogyLink l) {
    if (l.id == null) l.id = linkIds.incrementAndGet();
    links.add(l);
    return l;
  }

  public List<BatchGenealogyLink> findLinksBySource(String sourceBatchNo) {
    return links.stream().filter(l -> l.sourceBatchNo.equals(sourceBatchNo)).toList();
  }

  public List<BatchGenealogyLink> findLinksByTarget(String targetBatchNo) {
    return links.stream().filter(l -> l.targetBatchNo.equals(targetBatchNo)).toList();
  }

  public List<BatchGenealogyEvent> findAllEvents() { return List.copyOf(events); }

  public List<BatchGenealogyLink> findAllLinks() { return List.copyOf(links); }
}
