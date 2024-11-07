package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LucenehwApplication {

	public static void main(String[] args) {
        SpringApplication.run(LucenehwApplication.class, args);

        LucenehwApplication application = new LucenehwApplication();
        // TODO: Run the indexer  
	}

}
