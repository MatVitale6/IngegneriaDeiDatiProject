package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.apache.lucene.store.ByteBuffersDirectory;




@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LucenehwApplication {
	public static void main(String[] args) {
		try {
            // Step 1: Initialize an Analyzer (StandardAnalyzer for this example)
            StandardAnalyzer analyzer = new StandardAnalyzer();

            Directory index = new ByteBuffersDirectory();

            // Step 3: Set up IndexWriter configuration and create an IndexWriter
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(index, config);

			// Step 4: Create a document and add it to the index
			Document doc = new Document();
			doc.add(new TextField("titolo", "Hello World with Apache Lucene", TextField.Store.YES));
			writer.addDocument(doc);

            // Step 5: Build a Query using QueryParser
            String queryString = "Hello";
            QueryParser queryParser = new QueryParser("titolo", new WhitespaceAnalyzer());
			Query query = queryParser.parse(queryString);

            // Step 6: Search the Index
            DirectoryReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs results = searcher.search(query, 10); // top 10 results

            // Step 8: Clean up (index closed automatically by try-with-resources)
            index.close();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
	}

}
