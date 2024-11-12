import json
import matplotlib.pyplot as plt
import numpy as np

FILE_PATH = 'lucenehw/src/test/resources/results/query_results2.json'
FIGURES_DIR = 'figures/'

def print_query_results(file_path):
    with open(file_path, 'r') as file:
        data = json.load(file)

    for query_data in data:
        query = query_data.get('query', 'N/A')
        execution_time = query_data.get('executionTime', 'N/A')
        total_matches = query_data.get('totalMatches', 'N/A')
        avg_score = query_data.get('averageScore', 'N/A')
        score_variance = query_data.get('scoreVariance', 'N/A')
        score_decay = query_data.get('scoreDecay', 'N/A')

        print(f"Query: {query}")
        print(f"Execution Time: {execution_time} ms")
        print(f"Total Matches: {total_matches}")
        print(f"Average Score: {avg_score}")
        print(f"Score Variance: {score_variance}")
        print(f"Score Decay: {score_decay}")
        print("\nRetrieved Results:")

        # for result in query_data.get('results', []):
        #     title = result.get('title', 'No title')
        #     author = result.get('author', 'No author')
        #     score = result.get('score', 'No score')
        #     match_field = result.get('matchField', 'No field')
        #     link = result.get('link', 'No link')
            
        #     print(f"  Title: {title}")
        #     print(f"  Author: {author}")
        #     print(f"  Score: {score}")
        #     print(f"  Match Field: {match_field}")
        #     print(f"  Link: {link}")
        #     print("-" * 40)
        print("=" * 80)

#print_query_results(FILE_PATH)

def extract_query_data(file_path):
    with open(file_path, 'r') as file:
        data = json.load(file)
    
    # Initialize lists for each metric
    execution_times = []
    avg_scores = []
    score_variances = []
    score_decays = []
    queries = []  # To label the queries

    # Collect metrics for each query
    for i, query_data in enumerate(data, start=1):
        queries.append(f"{i}")
        execution_times.append(query_data.get('executionTime', 0))
        avg_scores.append(query_data.get('averageScore', 0))
        score_variances.append(query_data.get('scoreVariance', 0))
        score_decays.append(query_data.get('scoreDecay', 0))

    return queries, execution_times, avg_scores, score_variances, score_decays

def plot_metrics(queries, avg_scores, score_variances, score_decays):
    x = np.arange(len(queries))
    width = 0.2

    fig, ax = plt.subplots(figsize=(12, 6))

    ax.bar(x - width/2, avg_scores, width, label='Average Score')
    ax.bar(x + width/2, score_variances, width, label='Score Variance')
    ax.bar(x + width*1.5, score_decays, width, label='Score Decay')

    ax.set_xlabel("Queries")
    ax.set_ylabel("Score Points")
    ax.set_title("Score Points per ogni Query")
    ax.set_xticks(x)
    ax.set_xticklabels(queries, rotation=45)
    ax.legend()

    plt.tight_layout()
    plt.savefig(FIGURES_DIR + 'plot1.png')

# Extract data and plot
queries, execution_times, avg_scores, score_variances, score_decays = extract_query_data(FILE_PATH)
plot_metrics(queries, avg_scores, score_variances, score_decays)
