package com.generated.qualityTrace.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.models.BatchTransform;
import com.generated.qualityTrace.models.BatchTransformLine;

/**
 * 批次谱系仓储：按 bizNo 存转换单（幂等键），按来源/去向索引谱系边。
 */
@Repository
public class BatchGenealogyRepository {
  private final ConcurrentHashMap<String, BatchTransform> transforms = new ConcurrentHashMap<>();
  private final CopyOnWriteArrayList<BatchTransformLine> lines = new CopyOnWriteArrayList<>();
  private final AtomicLong idSeq = new AtomicLong(1);
  private final AtomicLong lineSeq = new AtomicLong(1);

  public Optional<BatchTransform> findByBizNo(String bizNo) {
    return Optional.ofNullable(transforms.get(bizNo));
  }

  public BatchTransform save(BatchTransform t) {
    t.id = idSeq.getAndIncrement();
    for (BatchTransformLine l : t.lines) {
      l.id = lineSeq.getAndIncrement();
    }
    transforms.put(t.bizNo, t);
    lines.addAll(t.lines);
    return t;
  }

  public List<BatchTransform> findAll() {
    List<BatchTransform> all = new ArrayList<>(transforms.values());
    all.sort(Comparator.comparing(t -> t.id));
    return all;
  }

  public List<BatchTransformLine> findLinesBySource(String batchNo) {
    return lines.stream().filter(l -> l.sourceBatchNo.equals(batchNo)).toList();
  }

  public List<BatchTransformLine> findLinesByTarget(String batchNo) {
    return lines.stream().filter(l -> l.targetBatchNo.equals(batchNo)).toList();
  }
}
