import re

file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\repository\CustomerTurnRepository.java'
with open(file_path, 'r', encoding='utf-8') as f:
    print("--- CustomerTurnRepository ---")
    print(f.read())

file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\PosService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()
    print("--- PosService lines 97-105 ---")
    print("".join(lines[95:105]))

file_path = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\service\TurnManagementService.java'
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()
    print("--- TurnManagementService lines 76-85 ---")
    print("".join(lines[75:85]))
    print("--- TurnManagementService lines 105-115 ---")
    print("".join(lines[105:115]))

