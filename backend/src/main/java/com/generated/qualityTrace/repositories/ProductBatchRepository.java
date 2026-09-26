package com.generated.qualityTrace.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.constants.BatchStatus;
import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.models.ProductBatch;

@Repository
public class ProductBatchRepository {
  private final List<ProductBatch> batches = new CopyOnWriteArrayList<>();
  private final AtomicLong ids = new AtomicLong(100);

  public ProductBatchRepository() {
    batches.add(new ProductBatch(1L, "B-2026-0001", 1L, 1000, "ML-A-01", "2026-09-02T10:00:00+08:00", BatchStatus.READY));
    batches.add(new ProductBatch(2L, "B-2026-0002", 1L, 500, "ML-A-02", "2026-09-05T10:00:00+08:00", BatchStatus.READY));
    batches.add(new ProductBatch(3L, "B-2026-0003", 2L, 800, "ML-B-01", "2026-09-06T10:00:00+08:00", BatchStatus.READY));
    batches.add(new ProductBatch(4L, "B-2026-0004", 3L, 300, "ML-C-01", "2026-09-11T10:00:00+08:00", BatchStatus.READY));
  }

  public List<Map<String, Object>> findAll() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (ProductBatch b : batches) out.add(ProductBatchDtoFactory.toMap(b));
    return out;
  }

  public Optional<ProductBatch> findByBatchNo(String batchNo) {
    return batches.stream().filter(b -> b.batchNo.equals(batchNo)).findFirst();
  }

  public ProductBatch save(ProductBatch b) {
    if (b.id == null) b.id = ids.incrementAndGet();
    batches.add(b);
    return b;
  }
}
