import pandas as pd

# Dữ liệu phù hợp cho việc import Tồn Kho Cửa Hàng (Store Inventory)
data = {
    "Mã Cửa Hàng (MaStore)": ["1101", "1103", "1104", "1101", "1103"],
    "Tên Cửa Hàng (Chỉ để tham khảo)": ["CH Chợ Lớn", "CH Điện Biên Phủ", "CH Quang Trung", "CH Chợ Lớn", "CH Điện Biên Phủ"],
    "Mã Giải Thưởng (MaGiaiThuong)": ["GIAI_AOMUA", "GIAI_AOMUA", "GIAI_MUBAOHIEM", "GIAI_BALO", "GIAI_BALO"],
    "Tên Quà Tặng (Chỉ để tham khảo)": ["Áo mưa thời trang", "Áo mưa thời trang", "Mũ bảo hiểm TLC", "Balo Biti's Hunter", "Balo Biti's Hunter"],
    "Tổng Lượng Cấp": [100, 150, 200, 30, 40],
    "Đã Phát": [20, 150, 195, 5, 40],
    "Tồn Kho Thực Tế": [80, 0, 5, 25, 0]
}

df = pd.DataFrame(data)

# Lưu thành file excel
file_path = "d:/webquaymayrui/files/Kho_Giai_Thuong_Mau.xlsx"
df.to_excel(file_path, index=False)
print(f"Da tao file Excel mau tai: {file_path}")
