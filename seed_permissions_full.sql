DELETE FROM [dbo].[phanquyen];
DELETE FROM [dbo].[chuc_nang];

INSERT INTO [dbo].[chuc_nang] (ma_chuc_nang, ten_chuc_nang, nhom) VALUES
-- TỔNG QUAN
('QL_TONGQUAN', N'Truy cập Trang Tổng Quan', N'Tổng Quan'),

-- CHIẾN DỊCH
('QL_CHIENDICH', N'Truy cập Quản lý chiến dịch', N'Chiến Dịch'),
('ACT_CHIENDICH_ADD', N'Thêm mới chiến dịch', N'Chiến Dịch'),
('ACT_CHIENDICH_EDIT', N'Sửa chiến dịch', N'Chiến Dịch'),
('ACT_CHIENDICH_EXPORT', N'Xuất Excel chiến dịch', N'Chiến Dịch'),

-- KHÁCH HÀNG
('QL_KHACHHANG', N'Truy cập Quản lý khách hàng', N'Khách Hàng'),
('ACT_KHACHHANG_EDIT', N'Sửa khách hàng', N'Khách Hàng'),
('ACT_KHACHHANG_EXPORT', N'Xuất Excel khách hàng', N'Khách Hàng'),

-- CỬA HÀNG
('QL_CUAHANG', N'Truy cập Quản lý cửa hàng', N'Cửa Hàng'),
('ACT_CUAHANG_ADD', N'Thêm cửa hàng mới', N'Cửa Hàng'),
('ACT_CUAHANG_EDIT', N'Sửa cửa hàng (Nút Thao Tác)', N'Cửa Hàng'),
('ACT_CUAHANG_EXPORT', N'Xuất Excel cửa hàng', N'Cửa Hàng'),
('ACT_CUAHANG_IMPORT', N'Nhập Excel cửa hàng', N'Cửa Hàng'),

-- NHÂN VIÊN
('QL_NHANVIEN', N'Truy cập Quản lý nhân viên', N'Nhân Viên'),
('ACT_NHANVIEN_ADD', N'Thêm nhân viên mới', N'Nhân Viên'),
('ACT_NHANVIEN_EDIT', N'Sửa nhân viên (Nút Thao Tác)', N'Nhân Viên'),
('ACT_NHANVIEN_EXPORT', N'Xuất Excel nhân viên', N'Nhân Viên'),
('QL_VAITRO', N'Truy cập Quản lý vai trò', N'Nhân Viên'),

-- GIẢI THƯỞNG
('QL_GIAITHUONG', N'Truy cập Quản lý kho giải thưởng', N'Giải Thưởng'),
('ACT_GIAITHUONG_ADD', N'Thêm giải thưởng', N'Giải Thưởng'),
('ACT_GIAITHUONG_EDIT', N'Sửa giải thưởng (Nút Thao Tác)', N'Giải Thưởng'),
('ACT_GIAITHUONG_EXPORT', N'Xuất Excel giải thưởng', N'Giải Thưởng'),
('ACT_GIAITHUONG_IMPORT', N'Import Excel giải thưởng', N'Giải Thưởng'),
('ACT_GIAITHUONG_NAPMA', N'Nạp mã hàng loạt', N'Giải Thưởng'),

-- PHÂN BỔ
('QL_PHANBO', N'Truy cập Phân bổ cửa hàng', N'Phân Bổ'),
('ACT_PHANBO_ADD', N'Thêm phân bổ', N'Phân Bổ'),
('ACT_PHANBO_EDIT', N'Sửa phân bổ (Nút Thao Tác)', N'Phân Bổ'),
('ACT_PHANBO_CANCEL', N'Hủy phân bổ', N'Phân Bổ'),
('ACT_PHANBO_REVOKE', N'Thu hồi phân bổ', N'Phân Bổ'),
('ACT_PHANBO_EXPORT', N'Xuất Excel phân bổ', N'Phân Bổ'),

-- CHỨC NĂNG KHÁC
('QL_VOUCHER', N'Truy cập Lịch sử trúng thưởng', N'Lịch Sử'),
('QL_LUOTQUAY', N'Truy cập Biến động lượt quay', N'Lịch Sử'),
('QL_HOADON', N'Truy cập Tra cứu hóa đơn', N'Lịch Sử'),
('QL_KIEMTRAMA', N'Sử dụng Kiểm tra mã', N'Quản Lý Chung');

INSERT INTO [dbo].[phanquyen] (role_id, ma_chuc_nang)
SELECT 'ADMIN', ma_chuc_nang FROM [dbo].[chuc_nang];
