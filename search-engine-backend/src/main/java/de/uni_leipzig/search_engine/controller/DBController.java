package de.uni_leipzig.search_engine.controller;

import de.uni_leipzig.search_engine_textanalyse.AnalyzerHelper;
import de.uni_leipzig.search_engine.dataobjects.UserQuery;
import de.uni_leipzig.search_engine.dataobjects.UserQueryComparator;
import de.uni_leipzig.search_engine_textanalyse.JaroWinklerHelper;
import de.uni_leipzig.search_engine_textanalyse.StringDistance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @author Danilo Morgado
 */
public class DBController {
    
    private final Integer MAX_RESULTS = 8;
    private final Integer MAX_QUERY_WORDS = 6;
    
    private final String CSV_DELIMITER = ";";

    private final AnalyzerHelper ANALYSER = new AnalyzerHelper();
    
    private final JaroWinklerHelper JAROWINKLER = new JaroWinklerHelper();
    
    private final StringDistance SD = new StringDistance();
    
    private final Comparator COMP = new UserQueryComparator();
    
    private final Map<Integer, Map<String, UserQuery>> INV_SINGLE_MAP = new HashMap();
    private final Map<String, UserQuery> SINGLE_MAP = new HashMap();
    
    private final Map<String, UserQuery> QUERY_MAP = new HashMap();
    
    private final List<String> L_RESULT = new LinkedList();
    
    private final List<UserQuery> L_STARTWIT = new LinkedList();
    private final List<UserQuery> L_CONTAINS = new LinkedList();
    private final List<UserQuery> L_SIM_JWD = new LinkedList();
    private final List<UserQuery> L_SIM_SD_DLD = new LinkedList();
    
    public DBController(){
        
        initDB();
        
    }
    
    public static void command(){
        
        DBController db = new DBController();
        
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        while(true){
            System.out.print("Enter your query: ");

            String query = scanner.nextLine();
            
            if (query.equals("-e")) break;

            if (query.startsWith("-a")) {
                query = query.replace("-a", "").trim();
                db.addQuery(query);
                continue;
            }
            
            List<String> suggestions = db.search(query);
            
            for(String suggestion : suggestions){
                System.out.println(">" + suggestion);
            }
            System.out.println();
        }
        
    }
    
    public final List<String> search(String search) {
        
        L_RESULT.clear();
        
        // return empty if to much query words
        {
            StringTokenizer st = new StringTokenizer(search);
            if (st.countTokens() > MAX_QUERY_WORDS) return L_RESULT;
        }
        
        // if query exist, return word suggestion by rank --->
        if (QUERY_MAP.containsKey(search.trim())) {
            Set<Integer> set = INV_SINGLE_MAP.keySet();
            SortedSet sortset = new TreeSet(set);
            Integer next;

            while (L_RESULT.size() < MAX_RESULTS){
                next = (Integer) sortset.last();
                sortset.remove(next);
                Map<String, UserQuery> map = INV_SINGLE_MAP.get(next);
                for (Map.Entry<String, UserQuery> entry : map.entrySet()) {
                    if (entry.getValue().getSearch().length() < 3) continue;
                    StringTokenizer st = new StringTokenizer(search);
                    Boolean exist = false;
                    while(st.hasMoreTokens()){
                        String token = st.nextToken();
                        if (token.equals(entry.getValue().getSearch())) exist = true;
                    }
                    if (exist) continue;
                    L_RESULT.add(search + " " + entry.getValue().getSearch());
//                    System.out.format("   %10s: '%-50s'   ['% 3d']%n", "existword", entry.getValue().getSearch(), entry.getValue().getCount());
                    if (L_RESULT.size() >= MAX_RESULTS) break;
                }
            }
            
            return L_RESULT;
        }
        
        // if normalized is empty, return
        String normalized = ANALYSER.analyzeSearch(search);
        if (normalized.isEmpty()) return L_RESULT;
//        System.out.format("   %10s: '%-50s'%n", "NORMALIZED", normalized);
        
        L_STARTWIT.clear();
        L_CONTAINS.clear();
        L_SIM_JWD.clear();
        L_SIM_SD_DLD.clear();
        
        // ---> otherwise rank queries
        for (Map.Entry<String, UserQuery> entry : QUERY_MAP.entrySet()) {
            
            if (entry.getValue().getNormalized().startsWith(normalized)) {
                L_STARTWIT.add(new UserQuery(entry.getKey(), entry.getValue().getCount()));
//                System.out.format("   %10s: '%-50s'   ['%-50s']%n", "start", entry.getKey(), entry.getValue().getNormalized());
            } else if (entry.getValue().getNormalized().contains(normalized)) {
                L_CONTAINS.add(new UserQuery(entry.getKey(), entry.getValue().getCount()));
//                System.out.format("   %10s: '%-50s'   ['%-50s']%n", "contain", entry.getKey(), entry.getValue().getNormalized());
            } else if (JAROWINKLER.getHighestDistance(normalized, entry.getValue().getNormalized()) > 0.95) {
                L_SIM_JWD.add(new UserQuery(entry.getKey(), entry.getValue().getCount()));
//                System.out.format("   %10s: '%-50s'   ['%-50s']%n", "jaro", entry.getKey(), entry.getValue().getNormalized());
            } else if (SD.getHighestDamerauLevenshtein(0.3, normalized, entry.getValue().getNormalized())) {
                L_SIM_SD_DLD.add(new UserQuery(entry.getKey(), entry.getValue().getCount()));
//                System.out.format("   %10s: '%-50s'   ['%-50s']%n", "dameraule", entry.getKey(), entry.getValue().getNormalized());
            } else {
//                System.out.format("   %10s: '%-50s'   ['%-50s']%n", "ELSE", entry.getKey(), entry.getValue().getNormalized());
            }
            
        }
        
        Collections.sort(L_STARTWIT, COMP);
        Collections.sort(L_CONTAINS, COMP);
        Collections.sort(L_SIM_JWD, COMP);
        Collections.sort(L_SIM_SD_DLD, COMP);
        
        Integer allSizes = L_STARTWIT.size() + L_CONTAINS.size() + L_SIM_JWD.size() + L_SIM_SD_DLD.size();
        Double all = 0.0 + allSizes;
        allSizes = Math.min(allSizes, MAX_RESULTS);
        all = allSizes/Math.max(1.0, all);
        
        // calculate the query per search method
        Long l_start_size = Math.round(all*L_STARTWIT.size());
        Long l_contain_size = Math.round(all*L_CONTAINS.size());
        Long l_sim_jwd_size = Math.round(all*L_SIM_JWD.size());
        Long l_sim_sd_dld_size = Math.round(all*L_SIM_SD_DLD.size());
        
        // calculate start and contains
        Long sc = l_start_size + l_contain_size;
        
        // set weight for start and contains
        double startWeight = 0.7;
        Double s = sc*startWeight;
        Double c = sc*(1-startWeight);
        Long sr = Math.round(s);
        Long cr = Math.round(c);
        
        // calculate the bias if a list is smaller then result
        Long bias = 0l;
        if (L_STARTWIT.size() < sr) bias = sr - L_STARTWIT.size();
        
        // get query per search method
        for (int i = 0; i < (sr - bias); i++) L_RESULT.add(L_STARTWIT.get(i).getSearch());
        for (int i = 0; i < (cr + bias); i++) L_RESULT.add(L_CONTAINS.get(i).getSearch());
        for (int i = 0; i < l_sim_jwd_size; i++) L_RESULT.add(L_SIM_JWD.get(i).getSearch());
        for (int i = 0; i < l_sim_sd_dld_size; i++) L_RESULT.add(L_SIM_SD_DLD.get(i).getSearch());
        
        return L_RESULT;
        
    }
    
    public final void initDB(){
        
        importCSVfile(new File("D:\\Public\\queryDB.csv"));
        
    }
    
    public void saveDB(){
        
        exportCSVfile(new File("D:\\Public\\queryDB.csv"));
        
    }
    
    private void addReverse(String word, Integer count){
        
        if (SINGLE_MAP.containsKey(word)){
            UserQuery userQuery = SINGLE_MAP.get(word);
            Map<String, UserQuery> map = INV_SINGLE_MAP.get(userQuery.getCount());
            
            map.remove(word);
            
            userQuery.setCount(userQuery.getCount() + count);
            Map<String, UserQuery> map_new = INV_SINGLE_MAP.get(userQuery.getCount());
            if (map_new == null) {
                map_new = new HashMap();
                INV_SINGLE_MAP.put(userQuery.getCount(), map_new);
            }
            
            map_new.put(word, userQuery);
            
        } else {
            UserQuery userQuery = new UserQuery(word);
            SINGLE_MAP.put(word, userQuery);
            
            Map<String, UserQuery> map = INV_SINGLE_MAP.get(userQuery.getCount());
            if (map == null) {
                map = new HashMap();
                INV_SINGLE_MAP.put(userQuery.getCount(), map);
            }
            
            map.put(word, userQuery);
        }
        
    }
    
    public void addQuery(String sentence, String count){
        
        addQuery(sentence, Integer.parseInt(count));
        
    }
    
    public void addQuery(String sentence){
        
        addQuery(sentence, 1);
        
    }
    
    public void addQuery(String sentence, Integer count){
        
        String reverse = ANALYSER.analyzeReverse(sentence);
        
        if (QUERY_MAP.containsKey(reverse)){
            QUERY_MAP.get(reverse).setCount(QUERY_MAP.get(reverse).getCount() + count);
        } else {
            String normalized = ANALYSER.analyzeQuery(sentence);
            UserQuery query = new UserQuery(reverse, count);
            query.setNormalized(normalized);
            
            QUERY_MAP.put(reverse, query);
        }
        
        StringTokenizer tokenizer = new StringTokenizer(reverse);
        while(tokenizer.hasMoreTokens()){
            addReverse(tokenizer.nextToken(), count);
        }
        
    }
    
    private void exportCSVfile(File file){
        
        FileOutputStream fos;
        BufferedWriter bw;
        
        try {
            fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
                        
            for (Map.Entry<String, UserQuery> entry : QUERY_MAP.entrySet()) {
                String text = entry.getKey() + CSV_DELIMITER + entry.getValue().getCount();
		bw.write(new String(text.getBytes()));
		bw.newLine();
            }
 
            bw.close();
            fos.close();
        
        } catch (FileNotFoundException ex) { }
        catch (IOException ex) { }
        
    }
    
    private void importCSVfile(File file){
        
        BufferedReader br;
        
        try {
            br = new BufferedReader(new FileReader(file));
            
            String currentLine;
            
            while ((currentLine = br.readLine()) != null) {
                
                currentLine = currentLine.trim();
                
                if (currentLine.length() <= 0) continue;
                
                String[] split = currentLine.split(CSV_DELIMITER);
                if (split.length == 2) {
                    addQuery(split[0], split[1]);
                } else {
                    addQuery(split[0]);
                }
                
            }
            
            br.close();
            
        } catch (FileNotFoundException ex) { }
        catch (IOException ex) { }
        
    }
    
}
