package com.generated.qualityTrace.validators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.services.BatchGenealogyException;
import com.generated.qualityTrace.types.BatchGenealogyPayload.BatchQty;

/** 批次谱系入参校验：必填、正数数量、非空清单、单内批号不重复。 */
public final class BatchGenealogyValidator {
  private BatchGenealogyValidator() {}

  public static String requireBizNo(String bizNo) {
    if (bizNo == null || bizNo.isBlank()) {
      throw new BatchGenealogyException(ErrorCodes.BIZ_NO_REQUIRED, ErrorMessages.BIZ_NO_REQUIRED);
    }
    return bizNo.trim();
  }

  public static String requireBatchNo(String batchNo, String field) {
    if (batchNo == null || batchNo.isBlank()) {
      throw new BatchGenealogyException(ErrorCodes.FIELD_REQUIRED, ErrorMessages.FIELD_REQUIRED + ": " + field);
    }
    return batchNo.trim();
  }

  public static long requirePositive(Long quantity, String field) {
    if (quantity == null) {
      throw new BatchGenealogyException(ErrorCodes.FIELD_REQUIRED, ErrorMessages.FIELD_REQUIRED + ": " + field);
    }
    if (quantity <= 0) {
      throw new BatchGenealogyException(ErrorCodes.INVALID_QUANTITY,
          ErrorMessages.INVALID_QUANTITY + ": " + field + "=" + quantity);
    }
    return quantity;
  }

  public static List<BatchQty> requireNonEmpty(List<BatchQty> list, String field) {
    if (list == null || list.isEmpty()) {
      throw new BatchGenealogyException(ErrorCodes.BATCH_LIST_EMPTY, ErrorMessages.BATCH_LIST_EMPTY + ": " + field);
    }
    return list;
  }

  public static void requireNoDuplicates(List<BatchQty> list) {
    Set<String> seen = new HashSet<>();
    for (BatchQty item : list) {
      if (item == null || item.batchNo() == null) {
        continue;
      }
      if (!seen.add(item.batchNo().trim())) {
        throw new BatchGenealogyException(ErrorCodes.DUPLICATE_BATCH_NO,
            ErrorMessages.DUPLICATE_BATCH_NO + ": " + item.batchNo());
      }
    }
  }
}
