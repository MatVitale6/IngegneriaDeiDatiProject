package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.FileIndexer;

/**
 * Classe principale dell'applicazione Lucenehw.
 * 'LucenehwApplication' avvia l'applicazione Spring Boot e gestisce la configurazione iniziale.
 * É esclusa la configurazione automatica del 'DataSource', dato che l'applicazione non utilizza un database relazionale.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LucenehwApplication {

    private static final Logger logger = LoggerFactory.getLogger(LucenehwApplication.class);

    @Autowired
    private ApplicationContext context;


    @Value("${lucene.index.path}")  
    private String indexDirectory;

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

    @Bean
    public CommandLineRunner runIndexer() {
        return args -> {
            Path flagFilePath = Paths.get(indexDirectory, "indexing_complete.flag");

            if (Files.exists(flagFilePath)){
                logger.info("Indexing has already completed. Skipping...");
                return;
            }
            if (context.containsBean("fileIndexer")) {
                FileIndexer indexer = context.getBean(FileIndexer.class);
                logger.info("Starting indexing process...");
                indexer.run();
            } else {
                logger.info("Indexer startup run is disabled");
            }
        };
    }
}           
