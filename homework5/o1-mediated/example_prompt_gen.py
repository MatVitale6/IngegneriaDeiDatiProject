import os
import json
import re
import pandas as pd

def normalize_strings(value):
    if isinstance(value, str):
        value = re.sub(r'\s+', ' ', value.strip())
        value = value.replace('\\n', '').strip()
    return value

def process_datasets(input_folder, output_file):
    # Dictionario per memorizzare i risultati
    data_summary = {}

    # Itera su tutti i file nella cartella
    for file_name in os.listdir(input_folder):
        file_path = os.path.join(input_folder, file_name)
        if file_path.endswith('.csv'):
            try:
                df = pd.read_csv(file_path, encoding='utf-8')
            except UnicodeDecodeError:
                df = pd.read_csv(file_path, encoding='ISO-8859-1')
        elif file_path.endswith('.json'):
            with open(file_path, 'r') as f:
                data = json.load(f)
            if isinstance(data, list):
                df = pd.json_normalize(data)
            else:
                df = pd.json_normalize([data])
        elif file_path.endswith('.xls'):
            df = pd.read_excel(file_path)
        elif file_path.endswith('.jsonl'):
            df = pd.read_json(file_path, lines=True)

        sampled_rows = df.sample(n=min(5, len(df)), random_state=42)
        sampled_rows = sampled_rows.map(normalize_strings)
        sampled_rows = sampled_rows.replace({pd.NA: None, pd.NaT: None})
        dataset_name = os.path.splitext(file_name)[0]
        data_summary[dataset_name] = sampled_rows.to_dict(orient='records')

    # Salva il risultato in un file JSON
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(data_summary, f, indent=4, ensure_ascii=False)

input_folder = '../sources'
output_file = 'summary.json'
process_datasets(input_folder, output_file)
