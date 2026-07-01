USE [luckydraw]
GO
ALTER PROCEDURE [dbo].[sp_AllocatePrizeToStore]
    @maStore VARCHAR(50),
    @maGiaiThuong VARCHAR(50),
    @quantity INT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;

        IF @quantity <= 0 AND @quantity <> -1
        BEGIN
            THROW 50000, N'Số lượng cấp phải lớn hơn 0 hoặc bằng -1 (không giới hạn)', 1;
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
        IF @tonKhoToanHeThong <> -1 AND @quantity <> -1 AND @tonKhoToanHeThong < @quantity
        BEGIN
            DECLARE @errMsg NVARCHAR(100) = N'Tồn kho tổng không đủ. Hiện chỉ còn ' + CAST(@tonKhoToanHeThong AS NVARCHAR(20));
            THROW 50000, @errMsg, 1;
        END
        
        IF @tonKhoToanHeThong <> -1 AND @quantity = -1
        BEGIN
            THROW 50000, N'Không thể cấp phát không giới hạn cho giải thưởng có giới hạn tồn kho', 1;
        END

        IF @tonKhoToanHeThong = -1 AND @quantity <> -1
        BEGIN
            THROW 50000, N'Giải thưởng không giới hạn phải luôn được cấp không giới hạn (-1).', 1;
        END

        -- 4. Deduct global inventory
        IF @tonKhoToanHeThong <> -1
        BEGIN
            UPDATE prize 
            SET ton_kho_toan_he_thong = ton_kho_toan_he_thong - @quantity
            WHERE ma_giai_thuong = @maGiaiThuong;
        END

        -- 5. Upsert Store inventory
        IF EXISTS (SELECT 1 FROM store_prize_inventory WHERE ma_store = @maStore AND ma_giai_thuong = @maGiaiThuong)
        BEGIN
            IF @quantity = -1
            BEGIN
                UPDATE store_prize_inventory
                SET tong_luong_cap = -1,
                    ton_kho = -1
                WHERE ma_store = @maStore AND ma_giai_thuong = @maGiaiThuong;
            END
            ELSE
            BEGIN
                UPDATE store_prize_inventory
                SET tong_luong_cap = CASE WHEN tong_luong_cap = -1 THEN -1 ELSE tong_luong_cap + @quantity END,
                    ton_kho = CASE WHEN ton_kho = -1 THEN -1 ELSE ton_kho + @quantity END
                WHERE ma_store = @maStore AND ma_giai_thuong = @maGiaiThuong;
            END
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

ALTER PROCEDURE [dbo].[sp_UpdateStorePrizeInventory]
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

        IF @newTongLuongCap <> -1 AND @newTongLuongCap < @daPhat
        BEGIN
            DECLARE @errMsg1 NVARCHAR(100) = N'Không thể giảm tổng cấp xuống dưới số lượng đã phát (' + CAST(@daPhat AS NVARCHAR(20)) + N').';
            THROW 50000, @errMsg1, 1;
        END

        -- 2. Lock Prize
        DECLARE @tonKhoToanHeThong INT;
        SELECT @tonKhoToanHeThong = ton_kho_toan_he_thong
        FROM prize WITH (UPDLOCK)
        WHERE ma_giai_thuong = @maGiaiThuong;
            
        IF @newTongLuongCap <> @oldTongLuongCap
        BEGIN
            -- Validation for -1 transitions
            IF @tonKhoToanHeThong <> -1 AND @newTongLuongCap = -1
            BEGIN
                THROW 50000, N'Không thể chỉnh thành không giới hạn vì giải thưởng này có giới hạn tồn kho tổng.', 1;
            END
            
            IF @tonKhoToanHeThong = -1 AND @newTongLuongCap <> -1 AND @newTongLuongCap <> @daPhat
            BEGIN
                THROW 50000, N'Giải thưởng không giới hạn phải luôn được cấp không giới hạn (-1), hoặc thu hồi về mức đã phát.', 1;
            END

            DECLARE @delta INT = 0;
            IF @newTongLuongCap <> -1 AND @oldTongLuongCap <> -1
            BEGIN
                SET @delta = @newTongLuongCap - @oldTongLuongCap;
            END
            ELSE IF @newTongLuongCap <> -1 AND @oldTongLuongCap = -1
            BEGIN
                SET @delta = 0; -- From unlimited to limited (revoking). Delta for global inventory is 0 because global is unlimited
            END

            IF @delta > 0 AND @tonKhoToanHeThong < @delta AND @tonKhoToanHeThong <> -1
            BEGIN
                DECLARE @errMsg2 NVARCHAR(100) = N'Tồn kho tổng không đủ để cấp thêm. Hiện chỉ còn ' + CAST(@tonKhoToanHeThong AS NVARCHAR(20));
                THROW 50000, @errMsg2, 1;
            END

            -- 3. Update Prize
            IF @delta <> 0 AND @tonKhoToanHeThong <> -1
            BEGIN
                UPDATE prize 
                SET ton_kho_toan_he_thong = ton_kho_toan_he_thong - @delta
                WHERE ma_giai_thuong = @maGiaiThuong;
            END

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
                    ton_kho = CASE WHEN @newTongLuongCap = -1 THEN -1 ELSE @newTongLuongCap - @daPhat END
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
