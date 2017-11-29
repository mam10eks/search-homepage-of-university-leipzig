package de.uni_leipzig.search_engine.events;

import java.util.HashMap;
import java.util.Map;

import org.approvaltests.Approvals;
import org.junit.Test;

public class RequestTest
{
	@Test(expected=NullPointerException.class)
	public void checkRequestCouldNotBeCreatedFromNull()
	{
		new Request(null);
	}
	
	@Test
	public void approveRequestCreationWithoutHeaders()
	{
		Approvals.verify(new Request(TestUtils.mockServletRequest("http://www.example.com")));
	}
	
	@Test
	public void approveRequestCreationWithHeaders()
	{
		Map<String, String> headers = new HashMap<>();
		headers.put("a", "value-1");
		headers.put("b", "value-2");
		headers.put("c", "value-3");
		
		Approvals.verify(new Request(TestUtils.mockServletRequest("http://www.example.com", headers)));
	}
}
