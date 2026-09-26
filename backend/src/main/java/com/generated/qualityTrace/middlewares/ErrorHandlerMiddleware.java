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
import com.generated.qualityTrace.exceptions.BatchNotFoundException;
import com.generated.qualityTrace.exceptions.GenealogyRejectionException;

@RestControllerAdvice
public class ErrorHandlerMiddleware {

  @ExceptionHandler(BatchNotFoundException.class)
  public ResponseEntity<Map<String, Object>> notFound(BatchNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(BatchGenealogyDtoFactory.error(e.getErrorCode(), e.getMessage()));
  }

  @ExceptionHandler(GenealogyRejectionException.class)
  public ResponseEntity<Map<String, Object>> rejected(GenealogyRejectionException e) {
    return ResponseEntity.unprocessableEntity()
        .body(BatchGenealogyDtoFactory.error(e.getErrorCode(), e.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e) {
    return ResponseEntity.badRequest().body(BatchGenealogyDtoFactory.error(
        ErrorCodes.GENEALOGY_INVALID_PAYLOAD,
        String.format(ErrorMessages.GENEALOGY_INVALID_PAYLOAD, "malformed request body")));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> fallback(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(BatchGenealogyDtoFactory.error(ErrorCodes.INTERNAL_ERROR, ErrorMessages.INTERNAL_ERROR));
  }
}
