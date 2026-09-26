package com.generated.qualityTrace.models;

import java.util.ArrayList;
import java.util.List;

/**
 * 批次转换单（拆分/合并）表头：一张业务单对应一次拆分或合并登记。
 * anchorBatchNo 拆分时为父批批号，合并时为合并后批号。
 */
public class BatchTransform {
  public Long id;
  public String bizNo;
  public String action;
  public String anchorBatchNo;
  public long totalQuantity;
  public String createdAt;
  public List<BatchTransformLine> lines = new ArrayList<>();
}
