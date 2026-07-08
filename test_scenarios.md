# 📋 Kịch Bản Test Chức Năng Toàn Bộ Hệ Thống Lucky Draw O2O

> **Dự án**: Web Quay Máy Rủi (Lucky Draw)
> **Ngày tạo**: 2026-07-08
> **Phạm vi**: Test chức năng (Functional Testing) toàn bộ nghiệp vụ, từ Admin → POS → Khách hàng → Nhân viên cửa hàng

---

## Mục Lục

- [Module A: Đăng nhập & Phân quyền](#module-a-đăng-nhập--phân-quyền)
- [Module B: Quản lý Cửa hàng (Store)](#module-b-quản-lý-cửa-hàng)
- [Module C: Quản lý Chiến dịch (Campaign)](#module-c-quản-lý-chiến-dịch)
- [Module D: Cấu hình Luật chơi (Campaign Rule)](#module-d-cấu-hình-luật-chơi)
- [Module E: Quản lý Giải thưởng (Prize)](#module-e-quản-lý-giải-thưởng)
- [Module F: Tồn kho Giải thưởng (Store Prize Inventory)](#module-f-tồn-kho-giải-thưởng)
- [Module G: Đồng bộ Hóa đơn từ POS (POS Sync)](#module-g-đồng-bộ-hóa-đơn-từ-pos)
- [Module H: Mã Dự Thưởng (Game Access Token)](#module-h-mã-dự-thưởng)
- [Module I: Quay số / Chơi game (Spin)](#module-i-quay-số--chơi-game)
- [Module J: Đổi quà / Gạch mã (Redemption)](#module-j-đổi-quà--gạch-mã)
- [Module K: Xử lý Đổi/Trả hàng (Delta Rule Engine)](#module-k-xử-lý-đổitrả-hàng)
- [Module L: Quản lý Khách hàng & Lượt quay](#module-l-quản-lý-khách-hàng--lượt-quay)
- [Module M: Báo cáo & Dashboard](#module-m-báo-cáo--dashboard)
- [Module N: Luồng End-to-End (E2E)](#module-n-luồng-end-to-end)

---

## Module A: Đăng nhập & Phân quyền

### Bảng liên quan: `staff`, `vai_tro`, `phan_quyen`, `chuc_nang`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| A-01 | Đăng nhập thành công | Tài khoản admin hợp lệ đã tồn tại | 1. Truy cập `/admin/login` <br> 2. Nhập username/password đúng <br> 3. Bấm Đăng nhập | Chuyển hướng đến `/admin/dashboard`. Session được tạo. | 🔴 Critical |
| A-02 | Đăng nhập sai mật khẩu | Tài khoản admin tồn tại | 1. Nhập username đúng, password sai <br> 2. Bấm Đăng nhập | Hiển thị thông báo lỗi "Sai tên đăng nhập hoặc mật khẩu". Không tạo session. | 🔴 Critical |
| A-03 | Đăng nhập tài khoản bị khóa | Tài khoản có `status = INACTIVE` | 1. Nhập username/password đúng <br> 2. Bấm Đăng nhập | Hiển thị lỗi "Tài khoản đã bị khóa". | 🟡 High |
| A-04 | Phân quyền: Admin truy cập đầy đủ | Đăng nhập với vai trò Admin (full quyền) | 1. Truy cập lần lượt các menu: Chiến dịch, Cửa hàng, Giải thưởng, Khách hàng, Nhân viên, Hóa đơn, Voucher | Tất cả menu đều hiển thị và truy cập được. | 🟡 High |
| A-05 | Phân quyền: Nhân viên cửa hàng bị chặn trang Admin | Đăng nhập với vai trò Staff (quyền hạn chế) | 1. Truy cập `/admin/campaigns` | Chuyển hướng về trang lỗi 403 hoặc trang chủ. | 🟡 High |
| A-06 | Đăng xuất | Đã đăng nhập | 1. Bấm Đăng xuất | Session bị hủy. Chuyển về `/admin/login`. Truy cập lại trang admin bị redirect về login. | 🟢 Medium |
| A-07 | Cập nhật thông tin cá nhân | Đã đăng nhập | 1. Truy cập `/admin/profile` <br> 2. Sửa tên, email <br> 3. Bấm Lưu | Thông tin được cập nhật. Hiển thị thông báo thành công. | 🟢 Medium |

---

## Module B: Quản lý Cửa hàng

### Bảng liên quan: `store`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| B-01 | Thêm cửa hàng mới | Đăng nhập Admin | 1. Truy cập `/admin/stores` <br> 2. Bấm Thêm mới <br> 3. Nhập tên, địa chỉ, POS Store ID <br> 4. Bấm Lưu | Cửa hàng được tạo. Xuất hiện trong danh sách. `pos_store_id` được lưu. | 🟡 High |
| B-02 | Thêm cửa hàng trùng POS Store ID | Đã có cửa hàng với `pos_store_id = 'STORE001'` | 1. Thêm cửa hàng mới với `pos_store_id = 'STORE001'` | Báo lỗi "POS Store ID đã tồn tại". Không tạo bản ghi mới. | 🟡 High |
| B-03 | Sửa thông tin cửa hàng | Cửa hàng đã tồn tại | 1. Bấm Sửa trên cửa hàng <br> 2. Thay đổi tên, địa chỉ <br> 3. Bấm Lưu | Thông tin được cập nhật thành công. | 🟢 Medium |
| B-04 | Xóa cửa hàng không có chiến dịch | Cửa hàng chưa được gán cho chiến dịch nào | 1. Bấm Xóa cửa hàng <br> 2. Xác nhận | Cửa hàng bị xóa khỏi danh sách. | 🟢 Medium |
| B-05 | Xóa cửa hàng đang có chiến dịch | Cửa hàng đang liên kết với chiến dịch ACTIVE | 1. Bấm Xóa cửa hàng | Báo lỗi "Không thể xóa cửa hàng đang tham gia chiến dịch". | 🟡 High |
| B-06 | Import cửa hàng từ Excel | File Excel đúng format mẫu | 1. Bấm Import <br> 2. Chọn file `.xlsx` <br> 3. Bấm Upload | Tất cả cửa hàng trong file được thêm. Hiển thị thông báo "Import thành công X cửa hàng". | 🟡 High |
| B-07 | Import Excel sai format | File Excel thiếu cột bắt buộc | 1. Upload file sai format | Báo lỗi chi tiết: dòng nào sai, cột nào thiếu. Không import bất kỳ bản ghi nào. | 🟡 High |

---

## Module C: Quản lý Chiến dịch

### Bảng liên quan: `campaign`, `campaign_store`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| C-01 | Tạo chiến dịch mới (DRAFT) | Đăng nhập Admin | 1. Truy cập `/admin/campaigns/new` <br> 2. Nhập tên, chọn ngày bắt đầu/kết thúc, chọn game type <br> 3. Bấm Tạo | Chiến dịch được tạo với `status = DRAFT`. `slug` được tự động sinh. Xuất hiện trong danh sách. | 🔴 Critical |
| C-02 | Tạo chiến dịch trùng tên | Chiến dịch "Hè Vui 2026" đã tồn tại | 1. Tạo chiến dịch với cùng tên | Tạo thành công (cho phép trùng tên) nhưng `slug` phải khác nhau (unique). | 🟡 High |
| C-03 | Sửa chiến dịch DRAFT | Chiến dịch ở trạng thái DRAFT | 1. Bấm Sửa <br> 2. Thay đổi tên, ngày <br> 3. Bấm Lưu | Cập nhật thành công. | 🟢 Medium |
| C-04 | Sửa chiến dịch ENDED | Chiến dịch ở trạng thái ENDED | 1. Bấm Sửa chiến dịch đã kết thúc | Báo lỗi "Không thể chỉnh sửa chiến dịch đã kết thúc". | 🟡 High |
| C-05 | Gán cửa hàng cho chiến dịch | Chiến dịch DRAFT, có cửa hàng trong hệ thống | 1. Vào chi tiết chiến dịch <br> 2. Chọn cửa hàng từ danh sách <br> 3. Bấm Gán | `campaign_store` được tạo. Cửa hàng hiển thị trong danh sách đã gán. | 🔴 Critical |
| C-06 | Gỡ cửa hàng khỏi chiến dịch | Chiến dịch DRAFT, có cửa hàng đã gán | 1. Bấm Gỡ cửa hàng | `campaign_store` bị xóa. | 🟢 Medium |
| C-07 | Kích hoạt chiến dịch (DRAFT → ACTIVE) | Chiến dịch DRAFT đã có rules, prizes, stores | 1. Bấm "Kích hoạt" | `status` chuyển sang `ACTIVE`. Chiến dịch bắt đầu nhận hóa đơn từ POS. | 🔴 Critical |
| C-08 | Kích hoạt chiến dịch chưa có rules | Chiến dịch DRAFT, chưa cấu hình luật chơi | 1. Bấm Kích hoạt | Báo lỗi "Vui lòng cấu hình ít nhất 1 luật chơi trước khi kích hoạt". | 🔴 Critical |
| C-09 | Kích hoạt chiến dịch chưa có giải thưởng | Chiến dịch DRAFT, chưa có prize | 1. Bấm Kích hoạt | Báo lỗi "Vui lòng cấu hình giải thưởng trước khi kích hoạt". | 🔴 Critical |
| C-10 | Kết thúc chiến dịch (ACTIVE → ENDED) | Chiến dịch đang ACTIVE | 1. Bấm "Kết thúc chiến dịch" <br> 2. Xác nhận | `status = ENDED`. Tất cả `game_access_token` UNUSED bị chuyển sang EXPIRED. Chiến dịch không nhận hóa đơn mới. | 🔴 Critical |
| C-11 | Thiết kế Minigame (Wheel Config) | Chiến dịch tồn tại | 1. Vào `/admin/campaigns/{id}/minigame` <br> 2. Tùy chỉnh màu nền, viền, ô giải thưởng <br> 3. Bấm Lưu | `wheel_config` (JSON) được lưu. Preview vòng quay phản ánh đúng cấu hình. | 🟡 High |

---

## Module D: Cấu hình Luật chơi

### Bảng liên quan: `campaign_rule`, `campaign_rule_payment`, `campaign_rule_sku`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| D-01 | Thêm luật theo số tiền (AMOUNT) | Chiến dịch DRAFT | 1. Vào tab Rules <br> 2. Chọn `rule_type = AMOUNT` <br> 3. Nhập `min_amount = 500000`, `turns_granted = 1` <br> 4. Chọn payment methods: CASH, CARD <br> 5. Bấm Thêm | Rule được tạo. Hóa đơn ≥ 500k thanh toán bằng tiền mặt hoặc thẻ sẽ được cấp 1 lượt. | 🔴 Critical |
| D-02 | Thêm luật theo SKU | Chiến dịch DRAFT | 1. Chọn `rule_type = SKU` <br> 2. Thêm các SKU code cụ thể <br> 3. Nhập `turns_granted = 2` <br> 4. Bấm Thêm | Rule được tạo. Hóa đơn chứa SKU phù hợp sẽ được cấp 2 lượt. | 🔴 Critical |
| D-03 | Thêm nhiều luật cho 1 chiến dịch | Chiến dịch đã có 1 rule AMOUNT | 1. Thêm thêm 1 rule SKU | Cả 2 rules cùng tồn tại. Khi đánh giá hóa đơn, tổng lượt = tổng các rules thỏa mãn. | 🟡 High |
| D-04 | Xóa luật chơi | Chiến dịch DRAFT có rules | 1. Bấm Xóa rule | Rule bị xóa cùng các `campaign_rule_payment` và `campaign_rule_sku` liên quan (cascade). | 🟢 Medium |
| D-05 | Luật AMOUNT: Hóa đơn dưới ngưỡng | Rule: min_amount = 500k | Gửi hóa đơn 400k | Hóa đơn được lưu nhưng `turns_granted = 0`. Không cấp lượt. | 🔴 Critical |
| D-06 | Luật AMOUNT: Hóa đơn đúng ngưỡng | Rule: min_amount = 500k | Gửi hóa đơn 500k | `turns_granted = 1` (hoặc theo cấu hình). Lượt được cấp. | 🔴 Critical |
| D-07 | Luật AMOUNT: Hóa đơn trên ngưỡng | Rule: min_amount = 500k, turns = 1 | Gửi hóa đơn 1.500.000 | `turns_granted = 1` (cấp đúng số lượt cấu hình, không nhân bội — trừ khi rule cho phép). | 🟡 High |
| D-08 | Luật AMOUNT: Sai phương thức thanh toán | Rule: chỉ cho CASH | Gửi hóa đơn thanh toán CARD | `turns_granted = 0`. Không cấp lượt. | 🔴 Critical |
| D-09 | Luật SKU: Hóa đơn có SKU phù hợp | Rule SKU: SKU001, SKU002 | Gửi hóa đơn chứa SKU001 | `turns_granted = 2` (theo cấu hình rule). | 🔴 Critical |
| D-10 | Luật SKU: Hóa đơn không có SKU phù hợp | Rule SKU: SKU001 | Gửi hóa đơn chỉ chứa SKU999 | `turns_granted = 0`. | 🟡 High |

---

## Module E: Quản lý Giải thưởng

### Bảng liên quan: `prize`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| E-01 | Thêm giải thưởng vật lý | Chiến dịch tồn tại | 1. Vào `/admin/prizes/new` <br> 2. Chọn campaign, nhập tên, type = PHYSICAL <br> 3. Nhập probability = 10%, total_quantity = 100, max_win_per_customer = 1 <br> 4. Bấm Tạo | Giải thưởng được tạo. `remaining_quantity = 100`. | 🔴 Critical |
| E-02 | Thêm giải thưởng voucher | Chiến dịch tồn tại | 1. Tạo prize type = VOUCHER | Tạo thành công. | 🟡 High |
| E-03 | Sửa xác suất giải thưởng | Giải thưởng đã tồn tại | 1. Sửa probability từ 10% → 5% <br> 2. Bấm Lưu | Cập nhật thành công. Xác suất trúng thay đổi ngay lập tức. | 🟡 High |
| E-04 | Xóa giải thưởng chưa có ai trúng | Prize chưa có reward_voucher | 1. Bấm Xóa | Xóa thành công. | 🟢 Medium |
| E-05 | Xóa giải thưởng đã có người trúng | Prize đã có reward_voucher liên kết | 1. Bấm Xóa | Báo lỗi "Không thể xóa giải thưởng đã có người trúng". | 🟡 High |
| E-06 | Import giải thưởng từ Excel | File Excel đúng format | 1. Bấm Import <br> 2. Chọn file <br> 3. Upload | Các giải thưởng được thêm hàng loạt. Thông báo thành công. | 🟡 High |
| E-07 | Import mã voucher cho giải thưởng | Prize type = VOUCHER đã tồn tại | 1. Bấm Import Codes <br> 2. Upload file chứa danh sách mã | Các mã được gắn cho giải thưởng. Sẵn sàng phát khi khách trúng. | 🟡 High |
| E-08 | Tổng xác suất > 100% | Thêm prizes sao cho tổng probability > 100% | 1. Thêm prize với probability khiến tổng vượt 100% | Hệ thống cảnh báo hoặc chặn: "Tổng xác suất không được vượt quá 100%". | 🔴 Critical |

---

## Module F: Tồn kho Giải thưởng

### Bảng liên quan: `store_prize_inventory`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| F-01 | Xem tồn kho cửa hàng | Nhân viên cửa hàng đăng nhập | 1. Truy cập `/staff/inventory` | Hiển thị danh sách giải thưởng kèm số lượng còn lại tại cửa hàng đó. | 🟡 High |
| F-02 | Cảnh báo sắp hết hàng | `remaining_quantity` ≤ ngưỡng cảnh báo | 1. Truy cập `/staff/inventory/alerts` | Hiển thị cảnh báo cho các giải thưởng sắp hết. | 🟢 Medium |
| F-03 | Tồn kho giảm khi đổi quà | Khách đổi voucher → nhân viên gạch mã | 1. Gạch mã thành công | `store_prize_inventory.remaining_quantity` giảm 1. | 🔴 Critical |
| F-04 | Tồn kho không âm (Trigger) | `remaining_quantity = 0` | 1. Cố gắng gạch mã thêm 1 lần | Trigger `trg_inventory_check` chặn. Báo lỗi "Hết hàng tại cửa hàng". | 🔴 Critical |

---

## Module G: Đồng bộ Hóa đơn từ POS

### Bảng liên quan: `invoice`, `customer`, `customer_turn`, `turn_transaction`, `game_access_token`
### Endpoint: `POST /api/pos/sync`

> [!IMPORTANT]
> Đây là luồng nghiệp vụ QUAN TRỌNG NHẤT của hệ thống. Mọi lượt quay đều bắt nguồn từ đây.

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| G-01 | Sync hóa đơn thành công (Happy Path) | Campaign ACTIVE, store đã gán, rule AMOUNT ≥ 500k | 1. Gửi `POST /api/pos/sync` với payload: `posInvoiceId`, `posStoreId`, `customerPhone`, `totalAmount = 600000`, `paymentMethod = CASH`, items | **Response 200**: <br> - `invoice` được tạo, `status = SYNCED` <br> - `customer` được tạo nếu chưa tồn tại (auto-create by phone) <br> - `customer_turn.remaining_turns` tăng theo rule <br> - `turn_transaction` type = CREDIT được tạo <br> - `game_access_token` status = UNUSED được tạo <br> - Response trả về `token` và `turnsGranted` | 🔴 Critical |
| G-02 | Sync hóa đơn trùng `posInvoiceId` | Hóa đơn cùng `posInvoiceId` đã sync | 1. Gửi lại cùng payload | Response lỗi "Hóa đơn đã được đồng bộ trước đó". Không tạo bản ghi mới, không cấp thêm lượt. | 🔴 Critical |
| G-03 | Sync hóa đơn - Store không tồn tại | `posStoreId` không khớp bất kỳ store nào | 1. Gửi payload với `posStoreId = 'INVALID'` | Response lỗi "Cửa hàng không tồn tại trong hệ thống". | 🔴 Critical |
| G-04 | Sync hóa đơn - Store không thuộc chiến dịch nào | Store tồn tại nhưng không được gán cho campaign ACTIVE nào | 1. Gửi payload hợp lệ | Response lỗi "Cửa hàng chưa được gán cho chiến dịch đang hoạt động" hoặc `turnsGranted = 0`. | 🟡 High |
| G-05 | Sync hóa đơn - Không thỏa rule | Campaign ACTIVE, rule min 500k | 1. Gửi hóa đơn `totalAmount = 200000` | Invoice được lưu. `turnsGranted = 0`. Không tạo `game_access_token`. | 🔴 Critical |
| G-06 | Sync hóa đơn - Khách hàng mới (Auto-create) | Phone chưa tồn tại trong `customer` | 1. Gửi payload với SĐT mới | `customer` mới được tạo tự động. `customer_turn` mới được tạo. Lượt được cấp. | 🔴 Critical |
| G-07 | Sync hóa đơn - Khách hàng cũ | Khách đã tồn tại, đã có `customer_turn` cho campaign | 1. Gửi payload với SĐT đã tồn tại | `customer_turn.remaining_turns` và `total_turns` tăng thêm. Không tạo customer mới. | 🔴 Critical |
| G-08 | Sync hóa đơn - Nhiều rules thỏa mãn | Campaign có rule AMOUNT 500k (1 lượt) + rule SKU (2 lượt) | 1. Gửi hóa đơn 600k chứa SKU phù hợp | `turnsGranted = 3` (1 + 2). `customer_turn.remaining_turns` tăng 3. | 🟡 High |
| G-09 | Sync hóa đơn - Campaign hết hạn | Campaign `end_date` < ngày hiện tại | 1. Gửi payload hợp lệ | Không match campaign nào. `turnsGranted = 0`. | 🟡 High |
| G-10 | POS Health Check | Hệ thống đang chạy | 1. Gửi `GET /api/pos/health` | Response 200 OK. | 🟢 Medium |
| G-11 | POS Simulator gửi test data | Đăng nhập admin | 1. Vào `/admin/pos-simulator` <br> 2. Điền form giả lập <br> 3. Bấm Gửi | Gọi `PosService` nội bộ. Kết quả hiển thị trên trang simulator. | 🟢 Medium |

---

## Module H: Mã Dự Thưởng (Game Access Token)

### Bảng liên quan: `game_access_token`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| H-01 | Quét mã QR thành công | Token UNUSED, chưa hết hạn | 1. Khách quét QR trên bill → truy cập `/token/{tokenValue}` | Token chuyển sang `USED`. `used_at` được ghi. Redirect đến `/game/{slug}` của campaign tương ứng. | 🔴 Critical |
| H-02 | Quét mã QR đã sử dụng | Token `status = USED` | 1. Quét lại cùng mã | Hiển thị thông báo "Mã đã được sử dụng". Không cho vào game lần nữa (hoặc cho vào nhưng không cấp thêm lượt). | 🟡 High |
| H-03 | Quét mã QR hết hạn | Token `status = EXPIRED` | 1. Quét mã | Hiển thị "Mã dự thưởng đã hết hạn". | 🟡 High |
| H-04 | Quét mã QR không tồn tại | Token value sai | 1. Truy cập `/token/INVALID_TOKEN` | Hiển thị "Mã dự thưởng không hợp lệ". | 🟢 Medium |
| H-05 | Admin tra cứu mã dự thưởng | Đăng nhập admin | 1. Vào `/admin/token-lookup` <br> 2. Nhập token value <br> 3. Bấm Tìm | Hiển thị chi tiết: trạng thái (UNUSED/USED/EXPIRED), khách hàng, chiến dịch, ngày tạo, ngày dùng. | 🟢 Medium |
| H-06 | Hết hạn token khi kết thúc campaign | Campaign ACTIVE → ENDED | 1. Kết thúc chiến dịch | Stored procedure `sp_expire_tokens` chạy. Tất cả token UNUSED của campaign chuyển sang EXPIRED. | 🔴 Critical |

---

## Module I: Quay số / Chơi game (Spin)

### Bảng liên quan: `customer_turn`, `turn_transaction`, `prize`, `reward_voucher`
### Endpoint: `POST /game/{slug}/spin`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| I-01 | Quay thành công - Trúng giải | Khách đã login, `remaining_turns ≥ 1`, prize còn hàng | 1. Vào `/game/{slug}/spin` <br> 2. Bấm Quay | - `remaining_turns` giảm 1, `used_turns` tăng 1 <br> - `turn_transaction` DEBIT được tạo <br> - Nếu trúng: `reward_voucher` được tạo (`status = WON`), `prize.remaining_quantity` giảm 1 <br> - Hiệu ứng quay + popup chúc mừng | 🔴 Critical |
| I-02 | Quay thành công - Không trúng | Khách có lượt, xác suất trúng < 100% | 1. Bấm Quay | - Lượt giảm 1 <br> - Không tạo `reward_voucher` <br> - Hiển thị "Chúc bạn may mắn lần sau" | 🔴 Critical |
| I-03 | Quay khi hết lượt | `remaining_turns = 0` | 1. Bấm Quay | Thông báo "Bạn đã hết lượt quay". Nút quay bị vô hiệu hóa. | 🔴 Critical |
| I-04 | Trúng giải khi giải đã hết hàng | `prize.remaining_quantity = 0` | 1. Bấm Quay (thuật toán chọn giải bỏ qua giải hết hàng) | Thuật toán loại trừ giải đã hết. Khách trúng giải khác hoặc không trúng. Trigger `trg_prize_quantity_check` đảm bảo `remaining_quantity` không âm. | 🔴 Critical |
| I-05 | Vượt giới hạn trúng / khách | `max_win_per_customer = 1`, khách đã trúng prize này 1 lần | 1. Bấm Quay nhiều lần | Giải thưởng đó bị loại khỏi pool xác suất cho khách này. Không trúng lại. | 🔴 Critical |
| I-06 | Quay liên tục (spam) | Khách có nhiều lượt | 1. Bấm Quay nhanh liên tục | Mỗi lần quay chỉ trừ 1 lượt. Không xảy ra race condition (concurrent debit). DB trigger `trg_customer_turn_check` chặn âm. | 🔴 Critical |
| I-07 | Xem lịch sử quay | Khách đã quay nhiều lần | 1. Vào `/game/{slug}/history` | Hiển thị danh sách `turn_transaction` của khách: ngày giờ, loại (CREDIT/DEBIT), lý do. | 🟢 Medium |
| I-08 | Xem túi quà (danh sách giải đã trúng) | Khách đã trúng giải | 1. Vào `/game/{slug}/rewards` | Hiển thị danh sách `reward_voucher` với trạng thái (WON/REDEEMED). | 🟢 Medium |
| I-09 | Xem chi tiết giải thưởng (QR đổi quà) | Khách có voucher status = WON | 1. Vào `/game/{slug}/rewards/{id}` | Hiển thị chi tiết giải + QR code / Barcode chứa `voucher_code` để nhân viên quét. | 🟡 High |

---

## Module J: Đổi quà / Gạch mã (Redemption)

### Bảng liên quan: `reward_voucher`, `store_prize_inventory`
### Endpoint: `POST /staff/redemption/redeem`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| J-01 | Gạch mã thành công | Voucher `status = WON`, nhân viên đăng nhập | 1. Vào `/staff/redemption` <br> 2. Nhập/quét `voucher_code` <br> 3. Bấm Xác nhận đổi quà | - `reward_voucher.status = REDEEMED` <br> - `redeemed_at` = now, `redeemed_by` = staff ID <br> - `store_prize_inventory.remaining_quantity` giảm 1 <br> - `system_audit_log` ghi nhận (trigger `trg_voucher_audit`) | 🔴 Critical |
| J-02 | Gạch mã đã đổi rồi | Voucher `status = REDEEMED` | 1. Nhập lại cùng `voucher_code` | Báo lỗi "Mã đã được đổi quà trước đó". Hiển thị thông tin: đổi bởi ai, lúc nào. | 🔴 Critical |
| J-03 | Gạch mã hết hạn | Voucher `status = EXPIRED` | 1. Nhập `voucher_code` hết hạn | Báo lỗi "Mã đã hết hạn". | 🟡 High |
| J-04 | Gạch mã không tồn tại | Mã nhập sai | 1. Nhập mã bất kỳ | Báo lỗi "Mã không hợp lệ". | 🟢 Medium |
| J-05 | Validate mã trước khi gạch (AJAX) | Voucher hợp lệ | 1. Gọi `GET /staff/redemption/validate/{code}` | Trả về thông tin: tên giải, tên khách hàng, trạng thái. Để nhân viên xác nhận trước khi bấm Đổi. | 🟡 High |
| J-06 | Gạch mã khi cửa hàng hết hàng | `store_prize_inventory.remaining_quantity = 0` | 1. Cố gạch mã | Trigger `trg_inventory_check` chặn. Báo lỗi "Kho hàng tại cửa hàng đã hết". | 🔴 Critical |

---

## Module K: Xử lý Đổi/Trả hàng (Delta Rule Engine)

### Service: `DeltaRuleEngine`
### Endpoint: `POST /api/pos/webhook`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| K-01 | Trả hàng - Hóa đơn giảm dưới ngưỡng | Hóa đơn gốc 600k (đã cấp 1 lượt), rule min 500k | 1. POS gửi webhook: khách trả hàng, `newAmount = 400k` | `DeltaRuleEngine` tính delta. Hóa đơn mới dưới ngưỡng → `sp_reclaim_turns` thu hồi 1 lượt. `turn_transaction` DEBIT với reason = "RETURN". | 🔴 Critical |
| K-02 | Trả hàng - Hóa đơn vẫn trên ngưỡng | Hóa đơn gốc 800k, trả hàng còn 600k, rule min 500k | 1. POS gửi webhook: `newAmount = 600k` | Vẫn thỏa rule → không thu hồi lượt. | 🟡 High |
| K-03 | Đổi hàng - Giá trị tăng | Hóa đơn gốc 400k (0 lượt), đổi hàng tăng lên 600k | 1. POS gửi webhook: `newAmount = 600k` | Thỏa rule → cấp thêm lượt. `turn_transaction` CREDIT với reason = "EXCHANGE". | 🟡 High |
| K-04 | Thu hồi lượt - Khách đã dùng hết | Khách đã quay hết lượt (`remaining_turns = 0`), cần thu hồi 1 | 1. POS gửi webhook trả hàng | `sp_reclaim_turns` chỉ giảm `remaining_turns` đến 0 (không âm). Trigger `trg_customer_turn_check` đảm bảo. | 🔴 Critical |

---

## Module L: Quản lý Khách hàng & Lượt quay

### Bảng liên quan: `customer`, `customer_turn`, `turn_transaction`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| L-01 | Xem danh sách khách hàng | Đăng nhập Admin | 1. Vào `/admin/customers` <br> 2. Tìm kiếm theo SĐT hoặc tên | Hiển thị danh sách phân trang. Tìm kiếm hoạt động chính xác. | 🟢 Medium |
| L-02 | Xem chi tiết khách hàng | Khách hàng tồn tại | 1. Vào `/admin/customers/{id}` | Hiển thị thông tin khách, danh sách ví lượt quay bóc tách theo chiến dịch (`customer_turn`). | 🟡 High |
| L-03 | Xem lịch sử biến động lượt quay | Khách có turn_transactions | 1. Vào `/admin/customers/{id}/turns` hoặc `/admin/turn-history` | Hiển thị chi tiết: CREDIT (cộng lượt từ hóa đơn nào), DEBIT (trừ lượt khi quay hoặc thu hồi). | 🟡 High |
| L-04 | Lượt quay không âm (Trigger) | `remaining_turns = 0` | 1. Cố consume thêm 1 lượt (qua spin hoặc reclaim) | Trigger `trg_customer_turn_check` chặn UPDATE. Lỗi được throw. | 🔴 Critical |

---

## Module M: Báo cáo & Dashboard

### View: `v_customer_turn_summary`, `v_invoice_detail`, `v_prize_status`, `v_voucher_detail`, `v_store_inventory_detail`

| ID | Tên Test Case | Điều kiện tiên quyết | Bước thực hiện | Kết quả mong đợi | Mức độ |
|----|---------------|---------------------|----------------|-------------------|--------|
| M-01 | Dashboard tổng quan | Có dữ liệu trong hệ thống | 1. Vào `/admin/dashboard` | Hiển thị đúng: tổng hóa đơn, tổng doanh thu, tổng lượt cấp, tổng lượt đã dùng, tổng giải đã trúng. | 🟡 High |
| M-02 | Dashboard lọc theo chiến dịch | Có nhiều chiến dịch | 1. Chọn lọc theo 1 campaign cụ thể | Số liệu chỉ phản ánh campaign được chọn. | 🟡 High |
| M-03 | Báo cáo giải thưởng | Campaign có prizes đã trúng | 1. Xem prize report | Mỗi giải hiển thị: tổng trúng, đã đổi, còn lại. Khớp với `v_prize_status`. | 🟡 High |
| M-04 | Báo cáo theo cửa hàng | Campaign có nhiều stores | 1. Xem store report | Mỗi cửa hàng hiển thị: số hóa đơn, lượt cấp, giải trúng. | 🟢 Medium |
| M-05 | Xem danh sách voucher (khách trúng thưởng) | Có reward_vouchers | 1. Vào `/admin/vouchers` <br> 2. Lọc theo trạng thái, chiến dịch | Danh sách chính xác. Lọc hoạt động đúng. | 🟢 Medium |
| M-06 | Xem chi tiết hóa đơn | Hóa đơn tồn tại | 1. Vào `/admin/invoices/{id}` | Hiển thị đầy đủ: cửa hàng, khách hàng, chiến dịch, số tiền, lượt đã cấp, mã token. | 🟢 Medium |

---

## Module N: Luồng End-to-End (E2E)

> [!CAUTION]
> Các kịch bản E2E kiểm tra toàn bộ chuỗi nghiệp vụ xuyên suốt. **BẮT BUỘC phải test trước khi go-live.**

### E2E-01: Luồng chính — Từ Hóa đơn đến Nhận quà

```
POS → Sync Hóa đơn → Cấp Lượt → In Mã QR → Khách Quét QR → Login → Quay Số → Trúng Giải → Nhân Viên Gạch Mã → Nhận Quà
```

| Bước | Hành động | Kiểm tra |
|------|-----------|----------|
| 1 | POS gửi `POST /api/pos/sync` với hóa đơn 600k, khách SĐT 0901234567 | Invoice được tạo. Customer auto-created. `turnsGranted = 1`. Token UNUSED trả về. |
| 2 | Khách quét QR → `GET /token/{tokenValue}` | Token → USED. Redirect `/game/{slug}`. |
| 3 | Khách login bằng SĐT → `POST /game/{slug}/login` | Xác thực thành công. Hiển thị 1 lượt quay. |
| 4 | Khách quay → `POST /game/{slug}/spin` | `remaining_turns`: 1→0. Kết quả: trúng hoặc không. |
| 5 | (Nếu trúng) Khách xem chi tiết giải → `/game/{slug}/rewards/{id}` | QR/Barcode `voucher_code` hiển thị. |
| 6 | Nhân viên quét mã → `POST /staff/redemption/redeem` | `reward_voucher.status = REDEEMED`. Kho giảm 1. Audit log ghi nhận. |
| 7 | Admin kiểm tra Dashboard | Số liệu cập nhật: +1 hóa đơn, +1 lượt, +1 giải (nếu trúng), +1 đổi quà. |

### E2E-02: Khách mua nhiều lần — Tích lũy lượt

| Bước | Hành động | Kiểm tra |
|------|-----------|----------|
| 1 | POS sync hóa đơn #1: 600k | `remaining_turns = 1` |
| 2 | POS sync hóa đơn #2: 700k | `remaining_turns = 2` (tích lũy) |
| 3 | Khách quay 1 lần | `remaining_turns = 1` |
| 4 | POS sync hóa đơn #3: 500k | `remaining_turns = 2` |
| 5 | Khách quay 2 lần | `remaining_turns = 0` |
| 6 | Khách thử quay thêm | Lỗi "Hết lượt". Nút quay bị disable. |

### E2E-03: Đổi/Trả hàng — Thu hồi lượt

| Bước | Hành động | Kiểm tra |
|------|-----------|----------|
| 1 | POS sync hóa đơn 600k → cấp 1 lượt | `remaining_turns = 1` |
| 2 | Khách chưa quay. POS gửi webhook trả hàng: `newAmount = 300k` | DeltaRuleEngine thu hồi 1 lượt. `remaining_turns = 0`. |
| 3 | Khách vào game thấy 0 lượt | Nút quay bị disable. |

### E2E-04: Kết thúc chiến dịch

| Bước | Hành động | Kiểm tra |
|------|-----------|----------|
| 1 | Admin kết thúc chiến dịch (ACTIVE → ENDED) | `campaign.status = ENDED`. |
| 2 | Kiểm tra token | Tất cả UNUSED tokens → EXPIRED. |
| 3 | POS gửi hóa đơn mới cho store của campaign đã kết thúc | Không match campaign nào. `turnsGranted = 0`. |
| 4 | Khách có voucher WON vẫn đổi quà được | Voucher WON vẫn valid cho redemption (tùy business rule). |

### E2E-05: Nhiều chiến dịch đồng thời

| Bước | Hành động | Kiểm tra |
|------|-----------|----------|
| 1 | Tạo Campaign A (store 1, 2) và Campaign B (store 2, 3) cùng ACTIVE | Cả 2 cùng hoạt động. |
| 2 | POS sync hóa đơn từ Store 2 | Hóa đơn match đúng 1 campaign (theo logic `findMatchingCampaign`). Lượt cấp cho campaign tương ứng. |
| 3 | Khách quay cho Campaign A | `customer_turn` của campaign A giảm. Campaign B không bị ảnh hưởng. |

---

## Tổng hợp Thống kê

| Phân loại | Số lượng Test Cases |
|-----------|-------------------|
| 🔴 Critical | **33** |
| 🟡 High | **27** |
| 🟢 Medium | **15** |
| **Tổng cộng** | **75** |

> [!TIP]
> **Thứ tự ưu tiên test**: Module G (POS Sync) → Module I (Spin) → Module J (Redemption) → Module K (Delta) → E2E → Còn lại.
> Các trigger DB (`trg_customer_turn_check`, `trg_prize_quantity_check`, `trg_inventory_check`) nên được test riêng bằng SQL script trực tiếp trước khi test qua API.
