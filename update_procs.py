import re

file_path = r'd:\webquaymayrui\databaseSQL\02_THICK_DB_PROCEDURES.sql'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace parameter definitions
replacements = {
    r'@id_khach_hang\s+BIGINT': r'@ma_khach_hang VARCHAR(255)',
    r'@id_chien_dich\s+BIGINT': r'@ma_chien_dich VARCHAR(255)',
    r'@id_cua_hang\s+BIGINT': r'@ma_store VARCHAR(255)',
    r'@id_giai_thuong_du_kien\s+BIGINT': r'@ma_giai_thuong_du_kien VARCHAR(255)',
    r'@id_giai_truot\s+BIGINT': r'@ma_giai_truot VARCHAR(255)',
    r'@ket_qua_giai_thuong\s+BIGINT\s+OUTPUT': r'@ket_qua_giai_thuong VARCHAR(255) OUTPUT',
    r'@id_cua_hang_phat_hanh\s+BIGINT': r'@ma_store_phat_hanh VARCHAR(255)',
    
    # Replace variable usages
    r'@id_khach_hang': r'@ma_khach_hang',
    r'@id_chien_dich': r'@ma_chien_dich',
    r'@id_cua_hang': r'@ma_store',
    r'@id_giai_thuong_du_kien': r'@ma_giai_thuong_du_kien',
    r'@id_giai_truot': r'@ma_giai_truot',
    
    # Replace column names
    r'\bid_khach_hang\b': r'ma_khach_hang',
    r'\bid_chien_dich\b': r'ma_chien_dich',
    r'\bid_cua_hang\b': r'ma_store',
    r'\bid_giai_thuong\b': r'ma_giai_thuong',
    r'\bid_cua_hang_phat_hanh\b': r'ma_store_phat_hanh',
    r'\bid_cua_hang_doi_thuong\b': r'ma_store_doi_thuong',

    # In prize query
    r'WHERE id = @ma_giai_thuong_du_kien': r'WHERE ma_giai_thuong = @ma_giai_thuong_du_kien',
    r'WHERE id = @id_giai_thuong_du_kien': r'WHERE ma_giai_thuong = @ma_giai_thuong_du_kien',

    # In sp_GetAvailablePrizesForStore
    r'p.id = spi.ma_giai_thuong': r'p.ma_giai_thuong = spi.ma_giai_thuong',
    r'p.id_chien_dich = @ma_chien_dich': r'p.ma_chien_dich = @ma_chien_dich',
    r'rv.ma_giai_thuong = p.id': r'rv.ma_giai_thuong = p.ma_giai_thuong',
    r'p.id,': r'p.ma_giai_thuong AS id,', # In SELECT p.id, change to p.ma_giai_thuong AS id or just p.ma_giai_thuong
    
    r'DECLARE @id_giai_thuong BIGINT': r'DECLARE @ma_giai_thuong VARCHAR(255)',
    r'DECLARE @id_cua_hang_phat_hanh BIGINT': r'DECLARE @ma_store_phat_hanh VARCHAR(255)',
    r'@id_giai_thuong = ma_giai_thuong': r'@ma_giai_thuong = ma_giai_thuong',
    r'@id_cua_hang_phat_hanh = ma_store_phat_hanh': r'@ma_store_phat_hanh = ma_store_phat_hanh',
    r'IF @id_giai_thuong IS NULL': r'IF @ma_giai_thuong IS NULL',
    r'IF @id_cua_hang_phat_hanh !=': r'IF @ma_store_phat_hanh !='
}

new_content = content
for pattern, replacement in replacements.items():
    new_content = re.sub(pattern, replacement, new_content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Updated 02_THICK_DB_PROCEDURES.sql")
