package com.generated.qualityTrace.types;

import java.util.List;

/**
 * 拆分/合并共用的请求载体：拆分用 parentBatchNo/parentQuantity/children，
 * 合并用 targetBatchNo/targetQuantity/sources，bizNo 为幂等业务单号。
 */
public record BatchGenealogyPayload(
    String bizNo,
    String parentBatchNo,
    Long parentQuantity,
    String targetBatchNo,
    Long targetQuantity,
    List<BatchQty> children,
    List<BatchQty> sources) {
  public record BatchQty(String batchNo, Long quantity) {}
}
