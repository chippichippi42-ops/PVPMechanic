#!/usr/bin/env python3

filepath = '/home/engine/project/buttons/miscellaneous/AGF.java'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

lambda_start = content.find('}, (player) -> {')
lambda_content = content[lambda_start:]
lines = lambda_content.split('\n')

print("Line 3 (the one with math expressions):")
print(repr(lines[3]))
print()

# The line is split across multiple lines. Let's look at the actual lambda content around line 3
print("Lines 0-7 of lambda:")
for i in range(8):
    print(f"{i}: {lines[i]}")
