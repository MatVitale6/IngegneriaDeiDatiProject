package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;
import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FIleIndexer {

    private static final int BATCH_SIZE = 500;

    @Autowired
    private IndexWriter indexWriter;

    public void indexHtmlFiles(String directoryPath) throws IOException {
        // Read, parse html, extract fields
    }

}
