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
 * Classe principale dell'applicazione Lucenehw.
 * 'LucenehwApplication' avvia l'applicazione Spring Boot e gestisce la configurazione iniziale.
 * É esclusa la configurazione automatica del 'DataSource', dato che l'applicazione non utilizza un database relazionale.
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
     * This method ensures that the indexing process runs only for resources
     * where indexing is incomplete. It uses {@link ResourceManager} to determine
     * the indexing status and {@link IndexingService} to perform indexing tasks.
     * </p>
     * 
     * @return a {@link CommandLineRunner} instance to manage the indexing process
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
