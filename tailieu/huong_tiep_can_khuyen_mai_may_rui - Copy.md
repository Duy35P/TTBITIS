# Hướng tiếp cận cơ bản: Website Quản lý và vận hành Chương trình khuyến mãi may rủi



## 1\. Phân tích chức năng cốt lõi (Core Features)

### Phía Quản trị viên (Admin)

* **Quản lý Chương trình (Campaign):** Tạo, sửa, xóa, tạm dừng các chương trình khuyến mãi (thời gian diễn ra, thể lệ).
* **Quản lý Giải thưởng (Prize):** Cấu hình cơ cấu giải thưởng cho từng chương trình (Tên giải, số lượng tổng, số lượng còn lại, tỷ lệ/xác suất trúng thưởng).
* **Thống kê:** Xem báo cáo số lượng người tham gia, danh sách trúng thưởng, số lượng giải thưởng đã phát.

### Phía Người dùng (User)

* **Tài khoản:** Đăng ký, đăng nhập, quản lý thông tin cá nhân.
* **Tham gia:** Nhận lượt quay (thông qua mua hàng, code khuyến mãi) và thực hiện quay thưởng.
* **Lịch sử:** Xem lại các giải thưởng mình đã trúng.

\---

## 2\. Thiết kế Cơ sở dữ liệu (Database Models)

Hệ thống cần các Entity chính (ánh xạ vào package `model`):

1. **`User` (Người dùng):** `id`, `username`, `password`, `email`, `phone`, ...
2. **`Campaign` (Chương trình KM):** `id`, `name`, `startDate`, `endDate`, `status`.
3. **`Prize` (Giải thưởng):** `id`, `campaign\_id`, `name`, `totalQuantity` (tổng số lượng), `remainQuantity` (số lượng còn lại), `probability` (xác suất trúng - %, ví dụ: 0.1%).
4. **`UserTurn` (Lượt quay của User):** `id`, `user\_id`, `campaign\_id`, `remainTurns` (số lượt quay còn lại).
5. **`DrawHistory` (Lịch sử trúng thưởng):** `id`, `user\_id`, `campaign\_id`, `prize\_id` (nếu quay trượt thì null), `drawTime`.

\---

## 3\. Kiến trúc mã nguồn (Theo cấu trúc project hiện tại)

Dựa trên cấu trúc chuẩn của Spring Boot mà bạn đang có, mã nguồn sẽ được chia như sau:

* **`model` / `entity`:** Chứa các class User, Campaign, Prize... (dùng `@Entity`, `@Table`).
* **`repository`:** Các interface kế thừa `JpaRepository` để tương tác với cơ sở dữ liệu.

  * VD: `PrizeRepository`, `DrawHistoryRepository`.
* **`controller`:** Nơi tiếp nhận request từ Frontend (Web).

  * VD: `DrawController` với API `/api/draw/{campaignId}`.
* **`service`:** **Lớp quan trọng nhất**, chứa toàn bộ logic xử lý nghiệp vụ, thuật toán quay số và tính toán xác suất.

\---

## 4\. Thuật toán quay thưởng cơ bản (Java Logic)

> \[!WARNING]
> \*\*Vấn đề Đồng thời (Concurrency/Race Condition)\*\*
> Trong các chương trình may rủi, có thể có hàng ngàn người quay cùng lúc. Rất dễ xảy ra tình trạng giải thưởng chỉ còn 1 nhưng 2 người cùng trúng. 
> \*\*Giải pháp trong Java:\*\* Sử dụng Cơ chế Khóa (Locking). Trong Spring Data JPA, bạn cần áp dụng `@Lock(LockModeType.PESSIMISTIC\_WRITE)` khi truy vấn cập nhật số lượng `Prize` hoặc sử dụng Redis để trừ số lượng giải thưởng (vì Redis chạy đơn luồng, cực kỳ an toàn cho việc này).

\---

## 5\. Lộ trình thực hiện (Roadmap)

1. **Giai đoạn 1:** Khởi tạo Spring Boot, kết nối DB (MySQL/PostgreSQL), tạo các Entities (Model).
2. **Giai đoạn 2:** Xây dựng CRUD cho Admin (Quản lý Chiến dịch, Thêm giải thưởng, Set xác suất).
3. **Giai đoạn 3:** Viết Service logic quay thưởng và trừ số lượng giải (Viết Unit Test thật kỹ cho phần Random này).
4. **Giai đoạn 4:** Xây dựng API và tích hợp giao diện (Frontend có thể dùng Thymeleaf, React, Vue tùy ý với hiệu ứng vòng quay/hộp quà).
5. **Giai đoạn 5:** Tối ưu hóa Database và xử lý Concurrency (Chống phát lố giải thưởng).

