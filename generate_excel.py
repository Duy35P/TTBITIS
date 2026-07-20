# -*- coding: utf-8 -*-
import pandas as pd

campaigns = [
    {'id': 'cd1', 'name': 'Chiến dịch 1'},
    {'id': 'cd2', 'name': 'Chiến dịch demo 2.0'},
    {'id': 'cd3', 'name': 'Chiến dịch demo 3'},
    {'id': 'cd4', 'name': 'Chiến dịch demo 4'},
]

prize_data = []
code_data = []

for c in campaigns:
    # 1. Trượt
    prize_data.append({
        'Mã Chiến Dịch': c['id'], 'Tên Chiến Dịch': c['name'], 'Mã Quà': f"TRUOT-{c['id']}", 'Tên Quà': "Chúc may mắn lần sau", 'Xác Suất': 50, 'Loại Quà': "Hiện vật", 'Là Quà Tặng?': "Không", 'Tồn Kho Tổng': "Không giới hạn", 'Giới hạn/Người': ""
    })
    
    # 2. Voucher
    ma_voucher = f"VOUCHER-{c['id']}"
    prize_data.append({
        'Mã Chiến Dịch': c['id'], 'Tên Chiến Dịch': c['name'], 'Mã Quà': ma_voucher, 'Tên Quà': "Voucher 50k", 'Xác Suất': 30, 'Loại Quà': "Voucher", 'Là Quà Tặng?': "Có", 'Tồn Kho Tổng': 100, 'Giới hạn/Người': 1
    })
    
    # Generate codes for voucher
    for i in range(1, 11):
        code_data.append({'Mã Giải Thưởng': ma_voucher, 'Code': f"{ma_voucher}-CODE{i}"})
        
    # 3. Hiện vật
    ma_hienvat = f"HUNTER-{c['id']}"
    prize_data.append({
        'Mã Chiến Dịch': c['id'], 'Tên Chiến Dịch': c['name'], 'Mã Quà': ma_hienvat, 'Tên Quà': "Giày Biti's Hunter", 'Xác Suất': 20, 'Loại Quà': "Hiện vật", 'Là Quà Tặng?': "Có", 'Tồn Kho Tổng': 5, 'Giới hạn/Người': 1
    })
    
    # Generate codes for hiện vật
    for i in range(1, 6):
        code_data.append({'Mã Giải Thưởng': ma_hienvat, 'Code': f"{ma_hienvat}-CODE{i}"})

df_prizes = pd.DataFrame(prize_data)
df_prizes.to_excel('GiaiThuong_4ChienDich.xlsx', index=False)

df_codes = pd.DataFrame(code_data)
df_codes.to_excel('MaQuaTang_4ChienDich.xlsx', index=False)
print("OK")
