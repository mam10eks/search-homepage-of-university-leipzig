package de.uni_leipzig.search_engine.backend.lucene;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import lombok.SneakyThrows;

public class SuggestionComponent
{
	private final IndexWriter suggestIndexWriter;

	private final Directory index = new RAMDirectory();
	
	private final Analyzer analyzer = new StandardAnalyzer();
	
	private final Function<Directory, Lookup> suggestionFactory;
	
	public SuggestionComponent()
	{
		this(d -> new AnalyzingSuggester(d, "query", new StandardAnalyzer()));
	}
	
	@SneakyThrows
	public SuggestionComponent(Function<Directory, Lookup> suggestionFactory)
	{
		this.suggestionFactory = suggestionFactory;
		IndexWriterConfig conf = new IndexWriterConfig(analyzer)
				.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		suggestIndexWriter = new IndexWriter(index, conf);
		suggestIndexWriter.commit();
	}
	
	@SneakyThrows
	public void add(String string)
	{
		Document doc = new Document();
		doc.add(new TextField("query", string, Store.YES));
		
		suggestIndexWriter.addDocument(doc);
		suggestIndexWriter.commit();
	}

	@SneakyThrows
	public List<String> suggest(String string)
	{
		Lookup suggester = suggestionFactory.apply(index);
		IndexReader reader = DirectoryReader.open(index);
		
		LuceneDictionary dict = new LuceneDictionary(reader, "query");
		suggester.build(dict);
		
		return suggester.lookup(string, Boolean.FALSE, 10).stream()
				.map(result -> result.key.toString())
				.collect(Collectors.toList());
	}
}
