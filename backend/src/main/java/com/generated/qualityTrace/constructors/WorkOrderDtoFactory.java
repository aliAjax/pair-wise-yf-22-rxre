package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.WorkOrder;

public final class WorkOrderDtoFactory {

  public static Map<String, Object> create() { return Map.of("id", 1, "name", "生产工单"); }

  public static Map<String, Object> toMap(WorkOrder w) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", w.id);
    m.put("orderNo", w.orderNo);
    m.put("productCode", w.productCode);
    m.put("productName", w.productName);
    m.put("plannedQty", w.plannedQty);
    m.put("lineCode", w.lineCode);
    m.put("startAt", w.startAt);
    m.put("status", w.status);
    return m;
  }

  private WorkOrderDtoFactory() {}
}
