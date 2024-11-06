package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LucenehwApplication {

    @Value("${lucene.index.path}")
    static String luceneIndexPath;

	public static void main(String[] args) {
        Path path = Paths.get(System.getProperty("user.dir")).resolve(luceneIndexPath);
        try {
            Directory directory = FSDirectory.open(path);
            Document doc1 = new Document();
            doc1.add(new TextField("titolo", "Come diventare un ingegnere dei dati, Data Engineer?", Field.Store.YES));
            doc1.add(new TextField("contenuto","Sembra che oggigiorno tutti vogliono diventare un Data Scientist", Field.Store.YES));
            
            Document doc2 = new Document();
            doc2.add(new TextField("titolo", "Curriculum Ingegneria dei Dati - Sezione di Informatica e Automazione", Field.Store.YES));
            doc2.add(new TextField("contenuto","Curriculum. Ingegneria dei dati. Laurea magistrale in ingegneria informatica", Field.Store.YES));

            IndexWriterConfig config = new IndexWriterConfig();
            IndexWriter writer = new IndexWriter(directory, config);

            writer.addDocument(doc1);
            writer.commit();

            Analyzer a = CustomAnalyzer.builder()
                            .withTokenizer(WhitespaceTokenizerFactory.class)
                            .addTokenFilter(LowerCaseFilterFactory.class)
                            .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                            .build();

            writer.close();

            Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();

            CharArraySet stopWords = new CharArraySet(Arrays.asList("in", "dei", "di"), true);
            perFieldAnalyzers.put("titolo", new WhitespaceAnalyzer());
            perFieldAnalyzers.put("contenuto", new StandardAnalyzer(stopWords));

            Analyzer perFieldAnalyzer = new PerFieldAnalyzerWrapper(new ItalianAnalyzer(), perFieldAnalyzers);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
