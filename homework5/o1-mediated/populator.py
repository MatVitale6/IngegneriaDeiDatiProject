import pandas as pd
import json
import os

schema_file_path = "mediated_schema.json"  
with open(schema_file_path, "r", encoding="utf-8") as f:
    schema_data = json.load(f)

mediated_schema_keys = list(schema_data["mediated_schema"].keys())
df = pd.DataFrame(columns=mediated_schema_keys)

field_mapping_path = "field_mapping.json"  # Modifica il percorso del file field mapping
with open(field_mapping_path, "r", encoding="utf-8") as f:
    field_mapping = json.load(f)

sources_folder = "../sources"  

def map_source_to_schema(source_df, field_mapping):
    temp_df = pd.DataFrame(columns=mediated_schema_keys)
    
    for schema_field, possible_source_fields in field_mapping.items():
        for source_field in possible_source_fields:
            if source_field in source_df.columns:
                temp_df[schema_field] = source_df[source_field]
                break  
        else:
            temp_df[schema_field] = None
    
    return temp_df

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
