package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.HTMLIndexer;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.JSONIndexer;

/**
 * This class provides the configuration for the Lucene indexing and searching functionalities.
 * It defines key components such as {@link ResourceManager}, which manages the resources
 * required for indexing and searching operations, and {@link IndexWriterConfig}, which 
 * configures the writing process for Lucene indexes.
 * 
 * The configuration includes:
 * <ul>
 *   <li>Registration of resources (e.g., "html" and "json") in the {@link ResourceManager}.</li>
 *   <li>Setup of analyzers and directories for resource-specific indexing.</li>
 *   <li>Configuration of {@link IndexWriterConfig} with default analyzers.</li>
 * </ul>
 * 
 * This class ensures the modular and scalable setup of indexing components, making it easy 
 * to add or update resource-specific configurations.
 */
@Configuration
public class LuceneConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(LuceneConfig.class);

    /**
     * Creates and configures the {@link ResourceManager} bean.
     * The {@link ResourceManager} is responsible for managing all resource-specific configurations,
     * such as index directories, analyzers, and search fields.
     * 
     * Registers the following resources:
     * <ul>
     *   <li>HTML Resource:
     *     <ul>
     *       <li>Indexer: {@link HTMLIndexer}</li>
     *       <li>Analyzer: HTML-specific {@link Analyzer}</li>
     *       <li>Search Fields: "title", "authors", "content", "abstract"</li>
     *     </ul>
     *   </li>
     *   <li>JSON Resource:
     *     <ul>
     *       <li>Indexer: {@link JSONIndexer}</li>
     *       <li>Analyzer: JSON-specific {@link Analyzer}</li>
     *       <li>Search Fields: "tableId", "tableHtml", "caption", "footnotes"</li>
     *     </ul>
     *   </li>
     * </ul>
     * 
     * @return The configured {@link ResourceManager}.
     */
    @Bean
    @Lazy 
    public ResourceManager resourceManager() {
        ResourceManager resourceManager = new ResourceManager();

        resourceManager.registerResource(
            "html", 
            new HTMLIndexer(), 
            AnalyzerFactory.getAnalyzer("html"),
            resourceManager.prepareIndexDirectory("html"),
            new String[] {"title", "authors", "content", "abstract"}
        );

        resourceManager.registerResource(
            "json", 
            new JSONIndexer(), 
            AnalyzerFactory.getAnalyzer("json"),
            resourceManager.prepareIndexDirectory("json"),
            new String[] {"tableId", "tableHtml", "caption", "footnotes", "references"}    
        );

        logger.info("Resources registered: {}", resourceManager.getRegisteredTypes());
        return resourceManager;
    }
}
