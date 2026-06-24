import re

def process_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Generic replacements
    content = content.replace('getIdChienDich()', 'getMaChienDich()')
    
    # Specific ones for PosService and TurnManagementService
    content = content.replace('invoice.setIdCuaHang(store.getId());', 'invoice.setMaStore(store.getMaStore());')
    content = content.replace('invoice.setIdCuaHang(invoiceData.getStoreId());', 'invoice.setMaStore(invoiceData.getStoreId().toString());') # Assuming invoiceData has long or string
    # Actually, store.getId() -> store.getMaStore() is correct if we have the Store object. 
    # Let's verify what PosService does exactly for invoice creation.

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

process_file(r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\PosService.java')
process_file(r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\TurnManagementService.java')
