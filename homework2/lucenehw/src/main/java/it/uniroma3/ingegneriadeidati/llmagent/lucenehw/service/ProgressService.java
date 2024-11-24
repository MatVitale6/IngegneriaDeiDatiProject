package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import jakarta.annotation.PostConstruct;

@Service
public class ProgressService {
    private final static Logger logger = LoggerFactory.getLogger(ProgressService.class);

    private volatile int progress = 0;

    @Autowired 
    private ResourceManager resourceManager;

    public void initializeProgress(String resourceType) {
        boolean isComplete = this.resourceManager.isIndexingComplete(resourceType);
        this.progress = isComplete ? 100 : 0;
    }

    /**
     * Retrieves the overall progress from the ResourceManager.
     * 
     * @return Progress percentage (0 to 100).
     */
    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void resetProgress() {
        this.progress = 0;
    }
}
