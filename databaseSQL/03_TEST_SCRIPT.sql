-- =========================================================================
-- FILE: 03_TEST_SCRIPT.sql
-- LƯU Ý QUAN TRỌNG: KHÔNG ĐƯỢC BÔI ĐEN TỪNG DÒNG.
-- Hãy bấm tổ hợp phím Ctrl + A (Chọn tất cả), sau đó bấm F5 (Execute).
-- Script này đã được nâng cấp để có thể chạy đi chạy lại nhiều lần không báo lỗi.
-- =========================================================================

USE [luckydraw];
GO

-- ==========================================
-- BƯỚC 1: TẠO DỮ LIỆU MẪU (CHẠY LẶP LẠI AN TOÀN)
-- ==========================================
PRINT N'--- ĐANG TẠO HOẶC LẤY DỮ LIỆU MẪU ---';

-- 1. Khách hàng
DECLARE @id_khach BIGINT;
IF NOT EXISTS (SELECT 1 FROM [dbo].[customer] WHERE phone = '0901234567')
    INSERT INTO [dbo].[customer] (phone) VALUES ('0901234567');
SELECT @id_khach = id FROM [dbo].[customer] WHERE phone = '0901234567';

-- 2. Cửa hàng
DECLARE @ma_store BIGINT;
IF NOT EXISTS (SELECT 1 FROM [dbo].[store] WHERE ten_cua_hang = N'Cửa hàng Quận 1')
    INSERT INTO [dbo].[store] (ten_cua_hang, dia_chi_store, ma_store) VALUES (N'Cửa hàng Quận 1', N'123 Lê Lợi', 'CH_Q1');
SELECT @ma_store = id FROM [dbo].[store] WHERE ten_cua_hang = N'Cửa hàng Quận 1';

-- 3. Chiến dịch
DECLARE @ma_chien_dich BIGINT;
IF NOT EXISTS (SELECT 1 FROM [dbo].[campaign] WHERE ten_chien_dich = N'Vòng Quay Chào Hè')
    INSERT INTO [dbo].[campaign] (ten_chien_dich, trang_thai) VALUES (N'Vòng Quay Chào Hè', 1);
SELECT @ma_chien_dich = id FROM [dbo].[campaign] WHERE ten_chien_dich = N'Vòng Quay Chào Hè';

-- 4. Giải thưởng
DECLARE @id_giai_iphone BIGINT;
IF NOT EXISTS (SELECT 1 FROM [dbo].[prize] WHERE ten_giai = N'Điện thoại iPhone 15')
    INSERT INTO [dbo].[prize] (ma_chien_dich, ten_giai, la_giai_thuong, loai_giai, xac_suat, ton_kho_toan_he_thong, gioi_han_trung_moi_customer)
    VALUES (@ma_chien_dich, N'Điện thoại iPhone 15', 1, 1, 10, 1, 1);
SELECT @id_giai_iphone = id FROM [dbo].[prize] WHERE ten_giai = N'Điện thoại iPhone 15';

DECLARE @id_giai_truot BIGINT;
IF NOT EXISTS (SELECT 1 FROM [dbo].[prize] WHERE ten_giai = N'Chúc bạn may mắn lần sau')
    INSERT INTO [dbo].[prize] (ma_chien_dich, ten_giai, la_giai_thuong, loai_giai, xac_suat)
    VALUES (@ma_chien_dich, N'Chúc bạn may mắn lần sau', 0, 0, 90);
SELECT @id_giai_truot = id FROM [dbo].[prize] WHERE ten_giai = N'Chúc bạn may mắn lần sau';

-- 5. Bơm tồn kho 
IF @id_giai_iphone IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM [dbo].[store_prize_inventory] WHERE ma_store = @ma_store AND ma_giai_thuong = @id_giai_iphone)
        INSERT INTO [dbo].[store_prize_inventory] (ma_store, ma_giai_thuong, ton_kho)
        VALUES (@ma_store, @id_giai_iphone, 1);
END


-- ==========================================
-- BƯỚC 2: TEST CỘNG LƯỢT QUAY
-- ==========================================
PRINT N'--- TEST 1: CỘNG LƯỢT QUAY ---';
EXEC [dbo].[sp_AddCustomerTurns_Safe] 
    @ma_khach_hang = @id_khach, 
    @ma_chien_dich = @ma_chien_dich, 
    @so_luong_cong = 2, 
    @nguon_tham_chieu = 'HD_TEST_001';

SELECT * FROM [dbo].[customer_turn] WHERE ma_khach_hang = @id_khach;


-- ==========================================
-- BƯỚC 3: TEST QUAY LẦN 1 (TRÚNG IPHONE)
-- ==========================================
PRINT N'--- TEST 2: QUAY LẦN 1 (TRÚNG IPHONE) ---';
DECLARE @ket_qua_lan_1 BIGINT;
DECLARE @ma_voucher VARCHAR(255) = 'VOUCHER-IPHONE-' + CAST(NEWID() AS VARCHAR(50));

EXEC [dbo].[sp_Main_QuayThuong]
    @ma_khach_hang = @id_khach,
    @ma_chien_dich = @ma_chien_dich,
    @ma_store = @ma_store,
    @ma_giai_thuong_du_kien = @id_giai_iphone,
    @id_giai_truot = @id_giai_truot,
    @ma_voucher_random = @ma_voucher,
    @ket_qua_giai_thuong = @ket_qua_lan_1 OUTPUT;

SELECT @ket_qua_lan_1 AS [Kết Quả Lần 1 (ID Giải Trúng)];
SELECT ton_kho AS [Tồn Kho Chi Nhánh Sau Khi Trúng] FROM [dbo].[store_prize_inventory] WHERE ma_giai_thuong = @id_giai_iphone;


-- ==========================================
-- BƯỚC 4: TEST CHỐNG GIAN LẬN (QUAY LẦN 2 TRÚNG TIẾP IPHONE)
-- ==========================================
PRINT N'--- TEST 3: QUAY LẦN 2 (HACKER CỐ NHÉT KẾT QUẢ IPHONE) ---';
DECLARE @ket_qua_lan_2 BIGINT;
DECLARE @ma_voucher_2 VARCHAR(255) = 'VOUCHER-HACK-' + CAST(NEWID() AS VARCHAR(50));

EXEC [dbo].[sp_Main_QuayThuong]
    @ma_khach_hang = @id_khach,
    @ma_chien_dich = @ma_chien_dich,
    @ma_store = @ma_store,
    @ma_giai_thuong_du_kien = @id_giai_iphone,
    @id_giai_truot = @id_giai_truot,
    @ma_voucher_random = @ma_voucher_2,
    @ket_qua_giai_thuong = @ket_qua_lan_2 OUTPUT;

SELECT @ket_qua_lan_2 AS [Kết Quả Lần 2 (Bị ép thành Trượt)];


-- ==========================================
-- BƯỚC 5: TEST ĐỔI QUÀ VẬT LÝ TẠI QUẦY
-- ==========================================
PRINT N'--- TEST 4: NHÂN VIÊN QUÉT MÃ TRẢ IPHONE CHO KHÁCH ---';
EXEC [dbo].[sp_DoiQuaVatLy]
    @ma_voucher = @ma_voucher,
    @ma_store = @ma_store;

SELECT ma_voucher AS [Mã Voucher], trang_thai AS [Trạng Thái (1=Đã nhận)] 
FROM [dbo].[reward_voucher] WHERE ma_voucher = @ma_voucher;
