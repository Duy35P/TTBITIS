import re

file_path = r'd:\webquaymayrui\databaseSQL\01_FINAL_SCHEMA.sql'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# find CREATE TABLE store
match = re.search(r'CREATE TABLE store\s*\((.*?)\);', content, re.IGNORECASE | re.DOTALL)
if match:
    print("--- store table ---")
    print(match.group(1))

# find CREATE TABLE campaign_store
match = re.search(r'CREATE TABLE campaign_store\s*\((.*?)\);', content, re.IGNORECASE | re.DOTALL)
if match:
    print("--- campaign_store table ---")
    print(match.group(1))
