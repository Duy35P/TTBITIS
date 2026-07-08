-- 1. Create Tables
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[prize_code]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[prize_code] (
        [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [ma_giai_thuong] VARCHAR(255) NOT NULL,
        [code] VARCHAR(255) NOT NULL UNIQUE,
        [is_used] BIT NOT NULL DEFAULT 0,
        [created_at] DATETIME DEFAULT GETDATE()
    );
END

-- 2. Alter Tables
IF COL_LENGTH('dbo.prize', 'is_pre_generated_code') IS NOT NULL
BEGIN
    ALTER TABLE prize DROP COLUMN is_pre_generated_code;
END
IF COL_LENGTH('dbo.campaign', 'hinh_anh_url') IS NOT NULL
BEGIN
    ALTER TABLE campaign ALTER COLUMN hinh_anh_url VARCHAR(MAX);
END

-- 3. Views
GO
CREATE OR ALTER VIEW vw_prize_list AS
SELECT 
    p.id,
    p.ma_giai_thuong AS maGiaiThuong,
    p.ten_giai AS tenGiai,
    p.ma_chien_dich AS maChienDich,
    c.ten_chien_dich AS tenChienDich,
    p.loai_giai AS loaiGiai,
    p.xac_suat AS xacSuat,
    p.ton_kho_toan_he_thong AS tonKhoToanHeThongGoc,
    CASE WHEN p.la_giai_thuong = 1 THEN 
        (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong AND pc.is_used = 0) 
        - ISNULL((SELECT SUM(ton_kho) FROM store_prize_inventory spi WHERE spi.ma_giai_thuong = p.ma_giai_thuong), 0)
    ELSE -1 END AS tonKhoToanHeThong,
    CASE WHEN p.la_giai_thuong = 1 THEN 
        (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong AND pc.is_used = 0)
    ELSE -1 END AS tonKhoThucTe,
    CASE WHEN p.la_giai_thuong = 1 THEN 
        (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong)
    ELSE -1 END AS tongMaNap,
    p.gioi_han_trung_moi_customer AS gioiHanTrungMoiCustomer,
    p.la_giai_thuong AS laGiaiThuong
FROM prize p
LEFT JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich;
GO

CREATE OR ALTER VIEW [dbo].[vw_reward_voucher_list] AS
SELECT 
    r.id,
    r.ma_voucher AS maVoucher,
    r.trang_thai AS trangThai,
    r.ngay_tao AS ngayTao,
    r.ngay_su_dung AS ngaySuDung,
    c.ho_ten AS hoTen,
    c.so_dien_thoai AS soDienThoai,
    p.ten_giai AS tenGiai,
    camp.ten_chien_dich AS tenChienDich
FROM reward_voucher r
LEFT JOIN customer c ON r.ma_khach_hang = c.so_dien_thoai
LEFT JOIN prize p ON r.ma_giai_thuong = p.ma_giai_thuong
LEFT JOIN campaign camp ON p.ma_chien_dich = camp.ma_chien_dich;
GO

-- 4. Stored Procedures
GO
CREATE OR ALTER PROCEDURE [dbo].[sp_TraoQuaVaTruKho]
    @ma_khach_hang VARCHAR(50),
    @ma_store VARCHAR(50),
    @ma_giai_thuong_du_kien VARCHAR(255),
    @ma_giai_truot VARCHAR(255),
    @ket_qua_giai_thuong VARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @la_giai_thuong BIT = 0;
    DECLARE @ton_kho_hien_tai INT = 0;
    DECLARE @gioi_han_trung_moi_user INT;
    DECLARE @so_lan_da_trung INT = 0;

    SET @ket_qua_giai_thuong = @ma_giai_thuong_du_kien;

    SELECT 
        @la_giai_thuong = la_giai_thuong,
        @gioi_han_trung_moi_user = gioi_han_trung_moi_customer
    FROM [dbo].[prize] 
    WHERE ma_giai_thuong = @ma_giai_thuong_du_kien;

    IF @la_giai_thuong = 1
    BEGIN
        SELECT @so_lan_da_trung = COUNT(*)
        FROM [dbo].[reward_voucher] WITH (NOLOCK)
        WHERE ma_khach_hang = @ma_khach_hang 
          AND ma_giai_thuong = @ma_giai_thuong_du_kien;

        IF @gioi_han_trung_moi_user IS NULL OR @so_lan_da_trung < @gioi_han_trung_moi_user
        BEGIN
            DECLARE @fetched_code VARCHAR(255) = NULL;
            SELECT TOP 1 @fetched_code = code 
            FROM [dbo].[prize_code] WITH (UPDLOCK, ROWLOCK)
            WHERE ma_giai_thuong = @ma_giai_thuong_du_kien AND is_used = 0;

            IF @fetched_code IS NULL
            BEGIN
                SET @ket_qua_giai_thuong = @ma_giai_truot;
                RETURN;
            END

            SELECT @ton_kho_hien_tai = ton_kho_toan_he_thong
            FROM [dbo].[prize] WITH (UPDLOCK, ROWLOCK)
            WHERE ma_giai_thuong = @ma_giai_thuong_du_kien;

            IF @ton_kho_hien_tai > 0 OR @ton_kho_hien_tai = -1
            BEGIN
                DECLARE @ton_kho_chi_nhanh INT = 0;

                SELECT @ton_kho_chi_nhanh = ton_kho
                FROM [dbo].[store_prize_inventory] WITH (UPDLOCK, ROWLOCK)
                WHERE ma_store = @ma_store AND ma_giai_thuong = @ma_giai_thuong_du_kien;

                IF @ton_kho_chi_nhanh > 0 OR @ton_kho_hien_tai = -1
                BEGIN
                    IF @ton_kho_hien_tai > 0
                    BEGIN
                        UPDATE [dbo].[prize] 
                        SET ton_kho_toan_he_thong = ton_kho_toan_he_thong - 1
                        WHERE ma_giai_thuong = @ma_giai_thuong_du_kien;
                    END
                    
                    IF @ton_kho_chi_nhanh > 0
                    BEGIN
                        UPDATE [dbo].[store_prize_inventory]
                        SET ton_kho = ton_kho - 1,
                            da_phat = da_phat + 1
                        WHERE ma_store = @ma_store AND ma_giai_thuong = @ma_giai_thuong_du_kien;
                    END
                    ELSE IF @ton_kho_hien_tai = -1
                    BEGIN
                        UPDATE [dbo].[store_prize_inventory]
                        SET da_phat = da_phat + 1
                        WHERE ma_store = @ma_store AND ma_giai_thuong = @ma_giai_thuong_du_kien;
                    END

                    UPDATE [dbo].[prize_code] 
                    SET is_used = 1 
                    WHERE code = @fetched_code;

                    INSERT INTO [dbo].[reward_voucher] (ma_giai_thuong, ma_khach_hang, ma_voucher, trang_thai, ma_store_phat_hanh)
                    VALUES (@ma_giai_thuong_du_kien, @ma_khach_hang, @fetched_code, 0, @ma_store);
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
