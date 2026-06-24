import re

file_path = r'd:\webquaymayrui\databaseSQL\01_FINAL_SCHEMA.sql'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

replacements = {
    r'\[id_cua_hang\]\s+BIGINT': r'[ma_store] VARCHAR(255)',
    r'\[id_chien_dich\]\s+BIGINT': r'[ma_chien_dich] VARCHAR(255)',
    r'\[id_khach_hang\]\s+BIGINT': r'[ma_khach_hang] VARCHAR(255)',
    r'\[id_giai_thuong\]\s+BIGINT': r'[ma_giai_thuong] VARCHAR(255)',
    r'\[id_hoa_don\]\s+BIGINT': r'[ma_hoa_don] VARCHAR(255)',
    r'\[id_khach_hang_kich_hoat\]\s+BIGINT': r'[ma_khach_hang_kich_hoat] VARCHAR(255)',
    r'\[id_cua_hang_phat_hanh\]\s+BIGINT': r'[ma_store_phat_hanh] VARCHAR(255)',
    r'\[id_cua_hang_doi_thuong\]\s+BIGINT': r'[ma_store_doi_thuong] VARCHAR(255)',
    
    # Also replace just the brackets if they are in UNIQUE, CONSTRAINT, etc.
    r'\[id_cua_hang\]': r'[ma_store]',
    r'\[id_chien_dich\]': r'[ma_chien_dich]',
    r'\[id_khach_hang\]': r'[ma_khach_hang]',
    r'\[id_giai_thuong\]': r'[ma_giai_thuong]',
    r'\[id_hoa_don\]': r'[ma_hoa_don]',
    r'\[id_khach_hang_kich_hoat\]': r'[ma_khach_hang_kich_hoat]',
    r'\[id_cua_hang_phat_hanh\]': r'[ma_store_phat_hanh]',
    r'\[id_cua_hang_doi_thuong\]': r'[ma_store_doi_thuong]'
}

new_content = content
for pattern, replacement in replacements.items():
    new_content = re.sub(pattern, replacement, new_content)

# Update the vw_StoreInventoryStatus query at the bottom
new_content = new_content.replace('spi.[id_cua_hang]', 'spi.[ma_store]')
new_content = new_content.replace('spi.[id_giai_thuong]', 'spi.[ma_giai_thuong]')
new_content = new_content.replace('p.[id_chien_dich]', 'p.[ma_chien_dich]')
new_content = new_content.replace('s.[id]    = spi.[ma_store]', 's.[ma_store] = spi.[ma_store]')
new_content = new_content.replace('p.[id]    = spi.[ma_giai_thuong]', 'p.[ma_giai_thuong] = spi.[ma_giai_thuong]')
new_content = new_content.replace('c.[id] = p.[ma_chien_dich]', 'c.[ma_chien_dich] = p.[ma_chien_dich]')

# Update the native query at the end
new_content = new_content.replace('ON s.id = cs.ma_store', 'ON s.ma_store = cs.ma_store')
new_content = new_content.replace('ON cs.ma_chien_dich = c.id', 'ON cs.ma_chien_dich = c.ma_chien_dich')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Updated 01_FINAL_SCHEMA.sql")
