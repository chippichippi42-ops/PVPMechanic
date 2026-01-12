#!/usr/bin/env python3
import re

filepath = '/home/engine/project/buttons/melee/AMA.java'

def extract_lore_from_add_call(lore_line_raw):
    """Extract and process lore from a lore.add() call, handling string concatenations."""
    match = re.search(r'lore\.add\((.+)\);', lore_line_raw.strip())
    if not match:
        return None, []

    content = match.group(1)
    parts = []
    custom_values = []

    # Find all string literals
    string_matches = re.finditer(r'"([^"]*)"', content)
    last_end = 0

    for str_match in string_matches:
        # Add text before this string (could be expressions or operators)
        between = content[last_end:str_match.start()].strip()

        # Check if it's an expression (not just + or whitespace)
        if between and between != '+':
            expr = between.rstrip('D')

            # Process mathematical expressions
            expr = re.sub(r'Math\.round\(\(float\)\(([^)]+)\)\)', r'round(\1)', expr)
            expr = re.sub(r'Math\.round\(\(float\)([^)]+)\)', r'round(\1)', expr)
            expr = expr.strip()

            # Add as a custom value
            if expr not in custom_values:
                custom_values.append(expr)
            idx = custom_values.index(expr)
            parts.append(f'{{value_{idx}}}')

        # Add string literal
        parts.append(str_match.group(1))
        last_end = str_match.end()

    # Combine all parts
    result = ''.join(parts)
    return result, custom_values

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

lambda_start = content.find('}, (player) -> {')
lambda_content = content[lambda_start:]
lines = lambda_content.split('\n')

for i, line in enumerate(lines[:30]):
    if 'lore.add(' in line:
        try:
            result, values = extract_lore_from_add_call(line)
            print(f"Line {i}: {line.strip()}")
            print(f"  Result: {repr(result)}")
            print(f"  Values: {values}")
            print()
        except Exception as e:
            print(f"Line {i}: ERROR - {e}")
            print(f"  Line: {repr(line)}")
            import traceback
            traceback.print_exc()
            print()
