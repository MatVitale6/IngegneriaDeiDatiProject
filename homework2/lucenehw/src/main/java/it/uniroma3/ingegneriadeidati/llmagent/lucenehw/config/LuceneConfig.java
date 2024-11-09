package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * Questa classe configura le impostazioni di base per l'uso di Lucene. Definisce ad esempio il percorso dell'indice
 * (cioé dove sono memorizzati i dati indicizzati), i tipi di analizzatori utilizzati e altre optioni necessarie.
 * Solitamente Lucene config viene caricata una volta sola e funge da riferimento per le altre classi.
 */

@Configuration
public class LuceneConfig {
    /**
     * Percorso della directory dell'indice Lucene
     * Il valore viene iniettato dalla proprietà configurata in `application.properties` tramite l'annotazione `@Value`.
     */
    @Value("${lucene.index.path}")
    private String indexDirectory;

    /**
     * Crea un bean 'Directory' che rappresenta la directory del file system in cui Lucene memorizza l'indice
     * @return un'istanza di 'FSDirectory' che punta al percorso specificato in 'indexDirectory'.
     * @throws IOException se il percorso specificato non é accessibile o non puó essere aperto.
     */
    @Bean
    public Directory luceneDirectory() throws IOException {
        Path path = Paths.get(indexDirectory);
        return FSDirectory.open(path);
    }

    /**
     * Configura un bean 'Analyzer che gestisce l'analisi di testi e campi specifici per l'indicizzazione e la ricerca.
     * Utilizza 'PerFieldAnalyzerWraooer' per applicare diversi analizzatori ai vari campi:
     * - 'title': utilizza 'StandardAnalyzer' per tokenizzare i titoli in modo generico.
     * - 'content': utilizza 'EnglishAnalyzer' per il contenuto, applicando filtri specifici per l'inglese.
     * - 'authors': utilizza 'KeywordAnalyzer' per il campo autori, analizzando il testo come una parolachiave unica.
     * @return un'istanza di 'Analyzer' per i vari campi definiti.
     */
    @Bean
    public Analyzer analyzer() {
        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();
        perFieldAnalyzer.put("title", new StandardAnalyzer());
        perFieldAnalyzer.put("authors", new WhitespaceAnalyzer());
        perFieldAnalyzer.put("content", new EnglishAnalyzer());
        perFieldAnalyzer.put("abstract", new EnglishAnalyzer());
        
        return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);
    }


    /**
     * Configura un bean 'IndexWriterConfig' per gestire le impostazioni di scrittura dell'indice.
     * Utilizza l''Analyzer' configurato per gestire l'analisi dei dati.
     * L''IndexWriterConfig' é fondamentale per la creazione e aggiornamento dell'indice.
     * @param analyzer l'analizzatore specificato per l'indice.
     * @return una nuova istanza di 'IndexWriterConfig' configurata con l'analizzatore
     */
    @Bean
    public IndexWriterConfig indexWriterConfig(Analyzer analyzer) {
        return new IndexWriterConfig(analyzer);
    }

}
