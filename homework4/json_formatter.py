import re
import os
import json

INPUT = 'claims/claims_GPT-4o'
OUTPUT = 'consegna/claims'

def parse_claims_to_json(claims_text):
    claim_pattern = r"Claim (\d+): \|\{(.*?)\}, (.*?), (.*?)\|"
    result = {}

    matches = re.finditer(claim_pattern, claims_text, re.DOTALL)

    for match in matches:
        claim_id = match.group(1)  # Extract claim ID
        specifications_raw = match.group(2)  # Extract raw specifications
        measure = match.group(3).strip()  # Extract measure
        outcome = match.group(4).strip()  # Extract outcome

        # Parse specifications into a dictionary
        specifications = {}
        spec_pattern = r"\|([^,]+), ([^|]+)\|"
        for i, spec_match in enumerate(re.finditer(spec_pattern, specifications_raw)):
            specifications[str(i)] = {
                "name": spec_match.group(1).strip(),
                "value": spec_match.group(2).strip(),
            }

        # Add the parsed claim to the result
        result[claim_id] = {
            "specifications": specifications,
            "Measure": measure,
            "Outcome": outcome,
        }

    return result


for filename in os.listdir(INPUT):
    with open(os.path.join(INPUT, filename), 'r') as ifile:
        claims_text = ifile.read()
    
    parsed_claims = parse_claims_to_json(claims_text)

    output_file = os.path.splitext(filename)[0] + "_claims.json"
    with open(os.path.join(OUTPUT, output_file), 'w') as ofile:
        json.dump(parsed_claims, ofile, indent=2)
