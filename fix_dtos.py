import os
import re

# 1. Update DTOs
dto_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\dto'
for filename in ['InvoiceRequestDTO.java', 'PosSyncRequest.java']:
    filepath = os.path.join(dto_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_content = content.replace('Long storeId;', 'String maStore;')
    new_content = new_content.replace('Long getStoreId()', 'String getMaStore()')
    new_content = new_content.replace('void setStoreId(Long storeId)', 'void setMaStore(String maStore)')
    new_content = new_content.replace('this.storeId = storeId', 'this.maStore = maStore')
    new_content = new_content.replace('return storeId;', 'return maStore;')
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filename}")

# 2. Update Services
service_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service'
for filename in ['PosService.java', 'TurnManagementService.java']:
    filepath = os.path.join(service_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_content = content.replace('getStoreId().toString()', 'getMaStore()')
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filename}")

# 3. Update pos-simulator.html
html_file = r'd:\webquaymayrui\src\main\resources\templates\admin\pos-simulator.html'
with open(html_file, 'r', encoding='utf-8') as f:
    content = f.read()
new_content = content.replace('"storeId":', '"maStore":')
new_content = new_content.replace('document.getElementById(\'storeId\').value', 'document.getElementById(\'maStore\').value')
new_content = new_content.replace('id="storeId"', 'id="maStore"')
with open(html_file, 'w', encoding='utf-8') as f:
    f.write(new_content)
print("Updated pos-simulator.html")
