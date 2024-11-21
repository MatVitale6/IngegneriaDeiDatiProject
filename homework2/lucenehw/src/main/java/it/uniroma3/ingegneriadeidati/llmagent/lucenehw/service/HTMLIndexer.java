package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;
import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util.HTMLParserUtils;

/**
 * Servizio responsabile dell'indicizzazione dei file HTML.
 * La classe 'FileIndexer gestisce l'iterazione dei file, la creazione dei documenti per Lucene e l'indicizzazione in batch.
 */
@Service
public class HTMLIndexer {

    private static final Logger logger = LoggerFactory.getLogger(HTMLIndexer.class);
    private Map<String, List<String>> emptyFieldsFiles = new HashMap<>();

    @Value("${html.files.path}")
    private String htmlFilesPath;

    /**
     * Directory di Lucene in cui vengono memorizzati gli indici
     * Viene iniettata da Spring utilizzando '@Autowired'
     */
    @Autowired
    private Directory directory;
    
    /**
     * Configurazione per l'`IndexWriter`, necessaria per creare e aggiornare l'indice.
     * Iniettata da Spring tramite `@Autowired`.
     */
    @Autowired
    private IndexWriterConfig indexWriterConfig;

    @Autowired
    private ProgressService progressService;

    public HTMLIndexer() {
        emptyFieldsFiles.put("title", new ArrayList<>());
        emptyFieldsFiles.put("authors", new ArrayList<>());
        emptyFieldsFiles.put("abstract", new ArrayList<>());
        emptyFieldsFiles.put("content", new ArrayList<>());
        // emptyFieldsFiles.put("keywords", new ArrayList<>());
    }

    public void run() {
        
    }

    /**
     * Metodo per indicizzare tutti i file HTML presenti nella directory specificata.
     * L' indicizzazione avviene in batch per migliorare l'efficenza.
     * @param directoryPath Percorso della directory contenente i file HTML da indicizzare.
     * @throws IOException se si verifica un errore di I/O durante l'indicizzazione
     */
    public void indexHtmlFiles(String directoryPath) throws IOException {
        logger.info("Starting HTML file indexing...");
        progressService.resetProgress();

        //Trova tutti i file HTML nella directory specificata
        File[] files = new File(directoryPath).listFiles((dir, name) -> name.endsWith(".html"));
        if (files == null || files.length == 0) {
            logger.warn("No HTML files found in directory: {}", directoryPath);
            return;  
        }
        
        long startTime = System.nanoTime();
        int totalIndexed = 0;
        int totalFiles = files.length;
        
        try (IndexWriter writer = new IndexWriter(directory, indexWriterConfig)) {
            for (File file : files) {
                long fileStartTime = System.nanoTime();
    
                Document doc = createDocumentFromFile(file);
                writer.addDocument(doc);
                writer.commit();     //effettua il commit al termine del batch
                totalIndexed++;
                long fileEndTime = System.nanoTime();
    
                logger.info("Indexed file: {} (Total Indexed: {}), File Time: {}ms",
                        file.getName(), totalIndexed, (fileEndTime - fileStartTime) / 1_000_000);
                
                int progress = (int) (((double) totalIndexed / totalFiles) * 100);
                progressService.setProgress(progress);
            }
        }
        progressService.setProgress(100);
        progressService.markIndexingComplete();
        long endTime = System.nanoTime();        
        logger.info("Total indexing time: {}ms", (endTime - startTime) / 1_000_000);

        // log empty fields summary
        logEmptyFieldsSummary(totalFiles);
    }

    

    private void logEmptyFieldsSummary(int totalFiles) {
        emptyFieldsFiles.forEach((field, filesWithEmptyField) -> {
            logger.info(filesWithEmptyField.toString());
            if (!filesWithEmptyField.isEmpty()) {
                double percentage = (filesWithEmptyField.size() * 100.0) / totalFiles;
                logger.warn("Files with empty {}: {} ({} files, {}% of total files)",
                    field, filesWithEmptyField, filesWithEmptyField.size(), String.format("%.2f", percentage));   
            } else {
                logger.info("No files with empty {}", field);
            }
        });
    }  

    /**
     * Crea un documento Lucene per ciascun file HTML.
     * Utilizza 'HTMLPARSERUTILS' per estrarre i contenuti rilevanti dal file HTML e aggiungerli come campi nel documento.
     * @param file File HTML da convertire in un documento
     * @return un ogetto 'Document' centenente i campi estratti dal file HTML.
     * @throws IOException se sis verifica un errore durante la lettura del file.
     */
    public Document createDocumentFromFile(File file) throws IOException {
        Document doc = new Document();

        //Estrae e aggiunge il titolo
        String title = HTMLParserUtils.parseTitle(file);
        if (!title.isEmpty()) {
            doc.add(new TextField("title", title, Field.Store.YES));
        } else {
            emptyFieldsFiles.get("title").add(file.getName());
        }

        //Estrae e aggiunge gli autori
        String authors = HTMLParserUtils.parseAuthors(file);
        if (!authors.isEmpty()) {
            doc.add(new TextField("authors", authors, Field.Store.YES));
        } else {
            emptyFieldsFiles.get("authors").add(file.getName());
        }

        //Estrae e aggiunge il contenuto del file
        String content = HTMLParserUtils.parseContent(file);
        if (!content.isEmpty()) {
            doc.add(new TextField("content", content, Field.Store.YES));
        } else {
            emptyFieldsFiles.get("content").add(file.getName());
        }

        // Estrae e aggiunge l'abstract
        String abstractText = HTMLParserUtils.parseAbstract(file);
        if (!abstractText.isEmpty()) {
             doc.add(new TextField("abstract", abstractText, Field.Store.YES));
        } else {
            emptyFieldsFiles.get("abstract").add(file.getName());
        }

        // // Estrae e aggiunge le keywords
        // String keywordsText = HTMLParserUtils.parseKeywords(file);
        // if (!keywordsText.isEmpty()) {
        //         doc.add(new TextField("keywords", keywordsText, Field.Store.YES));
        // } else {
        //     emptyFieldsFiles.get("keywords").add(file.getName());
        // }

        doc.add(new StringField("filename", file.getName(), Field.Store.YES));
        return doc;
    }

}