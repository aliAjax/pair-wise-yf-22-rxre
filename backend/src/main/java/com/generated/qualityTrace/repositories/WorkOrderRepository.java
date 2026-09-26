package com.generated.qualityTrace.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.constructors.WorkOrderDtoFactory;
import com.generated.qualityTrace.models.WorkOrder;

@Repository
public class WorkOrderRepository {
  private final List<WorkOrder> orders = new CopyOnWriteArrayList<>();

  public WorkOrderRepository() {
    orders.add(new WorkOrder(1L, "WO-2026-1001", "P-100", "变速箱壳体", 1200, "LINE-A", "2026-09-01T08:00:00+08:00", WorkOrderStatus.RUNNING.name()));
    orders.add(new WorkOrder(2L, "WO-2026-1002", "P-200", "液压阀体", 900, "LINE-B", "2026-09-03T08:00:00+08:00", WorkOrderStatus.RUNNING.name()));
    orders.add(new WorkOrder(3L, "WO-2026-1003", "P-300", "返工支架", 400, "LINE-C", "2026-09-10T08:00:00+08:00", WorkOrderStatus.PLANNED.name()));
  }

  public List<Map<String, Object>> findAll() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (WorkOrder w : orders) out.add(WorkOrderDtoFactory.toMap(w));
    return out;
  }

  public Optional<WorkOrder> findById(Long id) {
    return orders.stream().filter(w -> w.id.equals(id)).findFirst();
  }
}
