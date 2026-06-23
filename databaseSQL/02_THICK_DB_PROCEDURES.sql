-- =========================================================================
-- KIẾN TRÚC THICK DATABASE (Gom logic vào Stored Procedure)
-- =========================================================================

USE [luckydraw];
GO

-- =========================================================================
-- 1. THỦ TỤC CỘNG LƯỢT AN TOÀN (Dùng MERGE để Upsert)
-- =========================================================================
CREATE OR ALTER PROCEDURE [dbo].[sp_AddCustomerTurns_Safe]
    @id_khach_hang BIGINT,
    @id_chien_dich BIGINT,
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
        USING (SELECT @id_khach_hang AS uid, @id_chien_dich AS cid) AS s
        ON (t.id_khach_hang = s.uid AND t.id_chien_dich = s.cid)
        WHEN MATCHED THEN
            UPDATE SET t.luot_con_lai = t.luot_con_lai + @so_luong_cong
        WHEN NOT MATCHED THEN
            INSERT (id_khach_hang, id_chien_dich, luot_con_lai)
            VALUES (s.uid, s.cid, @so_luong_cong);

        -- 2. Ghi nhận lịch sử giao dịch
        INSERT INTO [dbo].[turn_transaction] (id_khach_hang, id_chien_dich, loai, so_luong, nguon_tham_chieu)
        VALUES (@id_khach_hang, @id_chien_dich, 1, @so_luong_cong, @nguon_tham_chieu);

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
    @id_khach_hang BIGINT,
    @id_chien_dich BIGINT
AS
BEGIN
    DECLARE @luot_hien_tai INT;
    SELECT @luot_hien_tai = luot_con_lai 
    FROM [dbo].[customer_turn] WITH (UPDLOCK, ROWLOCK)
    WHERE id_khach_hang = @id_khach_hang AND id_chien_dich = @id_chien_dich;

    IF @luot_hien_tai IS NULL OR @luot_hien_tai <= 0
        THROW 50001, N'Khách hàng không còn đủ lượt quay!', 1;

    UPDATE [dbo].[customer_turn] SET luot_con_lai = luot_con_lai - 1
    WHERE id_khach_hang = @id_khach_hang AND id_chien_dich = @id_chien_dich;

    INSERT INTO [dbo].[turn_transaction] (id_khach_hang, id_chien_dich, loai, so_luong, nguon_tham_chieu)
    VALUES (@id_khach_hang, @id_chien_dich, 0, 1, 'DRAW_SYSTEM');
END
GO

-- =========================================================================
-- 3. THỦ TỤC CON: TRAO QUÀ VÀ TRỪ KHO
-- =========================================================================
CREATE OR ALTER PROCEDURE [dbo].[sp_TraoQuaVaTruKho]
    @id_khach_hang BIGINT,
    @id_cua_hang BIGINT,
    @id_giai_thuong_du_kien BIGINT,
    @id_giai_truot BIGINT,
    @ma_voucher_random VARCHAR(255),
    @ket_qua_giai_thuong BIGINT OUTPUT
AS
BEGIN
    DECLARE @ton_kho_hien_tai INT = 0;
    DECLARE @la_giai_thuong BIT = 0;
    DECLARE @gioi_han_trung_moi_user INT = NULL;
    
    SET @ket_qua_giai_thuong = @id_giai_thuong_du_kien;

    SELECT 
        @la_giai_thuong = la_giai_thuong,
        @gioi_han_trung_moi_user = gioi_han_trung_moi_customer
    FROM [dbo].[prize] 
    WHERE prize_id = @id_giai_thuong_du_kien;

    IF @la_giai_thuong = 1
    BEGIN
        DECLARE @so_lan_da_trung INT = 0;
        IF @gioi_han_trung_moi_user IS NOT NULL
        BEGIN
            SELECT @so_lan_da_trung = COUNT(*)
            FROM [dbo].[reward_voucher] WITH (UPDLOCK, HOLDLOCK)
            WHERE id_giai_thuong = @id_giai_thuong_du_kien AND id_khach_hang = @id_khach_hang;
        END

        IF @gioi_han_trung_moi_user IS NULL OR @so_lan_da_trung < @gioi_han_trung_moi_user
        BEGIN
            SELECT @ton_kho_hien_tai = ton_kho_toan_he_thong
            FROM [dbo].[prize] WITH (UPDLOCK, ROWLOCK)
            WHERE prize_id = @id_giai_thuong_du_kien;

            IF @ton_kho_hien_tai > 0
            BEGIN
                DECLARE @ton_kho_chi_nhanh INT = 0;
                SELECT @ton_kho_chi_nhanh = ton_kho
                FROM [dbo].[store_prize_inventory] WITH (UPDLOCK, ROWLOCK)
                WHERE id_cua_hang = @id_cua_hang AND id_giai_thuong = @id_giai_thuong_du_kien;

                IF @ton_kho_chi_nhanh > 0
                BEGIN
                    UPDATE [dbo].[prize] 
                    SET ton_kho_toan_he_thong = ton_kho_toan_he_thong - 1
                    WHERE prize_id = @id_giai_thuong_du_kien;
                    
                    UPDATE [dbo].[store_prize_inventory]
                    SET ton_kho = ton_kho - 1
                    WHERE id_cua_hang = @id_cua_hang AND id_giai_thuong = @id_giai_thuong_du_kien;

                    INSERT INTO [dbo].[reward_voucher] (id_giai_thuong, id_khach_hang, ma_voucher, trang_thai, id_cua_hang_phat_hanh)
                    VALUES (@id_giai_thuong_du_kien, @id_khach_hang, @ma_voucher_random, 0, @id_cua_hang);
                END
                ELSE
                BEGIN
                    SET @ket_qua_giai_thuong = @id_giai_truot;
                END
            END
            ELSE
            BEGIN
                SET @ket_qua_giai_thuong = @id_giai_truot;
            END
        END
        ELSE
        BEGIN
            SET @ket_qua_giai_thuong = @id_giai_truot;
        END
    END
END
GO

-- =========================================================================
-- 4. THỦ TỤC MẸ: CHẠY VÒNG QUAY (GỌI 2 THỦ TỤC CON TRONG 1 TRANSACTION)
-- =========================================================================
CREATE OR ALTER PROCEDURE [dbo].[sp_Main_QuayThuong]
    @id_khach_hang BIGINT,
    @id_chien_dich BIGINT,
    @id_cua_hang BIGINT,
    @id_giai_thuong_du_kien BIGINT,
    @id_giai_truot BIGINT,
    @ma_voucher_random VARCHAR(255),
    @ket_qua_giai_thuong BIGINT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Bước 1: Gọi hàm trừ lượt
        EXEC [dbo].[sp_TruLuotQuay] 
            @id_khach_hang = @id_khach_hang, 
            @id_chien_dich = @id_chien_dich;

        -- Bước 2: Gọi hàm trao quà
        EXEC [dbo].[sp_TraoQuaVaTruKho] 
            @id_khach_hang = @id_khach_hang, 
            @id_cua_hang = @id_cua_hang, 
            @id_giai_thuong_du_kien = @id_giai_thuong_du_kien, 
            @id_giai_truot = @id_giai_truot, 
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
    @id_cua_hang BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @id_giai_thuong BIGINT = NULL;
        DECLARE @trang_thai_voucher INT = NULL;
        DECLARE @id_cua_hang_phat_hanh BIGINT = NULL;

        SELECT 
            @id_giai_thuong = id_giai_thuong,
            @trang_thai_voucher = trang_thai,
            @id_cua_hang_phat_hanh = id_cua_hang_phat_hanh
        FROM [dbo].[reward_voucher] WITH (UPDLOCK, ROWLOCK)
        WHERE ma_voucher = @ma_voucher;

        IF @id_giai_thuong IS NULL THROW 50002, N'Mã Voucher không tồn tại!', 1;
        IF @trang_thai_voucher = 1 THROW 50003, N'Mã Voucher này đã được sử dụng!', 1;
        IF @trang_thai_voucher = 2 THROW 50004, N'Mã Voucher này đã hết hạn!', 1;
        
        --CHỈ ĐƯỢC ĐỔI QUÀ TẠI CỬA HÀNG ĐÃ MUA HÀNG
        IF @id_cua_hang_phat_hanh != @id_cua_hang
            THROW 50006, N'Mã quà tặng này chỉ được đổi tại cửa hàng mà bạn đã mua hàng!', 1;
        UPDATE [dbo].[reward_voucher]
        SET trang_thai = 1, id_cua_hang_doi_thuong = @id_cua_hang, thoi_gian_doi = SYSDATETIME()
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
    @id_cua_hang BIGINT,
    @id_chien_dich BIGINT,
    @id_khach_hang BIGINT = NULL -- Bổ sung tham số để loại trừ các giải khách đã trúng Max
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        p.prize_id, 
        p.ten_giai, 
        p.loai_giai, 
        p.xac_suat
    FROM [dbo].[prize] p
    JOIN [dbo].[store_prize_inventory] spi ON p.prize_id = spi.id_giai_thuong
    WHERE spi.id_cua_hang = @id_cua_hang
      AND p.id_chien_dich = @id_chien_dich
      AND spi.ton_kho > 0             -- Điều kiện 1: Cửa hàng này phải còn hàng
      AND p.ton_kho_toan_he_thong > 0 -- Điều kiện 2: Tổng kho của giải này trên hệ thống cũng phải còn
      AND (
          p.gioi_han_trung_moi_customer IS NULL 
          OR @id_khach_hang IS NULL
          OR p.gioi_han_trung_moi_customer > (
              SELECT COUNT(*) 
              FROM [dbo].[reward_voucher] rv WITH (NOLOCK)
              WHERE rv.id_giai_thuong = p.prize_id AND rv.id_khach_hang = @id_khach_hang
          )
      )
END
GO
