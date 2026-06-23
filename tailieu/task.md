# Dự án Lucky Draw (O2O Architecture) - Task List

## Giai đoạn 1: Thiết lập Hệ thống & SQL Server Migration
- [x] Sửa đổi `pom.xml` (Gỡ MySQL, thêm SQL Server JDBC).
- [x] Cập nhật `application.properties` với thông tin kết nối mới.
- [x] Khởi tạo Database (Đọc và thực thi `script_db.sql`).
- [x] Tạo dữ liệu mẫu (Dummy data) cho `STORE`, `USER`, `CAMPAIGN`, `INVOICE` để chuẩn bị test.
- [x] Tạo các Entity JPA tương ứng với các bảng trong SQL Server.

## Giai đoạn 2: Tích hợp Webhook cho POS & Rule Engine tính lượt
- [ ] API Webhook nhận Hóa đơn từ POS.
- [ ] Rule Engine: Logic tính toán tổng tiền, PTTT, SKU.
- [ ] Rule Engine: Logic xử lý Delta cho hóa đơn Đổi/Trả.
- [ ] Giao diện Web Giả lập POS (POS Simulator) để test thực tế.

## Giai đoạn 3: Quản lý Lượt quay & Token Game (Mã QR)
- [ ] API sinh `GAME_ACCESS_TOKEN` sau khi cộng lượt.
- [ ] Trả URL/QR code về cho POS.

## Giai đoạn 4: Thuật toán Quay số & Bảo vệ ngân sách
- [ ] Thuật toán Random giải thưởng.
- [ ] Logic Max Win Limit (loại bỏ giải nếu đã đạt giới hạn).
- [ ] Pessimistic Locking chống Race condition khi trừ tồn kho.

## Giai đoạn 5: Hệ thống Đổi quà vật lý (Redeem Station)
- [ ] API cấp Voucher (Physical/Digital).
- [ ] API Redeem cho thu ngân (kiểm tra tồn kho và đổi voucher).

## Giai đoạn 6: Giao diện Web (UI Portals)
- [ ] Admin Portal (Quản lý Campaign, Prize, Inventory).
- [ ] Store Portal (Quét QR đổi quà).
- [ ] Customer Web-App (Giao diện quay số Responsive).
