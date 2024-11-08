package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util.HTMLParserUtils;

/** 
 * <h3>Unit test for Jsoup HTML parser</h3>
 * <p>
 * Testing authors and content parsing need more research, cause there's not a standard layout
 * for authors formatting, and don't know how to test the entire content extraction.
 * </p>
 * <p>
 * Default behaviour now is to extract all the 'authors section' and then we will refine it with
 * analyzers and indexers. 
 * </p>
 * <p>
 * Title extractor should be fine... 
 * </p>
 */
@SpringBootTest
public class HTMLParserUtilsTest {
    private static File testFile1;
    private static File testFile2;
    private static File testFile3;
    private static File testFile4;
    
    @Value("${html.files.path}")
    private String htmlFilesPath;

    private final static String fileName1 = "ar5iv_article_2407.18219.html";
    private final static String fileName2 = "1_arXiv2410.03427.html";
    private final static String fileName3 = "0710.0386v1.html";
    private final static String fileName4 = "arXiv_2108.02092.html";

    @BeforeEach
    public void setUp() throws IOException {
        testFile1 = Paths.get(this.htmlFilesPath, fileName1).toFile();
        testFile2 = Paths.get(this.htmlFilesPath, fileName2).toFile();
        testFile3 = Paths.get(this.htmlFilesPath, fileName3).toFile();
        testFile4 = Paths.get(this.htmlFilesPath, fileName4).toFile();

        if (!testFile1.exists()) {
            throw new IOException("Test file not found at: " + testFile1.getAbsolutePath());
        }
        if (!testFile2.exists()) {
            throw new IOException("Test file not found at: " + testFile2.getAbsolutePath());
        }
        if (!testFile3.exists()) {
            throw new IOException("Test file not found at: " + testFile3.getAbsolutePath());
        }
        if (!testFile4.exists()) {
            throw new IOException("Test file not found at: " + testFile4.getAbsolutePath());
        }
    }

    @Test
    public void testParseTitle_2407_18219() throws IOException {
        String title1 = HTMLParserUtils.parseTitle(testFile1);
        assertEquals("Recursive Introspection: Teaching Language Model Agents How to Self-Improve", title1);
    }

    @Test
    public void testParseAuthors_2407_18219() throws IOException {
        String authors = HTMLParserUtils.parseAuthors(testFile1);
        assertEquals("Yuxiao Qu Tianjun Zhang Naman Garg Aviral Kumar", authors);
    }
    
    // @Test
    // public void testParseContent_2407_18219() throws IOException {
    //     String content = HTMLParserUtils.parseContent(testFile1);
    //     assertEquals("content", content);
    // }

    @Test
    public void testParseTitle_2410_03427() throws IOException {
        String title2 = HTMLParserUtils.parseTitle(testFile2);
        assertEquals("Biodenoising: animal vocalization denoising without access to clean data", title2);
    }

    @Test
    public void testParseAuthors_2410_03427() throws IOException {
        String authors = HTMLParserUtils.parseAuthors(testFile2);
        assertEquals("Marius Miron, Sara Keen, Jen-Yu Liu, Benjamin Hoffman, Masato Hagiwara, Olivier Pietquin, Felix Effenberger, Maddie Cusimano", authors);
    }
    
    @Test
    public void testParseTitle_0710_0386v1() throws IOException {
        String title3 = HTMLParserUtils.parseTitle(testFile3);
        assertEquals("Comparing Maintenance Strategies for Overlays", title3);
    }

    @Test
    public void testParseAuthors_0710_0386v1() throws IOException {
        String authors = HTMLParserUtils.parseAuthors(testFile3);
        assertEquals("Supriya Krishnamurthy, Sameh El-Ansary, Erik Aurell and Seif Haridi", authors);
    }

    @Test
    public void testParseTitle_2108_02092() throws IOException {
        String title4 = HTMLParserUtils.parseTitle(testFile4);
        assertEquals("Online Knowledge Distillation for Efficient Pose Estimation", title4);
    }

    @Test
    public void testParseAuthors_2108_02092() throws IOException {
        String authors = HTMLParserUtils.parseAuthors(testFile4);
        assertEquals("Zheng Li, Jingwen Ye, Mingli Song, Ying Huang, Zhigeng Pan", authors);
    }
}
