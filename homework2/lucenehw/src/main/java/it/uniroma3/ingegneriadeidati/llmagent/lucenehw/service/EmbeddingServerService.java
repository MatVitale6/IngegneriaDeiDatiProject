package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PreDestroy;

@Service
@ConditionalOnProperty(name = "indexing.use.embeddings", havingValue = "true")
public class EmbeddingServerService {
    
    private final static Logger logger = LoggerFactory.getLogger(EmbeddingServerService.class);

    @Value("${python.server.url}")
    private String pythonServerUrl;

    private String containerId;

    public EmbeddingServerService() {
        try {
            startServer();
        } catch (IOException e) {
            logger.error("Failed to start BERT server: {}", e.getMessage());
        }
    }

    public boolean isServerRunning() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(pythonServerUrl + "/health",String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void startServer() throws IOException {
        if (isServerRunning()) {
            logger.info("BERT Server is already running");
            return;
        }

        logger.info("Starting Docker container for embedding server...");
        ProcessBuilder processBuilder = new ProcessBuilder(
            "docker", "run", "--rm", "--name", "bert-server","-d", "-p", "5000:5000", "hermannt1/bert-server:latest"
        );

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            this.containerId = reader.readLine();
        }
        logger.info("Docker container for BERT server started with id {}.", this.containerId);
    }

    //@PreDestroy
    public void stopServer() throws IOException {
        if (containerId == null || containerId.isEmpty()) {
            logger.warn("No container found. The server might not have been started");
        }

        logger.info("Stopping BERT Server in the Docker Container with ID: {}", this.containerId);
        ProcessBuilder processBuilder = new ProcessBuilder("docker", "stop", this.containerId);
        Process process = processBuilder.start();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String stoppedContainerId = reader.readLine();
            if (containerId.equals(stoppedContainerId)) {
                logger.info("Successfully stopped docker container with ID: {}", this.containerId);
            } else {
                logger.warn("Failed to stop docker container with ID: {}", this.containerId);
            }
        }
    }

    public float[] computeEmbedding(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text must not be null or empty");
        }

        String url = pythonServerUrl + "/embed";
        
        Map<String, String> request = Map.of("text", text);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
        
        // Esegui la richiesta
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map<String,Object>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request),
                responseType
            );

            Map<String, Object> response = responseEntity.getBody();
            if (response == null || !response.containsKey("embedding")) {
                throw new RuntimeException("Invalid response from embedding server");
            }

            Object embeddingObject = response.get("embedding");
            if (embeddingObject instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Double> embeddingList = (List<Double>) embeddingObject;

                float[] embeddingArray = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embeddingArray[i] = embeddingList.get(i).floatValue();
                }

                return embeddingArray;
            } else {
                throw new RuntimeException("Unexpected embedding format from server");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling BERT Server: " + e.getMessage());
        }
    }   
}
