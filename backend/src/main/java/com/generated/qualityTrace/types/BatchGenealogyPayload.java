package com.generated.qualityTrace.types;

import java.util.List;

public final class BatchGenealogyPayload {

  public record SplitChild(String batchNo, Integer quantity) {}

  public record SplitRequest(String docNo, String parentBatchNo, List<SplitChild> children) {}

  public record MergeSource(String batchNo, Integer quantity) {}

  public record MergeRequest(String docNo, String targetBatchNo, Integer mergeQuantity,
                             List<MergeSource> sources) {}

  private BatchGenealogyPayload() {}
}
