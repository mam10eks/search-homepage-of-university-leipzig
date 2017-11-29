package de.uni_leipzig.search_engine.events;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.approvaltests.Approvals;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.ServletRequestAttributes;

public class WebResponseEventTest
{
	@Test
	public void checkWebResponseEventCreationWithNullAsSessionId()
	{
		Map<String, String> header = new HashMap<>();
		header.put("key-1", "value-1");
		header.put("key-2", "value-2");
		
		HttpServletRequest request = TestUtils.mockServletRequest("http://www.bla.de", header);
		ServletRequestAttributes attributes = new ServletRequestAttributes(request);
		
		attributes = Mockito.spy(attributes);
		Mockito.doReturn(null).when(attributes).getSessionId();
		
		Approvals.verify(new WebResponseEvent(attributes));
	}
	
	@Test
	public void checkWebResponseEventCreationWithNonEmptySessionId()
	{
		Map<String, String> header = new HashMap<>();
		header.put("key-1", "value-1");
		header.put("key-2", "value-2");
		
		HttpServletRequest request = TestUtils.mockServletRequest("http://www.bla.de", header);
		ServletRequestAttributes attributes = new ServletRequestAttributes(request);
		
		attributes = Mockito.spy(attributes);
		Mockito.doReturn("MySessionId").when(attributes).getSessionId();
		
		Approvals.verify(new WebResponseEvent(attributes));
	}
	
	@Test
	public void checkSimpleWebResponseEventIsNoSearchResponseEvent()
	{
		Map<String, String> header = new HashMap<>();
		header.put("key-1", "value-1");
		header.put("key-2", "value-2");
		
		HttpServletRequest request = TestUtils.mockServletRequest("http://www.bla.de", header);
		ServletRequestAttributes attributes = new ServletRequestAttributes(request);
		
		attributes = Mockito.spy(attributes);
		Mockito.doReturn("MySessionId").when(attributes).getSessionId();

		Assert.assertNull(SearchResponseEvent.fromWebResponseEvent(new WebResponseEvent()));
		Assert.assertNull(SearchResponseEvent.fromWebResponseEvent(null));
		Assert.assertNull(SearchResponseEvent.fromWebResponseEvent(new WebResponseEvent(attributes)));
	}
	
	@Test
	public void checkSimpleWebResponseEventIsNoSearchResultSelectedEvent()
	{
		Map<String, String> header = new HashMap<>();
		header.put("key-1", "value-1");
		header.put("key-2", "value-2");
		
		HttpServletRequest request = TestUtils.mockServletRequest("http://www.bla.de", header);
		ServletRequestAttributes attributes = new ServletRequestAttributes(request);
		
		attributes = Mockito.spy(attributes);
		Mockito.doReturn("MySessionId").when(attributes).getSessionId();
		
		Assert.assertNull(SearchResultSelectedEvent.fromWebResponseEvent(new WebResponseEvent()));
		Assert.assertNull(SearchResultSelectedEvent.fromWebResponseEvent(null));
		Assert.assertNull(SearchResultSelectedEvent.fromWebResponseEvent(new WebResponseEvent(attributes)));
	}
	
	@Test
	public void checkThatSimpleWebResponseWithInvalidUriIsNoDerivedEvent()
	{
		HttpServletRequest request = TestUtils.mockServletRequest("\"");
		ServletRequestAttributes attributes = new ServletRequestAttributes(request);
		
		attributes = Mockito.spy(attributes);
		Mockito.doReturn("MySessionId").when(attributes).getSessionId();
		
		Assert.assertNull(SearchResponseEvent.fromWebResponseEvent(new WebResponseEvent(attributes)));
		Assert.assertNull(SearchResultSelectedEvent.fromWebResponseEvent(new WebResponseEvent(attributes)));
	}
}
