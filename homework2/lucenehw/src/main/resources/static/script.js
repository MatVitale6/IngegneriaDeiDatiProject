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

// Listener per il cambiamento del colore
document.getElementById('colorPicker').addEventListener('input', function(event) {
    const selectedColor = event.target.value;
    const complementaryColor = getComplementaryColor(selectedColor);

    // Cambia il colore di sfondo del container descrizione
    document.getElementById('descriptionContainer').style.backgroundColor = selectedColor;

    document.getElementById('resultsContainer').style.backgroundColor = selectedColor;

    // Cambia il colore del testo del titolo principale e di headingText al colore opposto
    document.getElementById('headingText').style.color = selectedColor;
    document.getElementById('mainTitle').style.color = complementaryColor;

    // Cambia il colore di sfondo e del testo del pulsante
    document.getElementById('submitButton').style.backgroundColor = selectedColor;
    document.getElementById('submitButton').style.color = complementaryColor;
});

// Funzione per gestire la richiesta di ricerca
function performSearch(event) {
    event.preventDefault();  // Previene l'invio predefinito del form

    // Ottieni il valore del campo di input
    const query = document.getElementById("inputString").value;

    // Invia la richiesta POST tramite fetch
    fetch("/search", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({ inputString: query })
    })
    .then(response => response.json())  // Converte la risposta in JSON

    /*
    * TODO: questa roba andrebbe tolta e fatta nel controller, usando il model SearchResult
    * e creando html dinamicamnte con thymleaf non con javascript
    */
    .then(results => {
        // Se il risultato è JSON, aggiorna i risultati nella pagina
        const resultsList = document.getElementById("resultsList");
        resultsList.innerHTML = "";  // Svuota il contenitore dei risultati

        results.forEach(result => {
            const li = document.createElement("li");
            li.textContent = result;  // Aggiunge ogni risultato come testo
            resultsList.appendChild(li);
        });
    })
    .catch(error => console.error("Errore durante la ricerca:", error));
}

