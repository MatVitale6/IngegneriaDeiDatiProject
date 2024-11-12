package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util.CustomAuthorAnalyzer;

@SpringBootTest
public class CustomAuthorAnalyzerTest {

    private Analyzer analyzer;
    
    @BeforeEach
    public void setUp() {
        analyzer = new CustomAuthorAnalyzer();

    }

    @AfterEach 
    public void tearDown() {
        analyzer.close();
    }

    @Test
    public void testAnalyzerSimpleText() throws IOException {
        String authors = "Franz Louis Cesista, Rui Aguiar, Jason Kim, Paolo Acilo";
        List <String> expectedTokens = List.of("franz", "louis", "cesista", "rui", "aguiar", "jason", "kim", "paolo", "acilo");

        List<String> actualTokens = analyzeText(authors);
        assertEquals(expectedTokens, actualTokens);
    }

    @Test
    public void testAnalyzerNoiseNumberText() throws IOException {
        String authors = "Holly Wilson1, Scott Wellington1, Foteini Simistira Liwicki2, Vibha Gupta2, Rajkumar Saini2,";
        List <String> expectedTokens = List.of("holly", "wilson", "scott", "wellington", "foteini", "simistira", "liwicki", "vibha", "gupta", "rajkumar", "saini");

        List<String> actualTokens = analyzeText(authors);
        assertEquals(expectedTokens, actualTokens);
    }

    @Test
    public void testAnalyzerNoiseSpecialCharsText() throws IOException {
        String authors = "Jia Fu1,2*, Xiaoting Qin3, Fangkai Yang3, Lu Wang3, Jue Zhang3†, Qingwei Lin3,";
        List <String> expectedTokens = List.of("jia", "fu", "xiaoting", "qin", "fangkai", "yang", "lu", "wang", "jue", "zhang", "qingwei", "lin");

        List<String> actualTokens = analyzeText(authors);
        assertEquals(expectedTokens, actualTokens);
    }

    @Test
    public void testAnalyzerNoiseTextWithEmails() throws IOException {
        String authors = "Zhepei Wei, Wei-Lin Chen, Yu Meng Department of Computer Science University of Virginia {zhepei.wei,tuy8sy,yumeng5}@virginia.edu";
        List <String> expectedTokens = List.of("zhepei", "wei", "wei", "lin", "chen", "yu", "meng");

        List<String> actualTokens = analyzeText(authors);
        assertTrue(actualTokens.containsAll(expectedTokens));
    }

    private List<String> analyzeText(String text) throws IOException{
        List<String> tokens = new ArrayList<>();
        try (TokenStream tokenStream = analyzer.tokenStream("author", text)) {
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                tokens.add(charTermAttribute.toString());
            }
            tokenStream.end();
        }
        return tokens;
    }
}
