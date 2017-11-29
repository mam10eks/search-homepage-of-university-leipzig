package de.uni_leipzig.search_engine.events;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.approvaltests.Approvals;
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
}
