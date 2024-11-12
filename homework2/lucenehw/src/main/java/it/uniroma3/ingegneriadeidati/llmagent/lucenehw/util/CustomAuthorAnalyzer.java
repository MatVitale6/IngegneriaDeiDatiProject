package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.util;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pattern.PatternTokenizer;

public class CustomAuthorAnalyzer extends Analyzer{

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Pattern tokenPattern = Pattern.compile("[^@\\\\s,\\\\d]+");
        PatternTokenizer tokenizer = new PatternTokenizer(tokenPattern,-1);
        TokenStream tokenStream = new LowerCaseFilter(tokenizer);
        return new TokenStreamComponents(tokenizer, tokenStream);
    }
    
}
