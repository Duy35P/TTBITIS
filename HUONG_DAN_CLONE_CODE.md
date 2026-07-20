# HƯỚNG DẪN CHẠY PROJECT KHI CLONE CODE VỀ MÁY MỚI

Nếu bạn mới tải (clone) source code này về máy, hệ thống sẽ **không thể hoạt động ngay lập tức** do thiếu CSDL và cấu hình. Hãy làm theo đúng thứ tự các bước dưới đây để khởi chạy dự án:

## 1. Yêu cầu hệ thống (Prerequisites)
- **Java:** JDK 21
- **Cơ sở dữ liệu:** Microsoft SQL Server
- **Công cụ build:** Maven (project đã tích hợp sẵn file chạy `mvnw`)

## 2. Thiết lập Cơ sở dữ liệu (Database Setup)
1. Mở **SQL Server Management Studio (SSMS)** hoặc bất kỳ tool quản lý SQL nào.
2. Tạo một Database mới có tên là `luckydraw`:
   ```sql
   CREATE DATABASE luckydraw;
   ```
3. Đảm bảo tài khoản đăng nhập SQL Server (VD: `sa`) đã được kích hoạt.

## 3. Cấu hình file properties
Mở file `src/main/resources/application.properties` và sửa lại 2 dòng sau cho khớp với tài khoản SQL Server trên máy của bạn:
```properties
spring.datasource.username=sa
spring.datasource.password=mat_khau_cua_ban
```

*(Lưu ý: Nếu bạn có ý định dùng Zalo Mini App riêng thay vì dùng App test có sẵn, hãy sửa lại `zalo.app.id` và `zalo.app.secret` trong file này).*

## 4. Khởi chạy App lần đầu (Tạo Bảng)
Mở Terminal/Command Prompt tại thư mục gốc của dự án và chạy lệnh sau để Spring Boot tự động kết nối CSDL và tạo ra các bảng (Tables):
```bash
# Trên Windows
.\mvnw spring-boot:run
```
Sau khi thấy dòng chữ `Started LuckyDraw...` trong console, hãy **Tắt server đi (Ctrl + C)**. Lúc này các bảng đã được tạo xong nhưng Database vẫn chưa có Dữ liệu và thủ tục (Stored Procedures).

## 5. Nạp cấu trúc cốt lõi (Stored Procedures & Dữ liệu)
Mở SQL Server Management Studio, kết nối vào database `luckydraw` và chạy các file Script sau:

1. **Tạo Stored Procedure:**
   Mở và chạy file `sp.sql` (nằm ở ngay ngoài cùng thư mục dự án). Đây là file chứa 2 thuật toán cốt lõi của hệ thống: `sp_TraoQuaVaTruKho` và `sp_CongLuotAnToan`. **Thiếu file này hệ thống sẽ báo lỗi 500 khi quay trúng thưởng!**

2. **Tạo tài khoản Admin mặc định:**
   Chạy câu lệnh SQL sau để tạo 1 tài khoản Admin cấp cao nhằm đăng nhập vào trang `/quanly`:
   ```sql
   INSERT INTO staff (username, password, ho_ten, chuc_vu, trang_thai) 
   VALUES ('admin', '$2a$10$Y1sL9R9.Q1Fv/t8P4w4pHO6O2bN1X/YhS.Rz9z5c.G2E5G.H3C4O.', N'Quản trị viên', 'ADMIN', 1);
   ```
   *(Tài khoản đăng nhập: `admin` | Mật khẩu: `123456`)*

## 6. Chạy chính thức
Bây giờ mọi thứ đã sẵn sàng. Bạn mở lại terminal và chạy lệnh:
```bash
.\mvnw spring-boot:run
```
- **Trang quản trị:** `http://localhost:8080/quanly/login`
- **Trang chơi game (Mini App Zalo):** `http://localhost:8080/`
