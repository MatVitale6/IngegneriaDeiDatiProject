package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;


/**
 * Service responsible for indexing JSON files.
 * <p>
 * The {@code JSONIndexer} processes JSON files, extracting relevant data and creating 
 * Lucene {@link Document} objects to be indexed. It supports handling of multiple tables 
 * in a single JSON file, dynamic progress tracking, and logging of empty fields and performance statistics.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Iterates through JSON files and extracts tables, captions, footnotes, and references.</li>
 *   <li>Generates and adds Lucene {@link Document} objects for each table.</li>
 *   <li>Tracks progress dynamically and logs average processing times for files and tables.</li>
 *   <li>Logs statistics on empty fields within tables for better data quality monitoring.</li>
 * </ul>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Handles JSON structures with multiple tables in a single file.</li>
 *   <li>Ensures proper progress updates using {@link ProgressService}.</li>
 *   <li>Logs detailed performance metrics, including processing times for files and tables.</li>
 *   <li>Detects and logs missing or empty fields in JSON tables.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <p>
 * The {@link #run()} method is the main entry point to start the indexing process. It initializes
 * the state, iterates through the files, and invokes internal methods to process each file and table.
 * </p>
 *
 * @see IIndexer
 * @see ResourceManager
 * @see ProgressService
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

    @Autowired
    private Optional<EmbeddingServerService> embeddingServerService;

    @PostConstruct
    private void initialize() {
        embeddingServerService.ifPresentOrElse(service -> {
            logger.info("Enabling embedding indexing...");
            int retries = 20;
            int waitTimeMs = 10000;

            for (int i = 0; i < retries; i++) {
                if (service.isServerRunning()) {
                    logger.info("BERT Server is ready.");
                    return;
                }
                logger.warn("BERT Server not ready. Retrying... ({}/{})", i+1, retries);
                try {
                    Thread.sleep(waitTimeMs);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for BERT server to become ready.", e);
                    Thread.currentThread().interrupt();
                    return;
                }   
            }
            logger.error("BERT server did not become ready within the expected time.");
            throw new RuntimeException("Failed to initialize embedding server: Server health check failed.");
        }, () -> {
            logger.info("Embedding indexing is disabled.");
        });
    }

    @Override
    public void run() throws IOException {
        initializeEmptyFields();
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

    /**
     * Processes an individual JSON file, extracting tables and creating Lucene {@link Document}s.
     *
     * @param file      the JSON file being processed
     * @param rootNode  the root JSON node representing the file's content
     * @param writer    the {@link IndexWriter} used for adding documents
     * @throws IOException if an I/O error occurs during table processing
     */
    private void processFile(File file, JsonNode rootNode, IndexWriter writer) throws IOException {
        rootNode.fieldNames().forEachRemaining(entryKey -> {
            try {
                JsonNode entryNode = rootNode.get(entryKey);
                
                if (entryNode.get("table") != null) {
                    long tableStartTime = System.nanoTime();

                    processTable(file, entryKey, entryNode, writer);

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

    /**
     * Processes a specific table within a JSON file, creating a Lucene {@link Document}.
     *
     * @param tableId   the identifier of the table being processed
     * @param entryNode the JSON node representing the table's content
     * @param writer    the {@link IndexWriter} used for adding documents
     * @throws IOException if an I/O error occurs during document creation
     */
    private void processTable(File file, String tableId, JsonNode entryNode, IndexWriter writer) throws IOException {
        JsonNode tableNode = entryNode.get("table");
        JsonNode captionNode = entryNode.get("caption");
        JsonNode footnotesNode = entryNode.get("footnotes");
        JsonNode referencesNode = entryNode.get("references");

        Document doc = createDocumentFromTable(file, tableId, tableNode, captionNode, footnotesNode, referencesNode);
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
    private Document createDocumentFromTable(File file, String tableId, JsonNode tableNode, JsonNode captionNode, JsonNode footnotesNode, JsonNode referencesNode ) throws IOException {
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

        if (tableNode != null) {
            //Field type personalizzatoss
            FieldType storedOnlyType = new FieldType();
            storedOnlyType.setStored(true);  // Il valore sarà memorizzato
            storedOnlyType.setIndexOptions(IndexOptions.NONE);  // Non sarà indicizzato
            storedOnlyType.freeze();  // Blocca il FieldType per renderlo immutabile
            doc.add(new Field("tableContent", tableNode.asText(), storedOnlyType));
        } else {
            // this should not be possible cause of cleaner.py
            logger.warn("Missing or empty 'tableContent' for tableId: {}", tableId);
            emptyFieldsTables.get("tableContent").add(tableId);
        }

        // Estrai e indicizza il campo "caption"
        if (captionNode != null && !captionNode.asText().isEmpty()) {
            doc.add(new TextField("caption", captionNode.asText(), Field.Store.YES)); // Campo analizzato

            embeddingServerService.ifPresent(service -> {
                try {
                    float[] captionEmbedding = service.computeEmbedding(captionNode.asText());
                    doc.add(new BinaryDocValuesField("caption_vector", toBytesRef(captionEmbedding)));
                } catch (RuntimeException e) {
                    logger.error("Failed to compute embedding fro caption in tableId {}. Error: {}", tableId, e.getMessage());
                }
            });
        } else {
            logger.warn("Missing or empty 'caption' for tableId: {}", tableId);
            emptyFieldsTables.get("caption").add(tableId);
        }

        // Estrai e indicizza il campo "footnotes"
        if (hasNonEmptyValues(referencesNode)) {
            String footnotesText = String.join(" ", convertJsonArrayToList(footnotesNode));
            doc.add(new TextField("footnotes", footnotesText, Field.Store.YES)); // Campo analizzato
        } else {
            logger.warn("Missing or empty 'footnotes' for tableId: {}", tableId);
            emptyFieldsTables.get("footnotes").add(tableId);
        }

        // Estrai e indicizza il campo "references"
        if (hasNonEmptyValues(referencesNode)) {
            String referencesText = String.join(" ", convertJsonArrayToList(referencesNode));
            doc.add(new TextField("references", referencesText, Field.Store.YES)); // Campo analizzato

            embeddingServerService.ifPresent(service -> {
                try{
                    float[] referencesEmbedding = service.computeEmbedding(referencesText);
                    doc.add(new BinaryDocValuesField("references_vector", toBytesRef(referencesEmbedding)));
                } catch (RuntimeException e) {
                    logger.error("Failed to compute embedding for references in tableId: {}. Error: {}", tableId, e.getMessage());
                }            
            });
        } else {
            logger.warn("Missing or empty 'footnotes' for tableId: {}", tableId);
            emptyFieldsTables.get("references").add(tableId);
        }
        doc.add(new StringField("filename", file.getName(), Field.Store.YES));
        return doc;
    }

    private BytesRef toBytesRef(float[] vector) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(vector.length * Float.BYTES);
        for (float value : vector) {
            byteBuffer.putFloat(value);
        }
        return new BytesRef(byteBuffer.array());
    }

    /**
     * Converte un array JSON in una lista di stringhe, gestendo anche array nidificati.
     *
     * @param arrayNode Il nodo JSON che rappresenta un array (può contenere valori semplici o array nidificati).
     * @return Una lista piatta di stringhe con tutti i valori presenti nell'array e nei sotto-array.
     */
    private List<String> convertJsonArrayToList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode.isArray()) {
            arrayNode.forEach(element -> {
                if (element.isArray()) {
                    list.addAll(convertJsonArrayToList(element));
                } else {
                    list.add(element.asText());
                }
            });
        }
        return list;
    }

    private boolean hasNonEmptyValues(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return false;
        }
        for (JsonNode element : arrayNode) {
            if (element.isArray()) {
                if (hasNonEmptyValues(element)) {
                    return true;
                }
            } else if (!element.asText().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
