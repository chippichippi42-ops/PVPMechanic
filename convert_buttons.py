#!/usr/bin/env python3
import re
import os
import glob
from pathlib import Path

def extract_mathematical_expressions(text):
    """Extract mathematical expressions from lore text and replace with placeholders."""
    custom_values = []
    modified_text = text
    patterns = [
        r'Utility\.toDecimal\(\(double\)\(([^)]+)\)\)',
        r'Utility\.toDecimal\(\(double\)([^)]+)\)',
        r'Math\.round\(\(float\)([^)]+)\)',
        r'Math\.round\(([^)]+)\)',
    ]

    # First, simplify nested Math.round inside Utility.toDecimal
    # Utility.toDecimal((double)(1 + Math.round((float)(level / 10))))
    # → (1 + round(level / 10))
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
    # First handle the case with parentheses around the expression
    match = re.search(r'Utility\.toDecimal\(\(double\)\(([^)]+)\)\)', modified_text)
    if match:
        expr = match.group(1)
        # Simplify if it's a simple multiplication like (level * 2) → level * 2
        expr = expr.strip()
        if expr.startswith('(') and expr.endswith(')'):
            # Check if the whole expression is wrapped in parentheses
            inner = expr[1:-1]
            # Only unwrap if it's a simple expression (no nested parentheses)
            if '(' not in inner:
                expr = inner
        custom_values.append(expr)
        modified_text = modified_text.replace(match.group(0), '{value_0}', 1)
    else:
        # Handle case without extra parentheses: Utility.toDecimal((double)level * 2.5D)
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
    # Pattern: ConversionUtil.createItem(Material.XXX, level, "" or texture, [number] or ConversionUtil.getDataValue(...), "&ename" + roman, lore, enchanted)
    # Note: " + roman is OUTSIDE the closing quote
    # Handle both literal numbers, ConversionUtil.getDataValue() calls, and skull textures
    create_item_match = re.search(
        r'ConversionUtil\.createItem\(\s*Material\.(\w+)\s*,\s*level\s*,\s*"([^"]*)"\s*,\s*(?:(-?\d+)|ConversionUtil\.getDataValue\(DyeColor\.(\w+)\))\s*,\s*"([^"]*(?:&[0-9a-fk-or][^"]*)*)"\s*(?:\+\s*roman)?\s*,\s*lore\s*,\s*(\w+)\s*\)',
        content
    )

    if not create_item_match:
        return None

    material = create_item_match.group(1)
    texture = create_item_match.group(2)  # skull texture (base64) or empty string
    # model_data is either group(3) (number) or group(4) (DyeColor name)
    model_data = create_item_match.group(3) if create_item_match.group(3) else create_item_match.group(4)
    display_name_raw = create_item_match.group(5)
    enchanted = create_item_match.group(6)

    # Remove color codes from display name
    display_name = re.sub(r'&[0-9a-fk-or]', '', display_name_raw)

    # Extract lore lines
    custom_values = []
    lore_lines = []

    # Find the lambda content for extracting lore
    lambda_start = content.find('}, (player) -> {')
    if lambda_start != -1:
        lambda_content = content[lambda_start:]

        # Parse the lambda content more carefully
        # We need to extract lore in the correct order and handle conditionals
        lines = lambda_content.split('\n')

        # Track our position in the code
        in_lore_creation = False
        in_level_zero_block = False
        in_req_block = False
        in_cost_block = False
        in_else_block = False
        level_zero_lore = []
        main_lore = []
        req_lore = []
        cost_lore = []
        else_lore = []

        i = 0
        while i < len(lines):
            line = lines[i]

            # Detect level <= 0 block
            if 'if (level <= 0)' in line:
                in_level_zero_block = True
                i += 1
                continue

            # End of level <= 0 block
            if in_level_zero_block and '}' in line and 'if' not in line:
                # Check if this closes the level <= 0 block
                # Look for the next non-whitespace, non-comment line
                in_level_zero_block = False
                i += 1
                continue

            # Detect requirement blocks (checking talent levels)
            if 'if (Talents.getInstance().getDatabase().getTalentLevel' in line and 'level >=' not in line:
                in_req_block = True
                i += 1
                continue

            # End of req block
            if in_req_block and '}' in line and 'if' not in line and 'else' not in line:
                in_req_block = False
                i += 1
                continue

            # Detect cost/max check block (if (req) { ... } else { ... })
            if 'if (req)' in line or 'if (level >= costlist.size())' in line:
                in_cost_block = True
                i += 1
                continue

            # Detect else block
            if in_cost_block and 'else' in line and '}' not in line:
                in_else_block = True
                i += 1
                continue

            # End of cost block
            if in_cost_block and not in_else_block and '}' in line:
                in_cost_block = False
                i += 1
                continue

            # End of else block
            if in_else_block and '}' in line:
                in_cost_block = False
                in_else_block = False
                i += 1
                continue

            # Extract lore.add() calls
            lore_match = re.search(r'lore\.add\("([^"]+)"\)', line)
            if lore_match:
                lore_text = lore_match.group(1)

                if in_level_zero_block:
                    level_zero_lore.append(lore_text)
                elif in_req_block:
                    req_lore.append(lore_text)
                elif in_else_block:
                    else_lore.append(lore_text)
                elif not in_cost_block:
                    # Main description lore (before cost check)
                    # Skip empty lines and separators for now
                    if lore_text.strip():
                        main_lore.append(lore_text)

            i += 1

        # Process main lore with mathematical expressions
        for lore_text in main_lore:
            processed_line, extracted_values = extract_mathematical_expressions(lore_text)
            final_line = processed_line
            for val in extracted_values:
                if val not in custom_values:
                    custom_values.append(val)
                idx = custom_values.index(val)
                final_line = final_line.replace('{value_' + str(extracted_values.index(val)) + '}',
                                            '{value_' + str(idx) + '}')
            lore_lines.append(final_line)

        # Add requirements section if exists
        if req_lore:
            lore_lines.append('')
            lore_lines.append("&cYêu cầu:")
            for req in req_lore:
                if req.strip() and 'Yêu cầu' not in req:
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

    # Class name as top-level key
    lines.append(f"{data['class_name']}:")
    lines.append(f"  display-name: {data['display_name']}")
    lines.append("  location:")
    lines.append(f"    x: {data['x']}")
    lines.append(f"    y: {data['y']}")
    lines.append("  custom_values:")

    # Add custom values
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
    # Add texture if present (for skull items)
    if data.get('texture'):
        lines.append(f"    texture: '{data['texture']}'")
    lines.append("    lore:")

    # Add lore lines
    for lore_line in data['lore']:
        lines.append(f"    - '{lore_line}'")

    return '\n'.join(lines)

def main():
    # Find all Java files in buttons directory
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

                # Output filename: ClassName.yml
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
