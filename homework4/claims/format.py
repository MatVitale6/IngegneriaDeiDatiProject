import os

def reformat_claims(input_dir, output_dir):
    """Reformats all claims files in the input directory, saving to the output directory."""
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    for filename in os.listdir(input_dir):
        input_file = os.path.join(input_dir, filename)
        output_file = os.path.join(output_dir, filename)

        if os.path.isfile(input_file):
            with open(input_file, 'r') as infile, open(output_file, 'w') as outfile:
                current_claim = ""

                for line in infile:
                    line = line.strip()  # Remove leading/trailing whitespace

                    if line.startswith("Claim"):
                        if current_claim:
                            # Write the current claim to the output file
                            outfile.write(current_claim + "\n")
                        # Start a new claim
                        current_claim = line
                    else:
                        # Append line to the current claim
                        current_claim += " " + line

                # Write the last claim if it exists
                if current_claim:
                    outfile.write(current_claim + "\n")

# Specify the input and output directory paths
input_dir = "claims_GPT-o1"
output_dir = "claims_GPT-o1_reformatted"

# Call the function to reformat the claims
reformat_claims(input_dir, output_dir)

print(f"Claims reformatted and saved to {output_dir}")
