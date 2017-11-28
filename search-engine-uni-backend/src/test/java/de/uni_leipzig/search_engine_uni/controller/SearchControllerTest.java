package de.uni_leipzig.search_engine_uni.controller;

import org.approvaltests.Approvals;
import org.junit.Before;
import org.junit.Test;

public class SearchControllerTest
{
	@Before
	public void setup()
	{
		SearchControllerMockUtils.setupHttpServlet();
	}
	
	@Test
	public void approveNullInput()
	{
		Approvals.verify(SearchControllerMockUtils.createSearchController(0).search(null, null));
	}
	
	@Test
	public void approveEmptyQueryInput()
	{
		Approvals.verify(SearchControllerMockUtils.createSearchController(0).search("", null));
	}
	
	@Test
	public void approveFirstPageResultWithOneMorePageAvailable()
	{
		Approvals.verify(SearchControllerMockUtils.createSearchController(11).search("queryDoesNotMatter", 1));
	}
	
	@Test
	public void approveSecondPageResultWithOnePageBeforeAvailable()
	{
		Approvals.verify(SearchControllerMockUtils.createSearchController(11).search("queryDoesNotMatter", 2));
	}
	
	@Test
	public void approveRequestOfFifthPageReturnsSecondPageIfOnlyElevenResultsAreAvailable()
	{
		Approvals.verify(SearchControllerMockUtils.createSearchController(11).search("queryDoesNotMatter", 5));
	}
	
	@Test
	public void approveNextAndPreviousPageLinksAreCorrectForSecondPageOfThreePages()
	{
		Approvals.verify(SearchControllerMockUtils.createSearchController(30).search("queryDoesNotMatter", 2));
	}
	
	@Test
	public void approveNegativePageRequestForFiftySevenAvailableResults()
	{
		Approvals.verify(SearchControllerMockUtils.createSearchController(57).search("queryDoesNotMatter", -1));
	}
	
	@Test
	public void approveLastPageRequestForFiftySevenAvailableResults()
	{
		Approvals.verify(SearchControllerMockUtils.createSearchController(57).search("queryDoesNotMatter", 6));
	}
}
