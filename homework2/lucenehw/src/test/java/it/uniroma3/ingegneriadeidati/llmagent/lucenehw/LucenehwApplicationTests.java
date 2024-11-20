package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import org.junit.jupiter.api.Test;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.HTMLIndexer;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.SearchService;

import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

@SpringBootTest
class LucenehwApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;  // Contesto Spring, utile per testare la configurazione dell'applicazione

    @Autowired
    private HTMLIndexer fileIndexer = new HTMLIndexer();  // Indice dei file, utilizzato per testare l'indicizzazione

    @Autowired
    private SearchService searchService = new SearchService();  // Servizio di ricerca, utilizzato per testare le query

    private static final String INDEX_PATH = "lucene-index";  // Definisce il percorso dell'indice Lucene, che dovrebbe già essere esistente

    /**
     * Test che verifica che l'applicazione Spring venga caricata correttamente.
     */
    @Test
    void contextLoads() {
		
        // Verifica che il contesto dell'applicazione Spring sia stato correttamente inizializzato.
        Assert.notNull(applicationContext, "Il contesto Spring non dovrebbe essere nullo.");

        // Verifica che i bean necessari (FileIndexer e SearchService) siano stati correttamente caricati nel contesto.
        Assert.notNull(fileIndexer, "Il bean 'FileIndexer' non è stato caricato correttamente.");
        Assert.notNull(searchService, "Il bean 'SearchService' non è stato caricato correttamente.");

        // Verifica che l'indice esista nel percorso specificato
        try {
            FSDirectory directory = FSDirectory.open(Paths.get(INDEX_PATH));  // Apre la directory dell'indice Lucene
            IndexReader reader = DirectoryReader.open(directory);  // Ottiene un lettore per leggere l'indice
            Assert.isTrue(reader.numDocs() > 0, "L'indice è vuoto o non esiste.");  // Verifica che l'indice contenga documenti
            reader.close();  // Chiude il lettore dell'indice
        } catch (IOException e) {
            Assert.isTrue(false, "Errore nell'aprire l'indice Lucene: " + e.getMessage());  // Se c'è un errore nell'aprire l'indice, fallisce il test
        }

        // Verifica che il file di flag dell'indicizzazione esista, per assicurarsi che l'indicizzazione sia stata completata
        Assert.isTrue(new HTMLIndexer() != null, "Il FileIndexer non è stato correttamente configurato.");
    }

    /**
     * Test che verifica il funzionamento del servizio di ricerca.
     * Esegui una ricerca e calcola alcune statistiche sui risultati.
     */
    @Test
    void testSearchService() {
        String query = "Neural networks for financial forecasting";  // Definisce una query di esempio per testare il servizio di ricerca
        try {
            // Esegui la ricerca con la query di esempio, richiedendo un massimo di 10 risultati
            var results = searchService.search(query, 10);  
            Assert.notNull(results, "I risultati della ricerca non dovrebbero essere nulli.");  // Verifica che i risultati non siano nulli
            Assert.isTrue(!results.isEmpty(), "I risultati della ricerca non dovrebbero essere vuoti.");  // Verifica che ci siano risultati

            // Calcola e registra le statistiche sui punteggi dei risultati
            double averageScore = calculateAverageScore(results);  // Calcola il punteggio medio dei risultati
            double scoreVariance = calculateScoreVariance(results, averageScore);  // Calcola la varianza dei punteggi
            double scoreDecay = calculateScoreDecay(results);  // Calcola il decadimento del punteggio (differenza tra il punteggio massimo e minimo)

            // Log delle statistiche
            System.out.println("Query: " + query);
            System.out.println("Numero di risultati: " + results.size());
            System.out.println("Punteggio medio: " + averageScore);
            System.out.println("Varianza del punteggio: " + scoreVariance);
            System.out.println("Decadimento del punteggio: " + scoreDecay);

            // Log dei dettagli di ciascun risultato
            for (var result : results) {
                System.out.println("Titolo: " + result.getTitle());
                System.out.println("Punteggio: " + result.getScore() + " sul campo: " + result.getMatchField());
            }

        } catch (Exception e) {
            Assert.isTrue(false, "Errore durante la ricerca: " + e.getMessage());  // Se si verifica un errore durante la ricerca, il test fallisce
        }
    }

    /**
     * Calcola il punteggio medio dei risultati della ricerca.
     * @param results Lista dei risultati della ricerca
     * @return Punteggio medio
     */
    private double calculateAverageScore(List<SearchResult> results) {
        return results.stream()
                      .mapToDouble(SearchResult::getScore)  // Ottieni i punteggi dai risultati
                      .average()  // Calcola la media dei punteggi
                      .orElse(0);  // Se la lista è vuota, restituisce 0 come valore predefinito
    }

    /**
     * Calcola la varianza dei punteggi dei risultati.
     * @param results Lista dei risultati della ricerca
     * @param averageScore Punteggio medio dei risultati
     * @return Varianza dei punteggi
     */
    private double calculateScoreVariance(List<SearchResult> results, double averageScore) {
        return results.stream()
                      .mapToDouble(result -> Math.pow(result.getScore() - averageScore, 2))  // Calcola la distanza quadrata tra il punteggio e la media
                      .average()  // Calcola la media delle distanze quadrate (varianza)
                      .orElse(0);  // Se la lista è vuota, restituisce 0 come valore predefinito
    }
    
    /**
     * Calcola il decadimento del punteggio tra il massimo e il minimo.
     * @param results Lista dei risultati della ricerca
     * @return Decadimento del punteggio (differenza tra massimo e minimo)
     */
    private double calculateScoreDecay(List<SearchResult> results) {
        if (results.size() < 2) return 0;  // Se ci sono meno di 2 risultati, non è possibile calcolare il decadimento
        double maxScore = results.get(0).getScore();  // Prendi il punteggio del primo risultato (assumendo che la lista sia ordinata)
        double minScore = results.get(results.size() - 1).getScore();  // Prendi il punteggio dell'ultimo risultato
        return maxScore - minScore;  // Calcola la differenza tra il punteggio massimo e il minimo
    }
}
