import re

# Fix AdminCampaignController.java
file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\controller\AdminCampaignController.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('.getIdCuaHang()', '.getMaStore()')
content = content.replace('mapping.setIdChienDich(campaign.getId())', 'mapping.setMaChienDich(campaign.getMaChienDich())')
# Wait, let's see how setIdCuaHang was called: mapping.setIdCuaHang(storeId) where storeId is Long.
# The payload from the form is probably a list of Long storeIds?
# Let's check AdminCampaignController.java line 102 to see exactly what it was.
