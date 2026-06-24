USE luckydraw;
GO

IF OBJECT_ID('sp_UpdateStorePrizeInventory', 'P') IS NOT NULL
    DROP PROCEDURE sp_UpdateStorePrizeInventory;
GO

CREATE PROCEDURE sp_UpdateStorePrizeInventory
    @maStore VARCHAR(50),
    @maGiaiThuong VARCHAR(50),
    @newTongLuongCap INT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Get current store inventory
        DECLARE @oldTongLuongCap INT;
        DECLARE @daPhat INT;
        DECLARE @oldTonKho INT;
        
        SELECT @oldTongLuongCap = tong_luong_cap, @daPhat = da_phat, @oldTonKho = ton_kho
        FROM store_prize_inventory WITH (UPDLOCK)
        WHERE ma_store = @maStore AND ma_giai_thuong = @maGiaiThuong;

        IF @oldTongLuongCap IS NULL
        BEGIN
            THROW 50000, N'Không tìm thấy dữ liệu phân bổ.', 1;
        END

        IF @newTongLuongCap < @daPhat
        BEGIN
            DECLARE @errMsg1 NVARCHAR(100) = N'Không thể giảm tổng cấp xuống dưới số lượng đã phát (' + CAST(@daPhat AS NVARCHAR(20)) + N').';
            THROW 50000, @errMsg1, 1;
        END

        DECLARE @delta INT = @newTongLuongCap - @oldTongLuongCap;

        IF @delta <> 0
        BEGIN
            -- 2. Lock Prize
            DECLARE @tonKhoToanHeThong INT;
            SELECT @tonKhoToanHeThong = ton_kho_toan_he_thong
            FROM prize WITH (UPDLOCK)
            WHERE ma_giai_thuong = @maGiaiThuong;

            IF @delta > 0 AND @tonKhoToanHeThong < @delta
            BEGIN
                DECLARE @errMsg2 NVARCHAR(100) = N'Tồn kho tổng không đủ để cấp thêm. Hiện chỉ còn ' + CAST(@tonKhoToanHeThong AS NVARCHAR(20));
                THROW 50000, @errMsg2, 1;
            END

            -- 3. Update Prize
            UPDATE prize 
            SET ton_kho_toan_he_thong = ton_kho_toan_he_thong - @delta
            WHERE ma_giai_thuong = @maGiaiThuong;

            -- 4. Update or Delete Store Inventory
            IF @newTongLuongCap = 0 AND @daPhat = 0
            BEGIN
                DELETE FROM store_prize_inventory
                WHERE ma_store = @maStore AND ma_giai_thuong = @maGiaiThuong;
            END
            ELSE
            BEGIN
                UPDATE store_prize_inventory
                SET tong_luong_cap = @newTongLuongCap,
                    ton_kho = @newTongLuongCap - @daPhat
                WHERE ma_store = @maStore AND ma_giai_thuong = @maGiaiThuong;
            END
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO
