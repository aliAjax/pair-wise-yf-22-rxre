package com.generated.qualityTrace.constants;

public final class LogTemplates {
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";
  public static final String GENEALOGY_SPLIT = "genealogy split registered docNo=%s parent=%s children=%s total=%s";
  public static final String GENEALOGY_MERGE = "genealogy merge registered docNo=%s target=%s sources=%s total=%s";
  public static final String GENEALOGY_REJECT = "genealogy rejected docNo=%s code=%s reason=%s";
  public static final String GENEALOGY_REPLAY = "genealogy replayed docNo=%s returning original registration";
  public static final String GENEALOGY_TRACE = "genealogy trace batchNo=%s upstream=%s downstream=%s";

  private LogTemplates() {}
}
