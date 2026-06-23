# Kế hoạch Triển khai Dự án Quay Thưởng (dựa theo plan_7.md)

Tài liệu này vạch ra các giai đoạn cụ thể để phát triển toàn bộ hệ thống O2O (Online-to-Offline) theo yêu cầu trong `plan_7`, đồng thời thay đổi cơ sở dữ liệu nền tảng từ MySQL sang **Microsoft SQL Server**.

> [!IMPORTANT]
> **Thay đổi cơ sở dữ liệu:**
> Chúng ta sẽ gỡ bỏ `mysql-connector-j` và sử dụng `mssql-jdbc`. Chuỗi kết nối JDBC sẽ được cấu hình lại để trỏ đến SQL Server.

## User Review Required

Bạn vui lòng xem lại kế hoạch phân chia giai đoạn (Phases) dưới đây. Nếu thống nhất với trình tự này và các thông số kết nối SQL Server, tôi sẽ bắt đầu viết mã nguồn theo từng giai đoạn.

## Open Questions

1. **Thông tin SQL Server:** Bạn vui lòng cung cấp thông tin kết nối SQL Server (host, port, username, password, và database name). Nếu bạn muốn dùng thông tin mặc định (localhost:1433, sa, password...) để test trước thì báo tôi biết nhé.
2. **Framework Frontend:** Đối với phần Admin Portal (Web quản trị) và User Portal (Web-App chơi game), bạn muốn sử dụng Thymeleaf (có sẵn trong dự án) kết hợp HTML/CSS/JS thuần, hay muốn tách ra dùng React/Vue.js riêng biệt?

---

## Proposed Changes

Dự án sẽ được triển khai theo 6 giai đoạn sau:

### Giai đoạn 1: Thiết lập Hệ thống & Chuyển đổi SQL Server (Migration)
*   **Chuyển đổi Database:** Thay đổi cấu hình trong `pom.xml` và `application.properties` để kết nối với SQL Server.
*   **Thiết kế Entities (JPA):** Xây dựng toàn bộ các bảng cơ sở dữ liệu dựa theo schema của `plan_7`: `STORE`, `CAMPAIGN`, `CAMPAIGN_RULE`, `PRIZE`, `INVOICE`, `GAME_ACCESS_TOKEN`, `USER_TURN`, `TURN_TRANSACTION`, `REWARD_VOUCHER`.

#### [MODIFY] [pom.xml](file:///d:/webquaymayrui/pom.xml)
- Xóa `mysql-connector-j`.
- Thêm `mssql-jdbc`.

#### [MODIFY] [application.properties](file:///d:/webquaymayrui/src/main/resources/application.properties)
- Thay đổi `spring.datasource.url` thành chuỗi JDBC của SQL Server.
- Thay đổi driver class và `hibernate.dialect` thành `SQLServerDialect`.

### Giai đoạn 2: Tích hợp Webhook cho POS & Rule Engine tính lượt
*   **Xây dựng API `/api/pos/invoice`:** Nhận payload từ máy POS.
*   **Delta Rule Engine:** Xử lý logic tính số lượt quay thưởng.
    *   Tính tổng tiền (minOrderValue).
    *   Cộng lượt ưu đãi cho `paymentMethod` (MOMO/VNPAY).
    *   Cộng lượt ưu đãi cho `SKU`.
    *   **Xử lý Đổi/Trả hàng:** Logic đọc `originalInvoiceNumber` để tính chênh lệch Delta và cấp lượt tương ứng.

### Giai đoạn 3: Quản lý Lượt quay & Token Game (Mã QR)
*   **Quản lý ví `USER_TURN`:** Ghi nhận `TURN_TRANSACTION` cộng lượt vào "ví" chung của khách hàng.
*   **Sinh Token:** Tạo ra `GAME_ACCESS_TOKEN` duy nhất cho mỗi hóa đơn hợp lệ.
*   Trả URL chứa Token (link QR) về cho POS in bill.

### Giai đoạn 4: Thuật toán Quay số & Bảo vệ ngân sách (Core Logic)
*   Xây dựng thuật toán quay số.
*   **Max Win Limit:** Lọc danh sách giải, tự động đánh trượt giải lớn nếu user đã trúng đủ số lượng `maxWinsPerUser`.
*   **Pessimistic Locking:** Lock dòng dữ liệu trong bảng `PRIZE` và trừ tồn kho `globalRemainQuantity` bằng kỹ thuật chống Race Condition an toàn (đã định nghĩa ở bản v3).

### Giai đoạn 5: Hệ thống Đổi quà vật lý (Redeem Station)
*   Sinh mã giảm giá `REWARD_VOUCHER` (Physical & Digital).
*   Xây dựng API cho thu ngân tại cửa hàng để quét mã Voucher khách mang tới.
*   Kiểm tra `STORE_PRIZE_INVENTORY` (Tồn kho vật lý tại cửa hàng đó) xem còn hàng không. Nếu còn -> trừ kho, đổi trạng thái voucher. Nếu hết -> Báo lỗi hết kho.

### Giai đoạn 6: Giao diện Web (UI Portals)
*   **Admin Portal:** Giao diện quản lý Campaign, cấu hình mức tiền, SKU, và phân bổ số lượng giải thưởng về các Cửa hàng.
*   **Store Portal (Thu ngân):** Giao diện quét QR đổi quà.
*   **Customer Web-App:** Giao diện trên điện thoại cho khách bấm "Quay thưởng" cực kỳ bắt mắt, tích hợp hiệu ứng animation.

---

## Verification Plan

Sau khi hoàn thành, tôi sẽ cùng bạn verify hệ thống theo các kịch bản:

### Automated Tests
*   Viết Unit Test cho Rule Engine: Đảm bảo truyền vào Hóa đơn đổi hàng (Delta âm) thì không sinh lượt. Hóa đơn mới thì tính lượt chuẩn xác.
*   Test Concurrency: Giả lập 100 request quay cùng lúc để kiểm tra kỹ thuật khóa bi quan (Pessimistic Lock) có bị lỗi cấp dư quà kho tổng hay không.

### Manual Verification
*   Dùng Postman giả lập POS gửi hóa đơn.
*   Lấy link token sinh ra dán vào trình duyệt để test giao diện Khách hàng.
*   Test luồng Đổi quà vật lý: Dán Voucher vào hệ thống xem có báo lỗi "Hết kho" đúng chuẩn hay không.
