package de.uni_leipzig.search_engine_uni.lucene;

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

import de.uni_leipzig.search_engine_uni.dto.SearchResult;
import lombok.SneakyThrows;

@Component
public class SearcherComponent
{
	public static final int HITS_PER_PAGE = 10;
	
	private final IndexSearcher searcher;
	
	private final QueryParser queryParser;
	
	@SneakyThrows
	public SearcherComponent()
	{
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("lucene_index")));
		searcher = new IndexSearcher(indexReader);
		queryParser = new MultiFieldQueryParser(new String[] {SearchResult.INDEX_FIELD_TITLE, SearchResult.INDEX_FIELD_CONTENT}, 
					new StandardAnalyzer());
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
