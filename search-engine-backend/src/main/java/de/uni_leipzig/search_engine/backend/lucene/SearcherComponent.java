package de.uni_leipzig.search_engine.backend.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.springframework.stereotype.Component;

import de.uni_leipzig.search_engine.backend.controller.search.dto.SearchResult;
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
	public SearcherComponent(Analyzer analyzer, IndexSearcher searcher)
	{
		this.searcher = searcher;
		queryParser = new MultiFieldQueryParser(ANALYZED_QUERY_FIELDS, analyzer);
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
