import pandas as pd
import json
import os

schema_file_path = "mediated_schema.json"  
with open(schema_file_path, "r", encoding="utf-8") as f:
    schema_data = json.load(f)

companies_keys = list(schema_data["schema"].keys())
companies_df = pd.DataFrame(columns=companies_keys)

employees_keys = list(schema_data['employees'].keys())
emp_df = pd.DataFrame(columns=employees_keys)

schema_mapping_path = "schema_mapping.json"  # Modifica il percorso del file field mapping
with open(schema_mapping_path, "r", encoding="utf-8") as f:
    field_mapping = json.load(f)

sources_folder = "../sources"  

def map_source_to_schema(source_df, field_mapping):
    

for source_file in os.listdir(sources_folder):
    file_path = os.path.join(sources_folder, source_file)
    
    if source_file.endswith(".csv"):
        try: 
            source_df = pd.read_csv(file_path)
        except UnicodeDecodeError:
            source_df = pd.read_csv(file_path, encoding="ISO-8859-1")
    elif source_file.endswith(".json"):
        source_df = pd.read_json(file_path)
    else:
        continue  
    
    mapped_df = map_source_to_schema(source_df, field_mapping)
    
    df = pd.concat([df, mapped_df], ignore_index=True)

df.to_csv("populated_mediated_schema.csv", index=False)
