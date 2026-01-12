#!/usr/bin/env python3
import re
import os
import glob

def extract_lore_from_add_call(lore_line_raw):
    """Extract and process lore from a lore.add() call, handling string concatenations."""
    # Pattern to match: lore.add("..." + ... + "..." + ...)
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
            # Strip + operators from both sides
            expr = between.strip().strip('+').strip()

            # Process mathematical expressions
            # Strip Utility.toDecimal - handle nested parentheses properly
            # Start by removing the outermost Utility.toDecimal wrapper
            if expr.startswith('Utility.toDecimal(') and expr.endswith(')'):
                # Extract content between first ( and last )
                expr = expr[18:-1]  # Remove 'Utility.toDecimal(' and ')'
            
            # Strip D suffix from numbers like 2.5D -> 2.5 or 10.0D -> 10.0 (BEFORE removing type casts)
            expr = re.sub(r'(\d+\.?\d*)\s*D\b', r'\1', expr)
            
            # First, strip Math.round (must come after D suffix and Utility.toDecimal removal)
            expr = re.sub(r'Math\.round\(\(float\)\(([^)]+)\)\)', r'round(\1)', expr)
            expr = re.sub(r'Math\.round\(\(float\)([^)]+)\)', r'round(\1)', expr)

            # Strip type casts (with or without space)
            expr = re.sub(r'\s*\(double\)\s*', ' ', expr)
            expr = re.sub(r'\s*\(float\)\s*', ' ', expr)

            # Clean up multiple spaces
            expr = re.sub(r'\s+', ' ', expr).strip()

            # Clean up any leftover parentheses around simple expressions
            if expr.startswith('(') and expr.endswith(')'):
                inner = expr[1:-1]
                if '(' not in inner:  # No nested parentheses
                    expr = inner

            expr = expr.strip()

            # Add as a custom value
            if expr not in custom_values:
                custom_values.append(expr)
            idx = custom_values.index(expr)
            parts.append(f'{{value_{idx}}}')

        # Add the string literal
        parts.append(str_match.group(1))
        last_end = str_match.end()

    # Combine all parts
    result = ''.join(parts)
    return result, custom_values

def parse_java_file(filepath):
    """Parse a Java button file and extract relevant information."""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract class name
    class_match = re.search(r'public class (\w+)', content)
    if not class_match:
        return None
    class_name = class_match.group(1)

    # Extract x and y coordinates
    x_match = re.search(r'private\s+int\s+x\s*=\s*(-?\d+)', content)
    y_match = re.search(r'private\s+int\s+y\s*=\s*(-?\d+)', content)

    x = int(x_match.group(1)) if x_match else 0
    y = int(y_match.group(1)) if y_match else 0

    # Extract ConversionUtil.createItem call
    # Handle both: Material.XXX, level, "", number, "name" + roman, lore, enchanted
    # And: Material.XXX, level, "", ConversionUtil.getDataValue(DyeColor.XXX), "name" + roman, lore, enchanted
    create_item_match = re.search(
        r'ConversionUtil\.createItem\(\s*Material\.(\w+)\s*,\s*level\s*,\s*"([^"]*)"\s*,\s*(?:(-?\d+)|ConversionUtil\.getDataValue\(DyeColor\.(\w+)\))\s*,\s*"([^"]*(?:&[0-9a-fk-or][^"]*)*)"\s*(?:\+\s*roman)?\s*,\s*lore\s*,\s*(\w+)\s*\)',
        content
    )

    if not create_item_match:
        return None

    material = create_item_match.group(1)
    texture = create_item_match.group(2)
    # model_data is either group(3) (number) or group(4) (DyeColor)
    model_data = create_item_match.group(3) if create_item_match.group(3) else create_item_match.group(4)
    display_name_raw = create_item_match.group(5)
    enchanted = create_item_match.group(6)

    # Remove color codes from display name
    display_name = re.sub(r'&[0-9a-fk-or]', '', display_name_raw)

    # Extract lore lines
    custom_values = []
    lore_lines = []

    lambda_start = content.find('}, (player) -> {')
    if lambda_start != -1:
        lambda_content = content[lambda_start:]
        lines = lambda_content.split('\n')

        # Find cost section start
        cost_section_start = None
        for i, line in enumerate(lines):
            if 'if (req)' in line or 'if (level >= costlist.size())' in line:
                cost_section_start = i
                break

        # Extract lore before cost section
        in_level_zero_block = False
        in_req_block = False
        level_zero_brace_count = 0
        req_brace_count = 0
        main_lore = []

        for i, line in enumerate(lines):
            if cost_section_start is not None and i >= cost_section_start:
                break

            # Track if (level <= 0) block
            if 'if (level <= 0)' in line:
                in_level_zero_block = True
                level_zero_brace_count = 0
                continue

            if in_level_zero_block:
                if '{' in line:
                    level_zero_brace_count += line.count('{')
                if '}' in line:
                    level_zero_brace_count -= line.count('}')
                    if level_zero_brace_count <= 0:
                        in_level_zero_block = False
                continue

            # Check if we're in a requirement block (getTalentLevel check with < 10, < 15, etc.)
            if 'getTalentLevel' in line and '<' in line and 'if' in line:
                # Check if it's a requirement check (contains a number after <)
                if re.search(r'<\s*\d+', line):
                    in_req_block = True
                    req_brace_count = line.count('{')  # Count opening braces on the same line
                    continue

            if in_req_block:
                if '{' in line:
                    req_brace_count += line.count('{')
                if '}' in line:
                    req_brace_count -= line.count('}')
                    if req_brace_count <= 0:
                        in_req_block = False
                continue

            # Extract lore.add() calls
            if 'lore.add(' in line:
                result, values = extract_lore_from_add_call(line)
                if result is not None:
                    # Skip requirement headers
                    if 'Yêu cầu' in result or 'Yêu cầu:' in result:
                        continue
                    # Skip empty lines
                    if not result.strip():
                        continue
                    # Track all custom values
                    for val in values:
                        if val not in custom_values:
                            custom_values.append(val)
                    # Map values to global indices
                    final_result = result
                    for idx, val in enumerate(values):
                        final_result = final_result.replace(f'{{value_{idx}}}', f'{{value_{custom_values.index(val)}}}')
                    main_lore.append(final_result)

        # Add main lore to final lore_lines (only core description)
        lore_lines.extend(main_lore)

        # Add {state} at end to handle all conditional logic
        lore_lines.append('{state}')

    return {
        'class_name': class_name,
        'x': x,
        'y': y,
        'material': material,
        'model_data': model_data,
        'texture': texture,
        'display_name': display_name,
        'display_name_raw': display_name_raw,
        'enchanted': enchanted == 'true',
        'lore': lore_lines,
        'custom_values': custom_values
    }

def generate_yaml(data):
    """Generate YAML content from parsed data."""
    lines = []

    lines.append(f"{data['class_name']}:")
    lines.append(f"  display-name: {data['display_name']}")
    lines.append("  location:")
    lines.append(f"    x: {data['x']}")
    lines.append(f"    y: {data['y']}")
    lines.append("  custom_values:")

    for val in data['custom_values']:
        lines.append(f"  - '{val}'")

    lines.append("  item:")
    lines.append(f"    material: {data['material']}")
    lines.append("    name: '{color}{displayName}{levelRoman}'")
    lines.append("    flags:")
    lines.append("    - HIDE_ATTRIBUTES")
    lines.append("    - HIDE_ENCHANTS")
    lines.append("    - HIDE_UNBREAKABLE")
    lines.append("    unbreakable: true")
    if data.get('texture'):
        lines.append(f"    texture: '{data['texture']}'")
    lines.append("    lore:")

    for lore_line in data['lore']:
        lines.append(f"    - '{lore_line}'")

    return '\n'.join(lines)

def main():
    java_files = glob.glob('/home/engine/project/buttons/**/*.java', recursive=True)

    output_dir = '/home/engine/project/buttons_yaml'
    os.makedirs(output_dir, exist_ok=True)

    print(f"Found {len(java_files)} Java files to convert\n")

    success_count = 0
    class_name_counts = {}
    
    for java_file in java_files:
        try:
            data = parse_java_file(java_file)
            if data:
                yaml_content = generate_yaml(data)
                
                class_name = data['class_name']
                
                # Handle duplicate class names by adding category prefix
                if class_name in class_name_counts:
                    # Extract category from path
                    path_parts = java_file.split(os.sep)
                    if 'buttons' in path_parts:
                        category_idx = path_parts.index('buttons') + 1
                        if category_idx < len(path_parts):
                            category = path_parts[category_idx]
                            output_filename = f"{category}_{class_name}.yml"
                        else:
                            output_filename = f"{class_name}_{class_name_counts[class_name]}.yml"
                    else:
                        output_filename = f"{class_name}_{class_name_counts[class_name]}.yml"
                    class_name_counts[class_name] += 1
                else:
                    output_filename = f"{class_name}.yml"
                    class_name_counts[class_name] = 1

                output_file = os.path.join(output_dir, output_filename)

                with open(output_file, 'w', encoding='utf-8') as f:
                    f.write(yaml_content)

                print(f"✓ {os.path.basename(java_file):20} → {output_filename}")
                success_count += 1
            else:
                print(f"✗ {os.path.basename(java_file):20} → Failed to parse")
        except Exception as e:
            print(f"✗ {os.path.basename(java_file):20} → Error: {e}")
            import traceback
            traceback.print_exc()

    print(f"\nConversion complete! {success_count}/{len(java_files)} files converted")
    print(f"YAML files saved to: {output_dir}")

if __name__ == '__main__':
    main()
