package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.jsoup.Jsoup;

/**
 * Service responsible for indexing HTML files.
 * Handles iterating through files, creating Lucene documents, and performing
 * batch indexing.
 */
@Service
public class JSONIndexer implements IIndexer {

    private static final Logger logger = LoggerFactory.getLogger(JSONIndexer.class);
    private final Map<String, List<String>> emptyFieldsTables = new HashMap<>();
    private final List<Long> filesTime = new ArrayList<>();
    private final List<Long> tablesTime = new ArrayList<>();

    @Value("${json.files.path}")
    private String jsonFilePath;

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private ProgressService progressService;

    @Override
    public void run() throws IOException {
        if (emptyFieldsTables.isEmpty()) {
            initializeEmptyFields();
        }
        progressService.startIndexing("json");
        indexFiles(jsonFilePath);
        progressService.completeIndexing("json");
    }

    @Override
    public void indexFiles(String directoryPath) throws IOException {
        logger.info("Starting JSON file indexing...");

        Directory directory = this.resourceManager.getDirectory("json");
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(resourceManager.getAnalyzer("json"));

        File[] files = new File(directoryPath).listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            logger.warn("No JSON files found in directory: {}", directoryPath);
            return;
        }

        long totalStartTime = System.nanoTime();
        int totalFiles = files.length;

        try (IndexWriter writer = new IndexWriter(directory, indexWriterConfig)) {
            for (File file : files) {
                JsonNode rootNode = parseJsonFile(file);
                if (rootNode != null && rootNode.isObject()) {  
                    long fileStartTime = System.nanoTime();

                    processFile(file, rootNode, writer);
                    
                    long fileEndTime = System.nanoTime();
                    this.filesTime.add((fileEndTime - fileStartTime) / 1_000_000);
                    logger.info("Indexed file: {} (Total Files Indexed: {}), File Time: {}ms",
                        file.getName(), this.filesTime.size(), (fileEndTime - fileStartTime) / 1_000_000);  // i'm using the size of time lists to count files and tables
                } else {
                    logger.warn("Invalid JSON structure in file: {}", file.getName());
                }

                int progress = (int) (((double) this.filesTime.size() / totalFiles) * 100);
                progressService.setProgress(progress);
                
            }
        }
        long endTime = System.nanoTime();
        logger.info("Total JSON indexing time: {}ms", (endTime - totalStartTime) / 1_000_000);

        logProcessingSummary();
    }

    private void processFile(File file, JsonNode rootNode, IndexWriter writer) throws IOException {
        rootNode.fieldNames().forEachRemaining(entryKey -> {
            try {
                JsonNode entryNode = rootNode.get(entryKey);
                
                if (entryNode.get("table") != null) {
                    long tableStartTime = System.nanoTime();

                    processTable(entryKey, entryNode, writer);

                    long tableEndTime = System.nanoTime();
                    this.tablesTime.add((tableEndTime - tableStartTime) / 1_000_000); 

                    logger.info("Indexed table: {} (Total Tables Indexed: {}), Table Time: {}ms",
                            entryKey, this.tablesTime.size(), (tableEndTime - tableStartTime) / 1_000_000);
                } else {
                    logger.warn("Skipping table {} in file {}: Missing 'table' field", entryKey, file.getName());
                }
            } catch (Exception e) {
                logger.error("Error indexing entry in file {}: {}", file.getName(), e.getMessage());
            }
        });
    }

    private void processTable(String tableId, JsonNode entryNode, IndexWriter writer) throws IOException {
        JsonNode tableNode = entryNode.get("table");
        JsonNode captionNode = entryNode.get("caption");
        JsonNode footnotesNode = entryNode.get("footnotes");
        JsonNode referencesNode = entryNode.get("references");

        Document doc = createDocumentFromTable(tableId, tableNode, captionNode, footnotesNode, referencesNode);
        writer.addDocument(doc);
        writer.commit();
    }

    private void logProcessingSummary() {
        // Calculate and log average processing time for files and tables 
        calculateAndLogAverageTimes();
        
        // Log empty field statistics for tables
        logEmptyFieldsSummary();
    }

    private void calculateAndLogAverageTimes() {
        if (!this.filesTime.isEmpty()) {
            double averageFileTime = this.filesTime.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
            logger.info("Average Processing time per file: {} ms", String.format("%.2f", averageFileTime));
        } else {
            logger.warn("No files were processed.");
        }

        if (!this.tablesTime.isEmpty()) {
            double averageTableTime = this.tablesTime.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
            logger.info("Average Processing time per table: {} ms", String.format("%.2f", averageTableTime));
        } else {
            logger.warn("No tables were processed.");
        }
    }

    private void logEmptyFieldsSummary() {
        if (tablesTime.isEmpty()) {
            logger.warn("No tables were processed, skipping empty fields analysis...");
            return;
        }

        emptyFieldsTables.forEach((field, tablesWithEmptyField) -> {
            if (!tablesWithEmptyField.isEmpty()) {
                double percentage = (tablesWithEmptyField.size() * 100.0) / this.tablesTime.size();

                logger.warn("Tables with empty {} ({} files, {}% of total tables)",
                    field, tablesWithEmptyField.size(), String.format("%.2f", percentage));   
            } else {
                logger.info("No tables with empty {}", field);
            }
        });
    }  

    private void initializeEmptyFields() {
        String[] fields = this.resourceManager.getSearchFields("json");
        for (String field : fields) {
            emptyFieldsTables.put(field, new ArrayList<>());
        }
    }

    private JsonNode parseJsonFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(file);
    }

    // ######################## PARTE TERRY CON L'INDICIZZAZIONE DI TUTTI I CAMPI
    // DEGLI ANALYZER##########################à
    private Document createDocumentFromTable(String tableId, JsonNode tableNode, JsonNode captionNode, JsonNode footnotesNode, JsonNode referencesNode ) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("tableId", tableId, Field.Store.YES));

        // Estrai e indicizza il campo "table":

        // Questo codice estrae il contenuto del campo table, pulisce i dati (es.
        // rimuove tag HTML) e li concatena in una stringa leggibile e indicizzabile.
        // 3 cose principali:
        // 1) iterazione su righe e celle -> Scorre ogni riga della tabella e ogni cella
        // all'interno della riga.
        // 2) pulizia html -> Usa Jsoup.parse(cell.asText()).text() per rimuovere tag
        // HTML.
        // 3) concatenzaione -> aggiunge il testo pulito di ogni cella a una stringa
        // cumulativa (table)

        if (tableNode != null && tableNode.isArray()) {
            // Pulizia e unione di celle/tabella in modo efficiente
            String tableContent = cleanAndJoinTable(tableNode);
            doc.add(new TextField("tableContent", tableContent, Field.Store.NO)); // Campo analizzato per la ricerca
        } else {
            emptyFieldsTables.get("tableContent").add(tableId);
        }

        // Estrai e indicizza il campo "caption"
        if (captionNode != null && !captionNode.asText().isEmpty()) {
            doc.add(new TextField("caption", captionNode.asText(), Field.Store.YES)); // Campo analizzato
        } else {
            emptyFieldsTables.get("caption").add(tableId);
        }

        // Estrai e indicizza il campo "footnotes"
        if (footnotesNode != null && footnotesNode.isArray()) {
            String footnotesText = String.join(" ", convertJsonArrayToList(footnotesNode));
            doc.add(new TextField("footnotes", footnotesText, Field.Store.NO)); // Campo analizzato
        } else {
            emptyFieldsTables.get("footnotes").add(tableId);
        }

        // Estrai e indicizza il campo "references"
        if (referencesNode != null && referencesNode.isArray()) {
            String referencesText = String.join(" ", convertJsonArrayToList(referencesNode));
            doc.add(new TextField("references", referencesText, Field.Store.NO)); // Campo analizzato
        } else {
            emptyFieldsTables.get("references").add(tableId);
        }

        return doc;
    }

    /**
     * Pulisce e concatena i contenuti delle celle di una tabella.
     */
    private String cleanAndJoinTable(JsonNode tableNode) {
        StringBuilder tableText = new StringBuilder();
        tableNode.forEach(row -> row.forEach(cell -> {
            tableText.append(Jsoup.parse(cell.asText()).text()).append(" ");
        }));
        return tableText.toString().trim();
    }

    /**
     * Converte un array JSON in una lista di stringhe.
     */
    private List<String> convertJsonArrayToList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        arrayNode.forEach(element -> list.add(element.asText()));
        return list;
    }
}
