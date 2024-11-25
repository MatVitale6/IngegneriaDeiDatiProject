package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.IndexingService;

/**
 * Main application class for the Lucenehw application.
 * <p>
 * This class serves as the entry point for the Spring Boot application. It handles
 * the initialization of the Spring context and manages the application's configuration.
 * The application is designed to work without a relational database, so automatic
 * {@code DataSource} configuration is excluded.
 * </p>
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Starts the Spring Boot application.</li>
 *   <li>Configures the indexing process to run at application startup.</li>
 *   <li>Manages dependencies like {@link ResourceManager} and {@link IndexingService}.</li>
 * </ul>
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LucenehwApplication {

    private static final Logger logger = LoggerFactory.getLogger(LucenehwApplication.class);

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private IndexingService indexingService;

    /**
     * Metodo principale che avvia l'applicazione Spring Boot.
     * Esegue 'SpringApplication.run()' per inizializzare e configurare il contesto Spring.
     * É presente un'istanza della stessa classe 'LucenehwApplication' per avviare eventualmente
     * altri componenti, come l'indicizzazatore, una volta che l'applicazione é in esecuzione.
     * @param args argomenti della riga di comando
     */
    public static void main(String[] args) {
        SpringApplication.run(LucenehwApplication.class, args);
    }

    /**
     * Configures a {@link CommandLineRunner} bean to handle the indexing process.
     * <p>
     * This method is executed after the Spring context is initialized. It iterates
     * through the registered resource types and checks if indexing is required for
     * each type. If indexing is incomplete, it triggers the indexing process using
     * {@link IndexingService}. If all resources are already indexed, it skips the
     * indexing process and logs this status.
     * </p>
     *
     * <h3>Behavior:</h3>
     * <ul>
     *   <li>Determines the indexing status for each registered resource type.</li>
     *   <li>Delegates indexing tasks to {@link IndexingService}.</li>
     *   <li>Logs information about the indexing status for transparency and debugging.</li>
     * </ul>
     *
     * @return a {@link CommandLineRunner} instance that manages the indexing process.
     */
    @Bean
    public CommandLineRunner runIndexing() {
        return args -> {
            boolean allIndexingComplete = true;

            for (String type : resourceManager.getRegisteredTypes()) {
                if (!resourceManager.isIndexingComplete(type)) {
                    logger.info("indexing required for {}", type);
                    indexingService.runIndexingForType(type);
                    allIndexingComplete = false;
                } else {
                    logger.info("Indexing already complete for {}", type);
                }
            }

            if (allIndexingComplete){
                logger.info("indexing already complete for all resources, skipping...");
            }
        };
    }
}           
