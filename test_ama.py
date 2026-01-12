#!/usr/bin/env python3
import re

filepath = '/home/engine/project/buttons/melee/AMA.java'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Try the current regex pattern
pattern = r'ConversionUtil\.createItem\(\s*Material\.(\w+)\s*,\s*level\s*,\s*"([^"]*)"\s*,\s*(?:(-?\d+)|ConversionUtil\.getDataValue\(DyeColor\.(\w+)\))\s*,\s*"([^"]*(?:&[0-9a-fk-or][^"]*)*)"\s*(?:\+\s*roman)?\s*,\s*lore\s*,\s*(\w+)\s*\)'
match = re.search(pattern, content)

if match:
    print("Pattern matched!")
    print(f"Groups: {match.groups()}")
else:
    print("Pattern did NOT match")
    print("Looking for ConversionUtil.createItem calls...")
    matches = re.finditer(r'ConversionUtil\.createItem\([^;]+\);', content)
    for m in matches:
        print(f"Found: {m.group(0)}")
        print()
