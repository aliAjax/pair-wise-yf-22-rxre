package com.generated.qualityTrace.models;

import java.util.Map;

public class BatchGenealogyEvent {
  public Long id;
  public String docNo;
  public String eventType;
  public String anchorBatchNo;
  public Integer totalQuantity;
  public String createdAt;
  public Map<String, Object> resultSnapshot;

  public BatchGenealogyEvent() {}

  public BatchGenealogyEvent(Long id, String docNo, String eventType, String anchorBatchNo,
                             Integer totalQuantity, String createdAt) {
    this.id = id;
    this.docNo = docNo;
    this.eventType = eventType;
    this.anchorBatchNo = anchorBatchNo;
    this.totalQuantity = totalQuantity;
    this.createdAt = createdAt;
  }
}
