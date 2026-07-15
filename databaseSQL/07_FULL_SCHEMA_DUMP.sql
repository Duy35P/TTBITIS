SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[reward_voucher](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_giai_thuong] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_khach_hang] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_voucher] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[trang_thai] [int] NOT NULL,
	[ma_store_phat_hanh] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_store_doi_thuong] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[thoi_gian_tao] [datetime2](7) NOT NULL,
	[thoi_gian_doi] [datetime2](7) NULL,
 CONSTRAINT [PK_REWARD_VOUCHER] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_REWARD_VOUCHER_MA] UNIQUE NONCLUSTERED 
(
	[ma_voucher] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[store](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ten_cua_hang] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[trang_thai] [int] NOT NULL,
	[dia_chi_store] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_store] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
 CONSTRAINT [PK_STORE] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_STORE_MA] UNIQUE NONCLUSTERED 
(
	[ma_store] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[campaign](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_chien_dich] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[ten_chien_dich] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ngay_bat_dau] [datetime2](7) NULL,
	[ngay_ket_thuc] [datetime2](7) NULL,
	[mo_ta] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[duong_dan_slug] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[hinh_anh_url] [varchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[cauhinh_theme_json] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[trang_thai] [int] NOT NULL,
	[doc_quyen] [bit] NOT NULL,
	[han_token_ngay] [int] NOT NULL,
 CONSTRAINT [PK_CAMPAIGN] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[duong_dan_slug] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_CAMPAIGN_MA] UNIQUE NONCLUSTERED 
(
	[ma_chien_dich] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[prize](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_giai_thuong] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[ma_chien_dich] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ten_giai] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[hinh_anh_url] [varchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[loai_giai] [int] NOT NULL,
	[la_giai_thuong] [bit] NOT NULL,
	[xac_suat] [float] NOT NULL,
	[ton_kho_toan_he_thong] [int] NOT NULL,
	[gioi_han_trung_moi_customer] [int] NULL,
	[is_pre_generated_code] [bit] NULL,
 CONSTRAINT [PK_PRIZE] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_PRIZE_MA] UNIQUE NONCLUSTERED 
(
	[ma_giai_thuong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[store_prize_inventory](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_store] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_giai_thuong] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ton_kho] [int] NOT NULL,
	[tong_luong_cap] [int] NOT NULL,
	[da_phat] [int] NOT NULL,
 CONSTRAINT [PK_STORE_PRIZE_INVENTORY] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_STORE_PRIZE] UNIQUE NONCLUSTERED 
(
	[ma_store] ASC,
	[ma_giai_thuong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF

CREATE VIEW vw_store_prize_inventory AS
SELECT 
    spi.ma_store AS maStore, 
    s.ten_cua_hang AS tenCuaHang, 
    p.ma_chien_dich AS maChienDich, 
    c.ten_chien_dich AS tenChienDich, 
    spi.ma_giai_thuong AS maGiaiThuong, 
    p.ten_giai AS tenGiai, 
    spi.tong_luong_cap AS tongLuongCap, 
    spi.da_phat AS daPhat, 
    spi.ton_kho AS tonKho,
    (SELECT COUNT(*) FROM reward_voucher rv WHERE rv.ma_store_doi_thuong = spi.ma_store AND rv.ma_giai_thuong = spi.ma_giai_thuong AND rv.trang_thai = 1) AS daDoiThucTe,
    (spi.tong_luong_cap - (SELECT COUNT(*) FROM reward_voucher rv WHERE rv.ma_store_doi_thuong = spi.ma_store AND rv.ma_giai_thuong = spi.ma_giai_thuong AND rv.trang_thai = 1)) AS tonKhoThucTe
FROM store_prize_inventory spi
LEFT JOIN store s ON spi.ma_store = s.ma_store
LEFT JOIN prize p ON spi.ma_giai_thuong = p.ma_giai_thuong
LEFT JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich;

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[campaign_store](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_chien_dich] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_store] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
 CONSTRAINT [PK_CAMPAIGN_STORE] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_CAMPAIGN_STORE] UNIQUE NONCLUSTERED 
(
	[ma_chien_dich] ASC,
	[ma_store] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF

-- 2. View for Store Campaigns
CREATE   VIEW vw_store_campaigns AS
SELECT 
    s.id AS storeId,
    STRING_AGG(CASE WHEN c.trang_thai = 1 THEN c.ten_chien_dich ELSE NULL END, ', ') AS activeCampaigns,
    STRING_AGG(CASE WHEN c.trang_thai = 0 THEN c.ten_chien_dich ELSE NULL END, ', ') AS pendingCampaigns
FROM store s
LEFT JOIN campaign_store cs ON s.ma_store = cs.ma_store
LEFT JOIN campaign c ON cs.ma_chien_dich = c.ma_chien_dich
GROUP BY s.id;

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[prize_code](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_giai_thuong] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[code] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[is_used] [bit] NULL,
	[created_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF
CREATE   VIEW vw_prize_list AS SELECT p.id, p.ma_giai_thuong AS maGiaiThuong, p.ten_giai AS tenGiai, p.ma_chien_dich AS maChienDich, c.ten_chien_dich AS tenChienDich, p.loai_giai AS loaiGiai, p.xac_suat AS xacSuat, p.ton_kho_toan_he_thong AS tonKhoToanHeThongGoc, CASE WHEN p.la_giai_thuong = 1 THEN (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong AND pc.is_used = 0) - ISNULL((SELECT SUM(ton_kho) FROM store_prize_inventory spi WHERE spi.ma_giai_thuong = p.ma_giai_thuong), 0) ELSE -1 END AS tonKhoToanHeThong, CASE WHEN p.la_giai_thuong = 1 THEN (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong AND pc.is_used = 0) ELSE -1 END AS tonKhoThucTe, CASE WHEN p.la_giai_thuong = 1 THEN (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong) ELSE -1 END AS tongMaNap, p.gioi_han_trung_moi_customer AS gioiHanTrungMoiCustomer, p.la_giai_thuong AS laGiaiThuong FROM prize p LEFT JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich;
SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[customer](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_khach_hang] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[phone] [varchar](15) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[zalo_id] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[ten_khach] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[trang_thai] [int] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_CUSTOMER] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_CUSTOMER_MA] UNIQUE NONCLUSTERED 
(
	[ma_khach_hang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_CUSTOMER_PHONE] UNIQUE NONCLUSTERED 
(
	[phone] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE VIEW vw_reward_voucher_list AS SELECT rv.id, rv.ma_voucher AS maVoucher, rv.ma_giai_thuong AS maGiaiThuong, p.ten_giai AS tenGiai, p.loai_giai AS loaiGiai, p.hinh_anh_url AS hinhAnhUrl, p.ma_chien_dich AS maChienDich, cam.ten_chien_dich AS tenChienDich, rv.ma_khach_hang AS maKhachHang, c.ten_khach AS tenKhach, c.phone AS phone, rv.ma_store_phat_hanh AS maStorePhatHanh, s1.ten_cua_hang AS tenStorePhatHanh, rv.thoi_gian_tao AS thoiGianTao, rv.ma_store_doi_thuong AS maStoreDoiThuong, s2.ten_cua_hang AS tenStoreDoiThuong, rv.thoi_gian_doi AS thoiGianDoi, rv.trang_thai AS trangThai FROM reward_voucher rv LEFT JOIN prize p ON rv.ma_giai_thuong = p.ma_giai_thuong LEFT JOIN campaign cam ON p.ma_chien_dich = cam.ma_chien_dich LEFT JOIN customer c ON rv.ma_khach_hang = c.ma_khach_hang LEFT JOIN store s1 ON rv.ma_store_phat_hanh = s1.ma_store LEFT JOIN store s2 ON rv.ma_store_doi_thuong = s2.ma_store;
SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[staff](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_nhan_vien] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[username] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[password] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ten_nhan_vien] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[role_id] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_store] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[trang_thai] [int] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_STAFF] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_STAFF_MA] UNIQUE NONCLUSTERED 
(
	[ma_nhan_vien] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_STAFF_USERNAME] UNIQUE NONCLUSTERED 
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF
CREATE VIEW vw_staff_list AS SELECT st.id, st.ma_nhan_vien AS maNhanVien, st.username, st.ten_nhan_vien AS tenNhanVien, st.role_id AS roleId, st.ma_store AS maStore, s.ten_cua_hang AS tenCuaHang, st.trang_thai AS trangThai FROM staff st LEFT JOIN store s ON st.ma_store = s.ma_store;
SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[campaign_rule](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_chien_dich] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[gia_tri_don_hang_toi_thieu] [float] NULL,
 CONSTRAINT [PK_CAMPAIGN_RULE] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[campaign_rule_payment](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_chien_dich] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[phuong_thuc_thanh_toan] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[so_luot_thuong] [int] NOT NULL,
 CONSTRAINT [PK_CAMPAIGN_RULE_PAYMENT] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[campaign_rule_sku](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_chien_dich] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_sku] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[so_luot_thuong] [int] NOT NULL,
 CONSTRAINT [PK_CAMPAIGN_RULE_SKU] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[chuc_nang](
	[ma_chuc_nang] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ten_chuc_nang] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[nhom] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
 CONSTRAINT [PK_CHUCNANG] PRIMARY KEY CLUSTERED 
(
	[ma_chuc_nang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[customer_turn](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_khach_hang] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_chien_dich] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[luot_con_lai] [int] NOT NULL,
 CONSTRAINT [PK_CUSTOMER_TURN] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_CUSTOMER_TURN_KH_CD] UNIQUE NONCLUSTERED 
(
	[ma_khach_hang] ASC,
	[ma_chien_dich] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[game_access_token](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[token] [varchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_hoa_don] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[so_luong_luot_thuong] [int] NOT NULL,
	[da_su_dung] [bit] NOT NULL,
	[ma_khach_hang_kich_hoat] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[het_han_luc] [datetime2](7) NOT NULL,
	[ngay_su_dung] [datetime2](7) NULL,
 CONSTRAINT [PK_GAME_ACCESS_TOKEN] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_GAME_ACCESS_TOKEN] UNIQUE NONCLUSTERED 
(
	[token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[invoice](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_hoa_don] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_hoa_don_goc] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[ma_store] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_khach_hang] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[tong_tien] [float] NOT NULL,
	[phuong_thuc_tt] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[san_pham_json] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[da_xu_ly] [bit] NOT NULL,
	[ngay_tao] [datetime] NULL,
 CONSTRAINT [PK_INVOICE] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_INVOICE_MA_HD] UNIQUE NONCLUSTERED 
(
	[ma_hoa_don] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[manager_store_assignment](
	[username] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_store] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[username] ASC,
	[ma_store] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[phanquyen](
	[role_id] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_chuc_nang] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
 CONSTRAINT [PK_PHANQUYEN] PRIMARY KEY CLUSTERED 
(
	[role_id] ASC,
	[ma_chuc_nang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[system_audit_log](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[staff_id] [bigint] NOT NULL,
	[action_type] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[target_table] [varchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[target_record_id] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[description] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[ip_address] [varchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[created_at] [datetime2](7) NOT NULL,
	[old_values] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[new_values] [nvarchar](max) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
 CONSTRAINT [PK_SYSTEM_AUDIT_LOG] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[turn_transaction](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ma_khach_hang] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[ma_chien_dich] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[loai] [int] NOT NULL,
	[so_luong] [int] NOT NULL,
	[nguon_tham_chieu] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[thoi_gian_tao] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_TURN_TRANSACTION] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE TABLE [dbo].[vai_tro](
	[role_id] [varchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[role_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[mo_ta] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[loai_phan_bo] [int] NOT NULL,
 CONSTRAINT [PK_VAITRO] PRIMARY KEY CLUSTERED 
(
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON


-- =========================================================================
-- 1. THỦ TỤC CỘNG LƯỢT AN TOÀN (Dùng MERGE để Upsert)
-- =========================================================================
CREATE   PROCEDURE [dbo].[sp_AddCustomerTurns_Safe]
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

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF
CREATE PROCEDURE [dbo].[sp_AllocatePrizeToStore]
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
            THROW 50000, N'Sá»‘ lÆ°á»£ng cáº¥p pháº£i lá»›n hÆ¡n 0 hoáº·c báº±ng -1 (khÃ´ng giá»›i háº¡n)', 1;
        END

        -- 1. Check if Prize exists and lock it
        DECLARE @maChienDich VARCHAR(50);
        DECLARE @tonKhoToanHeThong INT;
        SELECT @maChienDich = ma_chien_dich, @tonKhoToanHeThong = ton_kho_toan_he_thong
        FROM prize WITH (UPDLOCK)
        WHERE ma_giai_thuong = @maGiaiThuong;

        IF @maChienDich IS NULL
        BEGIN
            THROW 50000, N'KhÃ´ng tÃ¬m tháº¥y giáº£i thÆ°á»Ÿng', 1;
        END

        -- 2. Check if Store belongs to the Campaign
        IF NOT EXISTS (SELECT 1 FROM campaign_store WHERE ma_store = @maStore AND ma_chien_dich = @maChienDich)
        BEGIN
            THROW 50000, N'Cá»­a hÃ ng khÃ´ng thuá»™c chiáº¿n dá»‹ch cá»§a giáº£i thÆ°á»Ÿng nÃ y. HÃ£y chá»n cá»­a hÃ ng há»£p lá»‡.', 1;
        END

        -- 3. Check global inventory
        IF @tonKhoToanHeThong <> -1 AND @quantity <> -1 AND @tonKhoToanHeThong < @quantity
        BEGIN
            DECLARE @errMsg NVARCHAR(100) = N'Tá»“n kho tá»•ng khÃ´ng Ä‘á»§. Hiá»‡n chá»‰ cÃ²n ' + CAST(@tonKhoToanHeThong AS NVARCHAR(20));
            THROW 50000, @errMsg, 1;
        END
        
        IF @tonKhoToanHeThong <> -1 AND @quantity = -1
        BEGIN
            THROW 50000, N'KhÃ´ng thá»ƒ cáº¥p phÃ¡t khÃ´ng giá»›i háº¡n cho giáº£i thÆ°á»Ÿng cÃ³ giá»›i háº¡n tá»“n kho', 1;
        END

        IF @tonKhoToanHeThong = -1 AND @quantity <> -1
        BEGIN
            THROW 50000, N'Giáº£i thÆ°á»Ÿng khÃ´ng giá»›i háº¡n pháº£i luÃ´n Ä‘Æ°á»£c cáº¥p khÃ´ng giá»›i háº¡n (-1).', 1;
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

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON

-- =============================================
-- 3. STORED PROCEDURE: ĐỔI QUÀ TẠI QUẦY
-- =============================================
CREATE   PROCEDURE [dbo].[sp_DoiQuaVatLy]
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

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON
CREATE   PROCEDURE [dbo].[sp_GetAvailablePrizesForStore]
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
    LEFT JOIN [dbo].[store_prize_inventory] spi ON p.ma_giai_thuong = spi.ma_giai_thuong AND spi.ma_store = @ma_store
    WHERE p.ma_chien_dich = @ma_chien_dich
      AND (
          p.la_giai_thuong = 0 -- Bỏ qua check kho nếu là giải giả
          OR 
          (
              p.la_giai_thuong = 1 
              AND spi.ma_store IS NOT NULL
              AND spi.ton_kho > 0 
              AND p.ton_kho_toan_he_thong > 0 
              AND (
                  p.gioi_han_trung_moi_customer IS NULL 
                  OR @ma_khach_hang IS NULL
                  OR p.gioi_han_trung_moi_customer > (
                      SELECT COUNT(*) 
                      FROM [dbo].[reward_voucher] rv WITH (NOLOCK)
                      WHERE rv.ma_giai_thuong = p.ma_giai_thuong AND rv.ma_khach_hang = @ma_khach_hang
                  )
              )
          )
      )
END

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF
CREATE   PROCEDURE [dbo].[sp_Main_QuayThuong] @ma_khach_hang VARCHAR(50), @ma_store VARCHAR(50), @ma_chien_dich VARCHAR(255), @ma_giai_thuong_du_kien VARCHAR(255), @ma_giai_truot VARCHAR(255), @ket_qua_giai_thuong VARCHAR(255) OUTPUT AS BEGIN SET NOCOUNT ON; BEGIN TRY BEGIN TRANSACTION; EXEC [dbo].[sp_TruLuotQuay] @ma_khach_hang = @ma_khach_hang, @ma_chien_dich = @ma_chien_dich; EXEC [dbo].[sp_TraoQuaVaTruKho] @ma_khach_hang = @ma_khach_hang, @ma_store = @ma_store, @ma_giai_thuong_du_kien = @ma_giai_thuong_du_kien, @ma_giai_truot = @ma_giai_truot, @ket_qua_giai_thuong = @ket_qua_giai_thuong OUTPUT; COMMIT TRANSACTION; END TRY BEGIN CATCH IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION; THROW; END CATCH END
SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF
CREATE PROCEDURE [dbo].[sp_ReclaimUnredeemedVouchers]
    @maChienDich VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;

        -- Láº¥y danh sÃ¡ch cÃ¡c voucher chÆ°a Ä‘á»•i thuá»™c chiáº¿n dá»‹ch Ä‘Ã£ háº¿t háº¡n
        SELECT v.id, v.ma_giai_thuong, v.ma_store_phat_hanh
        INTO #ExpiredVouchers
        FROM reward_voucher v
        JOIN prize p ON v.ma_giai_thuong = p.ma_giai_thuong
        JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich
        WHERE v.trang_thai = 0 
          AND c.ngay_ket_thuc < GETDATE()
          AND (@maChienDich IS NULL OR c.ma_chien_dich = @maChienDich);

        IF NOT EXISTS (SELECT 1 FROM #ExpiredVouchers)
        BEGIN
            COMMIT TRANSACTION;
            RETURN;
        END

        -- 1. Cá»™ng láº¡i kho tá»•ng (chá»‰ cáº­p nháº­t nhá»¯ng giáº£i cÃ³ giá»›i háº¡n kho != -1)
        UPDATE p
        SET ton_kho_toan_he_thong = p.ton_kho_toan_he_thong + r.cnt
        FROM prize p
        JOIN (
            SELECT ma_giai_thuong, COUNT(*) as cnt 
            FROM #ExpiredVouchers 
            GROUP BY ma_giai_thuong
        ) r ON p.ma_giai_thuong = r.ma_giai_thuong
        WHERE p.ton_kho_toan_he_thong <> -1;

        -- 2. Cá»™ng láº¡i kho cá»­a hÃ ng vÃ  giáº£m da_phat
        UPDATE spi
        SET ton_kho = CASE WHEN spi.ton_kho = -1 THEN -1 ELSE spi.ton_kho + r.cnt END,
            da_phat = CASE WHEN spi.da_phat >= r.cnt THEN spi.da_phat - r.cnt ELSE 0 END
        FROM store_prize_inventory spi
        JOIN (
            SELECT ma_store_phat_hanh, ma_giai_thuong, COUNT(*) as cnt 
            FROM #ExpiredVouchers 
            WHERE ma_store_phat_hanh IS NOT NULL
            GROUP BY ma_store_phat_hanh, ma_giai_thuong
        ) r ON spi.ma_store = r.ma_store_phat_hanh AND spi.ma_giai_thuong = r.ma_giai_thuong;

        -- 3. Cáº­p nháº­t tráº¡ng thÃ¡i voucher thÃ nh -1 (Háº¿t háº¡n/ÄÃ£ thu há»“i)
        UPDATE v
        SET trang_thai = -1
        FROM reward_voucher v
        JOIN #ExpiredVouchers e ON v.id = e.id;

        DROP TABLE #ExpiredVouchers;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF
CREATE   PROCEDURE sp_RedeemStorePrize
    @MaStore VARCHAR(255),
    @MaGiaiThuong VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Kiá»ƒm tra dÃ²ng tá»“n táº¡i vÃ  láº¥y sá»‘ lÆ°á»£ng tá»“n kho
        DECLARE @CurrentStock INT;
        SELECT @CurrentStock = ton_kho 
        FROM store_prize_inventory WITH (UPDLOCK) 
        WHERE ma_store = @MaStore AND ma_giai_thuong = @MaGiaiThuong;

        IF @CurrentStock IS NULL
        BEGIN
            RAISERROR('KhÃ´ng tÃ¬m tháº¥y báº£n ghi tá»“n kho cho cá»­a hÃ ng vÃ  giáº£i thÆ°á»Ÿng nÃ y.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        IF @CurrentStock <= 0
        BEGIN
            RAISERROR('Tá»“n kho Ä‘Ã£ háº¿t. KhÃ´ng thá»ƒ Ä‘á»•i quÃ .', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Cáº­p nháº­t tá»“n kho vÃ  sá»‘ lÆ°á»£ng Ä‘Ã£ phÃ¡t
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

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF
CREATE   PROCEDURE [dbo].[sp_TraoQuaVaTruKho]
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

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER ON

-- =========================================================================
-- 2. THỦ TỤC CON: TRỪ LƯỢT QUAY
-- =========================================================================
CREATE   PROCEDURE [dbo].[sp_TruLuotQuay]
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

SET ANSI_NULLS ON
SET QUOTED_IDENTIFIER OFF

CREATE PROCEDURE [dbo].[sp_UpdateStorePrizeInventory]
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
            THROW 50000, N'KhÃ´ng tÃ¬m tháº¥y dá»¯ liá»‡u phÃ¢n bá»•.', 1;
        END

        IF @newTongLuongCap <> -1 AND @newTongLuongCap < @daPhat
        BEGIN
            DECLARE @errMsg1 NVARCHAR(100) = N'KhÃ´ng thá»ƒ giáº£m tá»•ng cáº¥p xuá»‘ng dÆ°á»›i sá»‘ lÆ°á»£ng Ä‘Ã£ phÃ¡t (' + CAST(@daPhat AS NVARCHAR(20)) + N').';
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
                THROW 50000, N'KhÃ´ng thá»ƒ chá»‰nh thÃ nh khÃ´ng giá»›i háº¡n vÃ¬ giáº£i thÆ°á»Ÿng nÃ y cÃ³ giá»›i háº¡n tá»“n kho tá»•ng.', 1;
            END
            
            IF @tonKhoToanHeThong = -1 AND @newTongLuongCap <> -1 AND @newTongLuongCap <> @daPhat
            BEGIN
                THROW 50000, N'Giáº£i thÆ°á»Ÿng khÃ´ng giá»›i háº¡n pháº£i luÃ´n Ä‘Æ°á»£c cáº¥p khÃ´ng giá»›i háº¡n (-1), hoáº·c thu há»“i vá» má»©c Ä‘Ã£ phÃ¡t.', 1;
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
                DECLARE @errMsg2 NVARCHAR(100) = N'Tá»“n kho tá»•ng khÃ´ng Ä‘á»§ Ä‘á»ƒ cáº¥p thÃªm. Hiá»‡n chá»‰ cÃ²n ' + CAST(@tonKhoToanHeThong AS NVARCHAR(20));
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

