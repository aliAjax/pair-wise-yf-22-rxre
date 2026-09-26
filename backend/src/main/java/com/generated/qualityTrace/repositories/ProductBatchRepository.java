package com.generated.qualityTrace.repositories;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ProductBatchRepository {
  /** 批次注册表：批号 -> 批次档案（工单、原料批、数量），供谱系校验与追溯查询使用。 */
  private final ConcurrentHashMap<String, Map<String, Object>> registry = new ConcurrentHashMap<>();

  public ProductBatchRepository() {
    seed("B2026-0001", "WO-1001", 100L, "MAT-LOT-001");
    seed("B2026-0002", "WO-1002", 200L, "MAT-LOT-002");
    seed("B2026-0003", "WO-1003", 150L, "MAT-LOT-003");
  }

  private void seed(String batchNo, String workOrderId, long quantity, String materialLotNo) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", (long) (registry.size() + 1));
    row.put("batchNo", batchNo);
    row.put("workOrderId", workOrderId);
    row.put("quantity", quantity);
    row.put("materialLotNo", materialLotNo);
    row.put("producedAt", "2026-09-01T00:00:00Z");
    row.put("batchStatus", "READY");
    registry.put(batchNo, row);
  }

  public List<Map<String,Object>> findAll(){ return List.of(Map.of("id",1,"name","产品批次","status","READY")); }

  public Optional<Map<String, Object>> findByBatchNo(String batchNo) {
    return Optional.ofNullable(registry.get(batchNo));
  }

  /** 拆分/合并产生的新批次登记入册；已存在的批号不覆盖。 */
  public void registerIfAbsent(Map<String, Object> row) {
    Object batchNo = row == null ? null : row.get("batchNo");
    if (batchNo != null) {
      registry.putIfAbsent(String.valueOf(batchNo), row);
    }
  }
}
