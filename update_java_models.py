import os
import re

model_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\model'

replacements = {
    r'private Long idCuaHang;': r'private String maStore;',
    r'private Long idChienDich;': r'private String maChienDich;',
    r'private Long idKhachHang;': r'private String maKhachHang;',
    r'private Long idGiaiThuong;': r'private String maGiaiThuong;',
    r'private Long idHoaDon;': r'private String maHoaDon;',
    r'private Long idKhachHangKichHoat;': r'private String maKhachHangKichHoat;',
    r'private Long idCuaHangPhatHanh;': r'private String maStorePhatHanh;',
    r'private Long idCuaHangDoiThuong;': r'private String maStoreDoiThuong;',
    
    r'name\s*=\s*"id_cua_hang"': r'name = "ma_store"',
    r'name\s*=\s*"id_chien_dich"': r'name = "ma_chien_dich"',
    r'name\s*=\s*"id_khach_hang"': r'name = "ma_khach_hang"',
    r'name\s*=\s*"id_giai_thuong"': r'name = "ma_giai_thuong"',
    r'name\s*=\s*"id_hoa_don"': r'name = "ma_hoa_don"',
    r'name\s*=\s*"id_khach_hang_kich_hoat"': r'name = "ma_khach_hang_kich_hoat"',
    r'name\s*=\s*"id_cua_hang_phat_hanh"': r'name = "ma_store_phat_hanh"',
    r'name\s*=\s*"id_cua_hang_doi_thuong"': r'name = "ma_store_doi_thuong"'
}

for filename in os.listdir(model_dir):
    if not filename.endswith('.java'):
        continue
    filepath = os.path.join(model_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = content
    for pattern, replacement in replacements.items():
        new_content = re.sub(pattern, replacement, new_content)

    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filename}")

