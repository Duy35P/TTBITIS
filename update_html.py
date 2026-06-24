# -*- coding: utf-8 -*-
import re

file_path = r'd:\webquaymayrui\src\main\resources\templates\admin\fragments\store-allocation-fragment.html'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('name="storeIds"', 'name="storeMas"')
content = content.replace('th:value=""', 'th:value=""')
content = content.replace('assignedStoreIds != null and assignedStoreIds.contains(store.storeId)', 'assignedStoreMas != null and assignedStoreMas.contains(store.maStore)')
content = content.replace("th:text=\" + ' (ID: ' +  + ')'\"", "th:text=\" + ' (Mã: ' +  + ')'\"")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated store-allocation-fragment.html")
