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

    # Find all string literals and expressions
    # This regex alternates between string literals and everything else
    pattern = r'"([^"]*)"|([^"]+)'
    for part_match in re.finditer(pattern, content):
        if part_match.group(1):  # String literal
            parts.append(part_match.group(1))
        elif part_match.group(2).strip():  # Expression (not just whitespace)
            expr = part_match.group(2).strip().rstrip('D')

            # Process mathematical expressions
            expr = re.sub(r'Math\.round\(\(float\)\(([^)]+)\)\)', r'round(\1)', expr)
            expr = re.sub(r'Math\.round\(\(float\)([^)]+)\)', r'round(\1)', expr)
            expr = expr.strip()

            # Add as a custom value
            if expr not in custom_values:
                custom_values.append(expr)
            idx = custom_values.index(expr)
            parts.append(f'{{value_{idx}}}')

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
    create_item_match = re.search(
        r'ConversionUtil\.createItem\(\s*Material\.(\w+)\s*,\s*level\s*,\s*"([^"]*)"\s*,\s*(?:(-?\d+)|ConversionUtil\.getDataValue\(DyeColor\.(\w+)\))\s*,\s*"([^"]*(?:&[0-9a-fk-or][^"]*)*)"\s*(?:\+\s*roman)?\s*,\s*lore\s*,\s*(\w+)\s*\)',
        content
    )

    if not create_item_match:
        return None

    material = create_item_match.group(1)
    texture = create_item_match.group(2)
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
        main_lore = []

        for i, line in enumerate(lines):
            if cost_section_start is not None and i >= cost_section_start:
                break

            # Track if (level <= 0) block
            if 'if (level <= 0)' in line:
                in_level_zero_block = True
                continue

            if in_level_zero_block and '}' in line:
                in_level_zero_block = False
                continue

            if in_level_zero_block:
                continue

            # Check if we're in a requirement block
            if 'getTalentLevel' in line and '< 10' in line:
                in_req_block = True
                continue

            if in_req_block and '}' in line:
                in_req_block = False
                continue

            if in_req_block:
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

        # Add main lore to final lore_lines
        lore_lines.extend(main_lore)

        # Extract requirements
        req_lines = []
        in_req_extraction = False
        for i, line in enumerate(lines):
            if 'getTalentLevel' in line and '< 10' in line:
                in_req_extraction = True
                continue
            if in_req_extraction and 'lore.add(' in line:
                result, _ = extract_lore_from_add_call(line)
                if result and result.strip() and 'Yêu cầu' not in result:
                    req_lines.append(result)
            if in_req_extraction and '}' in line:
                in_req_extraction = False

        # Add requirements section
        if req_lines:
            lore_lines.append('')
            lore_lines.append('&cYêu cầu:')
            for req in req_lines:
                lore_lines.append(req)

        # Add cost/upgrade info
        lore_lines.append('')
        lore_lines.append('&eBạn cần &d{cost} &eđá linh hồn để nâng cấp!')
        lore_lines.append('&bChuột phải để nâng cấp nhanh!')

        # Add {state} at the end
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
    lines.append("    name: '{displayName}{levelRoman}'")
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
    for java_file in java_files:
        try:
            data = parse_java_file(java_file)
            if data:
                yaml_content = generate_yaml(data)

                output_file = os.path.join(output_dir, f"{data['class_name']}.yml")

                with open(output_file, 'w', encoding='utf-8') as f:
                    f.write(yaml_content)

                print(f"✓ {os.path.basename(java_file):20} → {data['class_name']}.yml")
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
