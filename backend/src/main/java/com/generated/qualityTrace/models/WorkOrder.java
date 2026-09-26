package com.generated.qualityTrace.models;

public class WorkOrder {
  public Long id;
  public String orderNo;
  public String productCode;
  public String productName;
  public Integer plannedQty;
  public String lineCode;
  public String startAt;
  public String status;

  public WorkOrder() {}

  public WorkOrder(Long id, String orderNo, String productCode, String productName,
                   Integer plannedQty, String lineCode, String startAt, String status) {
    this.id = id;
    this.orderNo = orderNo;
    this.productCode = productCode;
    this.productName = productName;
    this.plannedQty = plannedQty;
    this.lineCode = lineCode;
    this.startAt = startAt;
    this.status = status;
  }
}
