# Database_NV.sql — Báo cáo Lỗi & Changelog

**Dự án:** AI Shopping Agent  
**File gốc:** `Database_NV.sql` (v1.0)  
**File sau sửa:** `Database_NV.sql` (v2.0)  
**Ngày kiểm tra:** 13/09/2026  
**Người kiểm tra:** Antigravity AI Assistant

---

## Tổng quan

`Database_NV.sql` v1.0 là bản nâng cấp từ `Database.sql` gốc — đã chuyển thành công toàn bộ cú pháp sang **PostgreSQL**, sử dụng `COMMENT ON`, `GIN index` đúng chỗ, và xử lý deferred constraint bằng `BEGIN / SET CONSTRAINTS ALL DEFERRED / COMMIT`.

Tuy nhiên qua kiểm tra phát hiện **1 lỗi nghiêm trọng**, **2 lỗi trung bình** và **nhiều điểm cần bổ sung thêm**. Tất cả đã được sửa trong v2.0.

---

## Mức độ phân loại lỗi

| Ký hiệu | Mức độ | Mô tả |
|---|---|---|
| 🔴 | **Nghiêm trọng** | Làm hệ thống không khởi động được hoặc không insert dữ liệu được |
| 🟡 | **Trung bình** | Không crash ngay nhưng gây lỗi ngầm hoặc thiếu chức năng quan trọng |
| 🟢 | **Cải tiến** | Bổ sung thêm để schema hoàn chỉnh hơn, không phải lỗi |

---

## 🔴 LỖI 1 — Foreign Key `review_summaries` khai báo ngược chiều

### Vị trí
Dòng 235 trong `Database_NV.sql` v1.0.

### Mô tả lỗi
```sql
-- ❌ SAI (v1.0)
ALTER TABLE "products"
  ADD FOREIGN KEY ("id") REFERENCES "review_summaries" ("product_id") DEFERRABLE INITIALLY IMMEDIATE;
```

Khai báo này có nghĩa: **một `product` chỉ được tồn tại khi đã có `review_summary` tương ứng**. Điều này là bất khả thi vì tóm tắt chỉ được LLM sinh ra *sau khi* sản phẩm đã tồn tại và có đủ review.

**Hậu quả thực tế:**  
Dù `SET CONSTRAINTS ALL DEFERRED` giúp file chạy qua không báo lỗi ngay, nhưng khi `COMMIT`, PostgreSQL kiểm tra lại và sẽ **từ chối toàn bộ bản ghi `products`** không có `review_summary` tương ứng — tức là gần như mọi sản phẩm đều không insert được trong luồng thông thường.

### Cách sửa
```sql
-- ✅ ĐÚNG (v2.0) — review_summaries tham chiếu đến products (quan hệ 1-1 optional)
ALTER TABLE "review_summaries"
  ADD FOREIGN KEY ("product_id") REFERENCES "products" ("id") DEFERRABLE INITIALLY IMMEDIATE;
```

**Lý do đúng:** `review_summaries` là bảng *phụ thuộc* vào `products`. Một sản phẩm có thể chưa có tóm tắt (chưa đủ review hoặc chưa gọi LLM), nhưng một tóm tắt bắt buộc phải thuộc về một sản phẩm cụ thể.

---

## 🟡 LỖI 2 — Seed data dùng `id = 0`, gây xung đột SERIAL sequence

### Vị trí
Khối `INSERT` trong phần seed data của v1.0.

### Mô tả lỗi
```sql
-- ❌ SAI (v1.0)
INSERT INTO "categories" ("id", "code", "name") VALUES
  (0, 'laptop', 'Laptop'),      -- id = 0 ⚠️
  (1, 'phone',  'Điện thoại');

INSERT INTO "users" ("id", "username", "role") VALUES
  (0, 'admin', 'admin');        -- id = 0 ⚠️
```

PostgreSQL `SERIAL` tự động tạo sequence bắt đầu từ `1`. Khi insert thủ công `id = 0`, sequence **không được cập nhật**, vẫn giữ giá trị `1`. Khi Spring Boot tạo bản ghi mới đầu tiên, sequence sinh ra `id = 1` — **trùng với `id = 1` của 'phone'** → lỗi `duplicate key value violates unique constraint`.

Ngoài ra, `id = 0` thường được một số framework coi là giá trị chưa khởi tạo (uninitialized), dễ gây nhầm lẫn logic ở tầng ứng dụng.

### Cách sửa
```sql
-- ✅ ĐÚNG (v2.0) — bắt đầu từ id = 1
INSERT INTO "categories" ("id", "code", "name", "description") VALUES
  (1, 'laptop', 'Laptop',     'Máy tính xách tay các loại'),
  (2, 'phone',  'Điện thoại', 'Điện thoại thông minh');

INSERT INTO "users" ("id", "username", "email", "password_hash", "display_name", "role") VALUES
  (1, 'admin', 'admin@shoppingagent.local', 'CHANGE_ME_BEFORE_DEPLOY', 'Administrator', 'admin');

-- Reset sequence về đúng vị trí sau khi insert thủ công
SELECT setval('categories_id_seq',         (SELECT MAX(id) FROM "categories"));
SELECT setval('ranking_weights_id_seq',     (SELECT MAX(id) FROM "ranking_weights"));
SELECT setval('category_attributes_id_seq', (SELECT MAX(id) FROM "category_attributes"));
SELECT setval('users_id_seq',               (SELECT MAX(id) FROM "users"));
```

---

## 🟡 LỖI 3 — Thiếu cột `latency_ms` và `tokens_used` trong `llm_request_logs`

### Vị trí
Định nghĩa bảng `llm_request_logs` trong v1.0.

### Mô tả lỗi
```sql
-- ❌ THIẾU (v1.0)
CREATE TABLE "llm_request_logs" (
  "id"            SERIAL      PRIMARY KEY,
  "query_id"      INTEGER,
  "request_type"  VARCHAR     NOT NULL,
  "prompt_text"   TEXT,
  "response_text" TEXT,
  "status"        VARCHAR     NOT NULL,
  "retry_count"   INTEGER     DEFAULT 0,
  "created_at"    TIMESTAMP   DEFAULT NOW()
  -- ❌ Không có latency_ms, tokens_used
);
```

Đề cương **tuần 10–11 (23/11 – 07/12/2026)** yêu cầu đo các chỉ số thực nghiệm:
- **Độ trễ API** (latency)
- **Chi phí gọi LLM** (số token / số lần cache hit–miss)

Không có 2 cột này, nhóm sẽ không có dữ liệu để đưa vào bảng số liệu thực nghiệm và báo cáo đồ án.

### Cách sửa
```sql
-- ✅ ĐÚNG (v2.0)
CREATE TABLE "llm_request_logs" (
  "id"            SERIAL      PRIMARY KEY,
  "query_id"      INTEGER,
  "request_type"  VARCHAR(50) NOT NULL,
  "prompt_text"   TEXT,
  "response_text" TEXT,
  "status"        VARCHAR(20) NOT NULL,
  "retry_count"   SMALLINT    DEFAULT 0,
  "latency_ms"    INTEGER,      -- ✅ Thời gian gọi LLM (ms)
  "tokens_used"   INTEGER,      -- ✅ Số token tiêu thụ (Gemini usageMetadata)
  "created_at"    TIMESTAMP   DEFAULT NOW()
);
```

---

## 🟢 CẢI TIẾN 1 — Bổ sung `display_name` vào bảng `users`

### Lý do
Bảng `users` v1.0 chỉ có `username` (dùng để đăng nhập). Khi UI muốn hiển thị lời chào hoặc tên thân thiện, cần thêm `display_name`.

```sql
-- ✅ Thêm vào (v2.0)
"display_name" VARCHAR(255)   -- Tên hiển thị trên UI, vd: Gia Thịnh
```

---

## 🟢 CẢI TIẾN 2 — Thêm CHECK constraint cho `products` và `reviews`

### Lý do
v1.0 không có kiểm tra ràng buộc giá trị hợp lệ ở tầng DB, dẫn đến có thể insert dữ liệu sai mà không bị báo lỗi.

```sql
-- ✅ Thêm vào products (v2.0)
"price"      BIGINT       NOT NULL CHECK ("price" >= 0),
"avg_rating" DECIMAL(3,2) CHECK ("avg_rating" >= 0 AND "avg_rating" <= 5),

-- ✅ Thêm vào reviews (v2.0)
"rating" SMALLINT CHECK ("rating" >= 1 AND "rating" <= 5),
```

---

## 🟢 CẢI TIẾN 3 — Mở rộng `category_attributes` cho cả 2 ngành hàng

### Lý do
v1.0 chỉ có 4 attribute cho Laptop và 4 cho Điện thoại — chưa đủ để lọc thực tế.

| Ngành hàng | v1.0 | v2.0 (bổ sung thêm) |
|---|---|---|
| **Laptop** | cpu, ram, storage, battery | + `screen_size` (inch), `weight` (kg) |
| **Điện thoại** | chipset, ram, battery, camera | + `storage` (GB), `screen_size` (inch) |

---

## 🟢 CẢI TIẾN 4 — Bổ sung index còn thiếu

### Index thêm mới trong v2.0

```sql
-- Composite index tối ưu luồng lọc sản phẩm (category + active + rating + price)
CREATE INDEX ON "products" ("category_id", "is_active", "avg_rating" DESC, "price");

-- Index tra cứu nhanh kết quả theo query
CREATE INDEX ON "search_results" ("query_id");

-- Index tra cứu log LLM theo query
CREATE INDEX ON "llm_request_logs" ("query_id");
```

---

## 🟢 CẢI TIẾN 5 — Giới hạn độ dài VARCHAR cụ thể

v1.0 dùng `varchar` không có độ dài (tương đương `text` trong PostgreSQL). v2.0 khai báo tường minh để rõ ràng hơn và dễ validate ở tầng ứng dụng:

| Cột | v1.0 | v2.0 |
|---|---|---|
| `categories.code` | `varchar` | `VARCHAR(50)` |
| `products.name` | `varchar` | `VARCHAR(500)` |
| `products.brand` | `varchar` | `VARCHAR(100)` |
| `users.role` | `varchar` | `VARCHAR(20)` |
| `llm_request_logs.request_type` | `varchar` | `VARCHAR(50)` |
| `llm_request_logs.status` | `varchar` | `VARCHAR(20)` |

---

## Bảng tổng hợp tất cả thay đổi

| # | Loại | Bảng / Vị trí | Mô tả | Mức độ |
|---|---|---|---|---|
| 1 | Sửa lỗi | `review_summaries` FK | Đảo chiều FK từ `products→review_summaries` thành `review_summaries→products` | 🔴 Nghiêm trọng |
| 2 | Sửa lỗi | Seed data `categories`, `users` | Đổi `id=0` thành `id=1,2`; thêm `setval()` reset sequence | 🟡 Trung bình |
| 3 | Sửa lỗi | `llm_request_logs` | Thêm cột `latency_ms`, `tokens_used` | 🟡 Trung bình |
| 4 | Cải tiến | `users` | Thêm cột `display_name VARCHAR(255)` | 🟢 Cải tiến |
| 5 | Cải tiến | `products`, `reviews` | Thêm `CHECK` constraint cho `price`, `avg_rating`, `rating` | 🟢 Cải tiến |
| 6 | Cải tiến | `category_attributes` | Bổ sung `screen_size`, `weight` (laptop) và `storage`, `screen_size` (phone) | 🟢 Cải tiến |
| 7 | Cải tiến | Index | Thêm composite index lọc sản phẩm, index cho `search_results`, `llm_request_logs` | 🟢 Cải tiến |
| 8 | Cải tiến | Toàn bộ schema | Giới hạn độ dài cụ thể cho các cột `VARCHAR` | 🟢 Cải tiến |

---

*Tài liệu này được tạo tự động trong quá trình review schema ngày 13/09/2026.*
