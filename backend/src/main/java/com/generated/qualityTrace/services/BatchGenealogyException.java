package com.generated.qualityTrace.services;

/** 批次谱系业务异常：携带错误码，由 ErrorHandlerMiddleware 统一转 400 响应。 */
public class BatchGenealogyException extends RuntimeException {
  private final String errorCode;

  public BatchGenealogyException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
