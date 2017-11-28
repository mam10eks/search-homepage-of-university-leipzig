package de.uni_leipzig.search_engine_uni.backend.lucene;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import de.uni_leipzig.search_engine_uni.backend.dto.SearchResult;
import lombok.SneakyThrows;

@Component
public class SearcherComponent
{
	public static final int HITS_PER_PAGE = 10;
	
	private final String[] ANALYZED_QUERY_FIELDS = new String[] {SearchResult.INDEX_FIELD_TITLE, SearchResult.INDEX_FIELD_CONTENT,
		SearchResult.INDEX_FIELD_LINK,SearchResult.INDEX_FIELD_ANCHOR};
	
	private final IndexSearcher searcher;
	
	private final QueryParser queryParser;
	
	@SneakyThrows
	public SearcherComponent()
	{
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("../example_indices/lips_informatik_uni_leipzig")));
		searcher = new IndexSearcher(indexReader);
		queryParser = new MultiFieldQueryParser(ANALYZED_QUERY_FIELDS, new StandardAnalyzer());
		queryParser.setFuzzyMinSim(0.1f);
	}
	
	@SneakyThrows
	public TopDocs search(String query, int n)
	{
		return searcher.search(queryParser.parse(query), n);
	}
	
	@SneakyThrows
	public Document doc(int docID)
	{
		return searcher.doc(docID);
	}
}
