package com.generated.qualityTrace.constructors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.generated.qualityTrace.constants.GenealogyAction;
import com.generated.qualityTrace.models.BatchTransform;
import com.generated.qualityTrace.models.BatchTransformLine;

/** 批次谱系响应构造器：登记结果、追溯树、错误体都从这里组装。 */
public final class BatchGenealogyDtoFactory {
  private BatchGenealogyDtoFactory() {}

  public static Map<String, Object> transformResult(BatchTransform t) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("bizNo", t.bizNo);
    out.put("action", t.action);
    if (GenealogyAction.SPLIT.name().equals(t.action)) {
      out.put("parentBatchNo", t.anchorBatchNo);
    } else {
      out.put("targetBatchNo", t.anchorBatchNo);
    }
    out.put("totalQuantity", t.totalQuantity);
    List<Map<String, Object>> lines = new ArrayList<>();
    for (BatchTransformLine l : t.lines) {
      lines.add(line(l));
    }
    out.put("lines", lines);
    out.put("createdAt", t.createdAt);
    return out;
  }

  public static Map<String, Object> line(BatchTransformLine l) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("sourceBatchNo", l.sourceBatchNo);
    out.put("targetBatchNo", l.targetBatchNo);
    out.put("quantity", l.quantity);
    return out;
  }

  /** 追溯树上的相邻批次节点：批号 + 谱系边上的数量，能查到档案就带上工单和原料批。 */
  public static Map<String, Object> traceNode(String batchNo, long quantity, Map<String, Object> registryRow) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("batchNo", batchNo);
    out.put("quantity", quantity);
    if (registryRow != null) {
      if (registryRow.get("workOrderId") != null) {
        out.put("workOrderId", registryRow.get("workOrderId"));
      }
      if (registryRow.get("materialLotNo") != null) {
        out.put("materialLotNo", registryRow.get("materialLotNo"));
      }
    }
    return out;
  }

  public static Map<String, Object> traceResult(String batchNo, Map<String, Object> batch,
      List<Map<String, Object>> upstreamBatches, List<String> upstreamWorkOrders,
      List<String> upstreamMaterialLots, List<Map<String, Object>> downstreamBatches) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("batchNo", batchNo);
    out.put("batch", batch);
    out.put("upstreamWorkOrders", upstreamWorkOrders);
    out.put("upstreamMaterialLots", upstreamMaterialLots);
    out.put("upstreamBatches", upstreamBatches);
    out.put("downstreamBatches", downstreamBatches);
    return out;
  }

  public static Map<String, Object> error(String code, String message) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("errorCode", code);
    out.put("message", message);
    return out;
  }
}
