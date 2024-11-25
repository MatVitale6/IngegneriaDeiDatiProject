package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import jakarta.annotation.PostConstruct;

/**
 * Service responsible for managing the progress of the indexing process.
 * <p>
 * The {@code ProgressService} tracks the current progress and resource type being indexed.
 * It ensures that the progress reflects the real-time state of the indexing process and checks 
 * whether indexing has been completed across multiple resource types.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Tracks the current progress of the indexing process as a percentage.</li>
 *   <li>Identifies the resource type currently being indexed.</li>
 *   <li>Initializes progress based on the state of indexing from a previous execution.</li>
 *   <li>Marks indexing as complete for a resource type and updates progress accordingly.</li>
 *   <li>Resets progress when necessary.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <p>
 * This service is primarily used by indexers ({@link IIndexer}) to dynamically update and 
 * track the progress of indexing operations. It interacts with the {@link ResourceManager} 
 * to manage resource-specific progress states.
 * </p>
 *
 * @see ResourceManager
 */
@Service
public class ProgressService {

    private ResourceManager resourceManager;
    private volatile int progress = 0;
    private volatile String currentResourceType = null;

    /**
     * Constructs a {@code ProgressService} with the required {@link ResourceManager}.
     *
     * @param resourceManager the resource manager to handle indexing state for resources
     */
    @Autowired
    public ProgressService(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Initializes the progress state based on the indexing completion status
     * for all resource types.
     * <p>
     * If indexing is complete for all resources, the progress is set to 100% 
     * and no resource type is marked as active. Otherwise, the progress is reset to 0.
     * </p>
     */
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
