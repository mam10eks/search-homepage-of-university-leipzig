package de.uni_leipzig.search_engine.backend.controller;

import java.util.Arrays;

import org.approvaltests.Approvals;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_leipzig.search_engine.backend.controller.SearchController;
import de.uni_leipzig.search_engine.backend.dto.SearchResultPage;

public class SearchControllerLinksTest
{
	@Before
	public void setup()
	{
		SearchControllerMockUtils.setupHttpServlet();
	}
	
	@Test
	public void testNullAsInput()
	{
		SearchController.injectPaginationLinks(null);
	}
	
	@Test
	public void testEmptyPageAsInput()
	{
		SearchResultPage expected = new SearchResultPage();
		expected.setNamedPageLinksBefore(Arrays.asList());
		expected.setNamedPageLinksAfter(Arrays.asList());
		
		SearchResultPage resultPageToVerify = new SearchResultPage();
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Assert.assertEquals(expected, resultPageToVerify);
	}
	
	@Test
	public void checkEmptyResultAsInput()
	{
		SearchResultPage resultPageToVerify = new SearchResultPage();
		resultPageToVerify.setPage(1);
		resultPageToVerify.setTotalHits(0);
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Approvals.verify(resultPageToVerify);
	}
	
	@Test
	public void checkThatNextPageLinkIsCreated()
	{
		SearchResultPage resultPageToVerify = new SearchResultPage();
		resultPageToVerify.setPage(1);
		resultPageToVerify.setTotalHits(11);
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Approvals.verify(resultPageToVerify);
	}
	
	@Test
	public void checkThatPreviousPageLinkIsCreated()
	{
		SearchResultPage resultPageToVerify = new SearchResultPage();
		resultPageToVerify.setPage(2);
		resultPageToVerify.setTotalHits(11);
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Approvals.verify(resultPageToVerify);
	}
	
	@Test
	public void checkThatNextAndPreviousPageLinksAreCreated()
	{
		SearchResultPage resultPageToVerify = new SearchResultPage();
		resultPageToVerify.setPage(2);
		resultPageToVerify.setTotalHits(21);
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Approvals.verify(resultPageToVerify);
	}
	
	@Test
	public void checkThatNextPageLinkAndNamedPageLinksAfterAreCreated()
	{
		SearchResultPage resultPageToVerify = new SearchResultPage();
		resultPageToVerify.setPage(1);
		resultPageToVerify.setTotalHits(61);
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Approvals.verify(resultPageToVerify);
	}
	
	@Test
	public void checkThatPreviousPageLinkAndNamedPageLinksBeforeAreCreated()
	{
		SearchResultPage resultPageToVerify = new SearchResultPage();
		resultPageToVerify.setPage(7);
		resultPageToVerify.setTotalHits(61);
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Approvals.verify(resultPageToVerify);
	}
	
	@Test
	public void checkThatNoFirstAndLastPageLinkIsCreatedIfTheyAreAlreadyContainedInNamedLinks()
	{
		SearchResultPage resultPageToVerify = new SearchResultPage();
		resultPageToVerify.setPage(4);
		resultPageToVerify.setTotalHits(70);
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Approvals.verify(resultPageToVerify);
	}
	
	@Test
	public void checkThatFirstAndLastPageLinkAndBeforeAndAfterLinksAreCreated()
	{
		SearchResultPage resultPageToVerify = new SearchResultPage();
		resultPageToVerify.setPage(5);
		resultPageToVerify.setTotalHits(100);
		SearchController.injectPaginationLinks(resultPageToVerify);
		
		Approvals.verify(resultPageToVerify);
	}
}
