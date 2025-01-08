import os

def create_empty_txt_files(source_folder, destination_folder):
    """
    Creates empty .txt files in the destination folder with the same names
    as the files in the source folder. If a file already exists, it does nothing.

    Args:
        source_folder (str): Path to the folder containing the source files.
        destination_folder (str): Path to the folder where empty .txt files will be created.
    """
    if not os.path.exists(destination_folder):
        os.makedirs(destination_folder)

    for filename in os.listdir(source_folder):
        # Get the base name without the extension
        base_name, _ = os.path.splitext(filename)
        txt_file_path = os.path.join(destination_folder, f"{base_name}.txt")

        # Create an empty file if it doesn't exist
        if not os.path.exists(txt_file_path):
            with open(txt_file_path, 'w') as f:
                pass

# Example usage
source_folder = "prompts"
destination_folder = "claims_GPT-o1"
create_empty_txt_files(source_folder, destination_folder)
