package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.core.subst.Tokenizer;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.HTMLIndexer;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.JsonIndexer;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util.CustomAuthorAnalyzer;

/**
 * 
 * Questa classe configura le impostazioni di base per l'uso di Lucene. Definisce ad esempio il percorso dell'indice
 * (cioé dove sono memorizzati i dati indicizzati), i tipi di analizzatori utilizzati e altre optioni necessarie.
 * Solitamente Lucene config viene caricata una volta sola e funge da riferimento per le altre classi.
 */

@Configuration
public class LuceneConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(LuceneConfig.class);

    @Value("${lucene.index.path}")
    private String indexDirectory;

    private static final String INDEXING_COMPLETE_FLAG = "indexing_complete.flag";

    /**
     * Crea un bean 'Directory' che rappresenta la directory del file system in cui Lucene memorizza l'indice
     * @return un'istanza di 'FSDirectory' che punta al percorso specificato in 'indexDirectory'.
     * @throws IOException se il percorso specificato non é accessibile o non puó essere aperto.
     */
    @Bean
    public Directory createIndexDirectory() throws IOException {
        Path path = Paths.get(indexDirectory);
        String flagFileName = "indexing_complete.flag";
        Path flagFilePath = path.resolve(flagFileName);
        boolean indexingCompleted = Files.exists(flagFilePath);
        
        if (!indexingCompleted) {
            try (DirectoryStream<Path> directoryStream =  Files.newDirectoryStream(path)) {
                for (Path filePath : directoryStream) {
                    if(!filePath.getFileName().toString().equals(".gitkeep") && !filePath.getFileName().toString().equals(flagFileName)) {
                        Files.delete(filePath);
                    }
                }
            } catch (IOException e) {
                throw new IOException("Error clearing indexDirectory " + e.getMessage(), e);
            }
        }

        return FSDirectory.open(path);
    }

    @Bean
    public HTMLIndexer htmlIndexer() {
        Path flagFilePath = Paths.get(indexDirectory, INDEXING_COMPLETE_FLAG);

        if (!Files.exists(flagFilePath)) {
            logger.info("Indexing Flag file not found, creating FileIndexer");
            return new HTMLIndexer();
        } else {
            logger.info("Indexing complete, skipping FileIndexer creation");
            return null;
        }
    }

    //manno so benissimo che l'if sulla flagFilePath é na bella cornice poi in caso si aggiunge eh
    @Bean
    public JsonIndexer jsonIndexer() {
        logger.info("Creating JsonIndexer bean");
        return new JsonIndexer();
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
    public Analyzer analyzer_html() {
        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();
        perFieldAnalyzer.put("title", new KeywordAnalyzer());
        perFieldAnalyzer.put("authors", new WhitespaceAnalyzer());
        perFieldAnalyzer.put("content", new EnglishAnalyzer());
        perFieldAnalyzer.put("abstract", new EnglishAnalyzer());
        // perFieldAnalyzer.put("keywords", new KeywordAnalyzer());
        
        PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

        logger.info("Analyzer configuration:");
        perFieldAnalyzer.forEach((field, fieldAnalyzer) -> 
            logger.info("Field '{}', Analyzer '{}'", field, fieldAnalyzer.getClass().getName())
        );

        return perFieldAnalyzerWrapper;
    }


    //PARTE CON GLI ANALYZER -BY TERRY <3
    @Bean
    public Analyzer analyzer_json() {
        // Mappa per definire analizzatori specifici per ogni campo
        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();

        // Configura un analizzatore adatto per ogni campo:
        perFieldAnalyzer.put("caption", new EnglishAnalyzer()); // Analisi linguistica per la caption
        perFieldAnalyzer.put("table", new StandardAnalyzer()); // Tokenizzazione generale per i dati tabellari
        perFieldAnalyzer.put("footnotes", new WhitespaceAnalyzer()); // Divide il testo su spazi, utile per liste brevi
        perFieldAnalyzer.put("references", new EnglishAnalyzer()); // Analisi linguistica per riferimenti complessi

        // PerFieldAnalyzerWrapper permette di usare diversi analizzatori per ogni campo
        PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

        // Logging per verificare la configurazione
        logger.info("Analyzer JSON configuration:");
        perFieldAnalyzer.forEach((field, fieldAnalyzer) -> 
            logger.info("Field '{}', Analyzer '{}'", field, fieldAnalyzer.getClass().getName())
        );

        return perFieldAnalyzerWrapper;
    }


    /*C'é chiamaremnte un problema, perché questo metodo IndexWriterConfig prende in input un solo anlayzer, per il momendo ho messo solo analyzer_json ma
     * avrebbe senso per il futuro mettere un unico analyzer "combined" come mi suggerisce chatgpt come questo:
     * @Bean
        public Analyzer combinedAnalyzer() {
            Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();

            // Aggiungi i campi di analyzer_html
            perFieldAnalyzer.put("title", new KeywordAnalyzer());
            perFieldAnalyzer.put("authors", new WhitespaceAnalyzer());
            perFieldAnalyzer.put("content", new EnglishAnalyzer());
            perFieldAnalyzer.put("abstract", new EnglishAnalyzer());

            // Aggiungi i campi di analyzer_json
            perFieldAnalyzer.put("caption", new EnglishAnalyzer());
            perFieldAnalyzer.put("table", new StandardAnalyzer());
            perFieldAnalyzer.put("footnotes", new WhitespaceAnalyzer());
            perFieldAnalyzer.put("references", new EnglishAnalyzer());

            // Crea un PerFieldAnalyzerWrapper con un analizzatore predefinito
            PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

            // Logging per verificare la configurazione
            logger.info("Combined Analyzer configuration:");
            perFieldAnalyzer.forEach((field, fieldAnalyzer) -> 
                logger.info("Field '{}', Analyzer '{}'", field, fieldAnalyzer.getClass().getName())
            );

            return perFieldAnalyzerWrapper;
        }
     */
//HEY//
            //LEGGERE QUESTO COMMENTO SOPRA//
//LEGGI//
//AO//
//AO//
//EDDAI//
        /**
         * Configura un bean 'IndexWriterConfig' per gestire le impostazioni di scrittura dell'indice.
         * Utilizza l''Analyzer' configurato per gestire l'analisi dei dati.
         * L''IndexWriterConfig' é fondamentale per la creazione e aggiornamento dell'indice.
         * @param analyzer l'analizzatore specificato per l'indice.
         * @return una nuova istanza di 'IndexWriterConfig' configurata con l'analizzatore
         */
    @Bean
    public IndexWriterConfig indexWriterConfig(Analyzer analyzer_json) {
        return new IndexWriterConfig(analyzer_json);
    }

}
