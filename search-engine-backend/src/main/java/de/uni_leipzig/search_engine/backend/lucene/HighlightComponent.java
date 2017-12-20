package de.uni_leipzig.search_engine.backend.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.uni_leipzig.search_engine.backend.controller.search.dto.SearchResult;
import lombok.SneakyThrows;

@Component
public class HighlightComponent
{
	@Autowired
	private IndexReader reader;
	
	private final FastVectorHighlighter fvHighlighter = new FastVectorHighlighter();
	
	private final QueryParser queryParser;
	
	public HighlightComponent(Analyzer analyzer)
	{
		queryParser = new QueryParser(SearchResult.INDEX_FIELD_CONTENT, analyzer);
	}
	
	@SneakyThrows
	public String buildHiglightForDocument(ScoreDoc scoreDoc, Query query)
	{
		return fvHighlighter.getBestFragment(fvHighlighter.getFieldQuery(query, reader), reader,
				scoreDoc.doc, SearchResult.INDEX_FIELD_CONTENT, 500);		
	}

	@SneakyThrows
	public Query parsQueryForHiglights(String query)
	{
		return queryParser.parse(query);
	}
}
