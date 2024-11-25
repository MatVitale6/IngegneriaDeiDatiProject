package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;

/**
 * Defines the contract for indexing services.
 * <p>
 * This interface is part of a <strong>Strategy pattern implementation</strong>, where different indexing strategies
 * (e.g., JSON, HTML) are encapsulated in separate classes implementing this interface.
 * <p>
 * Each concrete implementation of {@code IIndexer} provides a specific way to index files
 * based on their type. This approach promotes flexibility and adherence to the 
 * Open/Closed Principle by allowing new indexing strategies to be added without modifying existing code.
 * </p>
 * <p>
 * Typical usage involves:
 * <ul>
 *     <li>Calling {@link #run()} to execute the full indexing process.</li>
 *     <li>Calling {@link #indexFiles(String)} to index files in a specific directory.</li>
 * </ul>
 * </p>
 * <p>
 * This interface facilitates separation of concerns and dynamic strategy selection for indexing
 * within the application.
 * </p>
 * 
 * @see it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.JSONIndexer
 * @see it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.HTMLIndexer
 */
public interface IIndexer {
    void run() throws IOException;
    void indexFiles(String directoryPath) throws IOException;
}
