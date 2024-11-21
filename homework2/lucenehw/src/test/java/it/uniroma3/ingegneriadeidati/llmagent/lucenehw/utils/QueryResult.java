package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.utils;

import java.util.List;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResultHTML;

// Classe di supporto per organizzare i dati rilevanti della query
public class QueryResult {
    private final String query;
    private final long executionTime;
    private final int totalMatches;
    private final double averageScore;
    private final double scoreVariance;
    private final double scoreDecay;
    private final List<SearchResultHTML> results;

    public QueryResult(String query, long executionTime, int totalMatches, double averageScore,
                       double scoreVariance, double scoreDecay, List<SearchResultHTML> results) {
        this.query = query;
        this.executionTime = executionTime;
        this.totalMatches = totalMatches;
        this.averageScore = averageScore;
        this.scoreVariance = scoreVariance;
        this.scoreDecay = scoreDecay;
        this.results = results;
    }

    // Getters per l'esportazione
    public String getQuery() { return query; }
    public long getExecutionTime() { return executionTime; }
    public int getTotalMatches() { return totalMatches; }
    public double getAverageScore() { return averageScore; }
    public double getScoreVariance() { return scoreVariance; }
    public double getScoreDecay() { return scoreDecay; }
    public List<SearchResultHTML> getResults() { return results; }
}

