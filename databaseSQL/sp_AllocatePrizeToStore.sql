USE luckydraw;
GO

IF OBJECT_ID('sp_AllocatePrizeToStore', 'P') IS NOT NULL
    DROP PROCEDURE sp_AllocatePrizeToStore;
GO

CREATE PROCEDURE sp_AllocatePrizeToStore
    @maStore VARCHAR(50),
    @maGiaiThuong VARCHAR(50),
    @quantity INT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;

        IF @quantity <= 0
        BEGIN
            THROW 50000, N'Số lượng cấp phải lớn hơn 0', 1;
        END

        -- 1. Check if Prize exists and lock it
        DECLARE @maChienDich VARCHAR(50);
        DECLARE @tonKhoToanHeThong INT;
        
        SELECT @maChienDich = ma_chien_dich, @tonKhoToanHeThong = ton_kho_toan_he_thong
        FROM prize WITH (UPDLOCK)
        WHERE ma_giai_thuong = @maGiaiThuong;

        IF @maChienDich IS NULL
        BEGIN
            THROW 50000, N'Không tìm thấy giải thưởng', 1;
        END

        -- 2. Check if Store belongs to the Campaign
        IF NOT EXISTS (SELECT 1 FROM campaign_store WHERE ma_store = @maStore AND ma_chien_dich = @maChienDich)
        BEGIN
            THROW 50000, N'Cửa hàng không thuộc chiến dịch của giải thưởng này. Hãy chọn cửa hàng hợp lệ.', 1;
        END

        -- 3. Check global inventory
        IF @tonKhoToanHeThong < @quantity
        BEGIN
            DECLARE @errMsg NVARCHAR(100) = N'Tồn kho tổng không đủ. Hiện chỉ còn ' + CAST(@tonKhoToanHeThong AS NVARCHAR(20));
            THROW 50000, @errMsg, 1;
        END

        -- 4. Deduct global inventory
        UPDATE prize 
        SET ton_kho_toan_he_thong = ton_kho_toan_he_thong - @quantity
        WHERE ma_giai_thuong = @maGiaiThuong;

        -- 5. Upsert Store inventory
        IF EXISTS (SELECT 1 FROM store_prize_inventory WHERE ma_store = @maStore AND ma_giai_thuong = @maGiaiThuong)
        BEGIN
            UPDATE store_prize_inventory
            SET tong_luong_cap = tong_luong_cap + @quantity,
                ton_kho = ton_kho + @quantity
            WHERE ma_store = @maStore AND ma_giai_thuong = @maGiaiThuong;
        END
        ELSE
        BEGIN
            INSERT INTO store_prize_inventory (ma_store, ma_giai_thuong, tong_luong_cap, ton_kho, da_phat)
            VALUES (@maStore, @maGiaiThuong, @quantity, @quantity, 0);
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
