-- =========================================================================
-- KIẾN TRÚC THICK DATABASE (Gom logic vào Stored Procedure)
-- =========================================================================

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
        LEFT JOIN deleted d ON i.id = d.id
        WHERE (d.id IS NULL OR i.ngay_bat_dau <> d.ngay_bat_dau)
          AND i.ngay_bat_dau IS NOT NULL
          AND CAST(i.ngay_bat_dau AS DATE) < CAST(SYSDATETIME() AS DATE)
    )
    BEGIN
        RAISERROR (N'Lỗi: Ngày bắt đầu chiến dịch không được cài đặt ở trong quá khứ.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

END;
GO

CREATE OR ALTER TRIGGER TRG_CAMPAIGN_RULE_VALIDATE
ON [dbo].[campaign_rule]
AFTER INSERT, UPDATE
AS
BEGIN
   IF EXISTS (SELECT 1 FROM inserted WHERE gia_tri_don_hang_toi_thieu < 0)
   BEGIN
       RAISERROR(N'Lỗi: Giá trị đơn hàng tối thiểu không được nhỏ hơn 0!', 16, 1);
       ROLLBACK TRANSACTION;
   END
END
GO
-- Trigger cho bảng campaign_rule_payment
CREATE OR ALTER TRIGGER TRG_CAMPAIGN_RULE_PAYMENT_VALIDATE
ON [dbo].[campaign_rule_payment]
AFTER INSERT, UPDATE
AS
BEGIN
   IF EXISTS (SELECT 1 FROM inserted WHERE so_luot_thuong < 0)
   BEGIN
       RAISERROR(N'Lỗi: Số lượt thưởng theo phương thức thanh toán không được nhỏ hơn 0!', 16, 1);
       ROLLBACK TRANSACTION;
   END
END
GO
-- Trigger cho bảng campaign_rule_sku
CREATE OR ALTER TRIGGER TRG_CAMPAIGN_RULE_SKU_VALIDATE
ON [dbo].[campaign_rule_sku]
AFTER INSERT, UPDATE
AS
BEGIN
   IF EXISTS (SELECT 1 FROM inserted WHERE so_luot_thuong < 0)
   BEGIN
       RAISERROR(N'Lỗi: Số lượt thưởng theo sản phẩm SKU không được nhỏ hơn 0!', 16, 1);
       ROLLBACK TRANSACTION;
   END
END
GO


-- =========================================================================
-- 1. THỦ TỤC CỘNG LƯỢT AN TOÀN (Dùng MERGE để Upsert)
-- =========================================================================
CREATE OR ALTER PROCEDURE [dbo].[sp_AddCustomerTurns_Safe]
    @ma_khach_hang VARCHAR(255),
    @ma_chien_dich VARCHAR(255),
    @so_luong_cong INT,
    @nguon_tham_chieu VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        
        IF @so_luong_cong <= 0 
            THROW 50000, N'Số lượng lượt quay được cộng phải lớn hơn 0', 1;

        -- 1. Upsert bảng customer_turn
        MERGE [dbo].[customer_turn] WITH (UPDLOCK, HOLDLOCK) AS t
        USING (SELECT @ma_khach_hang AS uid, @ma_chien_dich AS cid) AS s
        ON (t.ma_khach_hang = s.uid AND t.ma_chien_dich = s.cid)
        WHEN MATCHED THEN
            UPDATE SET t.luot_con_lai = t.luot_con_lai + @so_luong_cong
        WHEN NOT MATCHED THEN
            INSERT (ma_khach_hang, ma_chien_dich, luot_con_lai)
            VALUES (s.uid, s.cid, @so_luong_cong);

        -- 2. Ghi nhận lịch sử giao dịch
        INSERT INTO [dbo].[turn_transaction] (ma_khach_hang, ma_chien_dich, loai, so_luong, nguon_tham_chieu)
        VALUES (@ma_khach_hang, @ma_chien_dich, 1, @so_luong_cong, @nguon_tham_chieu);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

-- =========================================================================
-- 2. THỦ TỤC CON: TRỪ LƯỢT QUAY
-- =========================================================================
CREATE OR ALTER PROCEDURE [dbo].[sp_TruLuotQuay]
    @ma_khach_hang VARCHAR(255),
    @ma_chien_dich VARCHAR(255)
AS
BEGIN
    DECLARE @luot_hien_tai INT;
    SELECT @luot_hien_tai = luot_con_lai 
    FROM [dbo].[customer_turn] WITH (UPDLOCK, ROWLOCK)
    WHERE ma_khach_hang = @ma_khach_hang AND ma_chien_dich = @ma_chien_dich;

    IF @luot_hien_tai IS NULL OR @luot_hien_tai <= 0
        THROW 50001, N'Khách hàng không còn đủ lượt quay!', 1;

    UPDATE [dbo].[customer_turn] SET luot_con_lai = luot_con_lai - 1
    WHERE ma_khach_hang = @ma_khach_hang AND ma_chien_dich = @ma_chien_dich;

    INSERT INTO [dbo].[turn_transaction] (ma_khach_hang, ma_chien_dich, loai, so_luong, nguon_tham_chieu)
    VALUES (@ma_khach_hang, @ma_chien_dich, 0, 1, 'DRAW_SYSTEM');
END
GO

-- =========================================================================
-- 3. THỦ TỤC CON: TRAO QUÀ VÀ TRỪ KHO
-- =========================================================================
CREATE OR ALTER PROCEDURE [dbo].[sp_TraoQuaVaTruKho]
    @ma_khach_hang VARCHAR(255),
    @ma_store VARCHAR(255),
    @ma_giai_thuong_du_kien VARCHAR(255),
    @ma_giai_truot VARCHAR(255),
    @ma_voucher_random VARCHAR(255),
    @ket_qua_giai_thuong VARCHAR(255) OUTPUT
AS
BEGIN
    DECLARE @ton_kho_hien_tai INT = 0;
    DECLARE @la_giai_thuong BIT = 0;
    DECLARE @gioi_han_trung_moi_user INT = NULL;
    
    SET @ket_qua_giai_thuong = @ma_giai_thuong_du_kien;

    SELECT 
        @la_giai_thuong = la_giai_thuong,
        @gioi_han_trung_moi_user = gioi_han_trung_moi_customer
    FROM [dbo].[prize] 
    WHERE ma_giai_thuong = @ma_giai_thuong_du_kien;

    IF @la_giai_thuong = 1
    BEGIN
        DECLARE @so_lan_da_trung INT = 0;
        IF @gioi_han_trung_moi_user IS NOT NULL
        BEGIN
            SELECT @so_lan_da_trung = COUNT(*)
            FROM [dbo].[reward_voucher] WITH (UPDLOCK, HOLDLOCK)
            WHERE ma_giai_thuong = @ma_giai_thuong_du_kien AND ma_khach_hang = @ma_khach_hang;
        END

        IF @gioi_han_trung_moi_user IS NULL OR @so_lan_da_trung < @gioi_han_trung_moi_user
        BEGIN
            SELECT @ton_kho_hien_tai = ton_kho_toan_he_thong
            FROM [dbo].[prize] WITH (UPDLOCK, ROWLOCK)
            WHERE ma_giai_thuong = @ma_giai_thuong_du_kien;

            IF @ton_kho_hien_tai > 0
            BEGIN
                DECLARE @ton_kho_chi_nhanh INT = 0;
                SELECT @ton_kho_chi_nhanh = ton_kho
                FROM [dbo].[store_prize_inventory] WITH (UPDLOCK, ROWLOCK)
                WHERE ma_store = @ma_store AND ma_giai_thuong = @ma_giai_thuong_du_kien;

                IF @ton_kho_chi_nhanh > 0
                BEGIN
                    UPDATE [dbo].[prize] 
                    SET ton_kho_toan_he_thong = ton_kho_toan_he_thong - 1
                    WHERE ma_giai_thuong = @ma_giai_thuong_du_kien;
                    
                    UPDATE [dbo].[store_prize_inventory]
                    SET ton_kho = ton_kho - 1
                    WHERE ma_store = @ma_store AND ma_giai_thuong = @ma_giai_thuong_du_kien;

                    INSERT INTO [dbo].[reward_voucher] (ma_giai_thuong, ma_khach_hang, ma_voucher, trang_thai, ma_store_phat_hanh)
                    VALUES (@ma_giai_thuong_du_kien, @ma_khach_hang, @ma_voucher_random, 0, @ma_store);
                END
                ELSE
                BEGIN
                    SET @ket_qua_giai_thuong = @ma_giai_truot;
                END
            END
            ELSE
            BEGIN
                SET @ket_qua_giai_thuong = @ma_giai_truot;
            END
        END
        ELSE
        BEGIN
            SET @ket_qua_giai_thuong = @ma_giai_truot;
        END
    END
END
GO

-- =========================================================================
-- 4. THỦ TỤC MẸ: CHẠY VÒNG QUAY (GỌI 2 THỦ TỤC CON TRONG 1 TRANSACTION)
-- =========================================================================
CREATE OR ALTER PROCEDURE [dbo].[sp_Main_QuayThuong]
    @ma_khach_hang VARCHAR(255),
    @ma_chien_dich VARCHAR(255),
    @ma_store VARCHAR(255),
    @ma_giai_thuong_du_kien VARCHAR(255),
    @ma_giai_truot VARCHAR(255),
    @ma_voucher_random VARCHAR(255),
    @ket_qua_giai_thuong VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Bước 1: Gọi hàm trừ lượt
        EXEC [dbo].[sp_TruLuotQuay] 
            @ma_khach_hang = @ma_khach_hang, 
            @ma_chien_dich = @ma_chien_dich;

        -- Bước 2: Gọi hàm trao quà
        EXEC [dbo].[sp_TraoQuaVaTruKho] 
            @ma_khach_hang = @ma_khach_hang, 
            @ma_store = @ma_store, 
            @ma_giai_thuong_du_kien = @ma_giai_thuong_du_kien, 
            @ma_giai_truot = @ma_giai_truot, 
            @ma_voucher_random = @ma_voucher_random, 
            @ket_qua_giai_thuong = @ket_qua_giai_thuong OUTPUT;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

-- =============================================
-- 3. STORED PROCEDURE: ĐỔI QUÀ TẠI QUẦY
-- =============================================
CREATE OR ALTER PROCEDURE [dbo].[sp_DoiQuaVatLy]
    @ma_voucher VARCHAR(255),
    @ma_store VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @ma_giai_thuong VARCHAR(255) = NULL;
        DECLARE @trang_thai_voucher INT = NULL;
        DECLARE @ma_store_phat_hanh VARCHAR(255) = NULL;

        SELECT 
            @ma_giai_thuong = ma_giai_thuong,
            @trang_thai_voucher = trang_thai,
            @ma_store_phat_hanh = ma_store_phat_hanh
        FROM [dbo].[reward_voucher] WITH (UPDLOCK, ROWLOCK)
        WHERE ma_voucher = @ma_voucher;

        IF @ma_giai_thuong IS NULL THROW 50002, N'Mã Voucher không tồn tại!', 1;
        IF @trang_thai_voucher = 1 THROW 50003, N'Mã Voucher này đã được sử dụng!', 1;
        IF @trang_thai_voucher = 2 THROW 50004, N'Mã Voucher này đã hết hạn!', 1;
        
        --CHỈ ĐƯỢC ĐỔI QUÀ TẠI CỬA HÀNG ĐÃ MUA HÀNG
        IF @ma_store_phat_hanh != @ma_store
            THROW 50006, N'Mã quà tặng này chỉ được đổi tại cửa hàng mà bạn đã mua hàng!', 1;
        UPDATE [dbo].[reward_voucher]
        SET trang_thai = 1, ma_store_doi_thuong = @ma_store, thoi_gian_doi = SYSDATETIME()
        WHERE ma_voucher = @ma_voucher;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

-- =========================================================================
-- 4. THỦ TỤC LẤY DANH SÁCH GIẢI THƯỞNG HỢP LỆ CHO 1 CỬA HÀNG (Lọc tồn kho + Giới hạn User)
-- =========================================================================
CREATE OR ALTER PROCEDURE [dbo].[sp_GetAvailablePrizesForStore]
    @ma_store VARCHAR(255),
    @ma_chien_dich VARCHAR(255),
    @ma_khach_hang VARCHAR(255) = NULL -- Bổ sung tham số để loại trừ các giải khách đã trúng Max
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        p.ma_giai_thuong AS id, 
        p.ten_giai, 
        p.loai_giai, 
        p.xac_suat
    FROM [dbo].[prize] p
    JOIN [dbo].[store_prize_inventory] spi ON p.ma_giai_thuong = spi.ma_giai_thuong
    WHERE spi.ma_store = @ma_store
      AND p.ma_chien_dich = @ma_chien_dich
      AND spi.ton_kho > 0             -- Điều kiện 1: Cửa hàng này phải còn hàng
      AND p.ton_kho_toan_he_thong > 0 -- Điều kiện 2: Tổng kho của giải này trên hệ thống cũng phải còn
      AND (
          p.gioi_han_trung_moi_customer IS NULL 
          OR @ma_khach_hang IS NULL
          OR p.gioi_han_trung_moi_customer > (
              SELECT COUNT(*) 
              FROM [dbo].[reward_voucher] rv WITH (NOLOCK)
              WHERE rv.ma_giai_thuong = p.ma_giai_thuong AND rv.ma_khach_hang = @ma_khach_hang
          )
      )
END
GO

CREATE OR ALTER PROCEDURE sp_RedeemStorePrize
    @MaStore VARCHAR(255),
    @MaGiaiThuong VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Kiểm tra dòng tồn tại và lấy số lượng tồn kho
        DECLARE @CurrentStock INT;
        SELECT @CurrentStock = ton_kho 
        FROM store_prize_inventory WITH (UPDLOCK) 
        WHERE ma_store = @MaStore AND ma_giai_thuong = @MaGiaiThuong;

        IF @CurrentStock IS NULL
        BEGIN
            RAISERROR('Không tìm thấy bản ghi tồn kho cho cửa hàng và giải thưởng này.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        IF @CurrentStock <= 0
        BEGIN
            RAISERROR('Tồn kho đã hết. Không thể đổi quà.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Cập nhật tồn kho và số lượng đã phát
        UPDATE store_prize_inventory
        SET ton_kho = ton_kho - 1,
            da_phat = da_phat + 1
        WHERE ma_store = @MaStore AND ma_giai_thuong = @MaGiaiThuong;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
            
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();
        
        RAISERROR (@ErrorMessage, @ErrorSeverity, @ErrorState);
    END CATCH
END;
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