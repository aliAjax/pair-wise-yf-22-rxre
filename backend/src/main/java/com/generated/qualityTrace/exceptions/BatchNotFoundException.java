package com.generated.qualityTrace.exceptions;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;

public class BatchNotFoundException extends RuntimeException {
  private final String errorCode;

  public BatchNotFoundException(String batchNo) {
    super(String.format(ErrorMessages.BATCH_NOT_FOUND, batchNo));
    this.errorCode = ErrorCodes.BATCH_NOT_FOUND;
  }

  public String getErrorCode() { return errorCode; }
}
