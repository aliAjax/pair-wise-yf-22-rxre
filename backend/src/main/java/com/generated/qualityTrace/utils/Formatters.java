package com.generated.qualityTrace.utils;

import java.time.OffsetDateTime;

public final class Formatters {
  public static String audit(String type, long id) { return type + "#" + id; }

  public static String batchRef(String batchNo) { return "BATCH#" + (batchNo == null ? "<none>" : batchNo); }

  public static String docRef(String docNo) { return "DOC#" + (docNo == null ? "<none>" : docNo); }

  public static String now() { return OffsetDateTime.now().toString(); }

  private Formatters() {}
}
