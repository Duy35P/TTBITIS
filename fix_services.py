import os

files_to_fix = [
    r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\PosService.java',
    r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\TurnManagementService.java',
    r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\DeltaRuleEngine.java'
]

for filepath in files_to_fix:
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace the remaining 'campaignId' with 'maChienDich'
    # But carefully not replacing anything else that might break
    new_content = content.replace('campaignId', 'maChienDich')
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print(f"Fixed {filepath}")
