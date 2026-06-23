# KẾ HOẠCH TRIỂN KHAI CHI TIẾT
Hệ thống Quay Thưởng O2O (Online-to-Offline)
Tích hợp POS · SQL Server · Spring Boot

## Thay đổi Quan trọng so với Bản Cũ
🔴  **Database**: Chuyển hoàn toàn từ MySQL sang Microsoft SQL Server. Docker-compose, JDBC driver, và Hibernate dialect đều phải cập nhật theo.
🔴  **Phân bổ Kiến trúc (Java vs SQL Server)**:
- **Java (Não bộ & Điều phối)**: Đảm nhận toàn bộ Logic phức tạp (Đọc hóa đơn, tính toán Delta) VÀ kiểm soát tranh chấp dữ liệu (Race Condition / Deadlock). Sử dụng **Hibernate Pessimistic Lock (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)** thay vì viết Stored Procedure phức tạp, giúp dễ bảo trì và dễ hiểu hơn rất nhiều.
- **SQL Server (Lưu trữ & Thống kê)**: Đóng vai trò là cỗ máy thực thi khóa dòng (Row-lock) do Java truyền xuống. Chỉ sử dụng **Stored Procedure đơn giản** cho các thao tác nhanh tại quầy (như `sp_DoiQuaVatLy`).
🔴  **Cập nhật Nghiệp vụ (v8.2)**:
- **Ví lượt quay CHUNG**: Token QR/SMS chỉ dùng để đăng nhập. Vào game sẽ quay bằng ví tổng `remainTurns`, không gắn chết mỗi lượt quay với 1 token.
- **Chia đơn**: Hệ thống xử lý độc lập từng hóa đơn, hoàn toàn hỗ trợ khách tách hóa đơn để tối ưu ưu đãi Momo/VNPAY.
- **Chỉ có Đổi hàng (không có Trả hàng)**: Lượt quay chỉ đi một chiều — không bao giờ bị trừ khi khách đổi hàng. Xem chi tiết tại GĐ 2.2.
- **Quản lý Voucher**: Bổ sung trang quản lý riêng biệt cho Admin để CSKH và vận hành.
- **Chỉ quay 1 lần (Single-draw)**: Hệ thống đơn giản hóa tối đa, mỗi lượt quay chỉ xử lý 1 phần thưởng duy nhất để tránh phức tạp hóa UI và Logic.

---

## TỔNG QUAN 6 GIAI ĐOẠN
| Giai đoạn | Tên | Nội dung chính | Thời gian |
| :--- | :--- | :--- | :--- |
| GĐ 1 | Setup & SQL Server Migration | Cấu hình project, 9 entities O2O, Spring Security | 3-4 ngày |
| GĐ 2 | Webhook POS + Rule Engine | API `/api/pos/invoice`, Delta Engine, JPA Lock (Cộng lượt) | 3-4 ngày |
| GĐ 3 | Core Draw Engine | Thuật toán quay đa luồng (Single-draw), JPA Lock (Trừ lượt, kho) | 4-5 ngày |
| GĐ 4 | Redeem Station | REWARD_VOUCHER, API thu ngân, `sp_RedeemVoucher` | 3-4 ngày |
| GĐ 5 | UI Portals | Admin Portal, Store Portal, Web-App (Nút quay 1 lần) | 3-4 ngày |
| GĐ 6 | Thống kê, Exception & Deploy | Dashboard, SP Thống kê, Deploy | 3-4 ngày |

---

## GIAI ĐOẠN 1: Setup & SQL Server Migration
Thiết lập nền tảng dự án và chuyển đổi toàn bộ database stack sang Microsoft SQL Server. Xây dựng đầy đủ 9 entities theo schema O2O.

**1.1 Chuyển đổi Database Stack**
- Xóa `mysql-connector-j`, thêm `mssql-jdbc`. Sửa `application.properties` trỏ về SQL Server.

**1.2 Entities — Bảng O2O Schema**
`STORE`, `CAMPAIGN`, `CAMPAIGN_RULE`, `PRIZE`, `INVOICE`, `GAME_ACCESS_TOKEN`, `USER_TURN`, `TURN_TRANSACTION`, `REWARD_VOUCHER`, `STORE_PRIZE_INVENTORY`

---

## GIAI ĐOẠN 2: Webhook POS + Delta Rule Engine
Nhận tín hiệu từ máy POS, Java tính số lượt quay, và gọi DB lưu trữ.

**2.1 API Webhook nhận hóa đơn POS**
- `POST /api/pos/invoice` — Validate payload, hỗ trợ chia đơn tự động (coi như các giao dịch độc lập).

**2.2 Delta Rule Engine — Tính lượt quay (Tại Java)**
- Rule 1: Theo giá trị. Rule 2: Theo PTTT. Rule 3: Theo SKU đặc biệt.
- **Xử lý Đổi hàng**: Khi POS gửi hóa đơn điều chỉnh, Java tính `delta = invoiceNewTotal - invoiceOldTotal`.
  - Nếu `delta ≤ 0` → **early return, không gọi SP**, lượt quay hiện tại của khách giữ nguyên.
  - Nếu `delta > 0` → đưa delta vào Rule Engine tính lượt bổ sung và gọi `sp_AddUserTurns` bình thường.
  - **Lượt quay chỉ đi một chiều — không bao giờ bị trừ do đổi hàng.** Hệ thống không có nghiệp vụ Trả hàng.
- Mọi tính toán diễn ra ở RAM của Backend. Java không bao giờ gửi delta âm xuống DB.

**2.3 Quản lý ví USER_TURN & Sinh Token**
- Sau khi Java tính ra số lượt mới, Java sử dụng **Pessimistic Lock** (`findByIdAndLock`) để cập nhật an toàn số lượng `luot_con_lai`. Cơ chế Try-Catch được dùng để bắt lỗi `DataIntegrityViolationException` (nếu user chưa có ví) và tự động tạo mới ví.
- Java sinh UUID cho `GAME_ACCESS_TOKEN` để POS in QR.

**2.4 Vòng đời GAME_ACCESS_TOKEN**

| Trạng thái | Mô tả |
| :--- | :--- |
| `PENDING` | Vừa sinh, POS in QR, chưa được sử dụng |
| `USED` | Khách đã quét, đã xác thực thành công — **one-time use** |
| `EXPIRED` | Quá `expiredAt` (ví dụ: 24h hoặc hết ngày campaign) |

- Sau khi xác thực token thành công, hệ thống issue **JWT** (chứa `userId`, `campaignId`) cho phiên chơi. Mọi request sau dùng JWT. Token không còn vai trò sau bước này.
- Token có `expiredAt` và trạng thái. Một token chỉ được dùng đúng một lần (trạng thái `PENDING` → `USED`).

---

## GIAI ĐOẠN 3: Core Draw Engine
Thuật toán quay thưởng an toàn, **chỉ hỗ trợ Quay 1 lần (Single-draw)**.

**3.1 Token Validation & Luồng vào game**
- Token QR chỉ dùng để **Xác thực (Login)**. Sau xác thực, khách quay bằng ví `remainTurns` tổng, không phụ thuộc vào token.

**3.2 Thuật toán Quay số (Tại Java)**
- Mỗi request gọi API tương đương với 1 lượt quay.
- Java chạy hàm Random 1 lần duy nhất dựa trên xác suất `probability` để ra 1 giải duy nhất.

**3.3 Trừ kho và Trừ lượt an toàn bằng Hibernate Pessimistic Lock**
- **Khóa Ví lượt quay**: Java dùng `@Lock(PESSIMISTIC_WRITE)` khóa dòng Ví của User, kiểm tra xem còn đủ 1 lượt quay hay không, sau đó trừ 1 lượt.
- **Không còn nỗi lo Deadlock**: Vì mỗi lượt quay chỉ tương tác với 1 giải duy nhất, không bao giờ xảy ra tình trạng khóa chéo 2 giải thưởng cùng lúc. Tốc độ hệ thống sẽ tăng vọt.
- **Fallback khi hết kho (Race Condition)**: Java khóa dòng cấu hình của giải thưởng vừa trúng (`findAndLockById`). Nếu `TonKho` >= 1, thực hiện trừ 1. Nếu kho = 0, Java tự động chuyển đổi thành giải Trượt (Giữ số dư kho không bao giờ bị âm).
- Tất cả xử lý được đặt trong 1 `@Transactional`. Giao dịch tự động Rollback nếu có Exception.

---

## GIAI ĐOẠN 4: Redeem Station — Đổi quà vật lý
Thu ngân quét mã voucher, hệ thống kiểm tra tồn kho cục bộ cửa hàng.

**4.1 Luồng Đổi quà (Tối ưu bằng SP)**
- Thu ngân gọi `POST /api/store/redeem` kèm `voucherCode` và `storeId`.
- Thay vì kéo dữ liệu lên Java, Java gọi thẳng `EXEC sp_RedeemVoucher @voucherCode, @storeId`.
- SQL Server thực hiện: Lock dòng `STORE_PRIZE_INVENTORY` → Trừ kho cục bộ → Cập nhật trạng thái `REWARD_VOUCHER` thành `REDEEMED`. Nếu hết hàng, SP tự động Rollback và ném lỗi ra Java.

---

## GIAI ĐOẠN 5: UI Portals
Ba giao diện phục vụ ba nhóm người dùng khác nhau.

**5.1 Admin Portal**
- `campaigns.html`, `prizes.html`, `store-inventory.html`.
- **`voucher-management.html`**: Trang quản lý mã Voucher để Admin đối soát và hỗ trợ CSKH.

**5.2 Store Portal — Thu ngân**
- Giao diện quét QR đổi quà (`redeem.html`).

**5.3 Customer Web-App**
- Nút bấm: Chỉ có 1 nút `[QUAY THƯỞNG]`.
- Animation: Hiển thị vòng quay may mắn hoặc hiệu ứng mở 1 hộp quà.
- Gọi API nhận về duy nhất 1 kết quả.

---

## GIAI ĐOẠN 6: Thống kê & Deploy
- **Dashboard Admin**: Java gọi các View hoặc SP Thống kê để lấy dữ liệu biểu đồ.
- **KPI tối thiểu cần có**:
  - Số người đang chơi (active sessions)
  - Tỷ lệ trúng/trượt thực tế (so sánh với xác suất cấu hình)
  - Kho còn lại từng giải theo từng cửa hàng
  - Cảnh báo khi tỷ lệ trượt thực tế > 95%
  - Voucher đã phát nhưng chưa đổi sau X ngày (hỗ trợ vận hành tồn kho)
- **Cơ chế cảnh báo**: Dùng scheduled job mỗi 5 phút để kiểm tra ngưỡng và đẩy cảnh báo. Nếu Admin cần real-time, cân nhắc bổ sung WebSocket hoặc SSE.
- **GlobalExceptionHandler**: Bắt và chuẩn hóa lỗi từ SP trả về, map sang HTTP response phù hợp.
- **Deploy**: Docker-compose với SQL Server container, Spring Boot service, và reverse proxy.

---

## KỊCH BẢN KIỂM THỬ BỔ SUNG
- **Kịch bản Race Condition kho (Giải chót)**: Giả lập 100 user cùng bấm nút Quay 1 lần nhắm vào 1 giải Balo cuối cùng trong kho. Kiểm tra Pessimistic Lock của Java xử lý đúng: duy nhất 1 request chiếm được Lock và báo trúng Balo, 99 request còn lại phải nhận giải Trượt thay thế.
- **Kịch bản @Transactional**: Giả lập DB bị lỗi kết nối ngay sau khi vừa trừ lượt thành công nhưng chưa kịp lưu Voucher. Đảm bảo Spring Boot tự động Rollback trả lại lượt cho khách.
- **Kịch bản Đổi hàng delta âm**: POS gửi hóa đơn điều chỉnh với tổng thấp hơn. Kiểm tra Java early return, không gọi DB, ví lượt không thay đổi.