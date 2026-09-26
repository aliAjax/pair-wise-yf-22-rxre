package com.generated.qualityTrace.models;

public class BatchGenealogyLink {
  public Long id;
  public String docNo;
  public String sourceBatchNo;
  public String targetBatchNo;
  public Integer quantity;
  public String createdAt;

  public BatchGenealogyLink() {}

  public BatchGenealogyLink(Long id, String docNo, String sourceBatchNo, String targetBatchNo,
                            Integer quantity, String createdAt) {
    this.id = id;
    this.docNo = docNo;
    this.sourceBatchNo = sourceBatchNo;
    this.targetBatchNo = targetBatchNo;
    this.quantity = quantity;
    this.createdAt = createdAt;
  }
}
