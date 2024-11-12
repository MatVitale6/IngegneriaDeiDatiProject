package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Classe di utilitá per il parsing di file HTML.
 * 'HTMLParserUtils' offre metodi statici per estrarre informazioni come titolo,
 * autori e contenuto dai file HTML
 * utilizzando la libreria Jsoup per analizzare la struttura del documento HTML
 */
public class HTMLParserUtils {
    private static final Logger logger = LoggerFactory.getLogger(HTMLParserUtils.class);

    /**
     * Estrae il titolo del docuemento HTML.
     * Il metodo cerca l'elemento 'h1' con classi 'ltx_title ltx_title_document' e restituisce il suo testo.
     * 
     * @param file Il file HTML da analizzare.
     * @return il titolo del documento, o una stringa vuota se il titolo non é
     *         trovato.
     * @throws IOException IOException se sis verifica un errore durante la lettura
     *                     del file.
     */
    public static String parseTitle(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");

        Element titleElement = htmlDoc.selectFirst("h1.ltx_title.ltx_title_document");
        if (titleElement == null) {
            logger.warn("Extracted empty title at {}", file.getName());
            return "";
        }
        return titleElement.text().trim();
    }

    /**
     * Estrae gli autori dal documento HTML.
     * Cerca una 'div' con classe 'ltx_authors' e seleziona gli elementi `span` con
     * classi `ltx_creator ltx_role_author`.
     * Il nome di ogni autore viene aggiunto alla lista restituita.
     * 
     * @param file Il file HTML da analizzare.
     * @return Una lista di nomi degli autori; restituisce una lista vuota se non
     *         sono trovati autori.
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public static String parseAuthors(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");
        Elements authorsElements = htmlDoc.select("span.ltx_personname");

        if (authorsElements == null) {
            logger.warn("Extracted empty authors at {}", file.getName());
            return "";
        }
        return authorsElements.text().trim();
    }

    /**
     * Estrae il contenuto dell'articolo HTML.
     * Cerca un elemento `<article>` e ne estrae il testo, suddiviso in sezioni, se
     * disponibili.
     * Ogni sezione viene aggiunta a un `StringBuilder`, con due righe di
     * separazione tra le sezioni.
     * 
     * @param file Il file HTML da analizzare.
     * @return Il contenuto completo dell'articolo come stringa, o una stringa vuota
     *         se l'elemento `article` non è trovato.
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public static String parseContent(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");
        Element articleElement = htmlDoc.selectFirst("article");

        if (articleElement == null) {
            logger.warn("Extracted empty article at {}", file.getName());
            return "";
        }

        StringBuilder contentBuilder = new StringBuilder();
        Elements sections = articleElement.select("section");
        for (Element section : sections) {
            String sectionText = section.text();
            contentBuilder.append(sectionText).append("\n\n");
        }
        return contentBuilder.toString().trim();
    }

    /**
     * Estrae l'abstract dal documento HTML.
     * Cerca un elemento 'div' con classe 'ltx_abstract' e ne restituisce il testo
     * contenuto nel paragrafo 'p'.
     * 
     * @param file Il file HTML da analizzare.
     * @return Il contenuto dell'abstract come stringa, o una stringa vuota se non è
     *         trovato.
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public static String parseAbstract(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");

        // Trova l'elemento <div> con classe "ltx_abstract"
        Element abstractElement = htmlDoc.selectFirst("div.ltx_abstract");

        if (abstractElement == null) {
            logger.warn("Abstract not found in {}", file.getName());
            return "";
        }

        // Estrae il contenuto del paragrafo <p> che contiene il testo dell'abstract, ma
        // solo all'interno di ltx_abstract
        Element paragraph = abstractElement.selectFirst("p.ltx_p");

        if (paragraph == null) {
            logger.warn("Abstract paragraph not found in {}", file.getName());
            return "";
        }

        return paragraph.text().trim();
    }

    // public static String parseKeywords(File file) throws IOException {
    //     Document htmlDoc = Jsoup.parse(file, "UTF-8");
    //     Element keywordsElement = htmlDoc.selectFirst("div.ltx_keywords");

    //     if (keywordsElement == null) {
    //         logger.warn("Extracted empty keywords at {}", file.getName());
    //         return "";
    //     }
    //     return keywordsElement.text().trim();
    // }

}
