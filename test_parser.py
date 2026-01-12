#!/usr/bin/env python3
import re

# Test with AGF.java
filepath = '/home/engine/project/buttons/miscellaneous/AGF.java'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

lambda_start = content.find('}, (player) -> {')
if lambda_start != -1:
    lambda_content = content[lambda_start:]
    lines = lambda_content.split('\n')

    print("First 50 lines of lambda:")
    for i, line in enumerate(lines[:50]):
        print(f"{i:3}: {line}")
