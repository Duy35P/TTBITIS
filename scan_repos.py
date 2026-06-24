import os
import re

repo_dir = r'd:\webquaymayrui\src\main\java\com\bitis\luckydraw\repository'

for filename in os.listdir(repo_dir):
    if not filename.endswith('.java'):
        continue
    filepath = os.path.join(repo_dir, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Look for any findById... that's not findById(Long id)
    # Look for @Query
    
    print(f"--- {filename} ---")
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if 'findBy' in line or '@Query' in line or 'List<' in line or 'Optional<' in line:
            if 'import ' not in line and 'class ' not in line and 'package ' not in line:
                print(f"Line {i+1}: {line.strip()}")

