document.getElementById('colorPickerL').addEventListener('input', function(event) {
    updateColors(event.target.value);
});

function updateColors(selectedColor) {
    const complementaryColor = getComplementaryColor(selectedColor);

    // Cambia il colore di sfondo del container descrizione
    document.getElementById('descriptionContainer').style.backgroundColor = selectedColor;
    document.getElementById('resultsContainer').style.backgroundColor = selectedColor;

    // Cambia il colore del testo del titolo principale
    document.getElementById('mainTitle').style.color = complementaryColor;
    document.getElementById('subTitle1').style.color = complementaryColor;
    document.getElementById('subTitle2').style.color = complementaryColor;
    
    // Cambia il colore di sfondo e del testo del pulsante di submit
    document.getElementById('submitButton1').style.backgroundColor = selectedColor;
    document.getElementById('submitButton1').style.color = complementaryColor;

    document.getElementById('submitButton2').style.backgroundColor = selectedColor;
    document.getElementById('submitButton2').style.color = complementaryColor;

    document.getElementById('researchResults').style.color = complementaryColor;

    const tables = document.querySelectorAll('.table-preview table'); // Selettore delle tabelle
    tables.forEach(table => {
        // Cambia lo sfondo delle celle pari e dispari per alternanza
        table.querySelectorAll('tr:nth-child(even) td').forEach(cell => {
            cell.style.backgroundColor = selectedColor; // Sfondo per righe pari
            cell.style.color = complementaryColor; // Colore testo
        });
        table.querySelectorAll('tr:nth-child(odd) td').forEach(cell => {
            cell.style.backgroundColor = complementaryColor; // Sfondo per righe dispari
            cell.style.color = selectedColor; // Colore testo
        });

        // Cambia stile intestazioni (th)
        table.querySelectorAll('th').forEach(header => {
            header.style.backgroundColor = complementaryColor;
            header.style.color = selectedColor;
        });
    });
}

// Funzione per calcolare il colore opposto
function getComplementaryColor(hexColor) {
    // Rimuove il # dal colore
    hexColor = hexColor.replace('#', '');

    // Converte il colore in componenti R, G, B
    const r = parseInt(hexColor.slice(0, 2), 16);
    const g = parseInt(hexColor.slice(2, 4), 16);
    const b = parseInt(hexColor.slice(4, 6), 16);

    // Calcola il colore opposto
    const complementaryR = (255 - r).toString(16).padStart(2, '0');
    const complementaryG = (255 - g).toString(16).padStart(2, '0');
    const complementaryB = (255 - b).toString(16).padStart(2, '0');

    return `#${complementaryR}${complementaryG}${complementaryB}`;
}

function performSearch(event) {
    event.preventDefault();  // Previene l'invio predefinito del form

    // Ottieni il valore del campo di input
    let query = document.getElementById("inputString").value;
    const resultCount = document.getElementById("resultCount").value;
    const resourceType = event.submitter.getAttribute("data-resource-type");

    query = sanitizeInput(query);
    console.log(query);
    // Invia la richiesta POST tramite fetch
    fetch("/search", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({ inputString: query, resultCount: resultCount, resourceType: resourceType})
    })
    .then(response => response.json())  // Converte la risposta in JSON
    .then(results => {
        const resultsList = document.getElementById("resultsList");
        resultsList.innerHTML = "";

        if (results.length === 0) {
            const li = document.createElement("li");
            li.textContent = "LISTA VUOTA! FORZA ROMA!";
            li.classList.add("fw-bold", "text-danger");
            resultsList.appendChild(li);
        } else {
            results.forEach(result => {
                const li = document.createElement("li");
                li.classList.add("mb-3", "p-4", "border", "rounded", "shadow-sm", "bg-light", "d-flex", "align-items-start");
            
                // Identifica il formato della risposta
                if (result.tableId && result.caption) {
                    // Nuovo formato
                    const matchField = document.createElement("div");
                    matchField.classList.add("text-danger", "fw-bold", "match-field");
                    matchField.style.fontSize = "0.7rem";
                    matchField.style.marginLeft = "auto";
                    matchField.textContent = result.matchField;
            
                    const resultContent = document.createElement("div");
                    resultContent.innerHTML = `
                        <h5 class="fw-bold" style="font-size: 1.1rem;">
                            <a href="${result.link}#${result.tableId}" style="color: black" target="_blank">Table ID: ${result.tableId}</a>
                        </h5>
                        <p class="text-muted" style="font-size: 0.9rem;">
                            <strong>Caption:</strong> ${result.caption}
                        </p>
                        `;
            
                    // Se ci sono contenuti della tabella, creiamo il contenitore della tabella
                    if (result.tableContent) {
                        const tableContainer = document.createElement("div");
                        tableContainer.classList.add("table-preview"); // Classe per il comportamento di scorrimento
                        tableContainer.innerHTML = `
                            <table>
                                ${result.tableContent} <!-- Righe e colonne valide -->
                            </table>
                        `;
                        
                        resultContent.appendChild(tableContainer);
                    }

                    if (result.references) {
                        const referencesContainer = document.createElement("div");
                        referencesContainer.style.fontSize = "0.9rem";
                    
                        // Intestazione "References"
                        const referencesTitle = document.createElement("p");
                        referencesTitle.classList.add("fw-bold", "mb-2"); // Grassetto e margine inferiore
                        referencesTitle.textContent = "References:";
                    
                        // Versione abbreviata delle references
                        const shortReferences = result.references.slice(0, 150) + "...";
                        const referencesText = document.createElement("span");
                        referencesText.classList.add("references-text");
                        referencesText.textContent = shortReferences;
                    
                        // Bottone per espandere/comprimere
                        const expandButton = document.createElement("button");
                        expandButton.textContent = "Read references";
                        expandButton.classList.add("btn", "btn-link", "p-0", "text-decoration-none");
                        expandButton.style.fontSize = "0.7rem";
                    
                        expandButton.addEventListener("click", () => {
                            const referencesText = referencesContainer.querySelector(".references-text");
                            if (expandButton.textContent === "Read references") {
                                referencesText.textContent = result.references; // Mostra tutte le references
                                expandButton.textContent = "Compress references";
                            } else {
                                referencesText.textContent = shortReferences; // Torna alla versione breve
                                expandButton.textContent = "Read references";
                            }
                        });
                    
                        // Costruzione del contenitore delle references
                        referencesContainer.appendChild(referencesTitle); // Aggiunge il titolo
                        referencesContainer.appendChild(referencesText); // Aggiunge il testo abbreviato
                        referencesContainer.appendChild(expandButton);   // Aggiunge il bottone
                    
                        resultContent.appendChild(referencesContainer); // Aggiunge tutto a resultContent
                    }                    
                    
            
                    li.appendChild(resultContent); // Aggiungiamo il contenuto del risultato (testo, tabella, ecc.)
                    li.appendChild(matchField); // Aggiungiamo il campo di match
            
                } else if (result.title && result.author) {
                    // Vecchio formato
                    const matchField = document.createElement("div");
                    matchField.classList.add("text-danger", "fw-bold");
                    matchField.style.fontSize = "0.7rem";
                    matchField.style.marginLeft = "auto";
                    matchField.textContent = result.matchField;
            
                    const resultContent = document.createElement("div");
                    resultContent.innerHTML = `
                        <h5 class="fw-bold" style="font-size: 1.1rem;">
                            <a href="${result.link}" style="color: black" target="_blank">${result.title}</a>
                        </h5>
                        <p class="text-muted" style="font-size: 0.9rem;">
                            <strong>Author:</strong> ${result.author}
                        </p>
                    `;
            
                    const abstractContainer = document.createElement("p");
                    abstractContainer.style.fontSize = "0.9rem";
            
                    const shortAbstract = result.abstract.slice(0, 150) + "...";
                    const abstractText = document.createElement("span");
                    abstractText.classList.add("abstract-text");
                    abstractText.textContent = shortAbstract;
            
                    const expandButton = document.createElement("button");
                    expandButton.textContent = "Read abstract";
                    expandButton.classList.add("btn", "btn-link", "p-0", "text-decoration-none");
                    expandButton.style.fontSize = "0.7rem";
            
                    expandButton.addEventListener("click", () => {
                        const abstractText = abstractContainer.querySelector(".abstract-text");
                        if (expandButton.textContent === "Read abstract") {
                            abstractText.textContent = result.abstract;
                            expandButton.textContent = "Compress abstract";
                        } else {
                            abstractText.textContent = shortAbstract;
                            expandButton.textContent = "Read abstract";
                        }
                    });
            
                    abstractContainer.appendChild(abstractText);
                    abstractContainer.appendChild(expandButton);
                    resultContent.appendChild(abstractContainer);
            
                    li.appendChild(resultContent); // Aggiungiamo il contenuto del risultato
                    li.appendChild(matchField); // Aggiungiamo il campo di match
                } else {
                    // Formato sconosciuto
                    const errorMessage = document.createElement("p");
                    errorMessage.textContent = "Formato della risposta non riconosciuto.";
                    errorMessage.classList.add("text-danger", "fst-italic");
                    li.appendChild(errorMessage);
                }
            
                // Alla fine, aggiungiamo il li a resultsList
                resultsList.appendChild(li);
            });            
            
        }
    })
}

function sanitizeInput(input) {
    // we <3 cysec and pizzo... and whitelist ofc
    const allowedCharacters = /^[a-zA-Z0-9\s_$-]+$/;
    const tempDiv = document.createElement("div");
    tempDiv.textContent = input.split('').filter(char => allowedCharacters.test(char)).join('');
    return tempDiv.innerHTML;
}
