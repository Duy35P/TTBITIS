USE [luckydraw];
GO

-- =============================================
-- Trigger: Kiểm tra dữ liệu hợp lệ khi Thêm/Sửa Chiến dịch
-- =============================================
CREATE OR ALTER TRIGGER TRG_CAMPAIGN_VALIDATE
ON [dbo].[campaign]
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- 1. Kiểm tra: Ngày kết thúc không được diễn ra trước Ngày bắt đầu
    IF EXISTS (
        SELECT 1 FROM inserted
        WHERE ngay_ket_thuc IS NOT NULL AND ngay_bat_dau IS NOT NULL
          AND ngay_ket_thuc < ngay_bat_dau
    )
    BEGIN
        RAISERROR (N'Lỗi: Ngày kết thúc chiến dịch không thể diễn ra trước ngày bắt đầu.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- 2. Kiểm tra: Ngày bắt đầu không được ở trong quá khứ
    -- Chỉ kiểm tra khi là dòng mới (INSERT) hoặc khi cột ngay_bat_dau bị thay đổi (UPDATE)
    IF EXISTS (
        SELECT 1 FROM inserted i
        LEFT JOIN deleted d ON i.campaign_id = d.campaign_id
        WHERE (d.campaign_id IS NULL OR i.ngay_bat_dau <> d.ngay_bat_dau)
          AND i.ngay_bat_dau IS NOT NULL
          AND CAST(i.ngay_bat_dau AS DATE) < CAST(SYSDATETIME() AS DATE)
    )
    BEGIN
        RAISERROR (N'Lỗi: Ngày bắt đầu chiến dịch không được cài đặt ở trong quá khứ.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
    
    -- 3. Kiểm tra: Tổng lượt quay dự kiến không được là số âm
    IF EXISTS (
        SELECT 1 FROM inserted
        WHERE tong_luot_du_kien IS NOT NULL AND tong_luot_du_kien < 0
    )
    BEGIN
        RAISERROR (N'Lỗi: Tổng số lượt quay dự kiến không được nhỏ hơn 0.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

END;
GO
