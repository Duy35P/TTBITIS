ALTER PROCEDURE [dbo].[sp_TraoQuaVaTruKho]
    @ma_khach_hang VARCHAR(50),
    @ma_store VARCHAR(50),
    @ma_giai_thuong_du_kien VARCHAR(255),
    @ma_giai_truot VARCHAR(255),
    @ma_voucher_random VARCHAR(100),
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
