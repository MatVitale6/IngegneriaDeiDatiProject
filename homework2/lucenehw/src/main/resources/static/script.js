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
                    <h5 class="fw-bold" style="font-size: 1.1rem;">
                        <a href="${result.link}" style="color: black" target="_blank">${result.title}</a>
                    </h5>
                    <p class="text-muted" style="font-size: 0.9rem;"><strong>Author:</strong> ${result.author}</p>
                `;

                const abstractContainer = document.createElement("p");
                abstractContainer.style.fontSize = "0.9rem"

                
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
                
                li.appendChild(resultContent);
                li.appendChild(matchField);
            
                resultsList.appendChild(li);
            });
            
        }
    })
}

