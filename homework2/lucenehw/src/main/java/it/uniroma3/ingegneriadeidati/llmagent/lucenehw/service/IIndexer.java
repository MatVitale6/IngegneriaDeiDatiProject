package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;

public interface IIndexer {
    void run() throws IOException;
    void indexFiles(String directoryPath) throws IOException;
}
