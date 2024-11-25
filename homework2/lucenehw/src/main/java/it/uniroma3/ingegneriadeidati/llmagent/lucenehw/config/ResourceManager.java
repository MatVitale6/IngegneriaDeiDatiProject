package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.IIndexer;

/**
 * Manages resources used in the Lucene indexing and search system.
 * 
 * The {@link ResourceManager} class provides a unified way to register and access
 * resources such as {@link Directory}, {@link Analyzer}, and {@link IIndexer}.
 * It is designed to ensure low coupling and high modularity for different resource types 
 * (e.g., "html", "json").
 * 
 * Responsibilities:
 * <ul>
 *   <li>Registers resources and associates them with their configuration.</li>
 *   <li>Manages index directories and ensures proper setup.</li>
 *   <li>Tracks and marks the completion of indexing for resource types.</li>
 *   <li>Provides analyzers, directories, and indexers for specific resource types.</li>
 * </ul>
 */
@Component
public class ResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    private final String htmlIndexPath;
    private final String jsonIndexPath;

    private static final String INDEXING_COMPLETE_FLAG = "indexing_complete.flag"; 
    private final Map<String, Resource> resources = new HashMap<>();

    public ResourceManager(String htmlIndexPath, String jsonIndexPath) {
        this.htmlIndexPath = htmlIndexPath;
        this.jsonIndexPath = jsonIndexPath;

        if (htmlIndexPath == null || jsonIndexPath == null) {
            throw new IllegalArgumentException("Index Paths must not be null");
        }
    }

    /**
     * Retrieves the directory associated with a given resource type.
     * Prepares the directory if it doesn't exist or is not ready.
     *
     * @param type the resource type (e.g., "html", "json")
     * @return the {@link Directory} associated with the resource type
     * @throws IOException if an I/O error occurs
     */
    public Directory getDirectory(String type) throws IOException {
        Resource resource = resources.get(type);
        if (resource == null) {
            throw new IllegalArgumentException("Resource type " + type + " not found");
        }
        return resource.getDirectory();
    }

    /**
     * Retrieves the analyzer for a given resource type.
     *
     * @param type the resource type (e.g., "html", "json")
     * @return the {@link Analyzer} for the resource type
     */
    public Analyzer getAnalyzer(String type) {
        Resource resource = resources.get(type);
        if (resource == null) {
            throw new IllegalArgumentException("Resource type " + type + " not found");
        }
        return resource.getAnalyzer();
    }

    /**
     * Retrieves the indexer for a given resource type.
     *
     * @param type the resource type (e.g., "html", "json")
     * @return the {@link IIndexer} for the resource type
     */
    public IIndexer getIndexer(String type) {
        Resource resource = resources.get(type);
        if (resource == null) {
            throw new IllegalArgumentException("Resource type " + type + " not found");
        }
        return resource.getIndexer();
    }

    /**
     * Checks if indexing is complete for a given resource type.
     *
     * @param type the resource type (e.g., "html", "json")
     * @return true if the indexing is complete, false otherwise
     */
    public boolean isIndexingComplete(String type) {
        Resource resource = resources.get(type);
        if (resource == null) {
            throw new IllegalArgumentException("Resource type " + type + " not found");
        }
        String path = type.equalsIgnoreCase("html") ? htmlIndexPath : jsonIndexPath;
        return Files.exists(Paths.get(path, INDEXING_COMPLETE_FLAG));
    }


    /**
     * Marks indexing as complete for a given resource type by creating the flag file.
     *
     * @param type the resource type (e.g., "html", "json")
     * @throws IOException if an error occurs creating the flag file
     */
    public void markIndexingComplete(String type) throws IOException {
        Resource resource = resources.get(type);
        if (resource == null) {
            throw new IllegalArgumentException("Resource type " + type + " not found");
        }
        String path = type.equalsIgnoreCase("html") ? htmlIndexPath : jsonIndexPath;
        Files.createFile(Paths.get(path, INDEXING_COMPLETE_FLAG));
    }

    /**
     * Registers a new resource with the resource manager.
     *
     * @param type the type of the resource (e.g., "html", "json")
     * @param indexer the {@link IIndexer} associated with the resource
     * @param analyzer the {@link Analyzer} associated with the resource
     * @param directory the {@link Directory} for the resource
     * @param searchFields the fields used for searching this resource
     */
    public void registerResource(String type, IIndexer indexer, Analyzer analyzer, Directory directory, String[] searchFields) {
        if (resources.containsKey(type)) {
            logger.warn("resource type '{}' already registered, skipping...", type);
        }
        Resource resource = new Resource(indexer, analyzer, directory, searchFields);
        resources.put(type, resource);

        logger.info("registered resource type '{}': Indexer={}, Analyzer={}, Directory={}", 
            type, indexer.getClass().getSimpleName(), analyzer.getClass().getSimpleName(), directory.getClass().getSimpleName());
        
    }

    /**
     * Retrieves the search fields for a given resource type.
     *
     * @param type the resource type (e.g., "html", "json")
     * @return an array of search fields for the resource type
     */
    public String[] getSearchFields(String type) {
        Resource resource = resources.get(type);
        if (resource == null) {
            throw new IllegalArgumentException("Resource type not registered: " + type);
        }
        return resource.getSearchFields();
    }

    public Set<String> getRegisteredTypes() { return resources.keySet(); }

    /**
     * Prepares the index directory for a given path.
     * Deletes old files if indexing is not complete.
     *
     * @param path the {@link Path} to the directory
     * @return the prepared {@link Directory}
     */
    public Directory prepareIndexDirectory(String type) {
        Path path = type.equalsIgnoreCase("html") ? Paths.get(htmlIndexPath) : Paths.get(jsonIndexPath);

        try {
            Path flagFilePath = path.resolve(INDEXING_COMPLETE_FLAG);
            
            if (!Files.exists(flagFilePath)) {
                try (DirectoryStream<Path> directoryStream =  Files.newDirectoryStream(path)) {
                    for (Path filePath : directoryStream) {
                        if(!filePath.getFileName().toString().equals(".gitkeep") && !filePath.getFileName().toString().equals(INDEXING_COMPLETE_FLAG)) {
                            Files.delete(filePath);
                        }
                    }
                } catch (IOException e) {
                    throw new IOException("Error clearing indexDirectory " + e.getMessage(), e);
                }
            }
            return FSDirectory.open(path);
        } catch (IOException e) {
            throw new RuntimeException("Error preparing directories", e);
        }
    }
}
