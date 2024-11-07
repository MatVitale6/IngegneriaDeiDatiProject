package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LuceneConfig {
    @Value("${lucene.index.path}")
    private String indexDirectory;

    @Bean
    public Directory luceneDirectory() throws IOException {
        Path path = Paths.get(indexDirectory);
        return FSDirectory.open(path);
    }

    @Bean
    public Analyzer analyzer() {
        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();
        perFieldAnalyzer.put("title", new StandardAnalyzer());
        perFieldAnalyzer.put("content", new EnglishAnalyzer());
        perFieldAnalyzer.put("authors", new KeywordAnalyzer());

        return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);
    }

    @Bean
    public IndexWriterConfig indexWriterConfig(Analyzer analyzer) {
        return new IndexWriterConfig(analyzer);
    }

}
