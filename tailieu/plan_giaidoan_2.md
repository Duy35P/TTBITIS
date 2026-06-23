# Giai Đoạn 2: Webhook POS + Delta Rule Engine

Xây dựng hệ thống API tiếp nhận hóa đơn từ máy POS, tính toán phần tiền chênh lệch (kể cả trong trường hợp Đổi hàng), và cộng lượt một cách an toàn vào ví khách hàng bằng cơ chế **Spring Data JPA Pessimistic Lock**.

## User Review Required

> [!IMPORTANT]
> - Chúng ta sẽ dùng tính năng `@Lock(LockModeType.PESSIMISTIC_WRITE)` của Spring.
> - Bạn có muốn tôi viết luôn một file `.sql` để bạn test thử quá trình Lock (Mô phỏng 2 phiên giao dịch chọc vào cùng lúc) trước khi tôi bắt đầu gõ code Java không? Điều này sẽ giúp bạn hiểu chính xác Hibernate đang làm gì dưới DB.

## Proposed Changes

### 1. Database Layer (Các Entity và Repository cơ bản)

Sử dụng thư viện `spring-boot-starter-data-jpa` để ánh xạ DB thành các Class Java.

#### [NEW] `com/bitis/luckydraw/entity/UserTurn.java`
Entity ánh xạ bảng `user_turn`.

#### [NEW] `com/bitis/luckydraw/repository/UserTurnRepository.java`
Repository quản lý ví. 
Khai báo hàm:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<UserTurn> findByUserIdAndCampaignId(Long userId, Long campaignId);
```
Hàm này sẽ ép DB sinh ra lệnh `SELECT ... WITH (UPDLOCK)` để chặn mọi luồng cập nhật khác xen vào.

#### [NEW] `com/bitis/luckydraw/entity/Invoice.java`
Entity ánh xạ bảng `invoice` để lưu vết lịch sử hóa đơn đổi hàng (`maHoaDonGoc`).

#### [NEW] `com/bitis/luckydraw/repository/InvoiceRepository.java`
Để lưu Hóa đơn.

#### [NEW] `com/bitis/luckydraw/entity/TurnTransaction.java`
Entity ánh xạ bảng `turn_transaction` để lưu bằng chứng CSKH.

### 2. Service Layer (Não bộ tính toán)

#### [NEW] `com/bitis/luckydraw/service/DeltaRuleEngine.java`
- Chứa logic nghiệp vụ: Lấy `tongTien` của Hóa đơn mới truyền vào từ Webhook.
- Nhận biến cấu hình từ DB (Ví dụ: 1 triệu = 1 lượt).
- Chia tiền ra lượt. Code độc lập để sau này dễ nhét thêm luật riêng (ví dụ thanh toán Momo được nhân đôi).

#### [NEW] `com/bitis/luckydraw/service/TurnManagementService.java`
Dịch vụ quản lý ví, đánh dấu `@Transactional`.
Thực thi quy trình:
1. Lock ví của User thông qua `UserTurnRepository`. (Nếu ví chưa có thì tạo mới).
2. Gọi `DeltaRuleEngine` để lấy số lượt mới cần cộng.
3. Cập nhật `luot_con_lai`.
4. Insert bằng chứng vào `TurnTransaction`.

### 3. API Layer (Cổng giao tiếp)

#### [NEW] `com/bitis/luckydraw/controller/PosWebhookController.java`
- Khai báo Endpoint `POST /api/pos/invoice`.
- Nhận payload từ POS (đã định nghĩa `InvoiceRequestDTO` từ trước).
- Gọi `TurnManagementService` xử lý.
- Trả về mã HTTP 200 OK cho POS biết đã ghi nhận.

---

## Kế hoạch Test Dữ Liệu SQL (Như bạn yêu cầu)

Để bạn hoàn toàn yên tâm về cơ chế khóa này, trước khi gõ file Java đầu tiên, tôi sẽ viết một file `test_lock_concurrency.sql`. 
Bạn sẽ mở 2 tab trong SQL Server Management Studio (SSMS) để giả lập việc **2 khách hàng (hoặc 2 máy POS) cùng cố gắng cộng lượt cho 1 ví cùng lúc**. Bạn sẽ tận mắt chứng kiến SQL Server treo (Block) 1 tab lại để chờ tab kia xử lý xong, chứng minh Race Condition đã bị tiêu diệt hoàn toàn mà không cần tới Stored Procedure!
