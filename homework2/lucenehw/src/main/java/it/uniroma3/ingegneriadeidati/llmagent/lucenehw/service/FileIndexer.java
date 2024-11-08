package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util.HTMLParserUtils;

/**
 * Servizio responsabile dell'indicizzazione dei file HTML.
 * La classe 'FileIndexer gestisce l'iterazione dei file, la creazione dei documenti per Lucene e l'indicizzazione in batch.
 */
@Service
public class FileIndexer {

    /**
     * Dimensione del batch per l'indicizzazione, per ridurre le operazioni di commit frequenti e migliorare le performance.
     */
    private static final int BATCH_SIZE = 500;
    /**
     * Logger per registrare le informazioni relative al processo di indicizzazione.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileIndexer.class);

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

    /**
     * Metodo per indicizzare tutti i file HTML presenti nella directory specificata.
     * L' indicizzazione avviene in batch per migliorare l'efficenza.
     * @param directoryPath Percorso della directory contenente i file HTML da indicizzare.
     * @throws IOException se si verifica un errore di I/O durante l'indicizzazione
     */
    public void indexHtmlFiles(String directoryPath) throws IOException {
        logger.info("Starting HTML file indexing...");

        long startTime = System.nanoTime();
        int totalIndexed = 0;

        //Trova tutti i file HTML nella directory specificata
        File[] files = new File(directoryPath).listFiles((dir, name) -> name.endsWith(".html"));
        if (files == null || files.length == 0) {
            logger.warn("No HTML files found in directory: {}", directoryPath);
            return;
        };
        
        //Indicizzazione in batch
        List<File> batch = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            batch.add(files[i]);

            //Processa il batch quando raggiunge la dimensione specificata o é l'ultimo file
            if (batch.size() == BATCH_SIZE || i == files.length - 1) {
                long batchStartTime = System.nanoTime();

                indexBatch(batch);
                totalIndexed += batch.size();

                long batchEndTime = System.nanoTime();
                logger.info("Indexed batch of {} documents (Total Indexed: {}), Batch Time: {}ms",
                        batch.size(), totalIndexed, (batchEndTime - batchStartTime) / 1_000_000);
                batch.clear();
            }
        }

        long endTime = System.nanoTime();
        logger.info("Total indexing time: {}ms", (endTime - startTime) / 1_000_000);
    }    


    /**
     * Indicizza un batch di file.
     * Crea un 'IndexWriter' per aggiungere i documenti all'indice Lucene.
     * @param batch Lista di file da indicizzare.
     * @throws IOException se si verifica un errore durante l'indicizzazione.
     */
    private void indexBatch(List<File> batch) throws IOException {
        try (IndexWriter writer = new IndexWriter(directory, indexWriterConfig)) {
            for (File file : batch) {
                Document doc = createDocumentFromFile(file);
                writer.addDocument(doc);
            }
            writer.commit();     //effettua il commit al termine del batch
        }
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
        if (title != null && !title.isEmpty()) {
            doc.add(new TextField("title", title, Field.Store.YES));
        }

        //Estrae e aggiunge gli autori
        List<String> authors = HTMLParserUtils.parseAuthors(file);
        if (authors != null && !authors.isEmpty()) {
            for (String author : authors) {
                doc.add(new TextField("authors", author, Field.Store.YES));
            }
        }

        //Estrae e aggiunge il contenuto del file
        String content = HTMLParserUtils.parseContent(file);
        if (content != null && !content.isEmpty()) {
            doc.add(new TextField("content", content, Field.Store.YES));
        }

        return doc;
    }
}
