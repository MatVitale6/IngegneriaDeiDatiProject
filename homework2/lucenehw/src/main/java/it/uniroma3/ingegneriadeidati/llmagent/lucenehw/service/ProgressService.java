package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ProgressService {

    private volatile int progress = 0;

    @Value("${lucene.index.path}")
    private String indexDirectory;

    private static final String INDEXING_COMPLETE_FLAG = "indexing_complete.flag";

    @PostConstruct
    public void initializeProgress() {
        File flagFile = new File(indexDirectory, INDEXING_COMPLETE_FLAG);
        if (flagFile.exists()) {
            progress = 100;
        } else {
            progress = 0;
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void markIndexingComplete() {
        File flagFile = new File(indexDirectory, INDEXING_COMPLETE_FLAG);
        try {
            if (flagFile.createNewFile()) {
                this.progress = 100;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to mark indexing as complete", e);
        } 
    }

    public void resetProgress() {
        File flagFile = new File(indexDirectory, INDEXING_COMPLETE_FLAG);
        if (flagFile.exists()) {
            flagFile.delete();
        }
        this.progress = 0;
    }


}
