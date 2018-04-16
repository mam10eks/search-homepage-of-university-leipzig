package de.uni_leipzig.search_engine.backend.controller.search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.uni_leipzig.search_engine.backend.controller.search.dto.SearchResult;
import de.uni_leipzig.search_engine.backend.controller.search.dto.SearchResultPage;
import de.uni_leipzig.search_engine.backend.lucene.HighlightComponent;
import de.uni_leipzig.search_engine.backend.lucene.SearcherComponent;
import lombok.SneakyThrows;

@Controller
public class SearchController
{
	@Autowired
	private SearcherComponent searcherComponent;
	
	@Autowired
	private HighlightComponent highlightComponent;

	@RequestMapping(method=RequestMethod.GET, path="/api/v1")
	public Object search(@RequestParam(defaultValue="") String q,
			@RequestParam(defaultValue="1") Integer p)
	{
		SearchResultPage ret = new SearchResultPage();
		ret.setOriginalQuery(q);
		p = Math.max(p == null? 1: p, 1);
		
		if("".equals(q) || q == null)
		{
			return ret;
		}
		
		int topNForSearchResult = Math.max(SearcherComponent.HITS_PER_PAGE, p * SearcherComponent.HITS_PER_PAGE);
		
		TopDocs queryTopDocs = searcherComponent.search(q, topNForSearchResult);
		Pair<Integer, List<ScoreDoc>> lastPageNumberAndContent = determineLastPageNumberAndContent(queryTopDocs);
		ret.setTotalHits(queryTopDocs.totalHits);
		ret.setResultsOnPage(mapScoreDocsToSearchResults(lastPageNumberAndContent.getRight(), q));
		ret.setPage(lastPageNumberAndContent.getLeft());
		injectPaginationLinks(ret);
		
		return ret;
	}
	
	public static Pair<Integer, List<ScoreDoc>> determineLastPageNumberAndContent(TopDocs topDocs)
	{
		if(topDocs == null || topDocs.scoreDocs == null || topDocs.scoreDocs.length == 0)
		{
			return Pair.of(1, new ArrayList<>());
		}
		
		final int page = ((topDocs.scoreDocs.length-1)/SearcherComponent.HITS_PER_PAGE);
		List<ScoreDoc> ret = new ArrayList<>();
		
		for(int i=page*SearcherComponent.HITS_PER_PAGE; i< topDocs.scoreDocs.length; i++)
		{
			ret.add(topDocs.scoreDocs[i]);
		}
		
		return Pair.of(page+1, ret);
	}
	
	private static LinkBuilder searchLink(String query, Integer page)
	{
		return ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).search(query, page));
	}
	
	@SneakyThrows
	private List<SearchResult> mapScoreDocsToSearchResults(List<ScoreDoc> scoreDocs, String query)
	{
		Query parsedQuery = highlightComponent.parsQueryForHiglights(query);
		
		return scoreDocs.stream()
			.map(topDoc -> Triple.of(searcherComponent.doc(topDoc.doc), topDoc.doc, 
					highlightComponent.buildHiglightForDocument(topDoc, parsedQuery)))
			.map(SearchResult::new)
			.collect(Collectors.toList());
	}

	public static void injectPaginationLinks(SearchResultPage searchResultPage)
	{
		if(searchResultPage == null)
		{
			return;
		}
		
		int currentPage = Math.max(searchResultPage.getPage(), 1);
		int maxPage = (((int)searchResultPage.getTotalHits()-1)/SearcherComponent.HITS_PER_PAGE) +1;
		
		if(currentPage > 1)
		{
			searchResultPage.setPreviousPage(searchLink(searchResultPage.getOriginalQuery(), currentPage -1).withRel("prev"));
		}
		
		if(currentPage < maxPage)
		{
			searchResultPage.setNextPage(searchLink(searchResultPage.getOriginalQuery(), currentPage+1).withRel("next"));
		}
		
		searchResultPage.setNamedPageLinksAfter(namedPaginationLinksInRange(currentPage+1,
			Math.min(currentPage+4, maxPage+1), searchResultPage));
		
		searchResultPage.setNamedPageLinksBefore(namedPaginationLinksInRange(Math.max(currentPage-3, 1),
				currentPage, searchResultPage));
		
		Link firstPageLink = searchLink(searchResultPage.getOriginalQuery(), 1).withRel("1");
		if(currentPage > 1 && !searchResultPage.getNamedPageLinksBefore().contains(firstPageLink))
		{
			searchResultPage.setFirstPageLink(firstPageLink);
		}
		
		Link lastPageLink = searchLink(searchResultPage.getOriginalQuery(), maxPage).withRel(String.valueOf(maxPage));
		if(currentPage < (maxPage-1) && !searchResultPage.getNamedPageLinksAfter().contains(lastPageLink))
		{
			searchResultPage.setLastPageLink(lastPageLink);
		}
	}
	
	private static List<Link> namedPaginationLinksInRange(int startInclusive, int endExclusive, SearchResultPage searchResultPage)
	{
		return IntStream.range(startInclusive, endExclusive)
			.mapToObj(i -> searchLink(searchResultPage.getOriginalQuery(), i).withRel(String.valueOf(i)))
			.collect(Collectors.toList());
	}
}
