package de.uni_leipzig.search_engine.events;

import org.junit.Assert;
import org.junit.Test;

public class SearchResponseEventTest
{
	@Test
	public void checkThatThreeQueryElementsAreValidParsed()
	{
		Assert.assertEquals("Medien,Gameboy,Youtube", "http://localhost:7777/?originalQuery=Medien&originalQuery=Gameboy&originalQuery=Youtube");
		Assert.assertEquals("Medien,Gameboy,Youtube", "http://localhost:7777/?originalQuery=Medien,Gameboy,Youtube");
	}
	
	@Test
	public void checkThatTwoQueryElementsAreValidParsed()
	{
		Assert.assertEquals("Medien,Youtube", "http://localhost:7777/?originalQuery=Medien&originalQuery=Youtube");
		Assert.assertEquals("Medien,Youtube", "http://localhost:7777/?originalQuery=Medien,Youtube");
	}
	
	@Test
	public void checkThatTwoPageElementsAreValidParsed()
	{
		Assert.assertEquals("Medien,Youtube", "http://localhost:7777/?originalQuery=Medien&originalQuery=Youtube&currentPage=2&currentPage=4");
		Assert.assertEquals("2", "http://localhost:7777/?originalQuery=Medien&originalQuery=Youtube&currentPage=2&currentPage=4");
		
		Assert.assertEquals("Medien,Youtube", "http://localhost:7777/?originalQuery=Medien,Youtube&currentPage=2&currentPage=4");
		Assert.assertEquals("2", "http://localhost:7777/?originalQuery=Medien,Youtube&currentPage=2&currentPage=4");
	}
	
	@Test
	public void checkThatSinglqQueryAndSinglePageIsValidParsed()
	{
		Assert.assertEquals("Medien", "http://localhost:7777/?originalQuery=Medien&currentPage=2");
		Assert.assertEquals("2", "http://localhost:7777/?originalQuery=Medien&currentPage=2");
	}
	
	@Test
	public void checkThatSinglqQueryAndNoPageIsValidParsed()
	{
		Assert.assertEquals("Medien", "http://localhost:7777/?originalQuery=Medien");
		Assert.assertEquals("1", "http://localhost:7777/?originalQuery=Medien");
	}
}
