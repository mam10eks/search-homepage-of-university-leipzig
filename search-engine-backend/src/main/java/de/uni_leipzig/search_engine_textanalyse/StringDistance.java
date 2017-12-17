package de.uni_leipzig.search_engine_textanalyse;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Danilo Morgado StringDistance
 */
public class StringDistance {
    
    private final int deleteCost;
    private final int insertCost;
    private final int replaceCost;
    private final int swapCost;

    public StringDistance() {
    
        this(1, 1, 1, 1);
                
    }
    
    public StringDistance(int deleteCost, int insertCost, int replaceCost, int swapCost) {
        
        if (2 * swapCost < insertCost + deleteCost) {
            throw new IllegalArgumentException("Unsupported cost assignment");
        }
        
        this.deleteCost = deleteCost;
        this.insertCost = insertCost;
        this.replaceCost = replaceCost;
        this.swapCost = swapCost;
        
    }
    
    public Boolean getHighestDamerauLevenshtein(double threshold, String source, String target){
        
        double similiarity = 1.0f;
        
        StringTokenizer tokenizer1 = new StringTokenizer(source);
        while(tokenizer1.hasMoreTokens()){
            String token1 = tokenizer1.nextToken();
            StringTokenizer tokenizer2 = new StringTokenizer(target);
            while(tokenizer2.hasMoreTokens()){
                String token2 = tokenizer2.nextToken();
                int cost = damerauLevenshteinAlgorithm(token1, token2);
                int maxStringLength = Math.max(token1.length(), token2.length());
                maxStringLength = Math.max(1, maxStringLength);
                double tmp = (double)cost/maxStringLength;
                if (tmp < similiarity) similiarity = tmp;
            }
        }
        
        return similiarity < threshold;
        
    }
    
    /*
        example 1: dis=10 : 1.0                   str1=abcdefghij  	str2=klmnop
        example 2: dis= 1 : 0.09090909090909091   str1=aa bbb cccc  	str2=a bbb cccc
        example 3: dis= 4 : 0.36363636363636365   str1=aa bbb cccc  	str2=aa bcc bbcc
        example 4: dis= 2 : 0.18181818181818182   str1=aa bbb cccc  	str2=a bbb ccc
        example 5: dis= 1 : 0.09090909090909091   str1=aa bbb cccc  	str2=aab bb cccc
    */
    public Boolean damerauLevenshtein(double threshold, String source, String target){
        
        int cost = damerauLevenshteinAlgorithm(source, target);
        
        int maxStringLength = Math.max(source.length(), target.length());
        
        maxStringLength = Math.max(1, maxStringLength);
        
        return (double)cost/maxStringLength < threshold;
        
    }
    
    /*
        example 1: dis=10 : 1.0   str1=abcdefghij  	str2=klmnop
        example 2: dis=1 : 0.09090909090909091   str1=aa bbb cccc  	str2=a bbb cccc
        example 3: dis=4 : 0.36363636363636365   str1=aa bbb cccc  	str2=aa bcc bbcc
        example 4: dis=2 : 0.18181818181818182   str1=aa bbb cccc  	str2=a bbb ccc
        example 5: dis=2 : 0.18181818181818182   str1=aa bbb cccc  	str2=aab bb cccc
    */
    public Boolean levenshtein(double threshold, String source, String target){
        
        int cost = levenshteinAlgorithm(source, target);
        
        int maxStringLength = Math.max(source.length(), target.length());
        
        maxStringLength = Math.max(1, maxStringLength);
        
        return (double)cost/maxStringLength < threshold;
        
    }
    
    private int damerauLevenshteinAlgorithm(String source, String target) {
        
        if (source.length() == 0) return target.length() * insertCost;
        if (target.length() == 0) return source.length() * deleteCost;
        
        int[][] table = new int[source.length()][target.length()];
        
        Map<Character, Integer> sourceIndexByCharacter = new HashMap<Character, Integer>();
        
        if (source.charAt(0) != target.charAt(0)) {
            table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
        }
        
        sourceIndexByCharacter.put(source.charAt(0), 0);
        
        for (int i = 1; i < source.length(); i++) {
            int deleteDistance = table[i - 1][0] + deleteCost;
            int insertDistance = (i + 1) * deleteCost + insertCost;
            int matchDistance = i * deleteCost + (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
            
            table[i][0] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
        }
        
        for (int j = 1; j < target.length(); j++) {
            int deleteDistance = (j + 1) * insertCost + deleteCost;
            int insertDistance = table[0][j - 1] + insertCost;
            int matchDistance = j * insertCost + (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
            
            table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
        }
        
        for (int i = 1; i < source.length(); i++) {
            int maxSourceLetterMatchIndex = source.charAt(i) == target.charAt(0) ? 0 : -1;
            
            for (int j = 1; j < target.length(); j++) {
                Integer candidateSwapIndex = sourceIndexByCharacter.get(target.charAt(j));
                
                int jSwap = maxSourceLetterMatchIndex;
                int deleteDistance = table[i - 1][j] + deleteCost;
                int insertDistance = table[i][j - 1] + insertCost;
                int matchDistance = table[i - 1][j - 1];
                
                if (source.charAt(i) != target.charAt(j)) {
                    matchDistance += replaceCost;
                } else {
                    maxSourceLetterMatchIndex = j;
                }
                
                int swapDistance;
                
                if (candidateSwapIndex != null && jSwap != -1) {
                    int iSwap = candidateSwapIndex;
                    int preSwapCost;
                    
                    if (iSwap == 0 && jSwap == 0) {
                        preSwapCost = 0;
                    } else {
                        preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
                    }
                    
                        swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost + (j - jSwap - 1) * insertCost + swapCost;
                
                } else {
                  swapDistance = Integer.MAX_VALUE;
                }
                table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance), swapDistance);
                
            }
            
            sourceIndexByCharacter.put(source.charAt(i), i);
            
        }
        
        return table[source.length() - 1][target.length() - 1];
    
    }
    
    private int levenshteinAlgorithm(CharSequence lhs, CharSequence rhs) {
        
        int len0 = lhs.length() + 1;                                                     
        int len1 = rhs.length() + 1;                                                     

        // the array of distances                                                       
        int[] cost = new int[len0];                                                     
        int[] newcost = new int[len0];                                                  

        // initial cost of skipping prefix in String s0                                 
        for (int i = 0; i < len0; i++) cost[i] = i;                                     

        // dynamically computing the array of distances                                  

        // transformation cost for each letter in s1                                    
        for (int j = 1; j < len1; j++) {                                                
            // initial cost of skipping prefix in String s1                             
            newcost[0] = j;                                                             

            // transformation cost for each letter in s0                                
            for(int i = 1; i < len0; i++) {                                             
                // matching current letters in both strings                             
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             

                // computing cost for each transformation                               
                int cost_replace = cost[i - 1] + match;                                 
                int cost_insert  = cost[i] + 1;                                         
                int cost_delete  = newcost[i - 1] + 1;                                  

                // keep minimum cost                                                    
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }                                                                           

            // swap cost/newcost arrays                                                 
            int[] swap = cost; cost = newcost; newcost = swap;                          
        }                                                                               

        // the distance is the cost for transforming all letters in both strings        
        return cost[len0 - 1];   
        
    }
    
}
