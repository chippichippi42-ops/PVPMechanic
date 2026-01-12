#!/usr/bin/env python3
import re
import os
import glob

def extract_mathematical_expressions(text):
    """Extract mathematical expressions from lore text and replace with placeholders."""
    custom_values = []
    modified_text = text

    # First, simplify nested Math.round inside Utility.toDecimal
    modified_text = re.sub(
        r'Math\.round\(\(float\)\(([^)]+)\)\)',
        r'round(\1)',
        modified_text
    )
    modified_text = re.sub(
        r'Math\.round\(\(float\)([^)]+)\)',
        r'round(\1)',
        modified_text
    )

    # Then strip Utility.toDecimal((double)(...)) or Utility.toDecimal((double)...)
    # Handle the case with parentheses around the expression
    match = re.search(r'Utility\.toDecimal\(\(double\)\(([^)]+)\)\)', modified_text)
    if match:
        expr = match.group(1)
        expr = expr.strip()
        if expr.startswith('(') and expr.endswith(')'):
            inner = expr[1:-1]
            if '(' not in inner:
                expr = inner
        custom_values.append(expr)
        modified_text = modified_text.replace(match.group(0), '{value_0}', 1)
    else:
        # Handle case without extra parentheses
        matches = re.finditer(r'Utility\.toDecimal\(\(double\)([^)]+)\)', modified_text)
        for match in matches:
            expr = match.group(1).rstrip('D')
            custom_values.append(expr)
            modified_text = modified_text.replace(match.group(0), '{value_' + str(len(custom_values) - 1) + '}', 1)

    return modified_text, custom_values

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

        # Extract all lore.add() calls
        lines = lambda_content.split('\n')

        # Find where the cost/upgrade section starts
        cost_section_start = None
        for i, line in enumerate(lines):
            if 'if (req)' in line or 'if (level >= costlist.size())' in line:
                cost_section_start = i
                break

        # Extract lore before the cost section
        for i, line in enumerate(lines):
            if cost_section_start is not None and i >= cost_section_start:
                break

            # Skip level <= 0 block (default description)
            if 'if (level <= 0)' in line:
                # Skip until we exit this block
                j = i + 1
                depth = 1
                while j < len(lines) and depth > 0:
                    if '{' in lines[j]:
                        depth += 1
                    if '}' in lines[j]:
                        depth -= 1
                    j += 1
                continue

            # Extract lore.add() calls
            lore_match = re.search(r'lore\.add\("([^"]+)"\)', line)
            if lore_match:
                lore_text = lore_match.group(1)

                # Skip requirement lines - we'll add them separately
                if any(x in lore_text for x in ['Yêu cầu:', 'Yêu cầu']):
                    continue

                # Skip empty lines
                if not lore_text.strip():
                    continue

                # This is main description lore
                processed_line, extracted_values = extract_mathematical_expressions(lore_text)
                final_line = processed_line
                for val in extracted_values:
                    if val not in custom_values:
                        custom_values.append(val)
                    idx = custom_values.index(val)
                    final_line = final_line.replace('{value_' + str(extracted_values.index(val)) + '}',
                                                '{value_' + str(idx) + '}')
                lore_lines.append(final_line)

        # Extract requirements
        req_lines = []
        for line in lines:
            # Look for pattern: lore.add("&cTalentName X") inside requirement checks
            if 'getTalentLevel' in line and '< 10' in line:
                # This is a requirement check, find the lore.add inside
                j = lines.index(line) + 1
                while j < len(lines) and 'if (req)' not in lines[j]:
                    lore_match = re.search(r'lore\.add\("([^"]+)"\)', lines[j])
                    if lore_match:
                        req_text = lore_match.group(1)
                        if 'Yêu cầu:' not in req_text and req_text.strip():
                            req_lines.append(req_text)
                    if '}' in lines[j] and 'if' not in lines[j]:
                        break
                    j += 1

        # Add requirements section if exists
        if req_lines:
            lore_lines.append('')
            lore_lines.append("&cYêu cầu:")
            for req in req_lines:
                lore_lines.append(req)

        # Add cost/upgrade information
        lore_lines.append('')
        lore_lines.append("&eBạn cần &d{cost} &eđá linh hồn để nâng cấp!")
        lore_lines.append("&bChuột phải để nâng cấp nhanh!")

        # Always add {state} at the end
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
