package de.uni_leipzig.search_engine.events;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils
{
	public static HttpServletRequest mockServletRequest(String requestUrl)
	{
		return mockServletRequest(requestUrl, null);
	}
	
	public static HttpServletRequest mockServletRequest(String requestUrl, Map<String, String> header)
	{
	    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
	    Mockito.when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
	    Mockito.when(mockRequest.getRequestURI()).thenReturn(requestUrl);
	    Mockito.when(mockRequest.getContextPath()).thenReturn("");
	    Mockito.when(mockRequest.getServletPath()).thenReturn("");
	    Mockito.when(mockRequest.getRemotePort()).thenReturn(1234);
	    Mockito.when(mockRequest.getRemoteHost()).thenReturn("localhost");
	    Mockito.when(mockRequest.getRemoteAddr()).thenReturn("0.0.0.0");
	    
	    if(header == null)
	    {
	    	Mockito.when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
	    }
	    else
	    {
	    	Mockito.when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(header.keySet()));
	    	Mockito.when(mockRequest.getHeader(Matchers.anyString())).thenAnswer(new Answer<String>()
	    	{
				@Override
				public String answer(InvocationOnMock invocation) throws Throwable
				{
					if(invocation != null && invocation.getArguments() != null
						&& invocation.getArguments().length == 1 && invocation.getArguments()[0] instanceof String)
					{
						return header.get(invocation.getArguments()[0]);
					}
					
					return null;
				}
	    		
	    	});
	    }
	    
	    return mockRequest;
	}
	
	public static void setupHttpServlet(HttpServletRequest request)
	{	

	    ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(request);
	    RequestContextHolder.setRequestAttributes(servletRequestAttributes);
	}
}
