#!/usr/bin/env python3
import re

filepath = '/home/engine/project/buttons/miscellaneous/AGF.java'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

lambda_start = content.find('}, (player) -> {')
lambda_content = content[lambda_start:]
lines = lambda_content.split('\n')

# Find cost section
cost_section_start = None
for i, line in enumerate(lines):
    if 'if (req)' in line or 'if (level >= costlist.size())' in line:
        cost_section_start = i
        break

print(f"Cost section starts at line {cost_section_start}")
print("\nLines before cost section:")
for i, line in enumerate(lines[:cost_section_start if cost_section_start else len(lines)]):
    print(f"{i:3}: {line}")
