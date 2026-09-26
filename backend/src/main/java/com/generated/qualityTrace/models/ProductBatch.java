package com.generated.qualityTrace.models;

public class ProductBatch {
  public Long id;
  public String batchNo;
  public Long workOrderId;
  public Integer quantity;
  public String materialLotNo;
  public String producedAt;
  public String batchStatus;

  public ProductBatch() {}

  public ProductBatch(Long id, String batchNo, Long workOrderId, Integer quantity,
                      String materialLotNo, String producedAt, String batchStatus) {
    this.id = id;
    this.batchNo = batchNo;
    this.workOrderId = workOrderId;
    this.quantity = quantity;
    this.materialLotNo = materialLotNo;
    this.producedAt = producedAt;
    this.batchStatus = batchStatus;
  }
}
