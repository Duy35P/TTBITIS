# Tổng quan Kiến trúc Dự án (Project Architecture)

Dự án này được xây dựng theo mô hình **MVC (Model-View-Controller)** với Spring Boot, kết hợp kiến trúc phân lớp chuẩn (Layered Architecture). Dưới đây là chức năng và ý nghĩa của từng lớp trong dự án:

## 1. Controllers (Trình điều khiển)
Thư mục: `src/main/java/com/bitis/luckydraw/controller`

Controllers có nhiệm vụ tiếp nhận Request (yêu cầu) từ người dùng (trình duyệt) hoặc từ hệ thống khác (như máy POS), gọi các Services để xử lý logic, và trả về kết quả (View HTML hoặc dữ liệu JSON).

* **Web MVC Controllers (Dành cho Trang Quản trị - Admin):**
  - Các class bắt đầu bằng chữ `Admin...`: `AdminDashboardController`, `AdminCampaignController`, `AdminCustomerController`, `AdminInvoiceController`, `AdminPrizeController`, `AdminStoreController`, v.v.
  - Chức năng: Xử lý các nghiệp vụ quản lý ở trang Admin (Back-office). Nhận request, lấy dữ liệu và bind (gắn) dữ liệu vào các Template HTML (Thymeleaf) để trả về cho trình duyệt.
* **REST API Controllers (Dành cho máy POS hoặc tích hợp Web):**
  - `PosApiController`, `PosWebhookController`: Cung cấp các API dạng JSON để hệ thống phần mềm bán hàng (POS) hoặc Web gọi tới (ví dụ: Đồng bộ hóa đơn để cộng lượt quay).
  - `AdminPosSimulatorController`: Dành cho trang giả lập máy POS để gửi test data.

---

## 2. Models (Thực thể / Bảng CSDL)
Thư mục: `src/main/java/com/bitis/luckydraw/model`

Lớp Model (hoặc Entity) chứa các class đại diện cho cấu trúc của các bảng trong Database (thông qua JPA/Hibernate). Mỗi Class thường tương ứng với 1 bảng, và mỗi thuộc tính trong Class tương ứng với 1 cột.

* **Nhóm Quản trị hệ thống:** `Staff`, `VaiTro`, `PhanQuyen`, `ChucNang`, `SystemAuditLog`, `SystemConfig`.
* **Nhóm Chiến dịch & Khách hàng:** `Campaign`, `Customer`, `Store`, `CampaignStore` (bảng nối).
* **Nhóm Thể lệ & Cộng lượt:** `CampaignRule`, `CampaignRulePayment`, `CampaignRuleSku`, `Invoice` (Hóa đơn).
* **Nhóm Giải thưởng & Quay số:** `Prize`, `RewardVoucher`, `StorePrizeInventory`, `TurnTransaction`, `CustomerTurn`.

---

## 3. Repositories (Lớp truy xuất Dữ liệu)
Thư mục: `src/main/java/com/bitis/luckydraw/repository`

Repositories đóng vai trò giao tiếp trực tiếp với Database. Thay vì phải viết các câu lệnh SQL dài dòng, Spring Data JPA cung cấp các hàm sẵn có (như `save`, `findById`, `findAll`) và cho phép định nghĩa thêm các hàm tìm kiếm tùy chỉnh.

* Tất cả các Repositories đều là Interface kế thừa `JpaRepository`.
* Mỗi Model sẽ có một Repository tương ứng (Ví dụ: `CustomerRepository` dùng để tương tác với bảng Customer).
* Lớp này sẽ được `Controller` hoặc `Service` gọi để lấy/lưu dữ liệu.

---

## 4. Services (Lớp Xử lý Nghiệp vụ)
Thư mục: `src/main/java/com/bitis/luckydraw/service`

Đây là trái tim của hệ thống, nơi chứa toàn bộ "Logic nghiệp vụ" phức tạp. Việc tách Service ra khỏi Controller giúp tái sử dụng code và dễ dàng bảo trì.

* `PosService`: Xử lý logic rất quan trọng khi đồng bộ hóa đơn từ POS. Nó kiểm tra xem hóa đơn có hợp lệ không, có thỏa mãn điều kiện chiến dịch nào không, và tính toán số lượt quay khách hàng được nhận.
* `TurnManagementService`: Xử lý việc quản lý lượt quay của khách hàng (Cộng lượt, Trừ lượt khi khách quay số, tính toán lịch sử giao dịch lượt).
* `DeltaRuleEngine`: Engine xử lý các logic tính toán số tiền chênh lệch (Delta Amount) phức tạp khi khách hàng đổi/trả hàng.
* `PrizeExcelService` & `StoreExcelService`: Xử lý logic đọc file Excel (Import) để thêm giải thưởng, cấu hình hoặc thêm cửa hàng hàng loạt.

---

## 5. DTOs (Data Transfer Objects - Đối tượng truyền dữ liệu)
Thư mục: `src/main/java/com/bitis/luckydraw/dto`

DTO là các class chỉ dùng để "đóng gói" và vận chuyển dữ liệu giữa Client và Server, hoặc giữa các Lớp với nhau. Khác với `Model` (bị gắn chặt với Database), DTO rất linh hoạt.

* **Nhận dữ liệu Request:** `PosSyncRequest` (đóng gói payload JSON từ máy POS), `InvoiceRequestDTO`, `CampaignRuleForm` (nhận dữ liệu từ form HTML nhiều trường).
* **Trả dữ liệu Response:** `PosSyncResponse` (đóng gói kết quả trả về cho máy POS), `InvoiceListDto`, `PrizeListDto`, `StoreInventoryDto` (thường kết hợp từ nhiều bảng khác nhau để mang đủ thông tin hiển thị lên UI, tránh việc phải query nhiều lần hoặc bị lộ các trường nhạy cảm trong Model).

## Tóm tắt Luồng đi của dữ liệu (Data Flow)
1. Browser/POS gửi Request -> **Controller** nhận Request.
2. Controller dùng **DTO** để lấy dữ liệu từ Request.
3. Controller gọi **Service** để xử lý logic nghiệp vụ.
4. Service gọi **Repository** để đọc/ghi dữ liệu (truyền vào các **Model**).
5. Repository thực hiện câu lệnh SQL xuống Database và trả **Model** lại cho Service.
6. Service xử lý xong, trả kết quả (hoặc **DTO**) về cho Controller.
7. Controller trả **DTO** dạng JSON về cho POS, hoặc đưa dữ liệu vào View (HTML) để hiển thị lên Browser.
