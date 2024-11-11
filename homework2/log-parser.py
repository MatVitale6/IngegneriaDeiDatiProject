import re

def convert_milliseconds(ms):
    seconds = (ms // 1000) % 60
    minutes = (ms // (1000 * 60)) % 60
    milliseconds = ms % 1000
    return f"{minutes} min, {seconds} sec, {milliseconds} ms"

# Define patterns to capture specific log data
analyzer_pattern = re.compile(r"Field '(\w+)', Analyzer '([\w\.]+)'")
file_index_time_pattern = re.compile(r"Indexed file: .+? \(Total Indexed: \d+\), File Time: (\d+)ms")
missing_title_pattern = re.compile(r"Extracted empty title at (.+)")
missing_abstract_pattern = re.compile(r"Abstract not found in (.+)")
missing_authors_pattern = re.compile(r"Authors not found in (.+)")
missing_percentage_pattern = re.compile(r"Files with empty (title|abstract|authors) size: (\d+), which is ([\d.]+)% of total files")

# Initialize counters and storage variables
analyzers = {}
file_times = []
missing_fields = {
    "title": [],
    "abstract": [],
    "authors": []
}
missing_percentages = {}

# Path to the log file
log_file_path = 'lucenehw/logs/spring.log'  # Replace with your actual path

# Process log file line by line
with open(log_file_path, 'r') as file:
    for line in file:
        # Match and store analyzer configuration
        analyzer_match = analyzer_pattern.search(line)
        if analyzer_match:
            field, analyzer = analyzer_match.groups()
            analyzers[field] = analyzer

        # Match and store file indexing time
        file_index_time_match = file_index_time_pattern.search(line)
        if file_index_time_match:
            file_time = int(file_index_time_match.group(1))
            file_times.append(file_time)

        # Count entries for missing fields and store filenames
        title_match = missing_title_pattern.search(line)
        if title_match:
            missing_fields["title"].append(title_match.group(1))

        abstract_match = missing_abstract_pattern.search(line)
        if abstract_match:
            missing_fields["abstract"].append(abstract_match.group(1))

        authors_match = missing_authors_pattern.search(line)
        if authors_match:
            missing_fields["authors"].append(authors_match.group(1))

        # Capture the percentage of missing fields
        missing_percentage_match = missing_percentage_pattern.search(line)
        if missing_percentage_match:
            field, count, percentage = missing_percentage_match.groups()
            missing_percentages[field] = (int(count), float(percentage))

# Calculate average file index time and total index time
average_index_time = sum(file_times) / len(file_times) if file_times else 0
total_index_time = sum(file_times)

# Print extracted information
print("Analyzers Used:")
for field, analyzer in analyzers.items():
    print(f"  {field}: {analyzer}")

print(f"\nAverage Index Time per File: {average_index_time:.2f} ms")
print(f"Total Index Time: {convert_milliseconds(total_index_time)}")

# Print missing fields information
print("\nMissing Fields Summary:")
for field, files in missing_fields.items():
    count = len(files)
    percentage_info = missing_percentages.get(field, (count, 0.0))
    print(f"  {field.capitalize()} - Missing Count: {percentage_info[0]} ({percentage_info[1]:.2f}%)")
    print("    Files:", ", ".join(files) if files else "None")
