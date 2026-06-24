import re

file_path = r'd:\webquaymayrui\databaseSQL\03_TEST_SCRIPT.sql'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace column names
replacements = {
    r'id_cua_hang': r'ma_store',
    r'id_chien_dich': r'ma_chien_dich',
    r'id_khach_hang': r'ma_khach_hang',
    r'id_giai_thuong': r'ma_giai_thuong',
    r'id_hoa_don': r'ma_hoa_don',
    
    # Values might be numeric (e.g. 1, 2) in VALUES (1, 2...). 
    # For a test script it's tricky to regex replace the exact values, but let's do a basic replace
    # Just to make the script valid, we'll replace the numeric parameters with string ones.
}

for pattern, replacement in replacements.items():
    content = re.sub(pattern, replacement, content)

# Replace numeric IDs with strings in the specific INSERT statements
content = content.replace('VALUES (1, 1)', "VALUES ('CD01', 'ST01')")
content = content.replace('VALUES (1, 1, 0, 50, 100)', "VALUES ('ST01', 'PRIZE01', 0, 50, 100)")
content = content.replace('VALUES (1, 2, 0, 0, 0)', "VALUES ('ST01', 'PRIZE02', 0, 0, 0)")
content = content.replace('EXEC [dbo].[sp_Main_QuayThuong] 1, 1, 1, 1, 2,', "EXEC [dbo].[sp_Main_QuayThuong] 'KH01', 'CD01', 'ST01', 'PRIZE01', 'PRIZE02',")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated 03_TEST_SCRIPT.sql")
