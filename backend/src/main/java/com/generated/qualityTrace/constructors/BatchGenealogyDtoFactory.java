package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.generated.qualityTrace.models.BatchGenealogyEvent;
import com.generated.qualityTrace.models.BatchGenealogyLink;
import com.generated.qualityTrace.models.ProductBatch;

public final class BatchGenealogyDtoFactory {

  public static Map<String, Object> linkToMap(BatchGenealogyLink l) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", l.id);
    m.put("docNo", l.docNo);
    m.put("sourceBatchNo", l.sourceBatchNo);
    m.put("targetBatchNo", l.targetBatchNo);
    m.put("quantity", l.quantity);
    m.put("createdAt", l.createdAt);
    return m;
  }

  public static Map<String, Object> eventToMap(BatchGenealogyEvent e) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", e.id);
    m.put("docNo", e.docNo);
    m.put("eventType", e.eventType);
    m.put("anchorBatchNo", e.anchorBatchNo);
    m.put("totalQuantity", e.totalQuantity);
    m.put("createdAt", e.createdAt);
    return m;
  }

  public static Map<String, Object> splitReceipt(BatchGenealogyEvent e, List<BatchGenealogyLink> links) {
    return receipt(e, "parentBatchNo", links);
  }

  public static Map<String, Object> mergeReceipt(BatchGenealogyEvent e, List<BatchGenealogyLink> links) {
    return receipt(e, "targetBatchNo", links);
  }

  private static Map<String, Object> receipt(BatchGenealogyEvent e, String anchorKey,
                                             List<BatchGenealogyLink> links) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("docNo", e.docNo);
    m.put("eventType", e.eventType);
    m.put(anchorKey, e.anchorBatchNo);
    m.put("totalQuantity", e.totalQuantity);
    m.put("links", links.stream().map(BatchGenealogyDtoFactory::linkToMap).toList());
    m.put("createdAt", e.createdAt);
    return m;
  }

  public static Map<String, Object> trace(ProductBatch batch, List<ProductBatch> upstream,
                                          List<Map<String, Object>> workOrders,
                                          List<String> materialLots, List<ProductBatch> downstream) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("batchNo", batch.batchNo);
    m.put("batch", ProductBatchDtoFactory.toMap(batch));
    m.put("upstreamBatches", upstream.stream().map(ProductBatchDtoFactory::toMap).toList());
    m.put("upstreamWorkOrders", workOrders);
    m.put("materialLots", materialLots);
    m.put("downstreamBatches", downstream.stream().map(ProductBatchDtoFactory::toMap).toList());
    return m;
  }

  public static Map<String, Object> error(String code, String message) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("error", code);
    m.put("message", message);
    return m;
  }

  private BatchGenealogyDtoFactory() {}
}
