package com.generated.qualityTrace.constants;

public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "missing token";
  public static final String RBAC_DENIED = "role denied";
  public static final String INTERNAL_ERROR = "internal error";
  public static final String BATCH_NOT_FOUND = "batch not found: %s";
  public static final String BATCH_ALREADY_EXISTS = "batch already exists: %s";
  public static final String GENEALOGY_INVALID_PAYLOAD = "invalid genealogy payload: %s";
  public static final String GENEALOGY_QUANTITY_MISMATCH = "quantity mismatch: %s";
  public static final String GENEALOGY_CYCLE_DETECTED = "genealogy cycle detected: source %s loops back to batch %s";
  public static final String GENEALOGY_BATCH_CONSUMED = "batch already consumed: %s (status %s)";

  private ErrorMessages() {}
}
