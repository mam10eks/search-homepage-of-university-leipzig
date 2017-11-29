package de.uni_leipzig.search_engine.events;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.Data;

@Data
@RunWith(Parameterized.class)
public class SearchResponseEventTest
{
	private static final String MOCKED_SESSION_ID = "my_session_id";
	
	private final String expectedQuery;
	
	private final int expectedResultPage;
	
	private final String inputUrl;
	
	@Parameters
	public static Collection<Object> parameters()
	{
		return Arrays.asList
			(
				new Object[] {"Medien,Gameboy,Youtube", 1, "http://www.bla.de/?originalQuery=Medien&originalQuery=Gameboy&originalQuery=Youtube"},
				new Object[] {"Medien,Gameboy,Youtube", 1, "http://www.bla.de?originalQuery=Medien,Gameboy,Youtube"},
				new Object[] {"Medien,Youtube", 1, "http://www.bla.de/?originalQuery=Medien&originalQuery=Youtube"},
				new Object[] {"Medien,Youtube", 1, "http://www.bla.de?originalQuery=Medien,Youtube"},
				new Object[] {"Medien,Youtube", 2, "http://www.bla.de/?originalQuery=Medien&originalQuery=Youtube&currentPage=2&currentPage=4"},
				new Object[] {"Medien,Youtube", 2, "http://www.bla.de/?originalQuery=Medien,Youtube&currentPage=2&currentPage=4"},
				new Object[] {"Medien", 2, "http://www.bla.de?originalQuery=Medien&currentPage=2"},
				new Object[] {"Medien", 1, "http://www.bla.de?originalQuery=Medien"},
				new Object[] {"Medien", 1, "http://www.bla.de?originalQuery=Medien&currentPage=drei"}
			);
	}
	
	@Test
	public void assertSearchResponseEventHasQueryAndPage()
	{
		Map<String, String> header = new HashMap<>();
		header.put("key-1", "value-1");
		header.put("key-2", "value-2");
		
		HttpServletRequest request = TestUtils.mockServletRequest(inputUrl, header);
		ServletRequestAttributes attributes = new ServletRequestAttributes(request);
		
		attributes = Mockito.spy(attributes);
		Mockito.doReturn(MOCKED_SESSION_ID).when(attributes).getSessionId();
		
		SearchResponseEvent actual = SearchResponseEvent.fromWebResponseEvent(new WebResponseEvent(attributes));
		
		Assert.assertEquals(expectedQuery, actual.getQuery());
		Assert.assertEquals(expectedResultPage, actual.getResultPage());
		Assert.assertEquals(MOCKED_SESSION_ID, actual.getClientId());
		Assert.assertNull(actual.getResponseModel());
		Assert.assertEquals(header, actual.getRequest().getHeaders());
		Assert.assertEquals("0.0.0.0", actual.getRequest().getRemoteAddr());
		Assert.assertEquals("localhost", actual.getRequest().getRemoteHost());
		Assert.assertEquals(Integer.valueOf(1234), actual.getRequest().getRemotePort());
		Assert.assertEquals(inputUrl, actual.getRequest().getRequestUrl());
	}
}
