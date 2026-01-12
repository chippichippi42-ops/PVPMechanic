#!/usr/bin/env python3
import re

test_expr = "10.0D + (double)level * 0.5D"
print(f"Original: {test_expr}")

# Strip all Utility.toDecimal without nested parentheses
test_expr = re.sub(r'Utility\.toDecimal\(([^)]+)\)', r'\1', test_expr)
print(f"After Utility.toDecimal: {test_expr}")

# Strip type casts
test_expr = re.sub(r'\(double\)', '', test_expr)
print(f"After (double): {test_expr}")

test_expr = re.sub(r'\(float\)', '', test_expr)
print(f"After (float): {test_expr}")

# Strip D suffix
test_expr = re.sub(r'(\d+\.?\d*)D', r'\1', test_expr)
print(f"After D suffix: {test_expr}")
