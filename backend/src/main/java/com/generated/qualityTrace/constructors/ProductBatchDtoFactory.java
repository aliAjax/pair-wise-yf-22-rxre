package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.ProductBatch;

public final class ProductBatchDtoFactory {

  public static Map<String, Object> create() { return Map.of("id", 1, "name", "产品批次"); }

  public static Map<String, Object> toMap(ProductBatch b) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", b.id);
    m.put("batchNo", b.batchNo);
    m.put("workOrderId", b.workOrderId);
    m.put("quantity", b.quantity);
    m.put("materialLotNo", b.materialLotNo);
    m.put("producedAt", b.producedAt);
    m.put("batchStatus", b.batchStatus);
    return m;
  }

  private ProductBatchDtoFactory() {}
}
