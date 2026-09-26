package com.generated.qualityTrace.middlewares;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constructors.BatchGenealogyDtoFactory;
import com.generated.qualityTrace.services.BatchGenealogyException;

/** 全局异常处理：谱系业务异常转 400，其余异常兜底 500，统一错误体结构。 */
@RestControllerAdvice
public class ErrorHandlerMiddleware {

  @ExceptionHandler(BatchGenealogyException.class)
  public ResponseEntity<Map<String, Object>> handleGenealogy(BatchGenealogyException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(BatchGenealogyDtoFactory.error(ex.getErrorCode(), ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(BatchGenealogyDtoFactory.error(ErrorCodes.FIELD_REQUIRED,
            ErrorMessages.FIELD_REQUIRED + ": request body"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(BatchGenealogyDtoFactory.error(ErrorCodes.INTERNAL_ERROR,
            ErrorMessages.INTERNAL_ERROR + ": " + ex.getMessage()));
  }
}
