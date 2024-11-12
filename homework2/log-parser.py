import re

def convert_milliseconds(ms):
    seconds = (ms // 1000) % 60
    minutes = (ms // (1000 * 60)) % 60
    milliseconds = ms % 1000
    return f"{minutes} min, {seconds} sec, {milliseconds} ms"

query_pattern = re.compile(r"Query: (.+)")
title_pattern = re.compile(r"Title: (.+)")
total_matches_pattern = re.compile(r"Total Matches: (\d+)")
average_score_pattern = re.compile(r"Average Score: ([\d.]+)")
score_variance_pattern = re.compile(r"Score Variance: ([\d.]+)")
score_decay_pattern = re.compile(r"Score Decay: ([\d.]+)")
individual_score_pattern = re.compile(r"Score: ([\d.]+) on Field: (\w+)")
analyzer_pattern = re.compile(r"Field '(\w+)', Analyzer '([\w\.]+)'")
file_index_time_pattern = re.compile(r"Indexed file: .+? \(Total Indexed: \d+\), File Time: (\d+)ms")
empty_field_pattern = re.compile(r"Files with empty (\w+): \[.*\] \((\d+) files, ([\d.]+)% of total files\)")

analyzers = {}
queries = []
titles = []
numeric_results = []
file_times = []
missing_percentages = {}

log_file_path = 'lucenehw/logs/spring.log'  

with open(log_file_path, 'r') as file:
    for line in file:
        analyzer_match = analyzer_pattern.search(line)
        if analyzer_match:
            field, analyzer = analyzer_match.groups()
            analyzers[field] = analyzer

        file_index_time_match = file_index_time_pattern.search(line)
        if file_index_time_match:
            file_time = int(file_index_time_match.group(1))
            file_times.append(file_time)

        missing_percentage_match = empty_field_pattern.search(line)
        if missing_percentage_match:
            field, count, percentage = missing_percentage_match.groups()
            missing_percentages[field] = (int(count), float(percentage))

        query_match = query_pattern.search(line)
        if query_match:
            queries.append(query_match.group(1))
        
        title_match = title_pattern.search(line)
        if title_match:
            titles.append(title_match.group(1))

        total_matches_match = total_matches_pattern.search(line)
        if total_matches_match:
            total_matches = int(total_matches_match.group(1))
            numeric_results.append({'Total Matches': total_matches})

        average_score_match = average_score_pattern.search(line)
        if average_score_match:
            average_score = float(average_score_match.group(1))
            numeric_results[-1]['Average Score'] = average_score

        score_variance_match = score_variance_pattern.search(line)
        if score_variance_match:
            score_variance = float(score_variance_match.group(1))
            numeric_results[-1]['Score Variance'] = score_variance

        score_decay_match = score_decay_pattern.search(line)
        if score_decay_match:
            score_decay = float(score_decay_match.group(1))
            numeric_results[-1]['Score Decay'] = score_decay

        individual_score_match = individual_score_pattern.search(line)
        if individual_score_match:
            score, field = individual_score_match.groups()
            numeric_results[-1].setdefault('Individual Scores', []).append((float(score), field))


average_index_time = sum(file_times) / len(file_times) if file_times else 0
total_index_time = sum(file_times)

print("Analyzers Used:")
for field, analyzer in analyzers.items():
    print(f"  {field}: {analyzer}")

print(f"\nAverage Index Time per File: {average_index_time:.2f} ms")
print(f"Total Index Time: {convert_milliseconds(total_index_time)}")

# Print missing fields information
print("\nMissing Fields Summary:")
for field, (count, percentage) in missing_percentages.items():
    print(f"  {field.capitalize()} - Missing Count: {count} ({percentage:.2f}%)")

print("\nQueries Executed:", queries)
print("\nNumeric Results of Queries:", numeric_results)
