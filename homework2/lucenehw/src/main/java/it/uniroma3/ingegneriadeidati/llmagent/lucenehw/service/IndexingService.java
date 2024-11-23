package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;

/**
 * Service for handling the indexing process for different resource types.
 * This class manages the lifecycle of the indexing process, delegating tasks
 * to specific indexers and ensuring resources are properly prepared and marked
 * as indexed upon completion.
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
            
            logger.info("Starting indexing process for {}", type);
            indexer.run();
            resourceManager.markIndexingComplete(type);
            logger.info("Indexing completed for resource type: {}", type);
        } catch (IOException e) {
            logger.error("Error during {} indexing", type, e);
        } catch (IllegalArgumentException e) {
            logger.error("Resource type {} not found: {}", type, e.getMessage());
        }
    }

}
