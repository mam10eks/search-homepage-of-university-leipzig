package de.uni_leipzig.search_engine_uni.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.junit.Assert;
import org.junit.Test;

import de.uni_leipzig.search_engine_uni.controller.SearchController;

public class SearchControllerPaginationTest
{	
	private static TopDocs topDocs(int numberOfTopDocs)
	{
		return new TopDocs(Long.MAX_VALUE, scoreDocs(0, numberOfTopDocs), Double.MAX_EXPONENT);
	}
	
	private static ScoreDoc[] scoreDocs(int startIndex, int endIndexExclusive)
	{
		List<ScoreDoc> ret = IntStream.range(startIndex, endIndexExclusive)
			.mapToObj(docID -> new ScoreDoc(docID, 0f))
			.collect(Collectors.toList());
		
		return ret.toArray(new ScoreDoc[ret.size()]);
	}
	
	@Test
	public void checkNullInput()
	{
		Assert.assertEquals(Pair.of(1, new ArrayList<>()), SearchController.determineLastPageNumberAndContent(null));
	}
	
	@Test
	public void checkEmptyInput()
	{
		Assert.assertEquals(Pair.of(1, new ArrayList<>()), SearchController.determineLastPageNumberAndContent(topDocs(0)));
	}
	
	@Test
	public void checkInputWithOneTopDoc()
	{
		Pair<Integer, List<ScoreDoc>> result = SearchController.determineLastPageNumberAndContent(topDocs(1));
		
		Assert.assertEquals(Integer.valueOf(1), result.getLeft());
		assertEqualDocumentIdsAreEqual(Arrays.asList(scoreDocs(0, 1)), result.getRight());
	}
	
	@Test
	public void checkInputWithTenTopDoc()
	{
		Pair<Integer, List<ScoreDoc>> result = SearchController.determineLastPageNumberAndContent(topDocs(10));
		
		Assert.assertEquals(Integer.valueOf(1), result.getLeft());
		assertEqualDocumentIdsAreEqual(Arrays.asList(scoreDocs(0, 10)), result.getRight());
	}
	
	@Test
	public void checkInputWithElevenTopDoc()
	{
		Pair<Integer, List<ScoreDoc>> result = SearchController.determineLastPageNumberAndContent(topDocs(11));
		
		Assert.assertEquals(Integer.valueOf(2), result.getLeft());
		assertEqualDocumentIdsAreEqual(Arrays.asList(scoreDocs(10, 11)), result.getRight());
	}
	
	@Test
	public void checkInputWithSixtySixTopDoc()
	{
		Pair<Integer, List<ScoreDoc>> result = SearchController.determineLastPageNumberAndContent(topDocs(66));
		
		Assert.assertEquals(Integer.valueOf(7), result.getLeft());
		assertEqualDocumentIdsAreEqual(Arrays.asList(scoreDocs(60, 66)), result.getRight());
	}
	
	@Test
	public void checkInputWithTwentyTopDoc()
	{
		Pair<Integer, List<ScoreDoc>> result = SearchController.determineLastPageNumberAndContent(topDocs(20));
		
		Assert.assertEquals(Integer.valueOf(2), result.getLeft());
		assertEqualDocumentIdsAreEqual(Arrays.asList(scoreDocs(10, 20)), result.getRight());
	}
	
	@Test
	public void checkInputWithTwentyOneTopDoc()
	{
		Pair<Integer, List<ScoreDoc>> result = SearchController.determineLastPageNumberAndContent(topDocs(21));
		
		Assert.assertEquals(Integer.valueOf(3), result.getLeft());
		assertEqualDocumentIdsAreEqual(Arrays.asList(scoreDocs(20, 21)), result.getRight());
	}
	
	private static void assertEqualDocumentIdsAreEqual(List<ScoreDoc> a, List<ScoreDoc> b)
	{
		Assert.assertEquals(mapToDocIds(a), mapToDocIds(b));
	}
	
	private static List<Integer> mapToDocIds(List<ScoreDoc> a)
	{
		return a.stream()
			.map(document -> document.doc)
			.collect(Collectors.toList());
	}
}
