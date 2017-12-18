package de.uni_leipzig.custom_index_cleaning;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import lombok.SneakyThrows;

public class IndexCleanerTest
{
	private static final SolrInputDocument DIGEST_ABC_DOC_ONE = solrDoc("ABC", "ABC");
	
	private static final SolrInputDocument DIGEST_AB_ONE = solrDoc("DIGEST_AB_ONE", "AB");
	
	private static final SolrInputDocument DIGEST_AB_TWO = solrDoc("DIGEST_AB_2", "AB");
	
	private static final SolrInputDocument DIGEST_A_ONE = solrDoc("digAOne", "A");
	
	private static final SolrInputDocument DIGEST_A_TWO = solrDoc("digAOne", "A");
	
	private static final SolrInputDocument DIGEST_A_THREE = solrDoc("digAOneAndOnely", "A");

	private static final SolrInputDocument DIGEST_A_FOUR = solrDoc("digAOne1", "A");
	
	private static final SolrInputDocument DIGEST_A_FIVE = solrDoc("digAOne1", "A");
	
	private EmbeddedSolrServer solrClient;
	
	@Before
	@SneakyThrows
	public void setUp()
	{
		Path core = Paths.get("src/test/resources/solr_conf");
		FileUtils.deleteDirectory(core.resolve("uni_leipzig_core").resolve("data").toFile());
		CoreContainer coreContainer = new CoreContainer(core.toString());
		coreContainer.load();
		
		solrClient = new EmbeddedSolrServer(coreContainer, "uni_leipzig_core");
	}
	
	@After
	@SneakyThrows
	public void tearDown()
	{
		solrClient.close();
	}
	
	@Test
	public void checkThatIndexCleanerCouldBeCreated()
	{
		IndexCleaner indexCleaner = new IndexCleaner(solrClient);
		List<String> distinctDigests = indexCleaner.distinctDigests();
		
		Assert.assertNotNull(distinctDigests);
		Assert.assertTrue(distinctDigests.isEmpty());
		
		indexCleaner.cleanIndex();
		assertResultDocuments(Collections.emptyMap());
	}
	
	@Test
	@SneakyThrows
	public void checkThatDistinctDigestsReturnSingleDigestForSingleDocument()
	{
		solrClient.add("uni_leipzig_core", Arrays.asList(DIGEST_ABC_DOC_ONE));
		solrClient.commit();
		
		IndexCleaner indexCleaner = new IndexCleaner(solrClient);
		List<String> distinctDigests = indexCleaner.distinctDigests();
		
		Assert.assertNotNull(distinctDigests);
		Assert.assertEquals(Arrays.asList("ABC"), distinctDigests);
		indexCleaner.cleanIndex();
		
		Map<String, Pair<String, List<String>>> expectedDbContent = new HashMap<>();
		expectedDbContent.put("ABC", Pair.of("ABC", null));
		
		assertResultDocuments(expectedDbContent);
	}
	
	@Test
	@SneakyThrows
	public void checkThatDocumentsForTwoDigestsAreValidCleaned()
	{
		solrClient.add("uni_leipzig_core", Arrays.asList(DIGEST_ABC_DOC_ONE, DIGEST_AB_ONE, DIGEST_AB_TWO));
		solrClient.commit();
		
		IndexCleaner indexCleaner = new IndexCleaner(solrClient);
		indexCleaner.cleanIndex();
		
		Map<String, Pair<String, List<String>>> expectedDbContent = new HashMap<>();
		expectedDbContent.put("ABC", Pair.of("ABC", null));
		expectedDbContent.put("AB", Pair.of("DIGEST_AB_ONE", Arrays.asList("http://DIGEST_AB_2")));
		
		assertResultDocuments(expectedDbContent);
	}
	
	@Test
	@SneakyThrows
	public void checkThatDocumentsForThreeDigestsAreValidCleaned()
	{
		solrClient.add("uni_leipzig_core", Arrays.asList(DIGEST_ABC_DOC_ONE, DIGEST_AB_ONE, DIGEST_AB_TWO,
				DIGEST_A_ONE, DIGEST_A_TWO, DIGEST_A_THREE, DIGEST_A_FOUR, DIGEST_A_FIVE));
		solrClient.commit();
		
		IndexCleaner indexCleaner = new IndexCleaner(solrClient);
		indexCleaner.cleanIndex();
		
		Map<String, Pair<String, List<String>>> expectedDbContent = new HashMap<>();
		expectedDbContent.put("ABC", Pair.of("ABC", null));
		expectedDbContent.put("AB", Pair.of("DIGEST_AB_ONE", Arrays.asList("http://DIGEST_AB_2")));
		expectedDbContent.put("A", Pair.of("digAOneAndOnely", Arrays.asList("http://digAOne", "http://digAOne1")));
		
		assertResultDocuments(expectedDbContent);
	}
	
	@Test
	@SneakyThrows
	public void checkThatDocumentsForSingleDigestWithOnlyDuplicatedUrlsAreValidCleaned()
	{
		solrClient.add("uni_leipzig_core", Arrays.asList(DIGEST_A_FOUR, DIGEST_A_FIVE));
		solrClient.commit();
		
		IndexCleaner indexCleaner = new IndexCleaner(solrClient);
		indexCleaner.cleanIndex();
		
		Map<String, Pair<String, List<String>>> expectedDbContent = new HashMap<>();
		expectedDbContent.put("A", Pair.of("digAOne1", null));
		
		assertResultDocuments(expectedDbContent);
	}
	
	@Test
	@SneakyThrows
	public void checkThatDistinctDigestsAreReturnedForThousandDigests()
	{
		List<String> expected = IntStream.range(0, 10000)
				.mapToObj(String::valueOf)
				.sorted()
				.collect(Collectors.toList());
		List<SolrInputDocument> documents = expected.stream()
				.map(i -> solrDoc(i, i))
				.collect(Collectors.toList());
		
		solrClient.add("uni_leipzig_core", documents);
		solrClient.commit();
		
		IndexCleaner indexCleaner = new IndexCleaner(solrClient);
		indexCleaner.cleanIndex();
		List<String> distinctDigests = indexCleaner.distinctDigests();
		
		Assert.assertNotNull(distinctDigests);
		Assert.assertEquals(expected, distinctDigests);
		
		for(String expectedDoc : expected)
		{
			QueryResponse response = solrClient.query(new SolrQuery("content: \""+ expectedDoc +"\" AND digest: \""+ expectedDoc +"\""));
			
			Assert.assertEquals(1, response.getResults().size());
			SolrDocument doc = response.getResults().get(0);
			Assert.assertNull(doc.get("duplicatesUrl"));
			Assert.assertEquals(expectedDoc, doc.get("content"));
			Assert.assertEquals(expectedDoc, doc.get("digest"));
		}
	}
	
	@SneakyThrows
	private void assertResultDocuments(Map<String, Pair<String, List<String>>> expectedResults)
	{
		QueryResponse response = solrClient.query(new SolrQuery("*:*"));
	
		@SuppressWarnings("unchecked")
		Map<String, Pair<String, List<String>>> actual = response.getResults().stream()
			.collect(Collectors.toMap(doc -> (String) ((SolrDocument) doc).get("digest"), 
					doc -> Pair.of((String) ((SolrDocument) doc).get("content"),
							(List<String>) ((SolrDocument) doc).get("duplicatesUrl"))));
		
		Assert.assertEquals(expectedResults, actual);
	}
	
	private static final SolrInputDocument solrDoc(String content, String digest)
	{
		SolrInputDocument ret = new SolrInputDocument();
		ret.addField("content", content);
		ret.addField("digest", digest);
		ret.addField("url", "http://"+ content);
		
		return ret;
	}
}
