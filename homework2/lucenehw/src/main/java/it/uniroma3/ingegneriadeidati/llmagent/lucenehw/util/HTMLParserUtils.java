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

public class HTMLParserUtils {
    private static final Logger logger = LoggerFactory.getLogger(HTMLParserUtils.class);

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

    public static String parseAuthors(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");
        Element authorsElement = htmlDoc.selectFirst("span.ltx_personname");

        if(authorsElement == null) {
            logger.warn("Extracted empty authors");
            return "";
        }
  
        return authorsElement.text();
    }

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

    public static String parseAbstract(File file) {
        return "";
    }

}
