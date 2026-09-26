package com.generated.qualityTrace.models;

/**
 * 批次谱系边：来源批 -> 去向批，附数量。
 * 拆分时 source=父批、target=子批；合并时 source=来源批、target=合并批。
 */
public class BatchTransformLine {
  public Long id;
  public String bizNo;
  public String action;
  public String sourceBatchNo;
  public String targetBatchNo;
  public long quantity;
}
