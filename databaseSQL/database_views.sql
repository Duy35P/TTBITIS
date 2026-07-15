-- =========================================================================
-- TỔNG HỢP CÁC VIEW DÙNG TRONG HỆ THỐNG LUCKY DRAW (TỐI ƯU HÓA LỌC DỮ LIỆU)
-- =========================================================================

-- 1. View tồn kho giải thưởng tại các cửa hàng
-- (View này hỗ trợ dashboard xem tiến độ phát quà, tổng lượng cấp, tồn kho, v.v...)
IF OBJECT_ID('vw_store_prize_inventory', 'V') IS NOT NULL DROP VIEW vw_store_prize_inventory;
GO
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
GO

-- 2. View chiến dịch của từng cửa hàng
-- (Hỗ trợ xác định cửa hàng nào đang chạy chiến dịch nào)
IF OBJECT_ID('vw_store_campaigns', 'V') IS NOT NULL DROP VIEW vw_store_campaigns;
GO
CREATE VIEW vw_store_campaigns AS
SELECT 
    s.id AS storeId,
    STRING_AGG(CASE WHEN c.trang_thai = 1 THEN c.ten_chien_dich ELSE NULL END, ', ') AS activeCampaigns,
    STRING_AGG(CASE WHEN c.trang_thai = 0 THEN c.ten_chien_dich ELSE NULL END, ', ') AS pendingCampaigns
FROM store s
LEFT JOIN campaign_store cs ON s.ma_store = cs.ma_store
LEFT JOIN campaign c ON cs.ma_chien_dich = c.ma_chien_dich
GROUP BY s.id;
GO

-- 3. View danh sách Kho giải thưởng
-- (Hỗ trợ tính toán xác suất tự động, kiểm tra kho thực tế vs hệ thống)
IF OBJECT_ID('vw_prize_list', 'V') IS NOT NULL DROP VIEW vw_prize_list;
GO
CREATE VIEW vw_prize_list AS 
SELECT 
    p.id, 
    p.ma_giai_thuong AS maGiaiThuong, 
    p.ten_giai AS tenGiai, 
    p.ma_chien_dich AS maChienDich, 
    c.ten_chien_dich AS tenChienDich, 
    p.loai_giai AS loaiGiai, 
    p.xac_suat AS xacSuat, 
    p.ton_kho_toan_he_thong AS tonKhoToanHeThongGoc, 
    CASE WHEN p.la_giai_thuong = 1 THEN (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong AND pc.is_used = 0) - ISNULL((SELECT SUM(ton_kho) FROM store_prize_inventory spi WHERE spi.ma_giai_thuong = p.ma_giai_thuong), 0) ELSE -1 END AS tonKhoToanHeThong, 
    CASE WHEN p.la_giai_thuong = 1 THEN (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong AND pc.is_used = 0) ELSE -1 END AS tonKhoThucTe, 
    CASE WHEN p.la_giai_thuong = 1 THEN (SELECT COUNT(*) FROM prize_code pc WHERE pc.ma_giai_thuong = p.ma_giai_thuong) ELSE -1 END AS tongMaNap, 
    p.gioi_han_trung_moi_customer AS gioiHanTrungMoiCustomer, 
    p.la_giai_thuong AS laGiaiThuong 
FROM prize p 
LEFT JOIN campaign c ON p.ma_chien_dich = c.ma_chien_dich;
GO

-- 4. View Lịch sử trúng thưởng & Đổi quà (Tối ưu hóa Lọc)
-- (View này đã được nâng cấp JOIN thẳng vào bảng Campaign để hỗ trợ filter từ Backend không qua Java Stream)
IF OBJECT_ID('vw_reward_voucher_list', 'V') IS NOT NULL DROP VIEW vw_reward_voucher_list;
GO
CREATE VIEW vw_reward_voucher_list AS 
SELECT 
    rv.id, 
    rv.ma_voucher AS maVoucher, 
    rv.ma_giai_thuong AS maGiaiThuong, 
    p.ten_giai AS tenGiai, 
    p.loai_giai AS loaiGiai, 
    p.hinh_anh_url AS hinhAnhUrl, 
    p.ma_chien_dich AS maChienDich, 
    cam.ten_chien_dich AS tenChienDich, 
    rv.ma_khach_hang AS maKhachHang, 
    c.ten_khach AS tenKhach, 
    c.phone AS phone, 
    rv.ma_store_phat_hanh AS maStorePhatHanh, 
    s1.ten_cua_hang AS tenStorePhatHanh, 
    rv.thoi_gian_tao AS thoiGianTao, 
    rv.ma_store_doi_thuong AS maStoreDoiThuong, 
    s2.ten_cua_hang AS tenStoreDoiThuong, 
    rv.thoi_gian_doi AS thoiGianDoi, 
    rv.trang_thai AS trangThai 
FROM reward_voucher rv 
LEFT JOIN prize p ON rv.ma_giai_thuong = p.ma_giai_thuong 
LEFT JOIN campaign cam ON p.ma_chien_dich = cam.ma_chien_dich 
LEFT JOIN customer c ON rv.ma_khach_hang = c.ma_khach_hang 
LEFT JOIN store s1 ON rv.ma_store_phat_hanh = s1.ma_store 
LEFT JOIN store s2 ON rv.ma_store_doi_thuong = s2.ma_store;
GO

-- 5. View Danh sách Nhân viên
-- (Hỗ trợ truy xuất danh sách nhân viên cùng với tên cửa hàng gán cho nhân viên đó)
IF OBJECT_ID('vw_staff_list', 'V') IS NOT NULL DROP VIEW vw_staff_list;
GO
CREATE VIEW vw_staff_list AS 
SELECT 
    st.id, 
    st.ma_nhan_vien AS maNhanVien, 
    st.username, 
    st.ten_nhan_vien AS tenNhanVien, 
    st.role_id AS roleId, 
    st.ma_store AS maStore, 
    s.ten_cua_hang AS tenCuaHang, 
    st.trang_thai AS trangThai 
FROM staff st 
LEFT JOIN store s ON st.ma_store = s.ma_store;
GO
