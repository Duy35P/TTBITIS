CREATE PROCEDURE [dbo].[sp_ReclaimUnredeemedVouchers]
    @maChienDich VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Lấy danh sách các voucher chưa đổi thuộc chiến dịch đã hết hạn
        SELECT v.id, v.ma_giai_thuong, v.ma_store_phat_hanh
        INTO #ExpiredVouchers
        FROM reward_voucher v
        JOIN prize p ON v.ma_giai_thuong = p.ma_giai_thuong
        JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich
        WHERE v.trang_thai = 0 
          AND c.ngay_ket_thuc < GETDATE()
          AND (@maChienDich IS NULL OR c.ma_chien_dich = @maChienDich);

        IF NOT EXISTS (SELECT 1 FROM #ExpiredVouchers)
        BEGIN
            COMMIT TRANSACTION;
            RETURN;
        END

        -- 1. Cộng lại kho tổng (chỉ cập nhật những giải có giới hạn kho != -1)
        UPDATE p
        SET ton_kho_toan_he_thong = p.ton_kho_toan_he_thong + r.cnt
        FROM prize p
        JOIN (
            SELECT ma_giai_thuong, COUNT(*) as cnt 
            FROM #ExpiredVouchers 
            GROUP BY ma_giai_thuong
        ) r ON p.ma_giai_thuong = r.ma_giai_thuong
        WHERE p.ton_kho_toan_he_thong <> -1;

        -- 2. Cộng lại kho cửa hàng và giảm da_phat
        UPDATE spi
        SET ton_kho = CASE WHEN spi.ton_kho = -1 THEN -1 ELSE spi.ton_kho + r.cnt END,
            da_phat = CASE WHEN spi.da_phat >= r.cnt THEN spi.da_phat - r.cnt ELSE 0 END
        FROM store_prize_inventory spi
        JOIN (
            SELECT ma_store_phat_hanh, ma_giai_thuong, COUNT(*) as cnt 
            FROM #ExpiredVouchers 
            WHERE ma_store_phat_hanh IS NOT NULL
            GROUP BY ma_store_phat_hanh, ma_giai_thuong
        ) r ON spi.ma_store = r.ma_store_phat_hanh AND spi.ma_giai_thuong = r.ma_giai_thuong;

        -- 3. Cập nhật trạng thái voucher thành -1 (Hết hạn/Đã thu hồi)
        UPDATE v
        SET trang_thai = -1
        FROM reward_voucher v
        JOIN #ExpiredVouchers e ON v.id = e.id;

        DROP TABLE #ExpiredVouchers;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO
