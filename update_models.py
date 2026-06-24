import os
import re

model_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\model'

for filename in os.listdir(model_dir):
    if not filename.endswith('.java'):
        continue
    filepath = os.path.join(model_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the @Id followed by @GeneratedValue and @Column(name = "..._id")
    # We want to replace the column name to "id" and the variable name to "id"
    # Example:
    # @Id
    # @GeneratedValue(...)
    # @Column(name = "xxx_id")
    # private Long xxxId;
    
    # We can just look for @Column(name = "xxx_id") right after @GeneratedValue
    # and private Long xxxId; right after that.
    
    pattern = re.compile(r'(@Column\s*\(\s*name\s*=\s*"[^"]+_id".*?\)\s*\n\s*private\s+Long\s+)([a-zA-Z0-9_]+)(\s*;)')
    
    def replacer(match):
        col_def = match.group(1)
        var_name = match.group(2)
        semicolon = match.group(3)
        # replace _id" with id"
        col_def = re.sub(r'name\s*=\s*"[^"]+_id"', 'name = "id"', col_def)
        return col_def + 'id' + semicolon

    new_content = pattern.sub(replacer, content)

    # Specific additions:
    if filename == 'Customer.java' and 'private String maKhachHang;' not in new_content:
        new_content = new_content.replace('public class Customer {\n', 'public class Customer {\n\n    @Column(name = "ma_khach_hang", unique = true)\n    private String maKhachHang;\n')
    elif filename == 'Staff.java' and 'private String maNhanVien;' not in new_content:
        new_content = new_content.replace('public class Staff {\n', 'public class Staff {\n\n    @Column(name = "ma_nhan_vien", unique = true)\n    private String maNhanVien;\n')
    elif filename == 'Campaign.java' and 'private String maChienDich;' not in new_content:
        new_content = new_content.replace('public class Campaign {\n', 'public class Campaign {\n\n    @Column(name = "ma_chien_dich", unique = true)\n    private String maChienDich;\n')
    elif filename == 'Prize.java' and 'private String maGiaiThuong;' not in new_content:
        new_content = new_content.replace('public class Prize {\n', 'public class Prize {\n\n    @Column(name = "ma_giai_thuong", unique = true)\n    private String maGiaiThuong;\n')

    # Specific removal:
    if filename == 'Campaign.java':
        new_content = re.sub(r'\s*@Column\(name\s*=\s*"tong_luot_du_kien"\)\s*\n\s*private\s+Integer\s+tongLuotDuKien;', '', new_content)

    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filename}")

