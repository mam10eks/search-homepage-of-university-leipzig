package de.uni_leipzig.search_engine.backend.controller.search;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.uni_leipzig.search_engine.backend.controller.search.SearchController;
import de.uni_leipzig.search_engine.backend.lucene.HighlightComponent;
import de.uni_leipzig.search_engine.backend.lucene.SearcherComponent;
import lombok.experimental.UtilityClass;

/**
 * 
 * @author Maik Fröbe
 *
 */
@UtilityClass
public class SearchControllerMockUtils
{
	/**
	 * Mock a {@link SearchController search controller}.
	 * 
	 * @param numberOfDocuments
	 * 		The number of dummy documents that should be contained in the index.
	 * @return
	 * 		A {@link SearchController search controller} which will search in the dummy documents.
	 */
	public static SearchController createSearchController(int numberOfDocuments)
	{		
		HighlightComponent highlightComponent = Mockito.mock(HighlightComponent.class);
		Mockito.when(highlightComponent.buildHiglightForDocument(Matchers.any(), Matchers.any()))
		.thenReturn(null);
		
		SearchController ret = new SearchController();
		Whitebox.setInternalState(ret, "searcherComponent", searchComponent(numberOfDocuments));
		Whitebox.setInternalState(ret, "highlightComponent", highlightComponent);
		return ret;
	}
	
	/**
	 * Mock a {@link SearcherComponent search component}.
	 * 
	 * @param numberOfDocuments
	 * 		The number of dummy documents that should be contained in the index.
	 * @return
	 * 		A {@link SearcherComponent} which will search in the dummy documents.
	 */
	public static SearcherComponent searchComponent(int numberOfDocuments)
	{
		final List<Document> documents = IntStream.range(0, numberOfDocuments)
				.mapToObj(SearchControllerMockUtils::intToDocument)
				.collect(Collectors.toList());
			
		SearcherComponent searcherComponent = Mockito.mock(SearcherComponent.class);
		Mockito.when(searcherComponent.doc(Matchers.anyInt())).thenAnswer(i -> documents.get((int)(i.getArguments()[0])));
		Mockito.when(searcherComponent.search(Mockito.anyString(), Mockito.anyInt())).thenAnswer(i ->
		{
			int n = (int)i.getArguments()[1];
			ScoreDoc[] scoreDocs = IntStream.range(0, Math.min(n, numberOfDocuments)).mapToObj(index -> new ScoreDoc(index, 0f))
				.collect(Collectors.toList()).toArray(new ScoreDoc[Math.min(n, numberOfDocuments)]);
				
			return new TopDocs(documents.size(), scoreDocs, 0f);
		});
		
		return searcherComponent;
	}
	
	private static Document intToDocument(Integer id)
	{
		Document ret = new Document();
		ret.add(new TextField("title", String.valueOf(id), Field.Store.YES));
		
		return ret;
	}
	
	/**
	 * Bind a mocked HTTP request to the current thread.
	 * Useful for some methods that are only executable during the processing of a HTTP request.
	 * 
	 * @see
	 * ControllerLinkBuilder
	 */
	public void setupHttpServlet()
	{	
	    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
	    Mockito.when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com"));
	    Mockito.when(mockRequest.getRequestURI()).thenReturn("http://www.example.com");
	    Mockito.when(mockRequest.getContextPath()).thenReturn("");
	    Mockito.when(mockRequest.getServletPath()).thenReturn("");
	    Mockito.when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
	    
	    ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(mockRequest);
	    RequestContextHolder.setRequestAttributes(servletRequestAttributes);
	}
}
