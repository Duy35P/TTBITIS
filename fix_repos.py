import os
import re

repo_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\repository'

# RewardVoucherRepository.java
filepath = os.path.join(repo_dir, 'RewardVoucherRepository.java')
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace('findByIdKhachHang(Long idKhachHang)', 'findByMaKhachHang(String maKhachHang)')
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

# StorePrizeInventoryRepository.java
filepath = os.path.join(repo_dir, 'StorePrizeInventoryRepository.java')
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()
content = content.replace('findByIdCuaHangAndIdGiaiThuong(Long idCuaHang, Long idGiaiThuong)', 'findByMaStoreAndMaGiaiThuong(String maStore, String maGiaiThuong)')
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

# Update Services calling findByIdKhachHang
service_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service'
for filename in os.listdir(service_dir):
    if not filename.endswith('.java'):
        continue
    filepath = os.path.join(service_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_content = content
    new_content = new_content.replace('findByIdKhachHang', 'findByMaKhachHang')
    new_content = new_content.replace('findByIdCuaHangAndIdGiaiThuong', 'findByMaStoreAndMaGiaiThuong')
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filename}")

