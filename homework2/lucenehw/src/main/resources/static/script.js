document.getElementById('colorPickerR').addEventListener('input', function(event) {
    updateColors(event.target.value);
});

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

    // Cambia il colore di sfondo e del testo del pulsante di submit
    document.getElementById('submitButton').style.backgroundColor = selectedColor;
    document.getElementById('submitButton').style.color = complementaryColor;
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
                
                const matchField = document.createElement("div");
                matchField.classList.add("text-danger", "fw-bold");
                matchField.style.fontSize = "0.7rem";
                matchField.style.marginLeft = "auto";
                matchField.textContent = result.matchField;
            
                
                const resultContent = document.createElement("div");
                resultContent.innerHTML = `
                    <h5 class="fw-bold" style="font-size: 0.9rem;">${result.title}</h5>
                    <p class="text-muted" style="font-size: 0.7rem;"><strong>Author:</strong> ${result.author}</p>
                    <p style="font-size: 0.7rem;"><strong>Abstract:</strong> ${result.abstract}</p>
                `;
            
                
                li.appendChild(resultContent);
                li.appendChild(matchField);
            
                resultsList.appendChild(li);
            });
            
        }
    })
}

