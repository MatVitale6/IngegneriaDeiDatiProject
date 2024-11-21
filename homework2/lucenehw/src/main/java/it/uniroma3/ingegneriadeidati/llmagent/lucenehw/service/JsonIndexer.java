package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;


@Service
public class JsonIndexer {

    private static final Logger logger = LoggerFactory.getLogger(JsonIndexer.class);
    private Map<String, List<String>> emptyFieldsFiles = new HashMap<>();

    @Value("${json.files.path}")
    private String jsonFilePath;

    @Autowired
    private Directory directory;

    @Autowired
    private IndexWriterConfig indexWriterConfig;

    @Autowired
    private ProgressService progressService;

    public JsonIndexer() {
        emptyFieldsFiles.put("table", new ArrayList<>());
        emptyFieldsFiles.put("caption", new ArrayList<>());
        emptyFieldsFiles.put("footnotes", new ArrayList<>());
        emptyFieldsFiles.put("references", new ArrayList<>());
    }

    public void run() {
        try {
            this.indexJsonFiles(jsonFilePath);
        } catch (IOException e) {
            logger.error("Error during file indexing ", e);
        }
    }

    public void indexJsonFiles(String directoryPath) throws IOException {
        logger.info("Starting JSON file indexing...");
        progressService.resetProgress();

        File[] files = new File(directoryPath).listFiles((dir, name) -> name.endsWith(".json"));
        if(files == null || files.length == 0) {
            logger.warn("No JSON files found in directory: {}", directoryPath);
            return;
        }

        long startTime = System.nanoTime();
        int totalIndexed = 0;
        int totalFiles = files.length;

        try(IndexWriter writer = new IndexWriter(directory, indexWriterConfig)) {
            for(File file : files) {
                long fileStartTime = System.nanoTime();

                List<Document> documents = createDocumentFromJsonFile(file);
                for (Document doc : documents) {
                    writer.addDocument(doc);
                }
                writer.commit();
                totalIndexed++;
                long fileEndTime = System.nanoTime();

                logger.info("Indexed JSON file: {} (Total Indexed: {}), File Time: {}ms",
                    file.getName(), totalIndexed, (fileEndTime - fileStartTime)/ 1_000_000);
                
                    int progress = (int) (((double) totalIndexed / totalFiles) * 100);
                    progressService.setProgress(progress);
            }
        }

        progressService.setProgress(100);
        progressService.markIndexingComplete();
        long endTime = System.nanoTime();
        logger.info("Total JSON indexing time: {}ms", (endTime - startTime) / 1_000_000);
    }

    public List<Document> createDocumentFromJsonFile(File file) throws IOException {
        List<Document> documents = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(file);

        rootNode.fields().forEachRemaining(entry -> {
            String tableId = entry.getKey();
            JsonNode tableData = entry.getValue();
    
            Document doc = new Document();
            doc.add(new StringField("tableId", tableId, Field.Store.YES));
    
            // Verifica se il nodo "table" esiste e non è null
            JsonNode tableNode = tableData.get("table");
            if (tableNode != null && !tableNode.asText().isEmpty()) {
                String tableHtml = tableNode.asText();
                doc.add(new TextField("tableHtml", tableHtml, Field.Store.YES));
            } else {
                // Se il nodo "table" non esiste o è vuoto, registra un avviso
                logger.warn("Empty table HTML for table ID: {}", tableId);
            }
    
            // Aggiungi altri campi con verifiche simili, se necessari
            JsonNode captionNode = tableData.get("caption");
            if (captionNode != null && !captionNode.asText().isEmpty()) {
                doc.add(new TextField("caption", captionNode.asText(), Field.Store.YES));
            }
    
            JsonNode footnotesNode = tableData.get("footnotes");
            if (footnotesNode != null && !footnotesNode.asText().isEmpty()) {
                doc.add(new TextField("footnotes", footnotesNode.asText(), Field.Store.YES));
            }
    
            JsonNode referencesNode = tableData.get("references");
            if (referencesNode != null && !referencesNode.asText().isEmpty()) {
                doc.add(new TextField("references", referencesNode.asText(), Field.Store.YES));
            }
    
            // Aggiungi il documento alla lista
            documents.add(doc);
        });
        return documents;
    }
    
}
