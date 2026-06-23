USE [luckydraw];
GO

-- XÓA DỮ LIỆU CŨ ĐỂ LÀM SẠCH BÀI TEST
DELETE FROM [dbo].[reward_voucher];
DELETE FROM [dbo].[turn_transaction];
DELETE FROM [dbo].[user_turn];
DELETE FROM [dbo].[store_prize_inventory];
DELETE FROM [dbo].[prize];
DELETE FROM [dbo].[campaign];
DELETE FROM [dbo].[store];
DELETE FROM [dbo].[users];

-- Reset Identity chuẩn bằng cách Insert xong SELECT @@IDENTITY
DBCC CHECKIDENT ('[users]', RESEED, 0);
DBCC CHECKIDENT ('[campaign]', RESEED, 0);
DBCC CHECKIDENT ('[prize]', RESEED, 0);

PRINT '==================================================';
PRINT '1. TẠO DỮ LIỆU MẪU MÔ PHỎNG';
PRINT '==================================================';
INSERT INTO [dbo].[users] (username, password, role) VALUES ('khach1', '123', 'CUSTOMER');
DECLARE @id_khach BIGINT = SCOPE_IDENTITY();

INSERT INTO [dbo].[campaign] (ten, trang_thai) VALUES (N'Chiến dịch Mùa Hè', 1);
DECLARE @id_chien_dich BIGINT = SCOPE_IDENTITY();

-- Tạo 2 giải thưởng: Giải Thật (Balo - Tồn kho: 1 cái duy nhất) và Giải Trượt
INSERT INTO [dbo].[prize] (id_chien_dich, ten, loai_giai, la_giai_thuong, xac_suat, ton_kho_toan_he_thong)
VALUES (@id_chien_dich, N'Balo Biti''s', 1, 1, 0.5, 1);
DECLARE @id_balo BIGINT = SCOPE_IDENTITY();

INSERT INTO [dbo].[prize] (id_chien_dich, ten, loai_giai, la_giai_thuong, xac_suat, ton_kho_toan_he_thong)
VALUES (@id_chien_dich, N'Chúc Bạn May Mắn', 0, 0, 0.5, 0);
DECLARE @id_truot BIGINT = SCOPE_IDENTITY();


PRINT '==================================================';
PRINT '2. TEST CỘNG LƯỢT (sp_AddUserTurns_Safe)';
PRINT 'Kịch bản: Khách hàng mua đơn 2 Triệu -> Được cộng 2 Lượt';
PRINT '==================================================';

EXEC [dbo].[sp_AddUserTurns_Safe] 
    @id_nguoi_dung = @id_khach,
    @id_chien_dich = @id_chien_dich,
    @so_luong_cong = 2,
    @nguon_tham_chieu = 'HD_2_TRIEU';

SELECT luot_con_lai AS [Số_Lượt_Đang_Có_Trong_Ví] FROM [dbo].[user_turn] WHERE id_nguoi_dung = @id_khach;
SELECT so_luong AS [Sổ_Sao_Kê_Cộng_Lượt] FROM [dbo].[turn_transaction] WHERE loai = 1;


PRINT '==================================================';
PRINT '3. TEST QUAY THƯỞNG LẦN 1: TRÚNG BALO THÀNH CÔNG';
PRINT '==================================================';
DECLARE @KetQua1 BIGINT;
EXEC [dbo].[sp_DeductTurnAndPrize_Single]
    @id_nguoi_dung = @id_khach,
    @id_chien_dich = @id_chien_dich,
    @id_giai_thuong_du_kien = @id_balo, -- Backend quay ra Balo
    @id_giai_truot = @id_truot,
    @ma_voucher_random = 'VOUCHER_BALO_01',
    @ket_qua_giai_thuong = @KetQua1 OUTPUT;

PRINT '>> KẾT QUẢ LẦN 1: Giai thuong nhan duoc la ID = ' + CAST(@KetQua1 AS VARCHAR);


PRINT '==================================================';
PRINT '4. TEST QUAY THƯỞNG LẦN 2: BỊ FALLBACK VÌ HẾT KHO';
PRINT '==================================================';
DECLARE @KetQua2 BIGINT;
EXEC [dbo].[sp_DeductTurnAndPrize_Single]
    @id_nguoi_dung = @id_khach,
    @id_chien_dich = @id_chien_dich,
    @id_giai_thuong_du_kien = @id_balo, -- Backend lỡ Random ra Balo tiếp
    @id_giai_truot = @id_truot,
    @ma_voucher_random = 'VOUCHER_BALO_02',
    @ket_qua_giai_thuong = @KetQua2 OUTPUT;

PRINT '>> KẾT QUẢ LẦN 2: Giai thuong nhan duoc la ID = ' + CAST(@KetQua2 AS VARCHAR);

PRINT '==================================================';
PRINT '5. KIỂM TRA LẠI BẢNG VOUCHER VÀ VÍ CUỐI CÙNG';
PRINT '==================================================';
-- Kiểm tra Voucher: Phải chỉ có ĐÚNG 1 VOUCHER được tạo ra (VOUCHER_BALO_01)
SELECT ma_voucher AS [Mã_Voucher_Được_Sinh_Ra] FROM [dbo].[reward_voucher];

-- Kiểm tra Ví: Ban đầu cộng 2 lượt, quay 2 lần, giờ phải còn 0 lượt
SELECT luot_con_lai AS [Số_Lượt_Cuối_Cùng_Trong_Ví] FROM [dbo].[user_turn] WHERE id_nguoi_dung = @id_khach;
GO
