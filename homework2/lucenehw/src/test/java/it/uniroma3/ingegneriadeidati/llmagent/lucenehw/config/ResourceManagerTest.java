package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.lang.constant.DirectMethodHandleDesc;

import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ResourceManagerTest {

    @Autowired
    private ResourceManager resourceManager;

    @Test
    public void testPathInjection() {
        assertNotNull(resourceManager);
        assertNotNull(resourceManager.getHtmlIndexPath());
        assertNotNull(resourceManager.getJsonIndexPath());
    }

    @Test
    public void testPrepareDirectory() throws IOException {
        Directory htmlDirectory = resourceManager.getDirectory("html");
        assertNotNull(htmlDirectory);

        Directory jsonDirectory = resourceManager.getDirectory("json");
        assertNotNull(jsonDirectory);
    }
}
