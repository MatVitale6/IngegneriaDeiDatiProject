package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import jakarta.annotation.PostConstruct;

@Service
public class ProgressService {
    private final static Logger logger = LoggerFactory.getLogger(ProgressService.class);

    private ResourceManager resourceManager;
    private volatile int progress = 0;
    private volatile String currentResourceType = null;

    @Autowired
    public ProgressService(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @PostConstruct
    public void initializeProgress() {
        if (this.resourceManager.isIndexingComplete("html") && this.resourceManager.isIndexingComplete("json")) {
            this.progress = 100;
            this.currentResourceType = null;
        } else {
            this.progress = 0;
            this.currentResourceType = null;
        }
    }

    public void startIndexing(String resourceType) {
        this.currentResourceType = resourceType;
        this.progress = 0;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void completeIndexing(String resourceType) {
        try {
            this.resourceManager.markIndexingComplete(resourceType);

            if (this.resourceManager.isIndexingComplete("html") && this.resourceManager.isIndexingComplete("json")) {
                this.progress = 100;
                this.currentResourceType = null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to mark indexing complete for " + resourceType, e);
        }
    }

    public int getProgress() {
        return this.progress;
    }

    public String getCurrentResourceType() {
        return this.currentResourceType;
    }

    public void resetProgress() {
        this.progress = 0;
        this.currentResourceType = null;
    }
}
