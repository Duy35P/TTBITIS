import os
import re

repo_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\repository'

for filename in os.listdir(repo_dir):
    if not filename.endswith('.java'):
        continue
    filepath = os.path.join(repo_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = content
    new_content = new_content.replace('findByIdChienDich(Long', 'findByMaChienDich(String')
    new_content = new_content.replace('deleteByIdChienDich(Long', 'deleteByMaChienDich(String')
    new_content = new_content.replace('findByIdCuaHang(Long', 'findByMaStore(String')

    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filename}")

