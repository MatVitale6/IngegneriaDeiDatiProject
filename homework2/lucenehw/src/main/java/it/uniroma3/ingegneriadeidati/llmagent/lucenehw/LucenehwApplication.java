package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Classe principale dell'applicazione Lucenehw.
 * 'LucenehwApplication' avvia l'applicazione Spring Boot e gestisce la configurazione iniziale.
 * É esclusa la configurazione automatica del 'DataSource', dato che l'applicazione non utilizza un database relazionale.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LucenehwApplication {

        /**
         * Metodo principale che avvia l'applicazione Spring Boot.
         * Esegue 'SpringApplication.run()' per inizializzare e configurare il contesto Spring.
         * É presente un'instanza della stessa classe 'LucenehwApplication' per avviare eventualmente
         * altri componenti, come l'indicizzazatore, una volta che l'applicazione é in esecuzione.
         * @param args argomenti della riga di comando
         */
	public static void main(String[] args) {
        SpringApplication.run(LucenehwApplication.class, args);

        LucenehwApplication application = new LucenehwApplication();
        // TODO: Run the indexer  
	}

}
