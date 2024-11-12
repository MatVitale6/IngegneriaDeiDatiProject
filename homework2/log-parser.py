import re

def convert_milliseconds(ms):
    seconds = (ms // 1000) % 60
    minutes = (ms // (1000 * 60)) % 60
    milliseconds = ms % 1000
    return f"{minutes} min, {seconds} sec, {milliseconds} ms"

# Define patterns to capture specific log data
analyzer_pattern = re.compile(r"Field '(\w+)', Analyzer '([\w\.]+)'")
file_index_time_pattern = re.compile(r"Indexed file: .+? \(Total Indexed: \d+\), File Time: (\d+)ms")
missing_percentage_pattern = re.compile(r"Files with empty (\w+): (\w+) \((\d+) files, ([\d.]+)% of total files\)")

# Initialize counters and storage variables
analyzers = {}
file_times = []
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

        # Capture the percentage of missing fields with the updated log format
        missing_percentage_match = missing_percentage_pattern.search(line)
        if missing_percentage_match:
            field, _, count, percentage = missing_percentage_match.groups()
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
for field, (count, percentage) in missing_percentages.items():
    print(f"  {field.capitalize()} - Missing Count: {count} ({percentage:.2f}%)")
