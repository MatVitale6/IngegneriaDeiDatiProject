// Function to update the progress bar
function updateProgress() {
    fetch('/indexing/progress')
        .then(response => response.json())
        .then(progress => {
            const progressBar = document.getElementById('progress-bar');
            if (progressBar) { // Check if progress bar exists on the page
                progressBar.style.width = progress + '%';
                progressBar.textContent = progress + '%';

                if (progress < 100) {
                    setTimeout(updateProgress, 1000);  // Poll every second
                } else {
                    location.reload();  // Reload page when indexing is complete
                }
            }
        })
        .catch(error => console.error('Error fetching progress:', error));
}

// Start polling the progress on page load, if the progress bar exists
window.addEventListener('load', () => {
    const progressBar = document.getElementById('progress-bar');
    if (progressBar) {
        updateProgress();
    }
});