# Tổng Hợp Giao Diện (UI Manifest) - Lucky Draw O2O

Tài liệu này đóng vai trò như một bản đồ (sitemap) dành cho AI và Lập trình viên để tra cứu nhanh toàn bộ hệ thống giao diện (Frontend Mockups) của dự án **Web Quay Máy Rủi (Lucky Draw)**. Hệ thống chia thành 3 nhóm người dùng chính tương ứng với 3 thư mục.

---

## 1. Dành Cho Quản Trị Viên (Thư mục: `files/CRUD/`)
Giao diện Web Admin (dựa trên AdminLTE) dùng để cấu hình toàn bộ hệ thống, quản lý Master Data, quản lý chiến dịch và xem báo cáo.

| Tên File | Vai trò / Tính năng chính | Mapping Database |
| --- | --- | --- |
| `index.html` | Dashboard Tổng quan (Thống kê hóa đơn, doanh thu, lượt quay). | `(All)` |
| `login.html` | Đăng nhập Admin / Quản lý. | `staff` |
| `profile.html` | Quản lý thông tin cá nhân của Admin. | `staff` |
| `campaign-list.html` | Quản lý Chiến dịch. Cấu hình Luật chơi (Rules), Thời gian, và Phân bổ chiến dịch xuống từng Cửa hàng cụ thể. | `campaign`, `campaign_rule`, `campaign_store` |
| `minigame-builder.html` | Giao diện Thiết kế Minigame (Vòng quay). Cho phép Marketing đổi màu sắc nền, màu viền, tùy chỉnh danh sách ô giải thưởng. Sinh ra `slug` động. | `campaign` |
| `store-list.html` | Quản lý Danh sách Cửa hàng / Đại lý. Xem các chiến dịch đang được áp dụng tại mỗi điểm bán. | `store` |
| `staff-list.html` | Quản lý Nhân viên cửa hàng và phân quyền cơ bản. | `staff` |
| `customer-list.html` | Quản lý Khách hàng. Hiển thị chi tiết ví lượt quay (bóc tách theo từng Chiến dịch) và Trạng thái hoạt động. | `customer`, `customer_turn` |
| `prize-list.html` | Kho Giải thưởng Tổng (Master Prize Data). Cấu hình xác suất (Tỷ lệ rớt giải), giới hạn trúng thưởng. | `prize` |
| `voucher-list.html` | Quản lý Danh sách Voucher / Quà tặng đã phát ra (Thực chất là Danh sách Khách Trúng Thưởng). | `reward_voucher` |
| `invoice-list.html` | Tra cứu Hóa đơn (Đồng bộ từ POS). Kiểm soát hóa đơn nào đã được cấp lượt, cấp cho chiến dịch nào. | `invoice` |
| `turn-history.html` | Lịch sử Biến động Lượt Quay (Audit Trail). Theo dõi dấu vết cộng/trừ lượt quay của từng khách theo từng chương trình. | `turn_transaction` |
| `token-lookup.html` | Tra cứu Mã Dự Thưởng (Game Access Token). Dùng cho CSKH kiểm tra trạng thái quét mã QR trên bill (Chưa quét/Đã quét/Hết hạn). | `game_access_token` |
| `zalo-zns-config.html`| Cấu hình Thông báo Zalo ZNS / SMS tự động gửi cho khách hàng (Mẫu tin nhắn trúng giải, nhắc nhở). | `(Config)` |

---

## 2. Dành Cho Nhân Viên Cửa Hàng (Thư mục: `files/Staff_UX/`)
Giao diện nghiệp vụ tại điểm bán vật lý (Cửa hàng Offline).

| Tên File | Vai trò / Tính năng chính | Mapping Database |
| --- | --- | --- |
| `pos-simulator.html` | Giả lập phần mềm Tính Tiền (POS). Cho phép chọn mặt hàng, thanh toán và in Hóa Đơn (Sinh ra Mã Dự Thưởng/QR Code đẩy về hệ thống). | `invoice`, `game_access_token` |
| `staff-redemption.html`| Giao diện Nhân viên Gạch Mã (Đổi Quà). Khách hàng trúng giải đưa mã Voucher, nhân viên nhập mã vào đây để xác nhận đã trả thưởng. | `reward_voucher` |
| `store-inventory.html` | Quản lý Tồn Kho Giải Thưởng vật lý tại Cửa hàng. Cảnh báo sắp hết quà để yêu cầu Tổng công ty chuyển thêm. | `store_prize_inventory` |

---

## 3. Dành Cho Khách Hàng Cuối (Thư mục: `files/User_UX/`)
Giao diện Mini App (Mobile-first) dành cho Khách hàng chơi quay thưởng sau khi quét mã QR trên Hóa đơn.

| Tên File | Vai trò / Tính năng chính | Mapping Database |
| --- | --- | --- |
| `login.html` | Khách hàng đăng nhập / Xác thực SĐT (Hoặc Zalo Login). | `customer` |
| `index.html` | Màn hình chính Mini App. Hiển thị Lượt quay đang có, các Chiến dịch đang diễn ra. | `customer`, `campaign` |
| `spin-wheel-demo.html` | Giao diện Chơi Game (Vòng Quay Bí Ẩn - Mystery Wheel). Hiển thị các hộp quà, hiệu ứng quay, và Popup chúc mừng khi trúng giải. | `customer_turn` |
| `history.html` | Lịch sử Tham gia của Khách hàng (Lịch sử biến động cộng/trừ lượt của riêng user đó). | `turn_transaction` |
| `account.html` | Túi Quà của tôi. Nơi chứa danh sách các phần thưởng đã trúng. | `reward_voucher` |
| `prize-detail.html` | Chi tiết một phần thưởng cụ thể. Hiển thị QR Code / Mã Barcode để khách đưa cho Nhân viên quét gạch mã nhận quà thực tế. | `reward_voucher` |

---
**Ghi chú hệ thống:** 
- Toàn bộ giao diện User_UX là kiến trúc **Dynamic Single Page Application**. Backend chỉ dựa vào `slug` của chiến dịch để trả về dữ liệu (JSON), Frontend tự động "khoác áo" theme và hiển thị mà không cần tạo file HTML mới cho mỗi chiến dịch.
- Toàn bộ luồng phát sinh lượt quay là Tự Động (POS -> Cấp Lượt -> In Mã QR). Không có thao tác nhân viên tự nhập tay số lượt, nhằm loại trừ tuyệt đối "Gian lận (Fraud)".
