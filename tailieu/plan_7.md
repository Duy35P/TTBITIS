# Kế hoạch Triển khai Kiến trúc O2O & Tích hợp POS (Kết hợp Tối ưu Kỹ thuật)

Bản kế hoạch này lấy Kiến trúc O2O (Online-to-Offline) làm cốt lõi, kết nối trực tiếp hệ thống POS vật lý, quản lý tồn kho theo từng điểm bán; ĐỒNG THỜI kế thừa toàn bộ **các cơ chế xử lý tối ưu từ bản v3 (Chống Race condition, Pessimistic Locking, Xử lý giải trượt)** để đảm bảo hệ thống vừa đáp ứng nghiệp vụ phức tạp, vừa chịu tải lớn mà không rớt mạng.

> [!IMPORTANT]
> **Quyết định Thiết kế Mới nhất **
> - **Khuyến khích "Chia đơn" & Không giới hạn lượt:** Khách hàng được tích lũy lượt vô hạn. Việc khách tách đơn để thanh toán MOMO nhiều lần lấy thêm lượt được xem là hành vi **hợp lệ để kích cầu**.
> - **Bảo vệ ngân sách bằng Backend (Tỉ lệ & Giới hạn giải):** Dùng `maxWinsPerUser` để cấm 1 người trúng quá nhiều giải lớn, và dùng Tỷ lệ trượt tự động khi hệ thống gần hết quà.
> - **Lượt quay gộp chung (Global Turns):** Khách hàng tích lũy lượt từ nhiều hóa đơn, cộng dồn vào chung 1 ví.
> - **Đổi quà linh hoạt (Flexible Redemption):** Khách mang Voucher vật lý đến **bất kỳ cửa hàng nào** để đổi.
> - **Hóa đơn đổi trả:** Chỉ tính thêm lượt nếu đơn mới có giá trị cao hơn đơn cũ (tính theo phần chênh lệch Delta). Không hỗ trợ tính năng Hủy đơn hoàn lượt.

>**Luồng Tích hợp POS & Xử lý Hóa đơn 
Chạy nhiều chương trình đồng thời, phân tách theo từng cửa hàng cụ thể và quản lý vị trí kho quà tặng (tránh đứt gãy trải nghiệm - khách trúng quà nhỏ nhưng cửa hàng hết quà vật lý).  
Khi khách hàng thanh toán, hệ thống POS gửi dữ liệu hóa đơn về Website Khuyến mãi thông qua API của Web quản lý để tính toán số lượt tham gia chương trình.
Bước 1: Kiểm tra phạm vi: Xác định store_id trên hóa đơn có nằm trong chiến dịch nào đang hoạt động không. Nếu không, hóa đơn bị bỏ qua.
Bước 2: Tính lượt theo tổng tiền: Lượt doanh thu = floor (Tổng tiền hóa đơn / Cấu hình Giá trị tối thiểu). (Ví dụ: Hóa đơn 550k, cấu hình 200k/lượt   được 2 lượt). Khách hàng được phép "chia đơn" thanh toán nhiều lần để tối ưu ưu đãi này.
Bước 3: Cộng lượt theo Phương thức thanh toán (PTTT): Nếu payment_method trên hóa đơn trùng với cấu hình ưu đãi (Ví dụ: VNPAY, MOMO), hệ thống cộng thêm số lượt cố định (X).
Bước 4: Cộng lượt theo Mã hàng hóa (SKU): Quét danh sách mặt hàng trong hóa đơn. Nếu trùng với SKU kích cầu được cấu hình, hệ thống cộng thêm lượt theo công thức: Số lượng hàng × Y lượt.   
Quy định xử lý Hóa đơn Đổi/Trả hàng (Delta Rule): Chỉ hỗ trợ tính toán lại lượt nếu khách hàng muốn đổi từ đơn hàng cũ thành đơn hàng. Có 2 trường hợp xảy ra:
1.	Giá đơn hàng giữ nguyên hoặc thấp hơn đơn cũ: Số lượt khách đã quay từ đơn trước giữ nguyên, không cộng thêm lượt mới.
2.	Giá trị đơn mới cao hơn đơn cũ: Tính phần tiền chênh lệch (Tiền đơn mới - Tiền đơn cũ). Đem khoản chênh lệch để quy đổi ra số lượt quay cộng thêm. Không hỗ trợ tính năng hủy đơn hoàn lượt.


Phân hệ Quản trị (Admin Portal) - Dành cho Marketing / Vận hành Tổng:
- Trang cấu hình chiến dịch (Campaign Management): Thiết lập luật tính lượt (giá trị tối thiểu, ưu đãi PTTT, ưu đãi SKU) và chọn danh sách cửa hàng áp dụng.
- Trang quản lý giải thưởng & Phân bổ kho (Prize & Allocation): Thiết lập loại quà, phân bổ số lượng tồn kho tổng, giới hạn trúng thưởng trên mỗi user, và phân bổ tồn kho về từng cửa hàng.
- Trang quản lý mã Voucher trúng giải tương ứng giải thưởng
-Trung tâm báo cáo trực quan (Analytics Dashboard).
Phân hệ Cửa hàng (Store/Merchant Portal) - Dành cho Thu ngân / Quản lý CH
- Màn hình Đổi thưởng (Redeem Station) – Kiểm tra thông tin đổi thưởng (có nút xác nhận trao quà khi không quy đổi được mã voucher sang quà tặng tại POS. 
Hệ thống tự động check tồn kho tại chi nhánh hiện tại. Nếu còn hàng, thu ngân bấm nút xác nhận trao quà (chuyển trạng thái voucher thành Redeemed và trừ tồn kho vật lý tại cửa hàng).
Trường hợp cửa hàng hết món quà đó, hệ thống sẽ báo lỗi hết kho nội bộ, voucher của khách vẫn được giữ nguyên giá trị để mang sang cửa hàng khác đổi.
Phân hệ Khách hàng (Customer Frontend) - Giao diện Web-App / QR Game
-Cơ chế tham gia: Khách hàng quét mã QR in trên hóa đơn hoặc nhận tin nhắn SMS chứa link game. Lượt chơi được cộng dồn chung vào một ”ví”.
Khách hàng nhấn "Quay thưởng". Ngầm bên dưới, thuật toán Backend sẽ kiểm tra giới hạn trúng thưởng, loại bỏ các giải lớn nếu khách đã trúng đủ số lượng cho phép, sau đó mới random ra giải thưởng cuối cùng để bảo vệ ngân sách chiến dịch.



## 1. Database Schema O2O & Audit

Bổ sung phân loại quà tặng, Giới hạn trúng thưởng, và bảng trung gian `CAMPAIGN_STORE` để quản lý phạm vi áp dụng.

```mermaid
erDiagram
    STORE {
        Long id PK
        String name
        String status
    }
    
    CAMPAIGN {
        Long id PK
        String name
        String status
    }

    CAMPAIGN_STORE {
        Long id PK
        Long campaign_id FK
        Long store_id FK
    }
    
    CAMPAIGN_RULE {
        Long id PK
        Long campaign_id FK
        Double minOrderValue "Cấu hình Giá trị tối thiểu/lượt"
        String paymentMethods "Ví dụ: ['VNPAY', 'MOMO']"
        Int paymentMethodBonus "Lượt ưu đãi PTTT"
        String skuList "Danh sách SKU kích cầu"
        Int skuBonusPerItem "Lượt ưu đãi theo SKU"
        Int maxTurnsPerInvoice "Giới hạn tối đa/1 hóa đơn (Tùy chọn)"
    }

    PRIZE {
        Long id PK
        Long campaign_id FK
        String name
        String prizeType "PHYSICAL (Áo thun) / DIGITAL (Mã 50k)"
        Boolean isWinningPrize "Giải thật / Trượt"
        Double probability "% xác suất tổng"
        Int globalRemainQuantity "Tổng tồn kho hệ thống (Cả áo & mã)"
        Int maxWinsPerUser "Giới hạn trúng giải này/User (Tránh gom giải lớn)"
    }

    STORE_PRIZE_INVENTORY {
        Long id PK
        Long store_id FK
        Long prize_id FK
        Int remainQuantity "Số lượng quà vật lý tại Cửa hàng (Chỉ áp dụng PHYSICAL)"
    }

    GAME_ACCESS_TOKEN {
        Long id PK
        String token UK
        Long invoice_id FK
        Boolean isUsed
        Timestamp expiredAt
    }

    INVOICE {
        Long id PK
        String invoiceNumber UK
        String originalInvoiceNumber "Cho đổi trả hàng"
        Long store_id FK
        Long user_id FK
        Double totalAmount
        String paymentMethod
        String itemsJson
        Boolean isProcessed
    }

    USER_TURN {
        Long id PK
        Long user_id FK
        Long campaign_id FK
        Int remainTurns
        Constraint UK "UNIQUE(user_id, campaign_id)"
    }
    
    TURN_TRANSACTION {
        Long id PK
        Long user_id FK
        Long campaign_id FK
        String type "ADD/MINUS"
        Int amount
        String sourceReference "INVOICE_ID, DRAW"
    }

    REWARD_VOUCHER {
        Long id PK
        Long prize_id FK
        Long user_id FK
        String voucherCode UK "Mã KH mang ra cửa hàng hoặc Mã giảm giá online"
        String status "PENDING, REDEEMED, EXPIRED"
        Long redeemed_at_store_id FK "Thực tế đổi ở cửa hàng nào (Null nếu Digital)"
        Timestamp createdAt
        Timestamp redeemedAt
    }

    CAMPAIGN ||--o{ CAMPAIGN_STORE : applies_to
    STORE ||--o{ CAMPAIGN_STORE : runs
    STORE ||--o{ STORE_PRIZE_INVENTORY : holds
    STORE ||--o{ INVOICE : issues
    PRIZE ||--o{ STORE_PRIZE_INVENTORY : allocated_to
    INVOICE ||--o{ GAME_ACCESS_TOKEN : generates
```

## 2. Các Cơ chế Kỹ thuật Tối ưu (Kế thừa từ bản v3)

1. **Atomic UPDATE cho `UserTurn`:** Trừ lượt bằng câu lệnh SQL an toàn.
2. **Pessimistic Locking có Timeout:** Khóa duy nhất dòng `PRIZE` được random trúng (timeout 3s).
3. **Double-check Pattern:** Sau khi lock, check lại `globalRemainQuantity > 0` và check giới hạn User.
4. **SecureRandom:** Đảm bảo tính minh bạch khi random tỷ lệ.
5. **@Transactional toàn cục (Rollback-safe):** Mọi lỗi đều rollback, không làm mất lượt.
6. **Connection Pool:** HikariCP `maximum-pool-size=30-50`.

## 3. Workflows (Luồng Nghiệp Vụ) Cập Nhật Lại

### 3.1 Luồng Xử lý Hóa đơn, Tính Delta Đổi hàng & Kích cầu
1. POS gửi payload hóa đơn qua Webhook API.
2. **Kiểm tra phạm vi:** Truy vấn bảng `CAMPAIGN_STORE`. Nếu `store_id` của hóa đơn KHÔNG nằm trong danh sách cửa hàng áp dụng Campaign hiện tại -> Bỏ qua hóa đơn, kết thúc.
3. **Kiểm tra loại hóa đơn và chạy Rule Engine:**
   - **Trường hợp Hóa đơn Mới:** Tính lượt = Tiền + PTTT + SKU. (Khách được phép tách thanh toán làm nhiều hóa đơn để nhận nhiều lượt bonus MOMO).
   - **Trường hợp Hóa đơn Đổi hàng (Có `originalInvoiceNumber`):** Truy xuất tổng tiền của hóa đơn gốc. Tính `Delta = Tổng tiền Đơn mới - Tổng tiền Đơn cũ`.
     - *Nếu `Delta <= 0`:* Đơn đổi bằng giá hoặc thấp hơn -> Không cộng thêm lượt.
     - *Nếu `Delta > 0`:* Khách bù thêm tiền -> Đem giá trị `Delta` này chạy qua Rule Engine để tính số lượt cộng thêm cho khoản tiền bù.
4. Ghi nhận `TURN_TRANSACTION` và cộng dồn vô hạn vào ví `remainTurns` của user.
5. Sinh `GAME_ACCESS_TOKEN`, in QR code trên bill.

### 3.2 Luồng Quay thưởng & Check Tỉ lệ bảo vệ Ngân sách
*Hệ thống chặn việc user "vét" quà bằng thuật toán backend thay vì chặn số lượt chơi.*
1. **Validate lượt:** Dùng Atomic UPDATE trừ 1 lượt của User.
2. **Cân bằng Tỉ lệ (Backend Check):** 
   - Truy vấn SQL danh sách giải mà User đã trúng. 
   - Nếu user đã trúng giải X đủ số lần `maxWinsPerUser` (Ví dụ iPhone max=1), hệ thống gạt giải X ra khỏi danh sách random của riêng user này trong lượt đó, dồn xác suất của giải X vào Giải Trượt.
3. **Random:** Quay ra 1 giải còn lại bằng `SecureRandom`.
4. **Chốt quà:**
   - Pessimistic Lock dòng `PRIZE` X.
   - Check lại `globalRemainQuantity > 0`. Trừ tồn kho tổng đi 1.
   - Phát sinh Voucher `PHYSICAL` (cần ra quầy) hoặc `DIGITAL` (hiển thị luôn mã online).

### 3.3 Luồng Đổi quà Vật lý tại Cửa hàng (Redeem Station)
1. Khách đến chi nhánh bất kỳ. Thu ngân quét mã Voucher `PENDING`.
2. Kiểm tra `STORE_PRIZE_INVENTORY` của chi nhánh hiện tại.
   - **Còn quà:** Khóa dòng tồn kho chi nhánh -> Trừ 1 -> Đổi Voucher sang `REDEEMED`.
   - **Hết quà:** Báo lỗi hết kho tại chi nhánh, Voucher vẫn giữ nguyên giá trị để đổi nơi khác.

## 4. Giao diện (UI) Portals

1. **Admin Portal:**
   - Cấu hình Campaign: Các rule tính lượt, chọn danh sách Cửa hàng áp dụng (`CAMPAIGN_STORE`).
   - Cấu hình Giải thưởng: Phân loại PHYSICAL / DIGITAL, set `maxWinsPerUser`.
   - Analytics Dashboard & Voucher Management.
2. **Store Portal:** (Cho Thu ngân) 
   - Màn hình check Voucher `PHYSICAL`. Giao diện báo ĐỦ KHO / HẾT KHO.
3. **User Portal:** (Khách hàng) 
   - Quét QR từ Token -> Nút "Quay" -> Lưu mã nhận quà.

## 5. Verification Plan (Test Plan)
- **Delta Exchange Test:** Mua áo 500k (được 1 lượt). Đổi lên áo 800k (Delta = 300k). Vì 300k < mức tối thiểu 500k -> Hóa đơn đổi hàng này KHÔNG được cộng thêm lượt nào.
- **Max Win Limit Test:** Giải iPhone có `maxWins=1`. Đảm bảo user có 100 lượt cũng chỉ trúng tối đa 1 iPhone, sau đó tự rớt xuống giải khác.
- **Scope Test:** Gửi hóa đơn từ Cửa hàng không tham gia Campaign -> Đảm bảo không sinh lượt, không sinh QR code.
