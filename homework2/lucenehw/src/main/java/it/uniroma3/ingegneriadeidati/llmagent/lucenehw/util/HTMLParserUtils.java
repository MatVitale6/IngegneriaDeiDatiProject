package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


/**
 * Classe di utilitá per il parsing di file HTML.
 * 'HTMLParserUtils' offre metodi statici per estrarre informazioni come titolo, autori e contenuto dai file HTML
 * utilizzando la libreria Jsoup per analizzare la struttura del documento HTML
 */
public class HTMLParserUtils {
    private static final Logger logger = LoggerFactory.getLogger(HTMLParserUtils.class);


    /**
     * Estrae il titolo del docuemento HTML.
     * Il metodo cerca l'elemento '<h1>' con classi 'ltx_title ltx_title_document' e restituisce il suo testo.
     * @param file Il file HTML da analizzare.
     * @return il titolo del documento, o una stringa vuota se il titolo non é trovato.
     * @throws IOException IOException se sis verifica un errore durante la lettura del file.
     */
    public static String parseTitle(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");
        
        Element titleElement = htmlDoc.selectFirst("h1.ltx_title.ltx_title_document");
        if(titleElement == null) {
            logger.warn("Extracted empty title");
            return "";
        }

        StringBuilder titleText = new StringBuilder();

        for (Node node : titleElement.childNodes()) {
            if (node instanceof TextNode) {
                // append pure text
                titleText.append(((TextNode) node).text());
            } else if (node instanceof Element) {
                Element childElement = (Element) node;
                // if there are sub elements, check if they refer to formatetd text only
                if (childElement.tagName().equals("em") || childElement.tagName().equals("strong") ||
                    childElement.tagName().equals("i") || childElement.tagName().equals("b")) {
                        titleText.append(childElement.ownText());
                }
            }
        }
        
        return titleText.toString().trim();
    }

    /**
     * Estrae gli autori dal documento HTML.
     * Cerca una 'div' con classe 'ltx_authors' e seleziona gli elementi `span` con classi `ltx_creator ltx_role_author`.
     * Il nome di ogni autore viene aggiunto alla lista restituita.
     * @param file Il file HTML da analizzare.
     * @return Una lista di nomi degli autori; restituisce una lista vuota se non sono trovati autori.
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public static String parseAuthors(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");
        List<String> authors = new ArrayList<>();

        Element authorsDiv = htmlDoc.selectFirst("div.ltx_authors");
        if (authorsDiv != null) {
            Elements authorSpans = authorsDiv.select("span.ltx_creator.ltx_role_author");
            for (Element author : authorSpans) {
                authors.add(author.select("span.ltx_personname").text());
            }
        }
        return authors;
    }


    /**
     * Estrae il contenuto dell'articolo HTML.
     * Cerca un elemento `<article>` e ne estrae il testo, suddiviso in sezioni, se disponibili.
     * Ogni sezione viene aggiunta a un `StringBuilder`, con due righe di separazione tra le sezioni.
     * @param file Il file HTML da analizzare.
     * @return Il contenuto completo dell'articolo come stringa, o una stringa vuota se l'elemento `article` non è trovato.
     * @throws IOException se si verifica un errore durante la lettura del file.
     */
    public static String parseContent(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");

        Element articleElement = htmlDoc.selectFirst("article");

        StringBuilder contentBuilder = new StringBuilder();
        Elements sections = articleElement.select("section");
        for (Element section : sections) {
            String sectionText = section.text();
            contentBuilder.append(sectionText).append("\n\n");
        }
        return contentBuilder.toString().trim();
    }

    
    /**
     * Placeholder per il metodo di estrazione dell'abstract dal documento HTML.
     * Attualmente, restituisce una stringa vuota.
     * @param file Il file HTML da analizzare.
     * @return Stringa vuota, in attesa di implementazione.
     */
    public static String parseAbstract(File file) {
        return "";
    }

}
