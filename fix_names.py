import os
import re

# 1. Update HTML templates
html_dir = r'd:\webquaymayrui\src\main\resources\templates'
for root, dirs, files in os.walk(html_dir):
    for filename in files:
        if filename.endswith('.html'):
            filepath = os.path.join(root, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content
            new_content = new_content.replace('campaign.campaignId', 'campaign.id')
            new_content = new_content.replace('store.storeId', 'store.id')
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {filename}")

# 2. Update Repositories
repo_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\repository'
for root, dirs, files in os.walk(repo_dir):
    for filename in files:
        if filename.endswith('.java'):
            filepath = os.path.join(root, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content
            new_content = new_content.replace('String idChienDich', 'String maChienDich')
            new_content = new_content.replace('String idCuaHang', 'String maStore')
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {filename}")

# 3. Update Services
service_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service'
for root, dirs, files in os.walk(service_dir):
    for filename in files:
        if filename.endswith('.java'):
            filepath = os.path.join(root, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content
            new_content = new_content.replace('String campaignId', 'String maChienDich')
            new_content = new_content.replace('calculateTurns(campaignId,', 'calculateTurns(maChienDich,')
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {filename}")
