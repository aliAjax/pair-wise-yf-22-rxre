package com.generated.qualityTrace.validators;

import java.util.HashSet;
import java.util.Set;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.exceptions.GenealogyRejectionException;
import com.generated.qualityTrace.types.BatchGenealogyPayload;

public final class BatchGenealogyValidator {

  public static void validateSplit(BatchGenealogyPayload.SplitRequest req) {
    if (req == null) reject("request body is required");
    requireText(req.docNo(), "docNo");
    requireText(req.parentBatchNo(), "parentBatchNo");
    if (req.children() == null || req.children().isEmpty()) reject("children must not be empty");
    Set<String> seen = new HashSet<>();
    for (BatchGenealogyPayload.SplitChild child : req.children()) {
      if (child == null) reject("child entry must not be null");
      requireText(child.batchNo(), "child batchNo");
      requirePositive(child.quantity(), "child quantity");
      if (!seen.add(child.batchNo())) reject("duplicate child batchNo: " + child.batchNo());
    }
  }

  public static void validateMerge(BatchGenealogyPayload.MergeRequest req) {
    if (req == null) reject("request body is required");
    requireText(req.docNo(), "docNo");
    requireText(req.targetBatchNo(), "targetBatchNo");
    requirePositive(req.mergeQuantity(), "mergeQuantity");
    if (req.sources() == null || req.sources().isEmpty()) reject("sources must not be empty");
    Set<String> seen = new HashSet<>();
    for (BatchGenealogyPayload.MergeSource source : req.sources()) {
      if (source == null) reject("source entry must not be null");
      requireText(source.batchNo(), "source batchNo");
      requirePositive(source.quantity(), "source quantity");
      if (!seen.add(source.batchNo())) reject("duplicate source batchNo: " + source.batchNo());
    }
  }

  private static void requireText(String value, String field) {
    if (value == null || value.isBlank()) reject(field + " is required");
  }

  private static void requirePositive(Integer value, String field) {
    if (value == null || value <= 0) reject(field + " must be a positive integer");
  }

  private static void reject(String detail) {
    throw new GenealogyRejectionException(
        ErrorCodes.GENEALOGY_INVALID_PAYLOAD,
        String.format(ErrorMessages.GENEALOGY_INVALID_PAYLOAD, detail));
  }

  private BatchGenealogyValidator() {}
}
