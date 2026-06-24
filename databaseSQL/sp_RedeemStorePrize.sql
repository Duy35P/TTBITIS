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
