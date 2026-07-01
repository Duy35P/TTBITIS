# Tự động kết thúc chiến dịch và Quản lý thời hạn

Kế hoạch này giải quyết các yêu cầu của bạn về việc tự động chuyển trạng thái chiến dịch khi hết hạn, cũng như các ràng buộc khi thay đổi ngày kết thúc.

## Open Questions

Dưới đây là câu trả lời cho câu hỏi của bạn và một số điểm cần bạn xác nhận:

> **Câu hỏi của bạn:** *"Nếu kết thúc chiến dịch sớm hơn thì xử lý như nào?"*

**Đề xuất xử lý:** Nếu bạn muốn kết thúc chiến dịch sớm (ví dụ: hết ngân sách, hết quà, hoặc có sự cố), bạn có 2 cách rất đơn giản:
1. **Cách 1 (Khuyên dùng):** Bấm nút **"Tạm ngưng"** (chuyển trạng thái về 0). Ngay lập tức chiến dịch sẽ ngừng hoạt động.


> [!IMPORTANT]
> **Câu hỏi dành cho bạn:** Hiện tại hệ thống đang có 2 trạng thái là `0` (Tạm ngưng) và `1` (Kích hoạt). Để thể hiện chiến dịch "Đã kết thúc", tôi sẽ thêm trạng thái `2` (Đã kết thúc) vào hệ thống nhé? Bạn có đồng ý với việc thêm trạng thái `2` này không, hay chỉ cần chuyển về trạng thái `0` (Tạm ngưng)?

## Proposed Changes

### 1. Thêm Scheduled Task để tự động cập nhật trạng thái
- Tạo một file service mới (ví dụ: `CampaignStatusScheduler.java`).
- Kích hoạt tính năng `@EnableScheduling` cho ứng dụng Spring Boot.
- Viết một tiến trình chạy ngầm mỗi phút (Cron job: `0 * * * * *`). Tiến trình này sẽ tìm tất cả các chiến dịch đang ở trạng thái `1` (Kích hoạt) nhưng có `NgayKetThuc` nhỏ hơn thời gian hiện tại, và chuyển trạng thái của chúng sang `2` (Đã kết thúc).

### 2. Ràng buộc khi thay đổi ngày kết thúc
- Trong `AdminCampaignController.java` (tại các hàm `saveCampaign` và `saveCampaignAjax`):
- Khi cập nhật một chiến dịch đã tồn tại, nếu người dùng có thay đổi `NgayKetThuc`, hệ thống sẽ kiểm tra:
  - Nếu `NgayKetThuc` mới nằm trong quá khứ (nhỏ hơn thời gian hiện tại), hệ thống sẽ báo lỗi: *"Không thể đổi ngày kết thúc về một thời điểm trong quá khứ."*
  - (Các trường hợp kéo dài chiến dịch hoặc chiến dịch chưa kích hoạt vẫn diễn ra bình thường).

## Verification Plan

### Automated Tests
- Không áp dụng, sẽ kiểm thử thủ công.

### Manual Verification
1. Sửa ngày kết thúc của một chiến dịch về một ngày trong quá khứ -> Kiểm tra xem hệ thống có báo lỗi đúng như mong đợi không.
2. Sửa ngày kết thúc dài ra -> Kiểm tra xem hệ thống có cho phép lưu bình thường không.
3. Đặt ngày kết thúc của một chiến dịch là 1-2 phút sau thời điểm hiện tại -> Đợi qua thời điểm đó và kiểm tra xem hệ thống có tự động đổi trạng thái sang "Đã kết thúc" hay không.
