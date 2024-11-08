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

@Service
public class FileIndexer {

    private static final int BATCH_SIZE = 500;
    private static final Logger logger = LoggerFactory.getLogger(FileIndexer.class);

    @Autowired
    private Directory directory;
    
    @Autowired
    private IndexWriterConfig indexWriterConfig;


    public void indexHtmlFiles(String directoryPath) throws IOException {
        logger.info("Starting HTML file indexing...");

        long startTime = System.nanoTime();
        int totalIndexed = 0;

        File[] files = new File(directoryPath).listFiles((dir, name) -> name.endsWith(".html"));
        if (files == null || files.length == 0) {
            logger.warn("No HTML files found in directory: {}", directoryPath);
            return;
        };
        
        List<File> batch = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            batch.add(files[i]);

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

    private void indexBatch(List<File> batch) throws IOException {
        try (IndexWriter writer = new IndexWriter(directory, indexWriterConfig)) {
            for (File file : batch) {
                Document doc = createDocumentFromFile(file);
                writer.addDocument(doc);
            }
            writer.commit();
        }
    }

    public Document createDocumentFromFile(File file) throws IOException {
        Document doc = new Document();

        String title = HTMLParserUtils.parseTitle(file);
        if (title != null && !title.isEmpty()) {
            doc.add(new TextField("title", title, Field.Store.YES));
        }

        String authors = HTMLParserUtils.parseAuthors(file);
        if (authors != null && !authors.isEmpty()) {
            doc.add(new TextField("authors", authors, Field.Store.YES));
        }

        String content = HTMLParserUtils.parseContent(file);
        if (content != null && !content.isEmpty()) {
            doc.add(new TextField("content", content, Field.Store.YES));
        }

        return doc;
    }
}
