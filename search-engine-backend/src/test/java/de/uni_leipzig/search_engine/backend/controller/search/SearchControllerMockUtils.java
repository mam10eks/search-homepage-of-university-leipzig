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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.uni_leipzig.search_engine.backend.controller.search.SearchController;
import de.uni_leipzig.search_engine.backend.lucene.HighlightComponent;
import de.uni_leipzig.search_engine.backend.lucene.SearcherComponent;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchControllerMockUtils
{
	public static SearchController createSearchController(int endIndexExclusive)
	{
		final List<Document> documents = IntStream.range(0, endIndexExclusive)
			.mapToObj(SearchControllerMockUtils::intToDocument)
			.collect(Collectors.toList());
		
		SearcherComponent searcherComponent = Mockito.mock(SearcherComponent.class);
		Mockito.when(searcherComponent.doc(Matchers.anyInt())).thenAnswer(i -> documents.get((int)(i.getArguments()[0])));
		Mockito.when(searcherComponent.search(Mockito.anyString(), Mockito.anyInt())).thenAnswer(i ->
		{
			int n = (int)i.getArguments()[1];
			ScoreDoc[] scoreDocs = IntStream.range(0, Math.min(n, endIndexExclusive)).mapToObj(index -> new ScoreDoc(index, 0f))
				.collect(Collectors.toList()).toArray(new ScoreDoc[Math.min(n, endIndexExclusive)]);
			
			return new TopDocs(documents.size(), scoreDocs, 0f);
		});
		
		HighlightComponent highlightComponent = Mockito.mock(HighlightComponent.class);
		Mockito.when(highlightComponent.buildHiglightForDocument(Matchers.any(), Matchers.any()))
		.thenReturn(null);
		
		SearchController ret = new SearchController();
		Whitebox.setInternalState(ret, "searcherComponent", searcherComponent);
		Whitebox.setInternalState(ret, "highlightComponent", highlightComponent);
		return ret;
	}
	
	private static Document intToDocument(Integer id)
	{
		Document ret = new Document();
		ret.add(new TextField("title", String.valueOf(id), Field.Store.YES));
		
		return ret;
	}
	
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
