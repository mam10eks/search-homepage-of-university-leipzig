package de.uni_leipzig.search_engine_textanalyse;

import java.util.StringTokenizer;

import org.apache.lucene.search.spell.JaroWinklerDistance;

/**
 * @author Danilo Morgado
 */
public class JaroWinklerHelper {
    
    private final JaroWinklerDistance JWD = new JaroWinklerDistance();
    
    public JaroWinklerHelper(){
        
    }
    
    public Float getHighestDistance(String sentence1, String sentence2){
        
        float similiarity = 0f;
        
        StringTokenizer tokenizer1 = new StringTokenizer(sentence1);
        while(tokenizer1.hasMoreTokens()){
            String token1 = tokenizer1.nextToken();
            StringTokenizer tokenizer2 = new StringTokenizer(sentence2);
            while(tokenizer2.hasMoreTokens()){
                String token2 = tokenizer2.nextToken();
                float tmp = JWD.getDistance(token1, token2);
                if (tmp > similiarity) similiarity = tmp;
            }
        }
        
        return similiarity;
        
    }
    
}
