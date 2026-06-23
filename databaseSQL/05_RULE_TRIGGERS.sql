-- Trigger cho bảng campaign_rule
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
