INSERT INTO [dbo].[chuc_nang] (ma_chuc_nang, ten_chuc_nang, nhom) VALUES
('ACT_CHIENDICH_ADD', N'Thêm chiến dịch', N'Chiến Dịch'),
('ACT_CHIENDICH_EDIT', N'Sửa chiến dịch', N'Chiến Dịch'),
('ACT_CUAHANG_EDIT', N'Sửa cửa hàng', N'Cửa Hàng'),
('ACT_CUAHANG_EXPORT', N'Xuất dữ liệu cửa hàng', N'Cửa Hàng'),
('ACT_CUAHANG_IMPORT', N'Nhập dữ liệu cửa hàng', N'Cửa Hàng'),
('ACT_GIAITHUONG_ADD', N'Thêm giải thưởng', N'Giải Thưởng'),
('ACT_GIAITHUONG_EXPORT', N'Xuất dữ liệu giải thưởng', N'Giải Thưởng'),
('ACT_GIAITHUONG_IMPORT', N'Nhập dữ liệu giải thưởng', N'Giải Thưởng'),
('ACT_KHACHHANG_EDIT', N'Sửa khách hàng', N'Khách Hàng'),
('ACT_KHACHHANG_EXPORT', N'Xuất dữ liệu khách hàng', N'Khách Hàng'),
('ACT_PHANBO_ADD', N'Thêm phân bổ', N'Phân Bổ'),
('ACT_PHANBO_CANCEL', N'Hủy phân bổ', N'Phân Bổ'),
('ACT_PHANBO_EDIT', N'Sửa phân bổ', N'Phân Bổ'),
('ACT_PHANBO_EXPORT', N'Xuất dữ liệu phân bổ', N'Phân Bổ'),
('ACT_PHANBO_REVOKE', N'Thu hồi phân bổ', N'Phân Bổ'),
('QL_CUAHANG', N'Quản lý cửa hàng', N'Quản Lý Chung'),
('QL_KIEMTRAMA', N'Kiểm tra mã', N'Quản Lý Chung'),
('QL_LUOTQUAY', N'Quản lý lượt quay', N'Quản Lý Chung');

INSERT INTO [dbo].[phanquyen] (role_id, ma_chuc_nang)
SELECT 'ADMIN', ma_chuc_nang FROM [dbo].[chuc_nang];
