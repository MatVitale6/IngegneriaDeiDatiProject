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
 * Handles iterating through files, creating Lucene documents, and performing batch indexing.
 */
@Service
public class JSONIndexer implements IIndexer {

    private static final Logger logger = LoggerFactory.getLogger(JSONIndexer.class);
    private Map<String, List<String>> emptyFieldsFiles;
    
    @Value("${json.files.path}")
    private String jsonFilePath;

    @Autowired
    private ResourceManager resourceManager;
    
    @Autowired
    private ProgressService progressService;

    public JSONIndexer() {
        this.emptyFieldsFiles = new HashMap<>();
    }
    
    @Override
    public void run() throws IOException {
        initializeEmptyFields();
        indexFiles(jsonFilePath);
    }

    @Override
    public void indexFiles(String directoryPath) throws IOException {
        logger.info("Starting JSON file indexing...");
        progressService.resetProgress();

        Directory directory = this.resourceManager.getDirectory("json");
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(resourceManager.getAnalyzer("json"));

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
                
                JsonNode jsonNode = parseJsonFile(file);
                JsonNode tableNode = jsonNode.get("table");

                if (tableNode != null) {
                    String tableId = jsonNode.get("tableId").asText();
                    Document doc = createDocumentFromTable(tableId, tableNode);

                    writer.addDocument(doc);
                    writer.commit();
                    
                    totalIndexed++;
                    long fileEndTime = System.nanoTime();
                    logger.info("Indexed JSON file: {} (Total Indexed: {}), File Time: {}ms",
                        file.getName(), totalIndexed, (fileEndTime - fileStartTime)/ 1_000_000);
                    
                    int progress = (int) (((double) totalIndexed / totalFiles) * 100);
                    progressService.setProgress(progress);
                } else {
                    logger.warn("Skipping file {}: Missing 'table' field", file.getName());
                }
            }
        }

        progressService.setProgress(100);
        progressService.markIndexingComplete();
        long endTime = System.nanoTime();
        logger.info("Total JSON indexing time: {}ms", (endTime - startTime) / 1_000_000);
    }

    private void initializeEmptyFields() {
        String[] fields = this.resourceManager.getSearchFields("json");
        for (String field : fields) {
            emptyFieldsFiles.put(field, new ArrayList<>());
        }
    }

    private JsonNode parseJsonFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(file);
    }

    
    
    //######################## PARTE TERRY CON L'INDICIZZAZIONE DI TUTTI I CAMPI DEGLI ANALYZER##########################à
    public Document createDocumentFromTable(String tableId, JsonNode tableData) throws IOException {
        Document doc = new Document();
        
        doc.add(new StringField("tableId", tableId, Field.Store.YES));

        // Estrai e indicizza il campo "table":

        //Questo codice estrae il contenuto del campo table, pulisce i dati (es. rimuove tag HTML) e li concatena in una stringa leggibile e indicizzabile.
        // 3 cose principali:
        //1) iterazione su righe e celle -> Scorre ogni riga della tabella e ogni cella all'interno della riga.
        //2) pulizia html -> Usa Jsoup.parse(cell.asText()).text() per rimuovere tag HTML.
        //3) concatenzaione -> aggiunge il testo pulito di ogni cella a una stringa cumulativa (table) 
        JsonNode tableNode = tableData.get("table");
        if (tableNode != null && tableNode.isArray()) {
            // Pulizia e unione di celle/tabella in modo efficiente
            String tableContent = cleanAndJoinTable(tableNode);
            doc.add(new TextField("tableContent", tableContent, Field.Store.NO)); // Campo analizzato per la ricerca
        } else {
            emptyFieldsFiles.get("tableContent").add(tableId);
        }

        // Estrai e indicizza il campo "caption"
        JsonNode captionNode = tableData.get("caption");
        if (captionNode != null && !captionNode.asText().isEmpty()) {
            doc.add(new TextField("caption", captionNode.asText(), Field.Store.YES)); // Campo analizzato
        } else {
            emptyFieldsFiles.get("caption").add(tableId);
        }

        // Estrai e indicizza il campo "footnotes"
        JsonNode footnotesNode = tableData.get("footnotes");
        if (footnotesNode != null && footnotesNode.isArray()) {
            String footnotesText = String.join(" ", convertJsonArrayToList(footnotesNode));
            doc.add(new TextField("footnotes", footnotesText, Field.Store.NO)); // Campo analizzato
        } else {
            emptyFieldsFiles.get("footnotes").add(tableId);
        }

        // Estrai e indicizza il campo "references"
        JsonNode referencesNode = tableData.get("references");
        if (referencesNode != null && referencesNode.isArray()) {
            String referencesText = String.join(" ", convertJsonArrayToList(referencesNode));
            doc.add(new TextField("references", referencesText, Field.Store.NO)); // Campo analizzato
        } else {
            emptyFieldsFiles.get("references").add(tableId);
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
