package de.uni_leipzig.search_engine.backend.lucene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.BytesRef;

import lombok.SneakyThrows;

public class SuggestionComponent
{
    private static final String QUERY_FIELD_ID = "id";
    private static final String QUERY_FIELD_NAME = "query";
    private static final String QUERY_FIELD_FREQ = "count";
    private static final String QUERY_FIELD_USER = "user";
    
    private static final Integer MAX_RESULT = 10;
    private static final Integer GLOBAL_QUERY_USERS = 2;
    
    private final IndexWriter suggestIndexWriter;

    private final Directory index;
    
    private final Analyzer analyzer = new StandardAnalyzer();

    private final Function<Directory, Lookup> suggestionFactory;

    public SuggestionComponent()
    {
        this(d -> new AnalyzingSuggester(d, QUERY_FIELD_NAME, new StandardAnalyzer()));
    }
    
    public SuggestionComponent(Function<Directory, Lookup> suggestionFactory)
    {
    	this(suggestionFactory, new RAMDirectory());
    }
    
    public SuggestionComponent(Directory index)
    {
    	this(d -> new AnalyzingSuggester(d, QUERY_FIELD_NAME, new StandardAnalyzer()), index);
    }
    
    @SneakyThrows
    public SuggestionComponent(Function<Directory, Lookup> suggestionFactory, Directory index)
    {
        this.suggestionFactory = suggestionFactory;
        this.index = index;
        
        IndexWriterConfig conf = new IndexWriterConfig(analyzer)
                        .setOpenMode(OpenMode.CREATE_OR_APPEND);

        suggestIndexWriter = new IndexWriter(index, conf);
        suggestIndexWriter.commit();
    }

    @SneakyThrows
    public void add(String query)
    {
        query = query.toLowerCase();
        
        Document doc = new Document();
        String id = UUID.randomUUID().toString();
        doc.add(new StoredField(QUERY_FIELD_ID, id));
        doc.add(new Field(QUERY_FIELD_ID, new BytesRef(id), StringField.TYPE_STORED)); 
        doc.add(new TextField(QUERY_FIELD_NAME, query, Store.YES));
        doc.add(new TextField(QUERY_FIELD_FREQ, String.valueOf(GLOBAL_QUERY_USERS), Store.YES));
        
        suggestIndexWriter.addDocument(doc);
        suggestIndexWriter.commit();
        
    }
    
    @SneakyThrows
    public void addOrUpdate(String query)
    {
        query = query.toLowerCase();
        
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(new QueryParser(QUERY_FIELD_NAME, analyzer).parse(query), 1);

        ScoreDoc[] hits = docs.scoreDocs;
        
        Document doc = null;
        if (hits.length > 0){
            ScoreDoc sd = hits[0];
            
            if (searcher.doc(sd.doc).get(QUERY_FIELD_NAME).equals(query)) {
                doc = searcher.doc(sd.doc);
                Long l = Long.valueOf(doc.getField(QUERY_FIELD_FREQ).stringValue());
                String id = doc.getField(QUERY_FIELD_ID).stringValue();
                Term term = new Term(QUERY_FIELD_ID, id);
                suggestIndexWriter.deleteDocuments(term);
                suggestIndexWriter.flush();
                
                doc = new Document();
                id = UUID.randomUUID().toString();
                doc.add(new StoredField(QUERY_FIELD_ID, id));
                doc.add(new Field(QUERY_FIELD_ID, new BytesRef(id), StringField.TYPE_STORED)); 
                doc.add(new TextField(QUERY_FIELD_NAME, query, Store.YES));
                doc.add(new TextField(QUERY_FIELD_FREQ, String.valueOf(l+1), Store.YES));
            }
        }
        
        if (doc == null) {
            doc = new Document();
            String id = UUID.randomUUID().toString();
            doc.add(new StoredField(QUERY_FIELD_ID, id));
            doc.add(new Field(QUERY_FIELD_ID, new BytesRef(id), StringField.TYPE_STORED));
            doc.add(new TextField(QUERY_FIELD_NAME, query, Store.YES));
            doc.add(new TextField(QUERY_FIELD_FREQ, "1", Store.YES));
        }
        
        suggestIndexWriter.addDocument(doc);
        suggestIndexWriter.commit();
        
    }
    
    @SneakyThrows
    public void addOrUpdate(String user, String query)
    {
        query = query.toLowerCase();
        
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(new QueryParser(QUERY_FIELD_NAME, analyzer).parse(query), 1);
        
        ScoreDoc[] hits = docs.scoreDocs;
        
        Document doc = null;
        if (hits.length > 0){
            ScoreDoc sd = hits[0];
            
            if (searcher.doc(sd.doc).get(QUERY_FIELD_NAME).equals(query)) {
                doc = searcher.doc(sd.doc);
                
                IndexableField[] fields = doc.getFields(QUERY_FIELD_USER);
                
                // if query is global
                if (fields.length == 0) {
                    
                    Long l = Long.valueOf(doc.getField(QUERY_FIELD_FREQ).stringValue());
                    String id = doc.getField(QUERY_FIELD_ID).stringValue();
                    Term term = new Term(QUERY_FIELD_ID, id);
                    suggestIndexWriter.deleteDocuments(term);
                    suggestIndexWriter.flush();
                    doc.removeFields(QUERY_FIELD_FREQ);
                    doc.add(new TextField(QUERY_FIELD_FREQ, String.valueOf(l+1), Store.YES));
                    
                } else {
                    
                    boolean userExist = false;
                    for (IndexableField field : fields) {
                        if (field.stringValue().equals(user)) {
                            userExist = true;
                            break;
                        }
                    }

                    if (!userExist){

                        Long l = Long.valueOf(doc.getField(QUERY_FIELD_FREQ).stringValue());
                        String id = doc.getField(QUERY_FIELD_ID).stringValue();
                        Term term = new Term(QUERY_FIELD_ID, id);
                        suggestIndexWriter.deleteDocuments(term);
                        suggestIndexWriter.flush();
                        
                        if (fields.length >= (GLOBAL_QUERY_USERS-1)){

                            doc.removeFields(QUERY_FIELD_USER);
                            doc.removeFields(QUERY_FIELD_FREQ);
                            doc.add(new TextField(QUERY_FIELD_FREQ, String.valueOf(l+fields.length), Store.YES));

                        } else {

                            doc.add(new TextField(QUERY_FIELD_USER, user, Store.YES));
                            
                        }

                    }
                
                    
                }
                
            }
        }
        
        if (doc == null) {
            doc = new Document();
            String id = UUID.randomUUID().toString();
            doc.add(new StoredField(QUERY_FIELD_ID, id));
            doc.add(new Field(QUERY_FIELD_ID, new BytesRef(id), StringField.TYPE_STORED));
            doc.add(new TextField(QUERY_FIELD_NAME, query, Store.YES));
            doc.add(new TextField(QUERY_FIELD_USER, user, Store.YES));
            doc.add(new TextField(QUERY_FIELD_FREQ, "1", Store.YES));
        }
        
        suggestIndexWriter.addDocument(doc);
        suggestIndexWriter.commit();
    }

    @SneakyThrows
    public List<String> suggest(String string)
    {
        IndexReader reader = DirectoryReader.open(index);
        Lookup suggester = suggestionFactory.apply(index);
        
        Dictionary dict = new LuceneDictionary(reader, QUERY_FIELD_NAME);
        suggester.build(dict);

        List<String> list = suggester.lookup(string, Boolean.FALSE, MAX_RESULT).stream()
                        .map(result -> result.key.toString())
                        .collect(Collectors.toList());
        
        List<String> result = new ArrayList<>();
        for (String word : list){
            
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(new QueryParser(QUERY_FIELD_NAME, analyzer).parse(word), MAX_RESULT);

            ScoreDoc[] hits = docs.scoreDocs;
            
            if (hits.length > 0) {
                for(int i=0;i<hits.length;++i) {
                    String w = searcher.doc(hits[i].doc).get(QUERY_FIELD_NAME);
                    if (!result.contains(w)) result.add(w);
                }
            }
            
        }
                
        return result;
        
    }
    
    @SneakyThrows
    public List<String> suggestByUpdated(String string)
    {
        IndexReader reader = DirectoryReader.open(index);
        Lookup suggester = suggestionFactory.apply(index);
        
        Dictionary dict = new LuceneDictionary(reader, QUERY_FIELD_NAME);
        suggester.build(dict);

        List<String> list = suggester.lookup(string, Boolean.FALSE, MAX_RESULT).stream()
                        .map(result -> result.key.toString())
                        .collect(Collectors.toList());
        
        Map<String, Integer> map = new HashMap<>();
        for (String word : list){
            
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(new QueryParser(QUERY_FIELD_NAME, analyzer).parse(word), MAX_RESULT);

            ScoreDoc[] hits = docs.scoreDocs;
            
            if (hits.length > 0) {
                for(int i=0;i<hits.length;++i) {
                    String query = searcher.doc(hits[i].doc).get(QUERY_FIELD_NAME);
                    Integer freq = Integer.parseInt(searcher.doc(hits[i].doc).get(QUERY_FIELD_FREQ));
                    
                    map.put(query, freq);
                }
            }   
        }
        
        List<Map.Entry<String, Integer>> entry_list = new ArrayList<>(map.entrySet());
        Collections.sort(entry_list, (Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> (o1.getValue()).compareTo(o2.getValue()));
        Collections.reverse(entry_list);
        
        list.clear();
        entry_list.forEach((e) -> {list.add(e.getKey());});
        return list.subList(0, Math.min(list.size(), MAX_RESULT));
        
    }
    
    @SneakyThrows
    public List<String> suggestByUpdated(String user, String string)
    {
        IndexReader reader = DirectoryReader.open(index);
        Lookup suggester = suggestionFactory.apply(index);
        
        Dictionary dict = new LuceneDictionary(reader, QUERY_FIELD_USER);
        suggester.build(dict);
            
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(new QueryParser(QUERY_FIELD_USER, analyzer).parse(user), Integer.MAX_VALUE);

        ScoreDoc[] hits = docs.scoreDocs;

        List<String> list = new ArrayList<>();
        
        if (hits.length > 0){
            
            Directory user_index = new RAMDirectory();
            
            IndexWriterConfig user_config = new IndexWriterConfig(analyzer);
            user_config = user_config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            
            IndexWriter user_writer = new IndexWriter(user_index, user_config);
            
            for (ScoreDoc hit : hits) {
                Document doc = searcher.doc(hit.doc);
                user_writer.addDocument(doc);
            }
            
            user_writer.commit();
            user_writer.close();
            
            IndexReader user_reader = DirectoryReader.open(user_index);
            Lookup user_suggester = suggestionFactory.apply(user_index);
            
            Dictionary user_dict = new LuceneDictionary(user_reader, QUERY_FIELD_NAME);
            user_suggester.build(user_dict);
            
            List<String> user_list = user_suggester.lookup(string, Boolean.FALSE, MAX_RESULT).stream()
                        .map(result -> result.key.toString())
                        .collect(Collectors.toList());
            
            for (String word : user_list){
                
                IndexSearcher user_searcher = new IndexSearcher(user_reader);
                TopDocs user_docs = user_searcher.search(new QueryParser(QUERY_FIELD_NAME, analyzer).parse(word), MAX_RESULT);

                ScoreDoc[] user_hits = user_docs.scoreDocs;

                for(int i=0;i<user_hits.length;++i) {
                    String query = user_searcher.doc(user_hits[i].doc).get(QUERY_FIELD_NAME);
                    if (!list.contains(query)) list.add(query);
                }
            
            }
            
        }
          
        if (list.size() >= MAX_RESULT) return list.subList(0, MAX_RESULT);
                
        dict = new LuceneDictionary(reader, QUERY_FIELD_NAME);
        suggester.build(dict);

        List<String> global_list = suggester.lookup(string, Boolean.FALSE, MAX_RESULT).stream()
                        .map(result -> result.key.toString())
                        .collect(Collectors.toList());
        
        Map<String, Integer> map = new HashMap<>();
        for (String word : global_list){
            
            searcher = new IndexSearcher(reader);
            docs = searcher.search(new QueryParser(QUERY_FIELD_NAME, analyzer).parse(word), Integer.MAX_VALUE);

            hits = docs.scoreDocs;
            
            if (hits.length > 0) {
                for(int i=0;i<hits.length;++i) {
                    if (searcher.doc(hits[i].doc).getField(QUERY_FIELD_USER) != null) continue;
                    String query = searcher.doc(hits[i].doc).get(QUERY_FIELD_NAME);
                    Integer freq = Integer.parseInt(searcher.doc(hits[i].doc).get(QUERY_FIELD_FREQ));
                    
                    map.put(query, freq);
                }
            }   
        }
        
        List<Map.Entry<String, Integer>> entry_list = new ArrayList<>(map.entrySet());
        Collections.sort(entry_list, (Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> (o1.getValue()).compareTo(o2.getValue()));
        Collections.reverse(entry_list);
        
        entry_list.forEach((e) -> {if (!list.contains(e.getKey())) list.add(e.getKey());});
        return list.subList(0, Math.min(list.size(), MAX_RESULT));
        
    }
        
}
