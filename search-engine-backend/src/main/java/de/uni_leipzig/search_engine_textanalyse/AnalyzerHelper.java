package de.uni_leipzig.search_engine_textanalyse;

import java.util.StringTokenizer;

/**
 * @author Danilo Morgado
 */
public class AnalyzerHelper {
    
    private static final GermanStemmer GERMAN_STEMMER = new GermanStemmer();
    private static final PorterStemmer ENGLISH_STEMMER = new PorterStemmer();
    
    private static final String[] SPECIAL_CHARAKTERS = {
        "<", "(", "[", "{", "\\^", "-", "=", "$", "!", "|", "]", "}", ")", "?", "*", "+", ".", ">", ";", ":"
    };
    
    private static final String[] GERMAN_STOP_WORDS = {
        "einer", "eine", "eines", "einem", "einen",
        "der", "die", "das", "dass", "daß",
        "du", "er", "sie", "es",
        "was", "wer", "wie", "wir",
        "und", "oder", "ohne", "mit",
        "am", "im", "in", "aus", "auf",
        "ist", "sein", "war", "wird",
        "ihr", "ihre", "ihres",
        "als", "für", "von", "mit",
        "dich", "dir", "mich", "mir",
        "mein", "sein", "kein",
        "durch", "wegen", "wird"
    };
    
    private static final String[] ENGLISH_STOP_WORDS = {
        "a", "an",
        "the", "that", "this", "these", "those",
        "you", "he", "she", "it",
        "what", "who", "how", "we",
        "and", "or", "without", "with",
        "at", "in", "on", "from",
        "is", "his", "her", "are", "will",
        "as", "for", "from",
        "your", "me", "my",
        "nothing",
        "want"
    };
    
    public String analyzeReverse(String sentence){
        
        return analyze(sentence, new String[][]{GERMAN_STOP_WORDS, ENGLISH_STOP_WORDS}, false);
        
    }
    
    public String analyzeSearch(String sentence){
        
        return analyze(sentence, new String[][]{}, true);
        
    }
    
    public String analyzeQuery(String sentence){
        
        return analyze(sentence, new String[][]{GERMAN_STOP_WORDS, ENGLISH_STOP_WORDS}, true);
        
    }
        
    private String analyze(String sentence, String[][] stopSets, Boolean stem){
        
        String edited = "";
        
        sentence = sentence.toLowerCase().trim();
        
        for (String sp : SPECIAL_CHARAKTERS) sentence = sentence.replaceAll("[\\" + sp + "]", " ");
        
        StringTokenizer st = new StringTokenizer(sentence);
        while (st.hasMoreElements()) {
            
            String word = st.nextElement().toString();
            
            if (isStop(word, stopSets)) continue;
                        
            if (stem) word = GERMAN_STEMMER.stem(word);
            
            if (stem) word = ENGLISH_STEMMER.stem(word);
            
            edited = edited + word + " ";
            
        }
                        
        return edited.trim();
        
    }
    
    private Boolean isStop(String word, String[][] stopSets){
        
        for (String[] stopSet : stopSets) {
            
            for(String replace : stopSet) {
                
                if(word.equals(replace)) return true;
                
            }
            
        }
    
        return false;
        
    }
       
}
