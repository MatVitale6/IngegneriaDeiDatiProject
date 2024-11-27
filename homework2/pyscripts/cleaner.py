import os
import re
import json

# Constants for regex patterns
PATTERN_VALID_KEY = r"^(S|A|Ch)[a-zA-Z0-9]*\d+\.T[a-zA-Z0-9]*\d+(\..*)?$|^id_table_\d+$"
PATTERN_TABLE_ID = r'id="(S|A|Ch)[a-zA-Z0-9]*\d+\.T[a-zA-Z0-9]*\d+(\..*)?"'
PATTERN_HTML_TABLE = r"<table"

# Global counters
number_of_keys_removed = 0
processed_files_count = 0
total_tables_count = 0

# Debug flag
DEBUG = True

def debug_log(message: str):
    """
    Print debug messages if DEBUG flag is enabled.
    """
    if DEBUG:
        print(f"[DEBUG] {message}")

def delete_empty_json(input_path: str) -> bool:
    """
    Delete the file if it is empty or only contains the "INFO" key.
    """
    try:
        with open(input_path, 'r') as infile:
            data = json.load(infile)
        if isinstance(data, dict) and (not data or set(data.keys()) == {"INFO"}):
            os.remove(input_path)
            debug_log(f"Deleted empty file: {input_path}")
            return True
    except (json.JSONDecodeError, FileNotFoundError) as e:
        debug_log(f"Error handling file {input_path}: {e}")
    return False

def fix_json(input_path: str, output_path: str):
    """
    Attempt to fix improperly formatted JSON by replacing '}{' with ','.
    """
    try:
        with open(input_path, 'r') as file:
            content = file.read()
        fixed_content = content.replace('}{', ',')
        with open(output_path, 'w') as file:
            file.write(fixed_content)
        debug_log(f"Fixed JSON saved to: {output_path}")
    except Exception as e:
        debug_log(f"Unexpected error fixing JSON {input_path}: {e}")



def is_valid_key(key: str) -> bool:
    """
    Check if the key matches the valid key pattern.
    """
    return bool(re.match(PATTERN_VALID_KEY, key))

def is_invalid_table_content(content) -> bool:
    """
    Check if the table content is invalid (null, empty string, or not an HTML table).
    """
    if content is None or content == "" or (isinstance(content, list) and not content):
        return True
    if isinstance(content, str) and not content.startswith(PATTERN_HTML_TABLE) and not re.search(PATTERN_TABLE_ID, content):
        return True
    return False

def process_nested_list(element) -> bool:
    """
    Recursively check if any element in a nested list contains invalid content.
    """
    if isinstance(element, str):
        return is_invalid_table_content(element)
    elif isinstance(element, list):
        for sub_element in element:
            if process_nested_list(sub_element):
                return True
    return False

def clean_json(data: dict, filename: str) -> int:
    """
    Clean the JSON object by removing invalid keys and tables.
    """
    global number_of_keys_removed, total_tables_count
    keys_to_remove = []
    total_tables_count += len(data.keys())  # Track total keys (tables) in the file

    debug_log(f"Processing file: {filename}, Initial keys: {list(data.keys())}")

    for key in list(data.keys()):
        # Remove invalid top-level keys
        if not is_valid_key(key):
            debug_log(f"Marking key for removal (invalid key): {key}")
            keys_to_remove.append(key)
            continue

        # Check if the key's table content is invalid
        table_content = data[key].get("table", None)
        if isinstance(table_content, str):
            if not table_content.startswith(PATTERN_HTML_TABLE) or not re.search(PATTERN_TABLE_ID, table_content):
                debug_log(table_content.startswith(PATTERN_HTML_TABLE))
                debug_log(f"Marking key for removal (table does not start with '<table' or lacks ID match): {key}")
                keys_to_remove.append(key)
                continue

        if isinstance(table_content, list):
            if not table_content or process_nested_list(table_content):
                debug_log(f"Marking key for removal (invalid nested list content): {key}")
                keys_to_remove.append(key)
                continue

        elif is_invalid_table_content(table_content):
            debug_log(f"Marking key for removal (invalid table content): {key}")
            keys_to_remove.append(key)
            continue

    # Remove identified keys
    for key in keys_to_remove:
        debug_log(f"Removing key: {key}")
        data.pop(key, None)

    number_of_keys_removed += len(keys_to_remove)
    debug_log(f"File processed: {filename}, Keys removed: {len(keys_to_remove)}, Remaining keys: {list(data.keys())}")
    return len(keys_to_remove)

def process_file(input_file: str, output_file: str) -> bool:
    """
    Process a single file: clean its JSON content and save the result if not empty.
    """
    try:
        debug_log(f"Reading file: {input_file}")
        with open(input_file, 'r') as infile:
            data = json.load(infile)

        keys_removed = clean_json(data, input_file)

        # If no keys remain, do not save the file
        if not data:
            debug_log(f"File is empty after cleaning, deleting: {input_file}")
            os.remove(input_file)
            return False

        # Save cleaned data if not empty
        debug_log(f"Saving cleaned file: {output_file}")
        with open(output_file, 'w') as outfile:
            json.dump(data, outfile, indent=2)

        return keys_removed > 0
    except (json.JSONDecodeError, FileNotFoundError) as e:
        debug_log(f"Error processing file {input_file}: {e}")
        return False

def main(input_dir: str, output_dir: str):
    """
    Main function to process all files in the directory.
    """
    global processed_files_count

    all_files = os.listdir(input_dir)

    for filename in all_files:
        input_file = os.path.join(input_dir, filename)
        try:
            with open(input_file, 'r') as i:
                json.load(i)
        except json.JSONDecodeError as e:
            debug_log(f"Fixing broken JSON file: {filename}")
            fix_json(input_file, input_file)

    all_files = [f for f in all_files if not delete_empty_json(os.path.join(input_dir, f))]

    for filename in all_files:
        input_file = os.path.join(input_dir, filename)
        output_file = os.path.join(output_dir, filename)

        if process_file(input_file, output_file):
            processed_files_count += 1

    # Final summary
    total_files = len(all_files)
    print(f"{processed_files_count} files were cleaned, which is {processed_files_count / total_files * 100:.2f}% of total files")
    print(f"{number_of_keys_removed} 'tables' were removed, which is {number_of_keys_removed / total_tables_count * 100:.2f}% of total tables")

# Example usage
if __name__ == "__main__":
    JSON_DIR = "../lucenehw/urls_htmls_tables/all_tables"
    main(JSON_DIR, JSON_DIR)
