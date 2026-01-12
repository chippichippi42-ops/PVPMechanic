#!/usr/bin/env python3
import re

filepath = '/home/engine/project/buttons/miscellaneous/AGF.java'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

lambda_start = content.find('}, (player) -> {')
lambda_content = content[lambda_start:]
lines = lambda_content.split('\n')

def extract_mathematical_expressions(text):
    custom_values = []
    modified_text = text
    modified_text = re.sub(r'Math\.round\(\(float\)\(([^)]+)\)\)', r'round(\1)', modified_text)
    modified_text = re.sub(r'Math\.round\(\(float\)([^)]+)\)', r'round(\1)', modified_text)
    match = re.search(r'Utility\.toDecimal\(\(double\)\(([^)]+)\)\)', modified_text)
    if match:
        expr = match.group(1).strip()
        if expr.startswith('(') and expr.endswith(')'):
            inner = expr[1:-1]
            if '(' not in inner:
                expr = inner
        custom_values.append(expr)
        modified_text = modified_text.replace(match.group(0), '{value_0}', 1)
    else:
        matches = re.finditer(r'Utility\.toDecimal\(\(double\)([^)]+)\)', modified_text)
        for match in matches:
            expr = match.group(1).rstrip('D')
            custom_values.append(expr)
            modified_text = modified_text.replace(match.group(0), '{value_' + str(len(custom_values) - 1) + '}', 1)
    return modified_text, custom_values

# Find all lore.add() in lambda
print("All lore.add() calls in lambda:")
for i, line in enumerate(lines):
    lore_match = re.search(r'lore\.add\("([^"]+)"\)', line)
    if lore_match:
        lore_text = lore_match.group(1)
        processed, values = extract_mathematical_expressions(lore_text)
        print(f"Line {i}: {lore_text}")
        print(f"        → {processed}")
        print(f"        Values: {values}")
        print()
