import os
import json

def check_empty_files(log_file):
    # Directory del file di log
    log_file_dir = os.path.dirname(os.path.abspath(log_file))
    print(f"Directory del file di log: {log_file_dir}")

    # Aggiungi 'homework2' al percorso base
    custom_base_path = os.path.join(log_file_dir, "homework2")
    print(f"Percorso base personalizzato: {custom_base_path}")

    all_files_empty = True  # Variabile per tracciare lo stato dei file
    non_empty_files_count = 0  # Contatore dei file non vuoti
    empty_files_count = 0  # Contatore dei file vuoti

    with open(log_file, 'r') as log:
        for line in log:
            # Estrarre il percorso del file dal log
            if line.startswith("Deleted empty file:"):
                file_path = line.split(":", 1)[1].strip()

                # Rimuovi i riferimenti relativi '..' manualmente
                file_path = file_path.replace("../", "/")  # Rimuove ../ per lavorare su percorsi assoluti
                print(f"Percorso normalizzato (rimosso ../): {file_path}")

                # Unisci il percorso base con il file normalizzato
                absolute_path = os.path.join(custom_base_path, file_path)
                print(f"Percorso assoluto calcolato: {absolute_path}")

                # Verifica se il file esiste
                if os.path.exists(absolute_path):
                    if os.path.getsize(absolute_path) > 0:
                        all_files_empty = False  # C'è almeno un file non vuoto
                        non_empty_files_count += 1
                        print(f"Il file {absolute_path} non è vuoto. Contenuto:")
                        try:
                            with open(absolute_path, 'r') as f:
                                content = json.load(f)
                                print(json.dumps(content, indent=4))
                        except json.JSONDecodeError:
                            print(f"Il file {absolute_path} non è un JSON valido.")
                        except Exception as e:
                            print(f"Errore durante la lettura del file {absolute_path}: {e}")
                    else:
                        empty_files_count += 1  # Incrementa il contatore dei file vuoti
                        print(f"Il file {absolute_path} è vuoto.")
                else:
                    empty_files_count += 1
                    print(f"Il file {absolute_path} non esiste.")

    # Stampa se tutti i file erano vuoti
    if all_files_empty:
        print("Tutti i file indicati nel log sono vuoti.")
    else:
        print("Non tutti i file sono vuoti.")
    print(f"\nTotale file non vuoti: {non_empty_files_count}")
    print(f"Totale file vuoti: {empty_files_count}")


# Specifica il percorso del file di log
log_file_path = "emptyJson.txt"

# Esegui il controllo
check_empty_files(log_file_path)
