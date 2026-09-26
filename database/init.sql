CREATE TABLE IF NOT EXISTS work_order (
  id INTEGER PRIMARY KEY,
  order_no TEXT,
  product_code TEXT,
  product_name TEXT,
  planned_qty TEXT,
  line_code TEXT,
  start_at TEXT,
  status TEXT
);

CREATE TABLE IF NOT EXISTS product_batch (
  id INTEGER PRIMARY KEY,
  batch_no TEXT,
  work_order_id TEXT,
  quantity TEXT,
  material_lot_no TEXT,
  produced_at TEXT,
  batch_status TEXT
);

CREATE TABLE IF NOT EXISTS quality_inspection (
  id INTEGER PRIMARY KEY,
  batch_id TEXT,
  inspector_id TEXT,
  inspection_type TEXT,
  standard_version TEXT,
  result_status TEXT,
  inspected_at TEXT
);

CREATE TABLE IF NOT EXISTS inspection_item_result (
  id INTEGER PRIMARY KEY,
  inspection_id TEXT,
  item_code TEXT,
  item_name TEXT,
  measured_value TEXT,
  limit_min TEXT,
  limit_max TEXT,
  item_status TEXT
);

CREATE TABLE IF NOT EXISTS defect_record (
  id INTEGER PRIMARY KEY,
  batch_id TEXT,
  defect_type TEXT,
  defect_qty TEXT,
  severity TEXT,
  root_cause TEXT,
  disposition_status TEXT
);

CREATE TABLE IF NOT EXISTS audit_log (
  id INTEGER PRIMARY KEY,
  actor TEXT,
  action TEXT,
  target_type TEXT,
  target_id TEXT,
  created_at TEXT
);

-- 批次谱系：拆分/合并业务单（biz_no 为幂等键，同一业务单重复到达返回首次登记结果）
CREATE TABLE IF NOT EXISTS batch_transform (
  id INTEGER PRIMARY KEY,
  biz_no TEXT,
  action TEXT,
  anchor_batch_no TEXT,
  total_quantity TEXT,
  created_at TEXT
);

-- 批次谱系边：来源批 -> 去向批，附数量
CREATE TABLE IF NOT EXISTS batch_transform_line (
  id INTEGER PRIMARY KEY,
  biz_no TEXT,
  action TEXT,
  source_batch_no TEXT,
  target_batch_no TEXT,
  quantity TEXT
);
