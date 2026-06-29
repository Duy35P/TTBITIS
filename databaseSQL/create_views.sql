-- 1. View for Store Prize Inventory
CREATE OR ALTER VIEW vw_store_prize_inventory AS
SELECT 
    spi.ma_store AS maStore, 
    s.ten_cua_hang AS tenCuaHang, 
    p.ma_chien_dich AS maChienDich, 
    c.ten_chien_dich AS tenChienDich, 
    spi.ma_giai_thuong AS maGiaiThuong, 
    p.ten_giai AS tenGiai, 
    spi.tong_luong_cap AS tongLuongCap, 
    spi.da_phat AS daPhat, 
    spi.ton_kho AS tonKho
FROM store_prize_inventory spi
LEFT JOIN store s ON spi.ma_store = s.ma_store
LEFT JOIN prize p ON spi.ma_giai_thuong = p.ma_giai_thuong
LEFT JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich;
GO

-- 2. View for Store Campaigns
CREATE OR ALTER VIEW vw_store_campaigns AS
SELECT 
    s.id AS storeId,
    STRING_AGG(CASE WHEN c.trang_thai = 1 THEN c.ten_chien_dich ELSE NULL END, ', ') AS activeCampaigns,
    STRING_AGG(CASE WHEN c.trang_thai = 0 THEN c.ten_chien_dich ELSE NULL END, ', ') AS pendingCampaigns
FROM store s
LEFT JOIN campaign_store cs ON s.ma_store = cs.ma_store
LEFT JOIN campaign c ON cs.ma_chien_dich = c.ma_chien_dich
GROUP BY s.id;
GO

-- 3. View for Prize List
CREATE OR ALTER VIEW vw_prize_list AS
SELECT 
    p.id,
    p.ma_giai_thuong AS maGiaiThuong,
    p.ten_giai AS tenGiai,
    p.ma_chien_dich AS maChienDich,
    c.ten_chien_dich AS tenChienDich,
    p.loai_giai AS loaiGiai,
    p.xac_suat AS xacSuat,
    p.ton_kho_toan_he_thong AS tonKhoToanHeThong,
    p.gioi_han_trung_moi_customer AS gioiHanTrungMoiCustomer,
    p.la_giai_thuong AS laGiaiThuong
FROM prize p
LEFT JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich;
GO

select * from vw_prize_list