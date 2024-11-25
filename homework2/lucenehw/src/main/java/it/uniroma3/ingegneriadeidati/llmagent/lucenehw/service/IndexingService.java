package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;

/**
 * Service for managing the indexing process for different resource types.
 * <p>
 * The {@code IndexingService} acts as a controller for the lifecycle of the indexing process. 
 * It delegates specific tasks to appropriate implementations of the {@link IIndexer} interface 
 * based on the resource type (e.g., "json", "html") and ensures that the indexing process is 
 * completed correctly. This class supports the dynamic selection of indexing strategies 
 * and promotes extensibility by relying on the {@link ResourceManager} to retrieve indexers.
 * </p>
 * 
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Fetch the appropriate {@link IIndexer} instance for a given resource type from the {@link ResourceManager}.</li>
 *   <li>Execute the indexing process using the selected {@link IIndexer}.</li>
 *   <li>Handle errors gracefully, such as invalid resource types or indexing failures.</li>
 * </ul>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Ensures loose coupling between resource-specific indexing logic and the overall application.</li>
 *   <li>Relies on the {@link ResourceManager} to manage and register resource-specific indexers.</li>
 *   <li>Facilitates the addition of new resource types without modifying existing code (Open/Closed Principle).</li>
 * </ul>
 * 
 * <h3>Usage:</h3>
 * To perform indexing for a specific resource type, call the {@link #runIndexingForType(String)} method 
 * with the desired resource type (e.g., "json", "html").
 * 
 * <h3>Design Pattern:</h3>
 * This class is an example of the Strategy pattern in action. It dynamically selects the appropriate 
 * strategy (i.e., {@link IIndexer} implementation) for indexing at runtime, promoting flexibility and reusability.
 * 
 * @see IIndexer
 * @see ResourceManager
 * @see it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.JSONIndexer
 * @see it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.HTMLIndexer
 */
@Service
public class IndexingService {

    private static Logger logger = LoggerFactory.getLogger(IndexingService.class);

    @Autowired
    private ResourceManager resourceManager;

    public void runIndexingForType(String type) {
        try {
            IIndexer indexer = resourceManager.getIndexer(type);

            if (indexer == null) {
                logger.warn("No indexer registered for resource type {}", type);
                return;
            }
            indexer.run();
        } catch (IOException e) {
            logger.error("Error during {} indexing", type, e);
        } catch (IllegalArgumentException e) {
            logger.error("Resource type {} not found: {}", type, e.getMessage());
        }
    }
}
