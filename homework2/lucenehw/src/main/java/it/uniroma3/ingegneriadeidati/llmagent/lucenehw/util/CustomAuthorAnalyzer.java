package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.pattern.PatternTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class CustomAuthorAnalyzer extends Analyzer{

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        //Pattern tokenPattern = Pattern.compile("[^@\\s,\\d]+");
        //PatternTokenizer tokenizer = new PatternTokenizer(tokenPattern,-1);
        StandardTokenizer tokenizer = new StandardTokenizer();
        TokenStream tokenStream = new PatternReplaceFilter(tokenizer, Pattern.compile("\\d+"), "", true);
        tokenStream = new PatternReplaceFilter(tokenStream, Pattern.compile("[,]"), "", true);
        tokenStream = new LowerCaseFilter(tokenStream);
        return new TokenStreamComponents(tokenizer, tokenStream);
    }
}
