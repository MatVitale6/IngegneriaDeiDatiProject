import json
import pandas as pd
import os

INPUT_DIR = '../consegna/LLMAGENTS_CLAIMS'
ALIGNMENT_SCHEMA_PATH = '2304.04370_alignment/alignment_schema.json'

def create_aligned_schema_df(json_data):
    columns = []
    for key, sub_dict in json_data.items():
        for sub_key, sub_values in sub_dict.items():
            columns.append(sub_key.lower())
    df = pd.DataFrame([], columns=columns)
    return df

def create_claims_df(json_data):
    rows = []
    for key, entry in json_data.items():
        row = {}
        # Extract specifications
        for spec_key, spec in entry["specifications"].items():
            row[spec["name"]] = spec["value"]
        # Add measure and outcome
        row["Measure"] = entry["Measure"]
        row["Outcome"] = entry["Outcome"]
        rows.append(row)
    df = pd.DataFrame(rows)
    return df


def normalize_claims(file_paths, schema_json ,schema_df):
    normalized_claims = []

    for file_path in file_paths:
        with open(os.path.join(INPUT_DIR, file_path), 'r') as f:
            for line in f:
                if line.strip():  # Process only non-empty lines
                    claim = line.strip().lower()
                    print(claim)
                    # Normalize specifications
                    for spec_key, spec_values in schema_json["specifications"].items():
                        for value in spec_values:
                            claim = claim.replace(f"|{value},", f"|{spec_key},")
                    # Normalize values
                    for value_key, value_variants in schema_json["values"].items():
                        for variant in value_variants:
                            claim = claim.replace(f"|{variant}|", f"|{value_key}|")
                    # Add normalized claim to list
                    normalized_claims.append(claim)
    return normalized_claims

# File paths for claims
claim_files = [
    '2304.04370_1_claims.json',
    '2304.04370_2_claims.json',
    '2304.04370_3_claims.json',
]

with open(os.path.join(INPUT_DIR, claim_files[2]), 'r') as f:
    data = json.load(f)

df = create_claims_df(data)
print(df.head())
exit()

with open(ALIGNMENT_SCHEMA_PATH, 'r') as f:
    schema_json = json.load(f)

schema_df = create_aligned_schema_df(schema_json)
normalized_claims = normalize_claims(claim_files, schema_json, schema_df)

# Save normalized claims to a file
normalized_claims_path = 'normalized_claims.txt'
with open(normalized_claims_path, 'w') as f:
    f.write("\n".join(normalized_claims))
