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

@SpringBootTest
public class HTMLParserUtilsTest {
    private static File testFile;
    
    @Value("${html.files.path}")
    private String htmlFilesPath;

    private final static String fileName = "ar5iv_article_2407.18219.html";

    @BeforeEach
    public void setUp() throws IOException {
        testFile = Paths.get(this.htmlFilesPath, fileName).toFile();

        if (!testFile.exists()) {
            throw new IOException("Test file not found at: " + testFile.getAbsolutePath());
        }
    }

    @Test
    public void testParseTitle() throws IOException {
        String title = HTMLParserUtils.parseTitle(testFile);
        assertEquals("Recursive Introspection: Teaching Language Model Agents How to Self-Improve", title);
    }

    @Test
    public void testParseAuthors() throws IOException {
        List<String> authors = HTMLParserUtils.parseAuthors(testFile);
        assertEquals(List.of("Yuxiao Qu", "Tianjun Zhang", "Naman Garg", "Aviral Kumar"), authors);
    }

    @Test
    public void testParseContent() throws IOException {
        // TODO: do something...
    }

}
