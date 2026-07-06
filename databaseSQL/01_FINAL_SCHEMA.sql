-- =============================================
-- DATABASE SCHEMA FINAL 
-- Hệ thống O2O Lucky Draw
-- =============================================

USE [master]
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'luckydraw')
    DROP DATABASE [luckydraw]
GO

CREATE DATABASE [luckydraw]
GO

USE [luckydraw]
GO

-- 1. CUSTOMER (Khách hàng)
CREATE TABLE [dbo].[customer] (
    [id]            BIGINT          NOT NULL IDENTITY(1,1),
    [ma_khach_hang] VARCHAR(255)    NULL,
    [phone]         VARCHAR(15)     NOT NULL, -- Bắt buộc có SĐT
    [zalo_id]       VARCHAR(255)    NULL,     -- Dự phòng đăng nhập Zalo
    [ten_khach]     NVARCHAR(255)   NULL,
    [trang_thai]    INT             NOT NULL DEFAULT 1,
    [created_at]    DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT [PK_CUSTOMER] PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_CUSTOMER_PHONE] UNIQUE ([phone]),
    CONSTRAINT [UQ_CUSTOMER_MA] UNIQUE ([ma_khach_hang])
)
GO

--. Bảng vai trò (Lưu các vai trò: ADMIN, STORE_STAFF, MANAGER...)
CREATE TABLE [dbo].[vai_tro] (
    [role_id]   VARCHAR(20)   NOT NULL, 
    [role_name] NVARCHAR(100) NOT NULL, -- Tên hiển thị (VD: 'Quản trị viên', 'Nhân viên bán hàng')
    [mo_ta]     NVARCHAR(500) NULL,
    CONSTRAINT [PK_VAITRO] PRIMARY KEY CLUSTERED ([role_id] ASC)
)
GO


--. Bảng nhân viên 
CREATE TABLE [dbo].[staff] (
    [id]            BIGINT          NOT NULL IDENTITY(1,1),
    [ma_nhan_vien]  VARCHAR(255)    NULL,
    [username]      NVARCHAR(255)   NOT NULL,
    [password]      NVARCHAR(255)   NOT NULL,
    [ten_nhan_vien] NVARCHAR(255)   NOT NULL,
    [role_id]       VARCHAR(20)     NOT NULL, -- Trỏ về bảng vai_tro
    [ma_store] VARCHAR(255)          NULL,
    [trang_thai]    INT             NOT NULL DEFAULT 1,
    [created_at]    DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT [PK_STAFF] PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_STAFF_USERNAME] UNIQUE ([username]),
    CONSTRAINT [UQ_STAFF_MA] UNIQUE ([ma_nhan_vien])
)
GO

--. Bảng chức năng
CREATE TABLE [dbo].[chuc_nang] (
    [ma_chuc_nang]  VARCHAR(50)   NOT NULL,
    [ten_chuc_nang] NVARCHAR(255) NOT NULL,
    [nhom]          NVARCHAR(100) NULL,
    CONSTRAINT [PK_CHUCNANG] PRIMARY KEY CLUSTERED ([ma_chuc_nang] ASC)
)
GO

--. Bảng phân quyền (Chỉ cần 2 khóa ngoại tạo thành khóa chính)
CREATE TABLE [dbo].[phanquyen] (
    [role_id]      VARCHAR(20) NOT NULL,
    [ma_chuc_nang] VARCHAR(50) NOT NULL,
    CONSTRAINT [PK_PHANQUYEN] PRIMARY KEY CLUSTERED ([role_id] ASC, [ma_chuc_nang] ASC)
)
GO

-- 3. STORE
CREATE TABLE [dbo].[store] (
    [id]            BIGINT          NOT NULL IDENTITY(1,1),
    [ten_cua_hang]  NVARCHAR(255)   NOT NULL,
    [trang_thai]    INT             NOT NULL DEFAULT 1,
    [dia_chi_store] NVARCHAR(255)   NOT NULL,
    [ma_store]      VARCHAR(255)    NOT NULL,
    CONSTRAINT [PK_STORE]               PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_STORE_MA]            UNIQUE ([ma_store]),
    CONSTRAINT [CK_STORE_TRANG_THAI]    CHECK ([trang_thai] IN (0, 1))
)
GO

-- 4. CAMPAIGN
CREATE TABLE [dbo].[campaign] (
    [id]            BIGINT          NOT NULL IDENTITY(1,1),
    [ma_chien_dich] VARCHAR(255)    NULL,
    [ten_chien_dich]NVARCHAR(255)   NOT NULL,
    [ngay_bat_dau]  DATETIME2       NULL,
    [ngay_ket_thuc] DATETIME2       NULL,
    [mo_ta]         NVARCHAR(MAX)   NULL,
    [duong_dan_slug] VARCHAR(255)   NULL UNIQUE, -- Ví dụ: vui-he-2026
    [hinh_anh_url]  VARCHAR(500)    NULL, -- Hình ảnh Banner của chiến dịch
    [cauhinh_theme_json] NVARCHAR(MAX) NULL, -- Lưu cấu hình màu sắc Minigame Builder
    [trang_thai]    INT             NOT NULL DEFAULT 2,
    CONSTRAINT [PK_CAMPAIGN]            PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_CAMPAIGN_MA]         UNIQUE ([ma_chien_dich]),
    CONSTRAINT [CK_CAMPAIGN_TRANG_THAI] CHECK ([trang_thai] IN (0, 1, 2))
)
GO

-- 5. CAMPAIGN_STORE
CREATE TABLE [dbo].[campaign_store] (
    [id]                BIGINT  NOT NULL IDENTITY(1,1),
    [ma_chien_dich] VARCHAR(255)  NOT NULL,
    [ma_store] VARCHAR(255)  NOT NULL,
    CONSTRAINT [PK_CAMPAIGN_STORE]  PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_CAMPAIGN_STORE]  UNIQUE ([ma_chien_dich], [ma_store])
)
GO

-- 6. CAMPAIGN_RULE (Luật Cơ Bản)
CREATE TABLE [dbo].[campaign_rule] (
    [id]                            BIGINT          NOT NULL IDENTITY(1,1),
    [ma_chien_dich] VARCHAR(255)          NOT NULL,
    [gia_tri_don_hang_toi_thieu]    FLOAT           NULL,
    CONSTRAINT [PK_CAMPAIGN_RULE]   PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- 6.1 CAMPAIGN_RULE_PAYMENT (Thưởng theo phương thức thanh toán)
CREATE TABLE [dbo].[campaign_rule_payment] (
    [id]                        BIGINT          NOT NULL IDENTITY(1,1),
    [ma_chien_dich] VARCHAR(255)          NOT NULL,
    [phuong_thuc_thanh_toan]    VARCHAR(100)    NOT NULL,
    [so_luot_thuong]            INT             NOT NULL DEFAULT 1,
    CONSTRAINT [PK_CAMPAIGN_RULE_PAYMENT] PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- 6.2 CAMPAIGN_RULE_SKU (Thưởng theo SKU sản phẩm)
CREATE TABLE [dbo].[campaign_rule_sku] (
    [id]                        BIGINT          NOT NULL IDENTITY(1,1),
    [ma_chien_dich] VARCHAR(255)          NOT NULL,
    [ma_sku]                    VARCHAR(100)    NOT NULL,
    [so_luot_thuong]            INT             NOT NULL DEFAULT 1,
    CONSTRAINT [PK_CAMPAIGN_RULE_SKU] PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- 7. PRIZE
CREATE TABLE [dbo].[prize] (
    [id]                        BIGINT          NOT NULL IDENTITY(1,1),
    [ma_giai_thuong]            VARCHAR(255)    NULL,
    [ma_chien_dich] VARCHAR(255)          NOT NULL,
    [ten_giai]                  NVARCHAR(255)   NOT NULL,
    [hinh_anh_url]              VARCHAR(500)    NULL, -- Hình ảnh hộp quà hoặc phần thưởng
    [loai_giai]                 INT             NOT NULL,
    [la_giai_thuong]            BIT             NOT NULL DEFAULT 1,
    [xac_suat]                  FLOAT           NOT NULL,
    [ton_kho_toan_he_thong]     INT             NOT NULL DEFAULT 0,
    [gioi_han_trung_moi_customer]   INT         NULL,
    CONSTRAINT [PK_PRIZE]       PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_PRIZE_MA]    UNIQUE ([ma_giai_thuong]),
    CONSTRAINT [CK_PRIZE_LOAI]  CHECK ([loai_giai] IN (0, 1)),
    --CHỐNG ÂM KHO:
    CONSTRAINT [CK_PRIZE_INVENTORY] CHECK ([ton_kho_toan_he_thong] >= -1) 
)
GO

-- 8. STORE_PRIZE_INVENTORY
CREATE TABLE [dbo].[store_prize_inventory] (
    [id]                BIGINT  NOT NULL IDENTITY(1,1),
    [ma_store] VARCHAR(255)  NOT NULL,
    [ma_giai_thuong] VARCHAR(255)  NOT NULL,
    [ton_kho]           INT     NOT NULL DEFAULT 0,
    CONSTRAINT [PK_STORE_PRIZE_INVENTORY]   PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_STORE_PRIZE]             UNIQUE ([ma_store], [ma_giai_thuong]),
    CONSTRAINT [CK_STORE_INVENTORY]         CHECK ([ton_kho] >= 0)
)
GO

-- 9. STORE_POS_KEY
CREATE TABLE [dbo].[store_pos_key] (
    [id]                BIGINT          NOT NULL IDENTITY(1,1),
    [ma_store] VARCHAR(255)          NOT NULL,
    [api_key_hash]      VARCHAR(255)    NOT NULL,
    [trang_thai]        INT             NOT NULL DEFAULT 1,
    [thoi_gian_tao]     DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    [thoi_gian_het_han] DATETIME2       NULL,
    CONSTRAINT [PK_STORE_POS_KEY]   PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [CK_POS_KEY_TT]      CHECK ([trang_thai] IN (0, 1))
)
GO

-- 10. INVOICE
CREATE TABLE [dbo].[invoice] (
    [id]                BIGINT          NOT NULL IDENTITY(1,1),
    [ma_hoa_don]        VARCHAR(255)    NOT NULL,
    [ma_hoa_don_goc]    VARCHAR(255)    NULL, 
    [ma_store] VARCHAR(255)          NOT NULL,
    [ma_khach_hang] VARCHAR(255)          NULL, -- NULL để hỗ trợ khách chưa quét mã
    [tong_tien]         FLOAT           NOT NULL DEFAULT 0,
    [phuong_thuc_tt]    VARCHAR(50)     NULL,
    [san_pham_json]     NVARCHAR(MAX)   NULL,
	ngay_tao DATETIME DEFAULT GETDATE(),
    [da_xu_ly]          BIT             NOT NULL DEFAULT 0,
    CONSTRAINT [PK_INVOICE]         PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_INVOICE_MA_HD]   UNIQUE ([ma_hoa_don])
)
GO

-- 11. GAME_ACCESS_TOKEN
CREATE TABLE [dbo].[game_access_token] (
    [id]                        BIGINT          NOT NULL IDENTITY(1,1),
    [token]                     VARCHAR(500)    NOT NULL,
    [ma_hoa_don] VARCHAR(255)          NOT NULL,
    [so_luong_luot_thuong]      INT             NOT NULL DEFAULT 0,
    [da_su_dung]                BIT             NOT NULL DEFAULT 0,
    [ma_khach_hang_kich_hoat] VARCHAR(255)          NULL, -- Lưu ai là người đã quét mã
    [het_han_luc]               DATETIME2       NOT NULL,
    CONSTRAINT [PK_GAME_ACCESS_TOKEN]   PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_GAME_ACCESS_TOKEN]   UNIQUE ([token])
)
GO

-- 12. CUSTOMER_TURN 
CREATE TABLE [dbo].[customer_turn] (
    [id]            BIGINT  NOT NULL IDENTITY(1,1),
    [ma_khach_hang] VARCHAR(255)  NOT NULL,
    [ma_chien_dich] VARCHAR(255)  NOT NULL,
    [luot_con_lai]  INT     NOT NULL DEFAULT 0,
    CONSTRAINT [PK_CUSTOMER_TURN]       PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_CUSTOMER_TURN_KH_CD] UNIQUE ([ma_khach_hang], [ma_chien_dich]),
    CONSTRAINT [CK_CUSTOMER_TURN_POSITIVE] CHECK ([luot_con_lai] >= 0)
)
GO

-- 13. TURN_TRANSACTION
CREATE TABLE [dbo].[turn_transaction] (
    [id]                BIGINT          NOT NULL IDENTITY(1,1),
    [ma_khach_hang] VARCHAR(255)          NOT NULL,
    [ma_chien_dich] VARCHAR(255)          NOT NULL,
    [loai]              INT             NOT NULL,  --Loại giao dịch cộng lượt (loai=1) hoặc trừ lượt (loai=0)
    [so_luong]          INT             NOT NULL,
    [nguon_tham_chieu]  VARCHAR(255)    NULL,
    CONSTRAINT [PK_TURN_TRANSACTION]    PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [CK_TURN_LOAI]           CHECK ([loai] IN (0, 1))
)
GO

-- 14. REWARD_VOUCHER
CREATE TABLE [dbo].[reward_voucher] (
    [id]                        BIGINT          NOT NULL IDENTITY(1,1),
    [ma_giai_thuong] VARCHAR(255)          NOT NULL,
    [ma_khach_hang] VARCHAR(255)          NOT NULL,
    [ma_voucher]                VARCHAR(255)    NOT NULL,
    [trang_thai]                INT             NOT NULL DEFAULT 0,
    [ma_store_phat_hanh] VARCHAR(255)          NOT NULL, -- Nơi trúng giải (Nơi mua hàng)
    [ma_store_doi_thuong] VARCHAR(255)          NULL,     -- Nơi đổi quà
    [thoi_gian_tao]             DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    [thoi_gian_doi]             DATETIME2       NULL,
    CONSTRAINT [PK_REWARD_VOUCHER]      PRIMARY KEY CLUSTERED ([id] ASC),
    CONSTRAINT [UQ_REWARD_VOUCHER_MA]   UNIQUE ([ma_voucher]),
    CONSTRAINT [CK_REWARD_VOUCHER_TT]   CHECK ([trang_thai] IN (0, 1, 2))
)
GO

-- 15. SYSTEM_CONFIG (Cấu hình hệ thống, Zalo ZNS, API Keys)
CREATE TABLE [dbo].[system_config] (
    [config_key]    VARCHAR(100)    NOT NULL,
    [config_value]  NVARCHAR(MAX)   NULL,
    [mo_ta]         NVARCHAR(255)   NULL,
    CONSTRAINT [PK_SYSTEM_CONFIG] PRIMARY KEY CLUSTERED ([config_key] ASC)
)
GO

-- 16.Audit Logs
CREATE TABLE [dbo].[system_audit_log] (
    [id]                BIGINT          NOT NULL IDENTITY(1,1),
    [staff_id]          BIGINT          NOT NULL,       -- Người thực hiện thao tác
    [action_type]       VARCHAR(50)     NOT NULL,       -- Loại thao tác: CREATE, UPDATE, DELETE
    [target_table]      VARCHAR(100)    NOT NULL,       -- Tên bảng bị tác động (vd: 'prize')
    [target_record_id]  VARCHAR(255)    NOT NULL,       -- ID của dòng dữ liệu bị tác động
    [description]       NVARCHAR(500)   NULL,           -- Mô tả tóm tắt để người quản lý dễ đọc
    [ip_address]        VARCHAR(50)     NULL,           -- IP thiết bị của Staff/Admin
    [created_at]        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT [PK_SYSTEM_AUDIT_LOG] PRIMARY KEY CLUSTERED ([id] ASC)
)
GO

-- =============================================
-- VIEW THỐNG KÊ (Cho Dashboard Admin)
-- =============================================
CREATE OR ALTER VIEW [dbo].[vw_StoreInventoryStatus] AS
SELECT
    s.[ten_cua_hang]                         AS StoreName,
    c.[ten_chien_dich]                         AS CampaignName,
    p.[ten_giai]                         AS PrizeName,
    spi.[ton_kho]                   AS LocalInventory,
    p.[ton_kho_toan_he_thong]       AS GlobalInventory
FROM       [dbo].[store_prize_inventory] spi
JOIN [dbo].[store]      s ON s.[ma_store] = spi.[ma_store]
JOIN [dbo].[prize]      p ON p.[ma_giai_thuong] = spi.[ma_giai_thuong]
JOIN [dbo].[campaign]   c ON c.[ma_chien_dich] = p.[ma_chien_dich]
WHERE c.[trang_thai] = 1
GO


select * from vai_tro
select * from staff
select * from store
select * from prize
select * from invoice
select * from customer
select * from campaign
select * from campaign_store
select * from campaign_rule
select * from customer_turn
select * from store_prize_inventory
select * from vai_tro

  truncate table customer
  truncate table customer_turn
  truncate table game_access_token
  truncate table invoice
  truncate table turn_transaction
  truncate table reward_voucher
