package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLParserUtils {
    public static String parseTitle(File file) throws IOException {
        Document htmlDoc = Jsoup.parse(file, "UTF-8");
        
        Element titleElement = htmlDoc.selectFirst("h1.ltx_title.ltx_title_document");
        return titleElement != null ? titleElement.text() : "";
    }

    public static List<String> parseAuthors(File file) throws IOException {
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
