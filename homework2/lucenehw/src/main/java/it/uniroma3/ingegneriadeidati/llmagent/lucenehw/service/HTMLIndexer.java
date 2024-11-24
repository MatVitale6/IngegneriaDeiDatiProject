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

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util.HTMLParserUtils;

/**
 * Service responsible for indexing HTML files.
 * Handles iterating through files, creating Lucene documents, and performing batch indexing.
 */
@Service
public class HTMLIndexer implements IIndexer{

    private static final Logger logger = LoggerFactory.getLogger(HTMLIndexer.class);
    private final Map<String, List<String>> emptyFieldsFiles = new HashMap<>();

    @Value("${html.files.path}")
    private String htmlFilesPath;

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private ProgressService progressService;

    public HTMLIndexer() {
        emptyFieldsFiles.put("title", new ArrayList<>());
        emptyFieldsFiles.put("authors", new ArrayList<>());
        emptyFieldsFiles.put("abstract", new ArrayList<>());
        emptyFieldsFiles.put("content", new ArrayList<>());
        // emptyFieldsFiles.put("keywords", new ArrayList<>());
    }

    @Override
    public void run() throws IOException {
        indexFiles(htmlFilesPath);
    }

    /**
     * Metodo per indicizzare tutti i file HTML presenti nella directory specificata.
     * @param directoryPath Percorso della directory contenente i file HTML da indicizzare.
     * @throws IOException se si verifica un errore di I/O durante l'indicizzazione
     */
    @Override
    public void indexFiles(String directoryPath) throws IOException {
        logger.info("Starting HTML file indexing...");
        progressService.resetProgress();

        Directory directory = this.resourceManager.getDirectory("html");
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(resourceManager.getAnalyzer("html"));

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
                writer.commit();     
                totalIndexed++;
                long fileEndTime = System.nanoTime();
    
                logger.info("Indexed file: {} (Total Indexed: {}), File Time: {}ms",
                        file.getName(), totalIndexed, (fileEndTime - fileStartTime) / 1_000_000);
                
                int progress = (int) (((double) totalIndexed / totalFiles) * 100);
                progressService.setProgress(progress);
            }
        }
        progressService.setProgress(100);
        long endTime = System.nanoTime();        
        logger.info("Total indexing time: {}ms", (endTime - startTime) / 1_000_000);

        logEmptyFieldsSummary(totalFiles);
    }

    private void logEmptyFieldsSummary(int totalFiles) {
        emptyFieldsFiles.forEach((field, filesWithEmptyField) -> {
            if (!filesWithEmptyField.isEmpty()) {
                double percentage = (filesWithEmptyField.size() * 100.0) / totalFiles;
                logger.warn("Files with empty {} ({} files, {}% of total files)",
                    field, filesWithEmptyField.size(), String.format("%.2f", percentage));   
            } else {
                logger.info("No files with empty {}", field);
            }
        });
    }  

    /**
     * Creates a Lucene document for each HTML file.
     * Uses {@link HTMLParserUtils} to extract relevant content from the HTML file and adds it as fields.
     *
     * @param file the HTML file to convert to a document
     * @return a {@link Document} containing the extracted fields
     * @throws IOException if an error occurs while reading the file
     */
    public Document createDocumentFromFile(File file) throws IOException {
        Document doc = new Document();

        String title = HTMLParserUtils.parseTitle(file);
        if (!title.isEmpty()) {
            doc.add(new TextField("title", title, Field.Store.YES));
        } else {
            emptyFieldsFiles.get("title").add(file.getName());
        }

        String authors = HTMLParserUtils.parseAuthors(file);
        if (!authors.isEmpty()) {
            doc.add(new TextField("authors", authors, Field.Store.YES));
        } else {
            emptyFieldsFiles.get("authors").add(file.getName());
        }

        String content = HTMLParserUtils.parseContent(file);
        if (!content.isEmpty()) {
            doc.add(new TextField("content", content, Field.Store.YES));
        } else {
            emptyFieldsFiles.get("content").add(file.getName());
        }

        String abstractText = HTMLParserUtils.parseAbstract(file);
        if (!abstractText.isEmpty()) {
             doc.add(new TextField("abstract", abstractText, Field.Store.YES));
        } else {
            emptyFieldsFiles.get("abstract").add(file.getName());
        }

        doc.add(new StringField("filename", file.getName(), Field.Store.YES));
        return doc;
    }
}
