package com.generated.qualityTrace.exceptions;

public class GenealogyRejectionException extends RuntimeException {
  private final String errorCode;

  public GenealogyRejectionException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() { return errorCode; }
}
