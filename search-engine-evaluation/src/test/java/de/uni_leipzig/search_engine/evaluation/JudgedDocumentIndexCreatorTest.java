package de.uni_leipzig.search_engine.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_leipzig.search_engine.backend.aspects.EnrichResponsesWithViewAspect;
import de.uni_leipzig.search_engine.backend.controller.search.SearchControllerMockUtils;
import de.uni_leipzig.search_engine.backend.controller.search.dto.SearchResult;
import de.uni_leipzig.search_engine.backend.controller.search.dto.SearchResultPage;
import de.uni_leipzig.search_engine.evaluation.topic.RelevanceJudgement;
import de.uni_leipzig.search_engine.evaluation.topic.Topic;
import de.uni_leipzig.search_engine.events.SearchResponseEvent;
import lombok.SneakyThrows;

/**
 * 
 * @author Maik Fröbe
 *
 */
public class JudgedDocumentIndexCreatorTest
{
	private final JudgedDocumentIndexCreator judgedIndexCreator = new JudgedDocumentIndexCreator(SearchControllerMockUtils.searchComponent(10));

	private Directory dir;
	
	private IndexWriter indexWriter;
	
	@Before
	@SneakyThrows
	public void prepareTest()
	{
		dir = new RAMDirectory();
		SearchControllerMockUtils.setupHttpServlet();
		indexWriter = new IndexWriter(dir, new IndexWriterConfig());
	}
	
	@Test
	@SneakyThrows
	public void checkThatNoJudgementsCreateEmptyIndex()
	{
		Topic topic = new Topic();
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, new ArrayList<SearchResponseEvent>());
		assertIndexIsEqualToJudgements(Collections.emptyList());
	}
	
	@Test
	@SneakyThrows
	public void checkThatSingleRelevantJudgedDocumentIsAddedToTheIndex()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] {RelevanceJudgement.RELEVANT});
		SearchResponseEvent searchEvent = searchResponseEvent(topic, 1, Arrays.asList(4));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, Arrays.asList(searchEvent));
		assertIndexIsEqualToJudgements(Arrays.asList(Pair.of(4, RelevanceJudgement.RELEVANT)));
	}
	
	@Test
	@SneakyThrows
	public void checkThatMultipleRelevantJudgedDocumentsFromSinglePageAreAddedToTheIndex()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] {RelevanceJudgement.RELEVANT,
						RelevanceJudgement.NOT_RELEVANT, RelevanceJudgement.RELEVANT});
		SearchResponseEvent searchEvent = searchResponseEvent(topic, 1, Arrays.asList(4, 7, 9));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, Arrays.asList(searchEvent));
		assertIndexIsEqualToJudgements(Arrays.asList(Pair.of(4, RelevanceJudgement.RELEVANT),
				Pair.of(7, RelevanceJudgement.NOT_RELEVANT), Pair.of(9, RelevanceJudgement.RELEVANT)));
	}
	
	@Test
	@SneakyThrows
	public void checkThatSingleRelevantJudgedDocumentIsAddedToTheIndexOutOfMultiplePages()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] {RelevanceJudgement.NOT_RELEVANT, 
						RelevanceJudgement.NOT_RELEVANT, RelevanceJudgement.RELEVANT});
		List<SearchResponseEvent> searchEvents = Arrays.asList(
				searchResponseEvent(topic, 1, Arrays.asList(3)),
				searchResponseEvent(topic, 2, Arrays.asList(8)),
				searchResponseEvent(topic, 3, Arrays.asList(4)));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, searchEvents);
		assertIndexIsEqualToJudgements(Arrays.asList(Pair.of(3, RelevanceJudgement.NOT_RELEVANT),
				Pair.of(8, RelevanceJudgement.NOT_RELEVANT), Pair.of(4, RelevanceJudgement.RELEVANT)));
	}
	
	@Test
	@SneakyThrows
	public void checkThatMultipleRelevantJudgedDocumentIsAddedToTheIndexOutOfMultiplePages()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] {RelevanceJudgement.NOT_RELEVANT, 
						RelevanceJudgement.NOT_RELEVANT, RelevanceJudgement.RELEVANT, 
						RelevanceJudgement.NOT_RELEVANT, RelevanceJudgement.RELEVANT});
		List<SearchResponseEvent> searchEvents = Arrays.asList(
				searchResponseEvent(topic, 1, Arrays.asList(3)),
				searchResponseEvent(topic, 2, Arrays.asList(8)),
				searchResponseEvent(topic, 3, Arrays.asList(6,7)),
				searchResponseEvent(topic, 4, Arrays.asList(4)));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, searchEvents);
		assertIndexIsEqualToJudgements(Arrays.asList(Pair.of(3, RelevanceJudgement.NOT_RELEVANT),
				Pair.of(8, RelevanceJudgement.NOT_RELEVANT), Pair.of(6, RelevanceJudgement.RELEVANT),
				Pair.of(7, RelevanceJudgement.NOT_RELEVANT), Pair.of(4, RelevanceJudgement.RELEVANT)));
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatMissingDocumentForJudgementFails()
	{
		Topic topic = new Topic().setQuery("a");
		SearchResponseEvent searchEvent = searchResponseEvent(topic, 1, Arrays.asList(12));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, Arrays.asList(searchEvent));
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatDifferentQueriesFail()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] { RelevanceJudgement.RELEVANT});
		SearchResponseEvent searchEvent = searchResponseEvent(topic, 1, Collections.emptyList()).setQuery("b");
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, Arrays.asList(searchEvent));
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatDifferentQueriesWithinEventsFail()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] {RelevanceJudgement.RELEVANT});
		List<SearchResponseEvent> searchEvents = Arrays.asList(
				searchResponseEvent(topic, 1, Collections.emptyList()),
				searchResponseEvent(topic, 2, Collections.emptyList()).setQuery("b"));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, searchEvents);
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatMissingFirstPageWillFail()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] { RelevanceJudgement.RELEVANT});
		List<SearchResponseEvent> searchEvents = Arrays.asList(
				searchResponseEvent(topic, 2, Collections.emptyList()));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, searchEvents);
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatMissingIntermediatePageWillFail()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] {RelevanceJudgement.NOT_RELEVANT, RelevanceJudgement.RELEVANT});
		List<SearchResponseEvent> searchEvents = Arrays.asList(
				searchResponseEvent(topic, 1, Collections.emptyList()),
				searchResponseEvent(topic, 3, Collections.emptyList()));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, searchEvents);
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatDouplingPagesWillFail()
	{
		Topic topic = new Topic().setQuery("a")
				.setRelevanceGainVector(new RelevanceJudgement[] {RelevanceJudgement.NOT_RELEVANT, RelevanceJudgement.RELEVANT});
		List<SearchResponseEvent> searchEvents = Arrays.asList(
				searchResponseEvent(topic, 1, Collections.emptyList()),
				searchResponseEvent(topic, 1, Collections.emptyList()));
		
		judgedIndexCreator.insertJudgedDocumentsIntoIndex(indexWriter, topic, searchEvents);
	}
	
	@SneakyThrows
	private void assertIndexIsEqualToJudgements(List<Pair<Integer, RelevanceJudgement>> expectedIndexContent)
	{
		IndexReader indexReader = DirectoryReader.open(dir);
		Assert.assertEquals(expectedIndexContent.size(), indexReader.numDocs());
		
		for(int docId=0; docId< expectedIndexContent.size(); docId++)
		{
			Document expectedDocument = judgedIndexCreator.getSearcherComponent().doc(expectedIndexContent.get(docId).getLeft());
			Document actualDocument = indexReader.document(docId);
			
			assertFirstDocumentIsSubset(expectedDocument, actualDocument);
			Assert.assertEquals(expectedIndexContent.get(docId).getRight().name(),
					indexReader.document(docId).get(JudgedDocumentIndexCreator.DOCUMENT_FIELD_RELEVANCE_JUDGEMENT));
		}
	}
	
	private void assertFirstDocumentIsSubset(Document first, Document second)
	{
		for(IndexableField field : first.getFields())
		{
			Assert.assertEquals(first.get(field.name()), second.get(field.name()));
		}
	}
	
	private SearchResponseEvent searchResponseEvent(Topic topic, int page, List<Integer> documentsOnPage)
	{
		SearchResultPage resultPage = new SearchResultPage();
		resultPage.setResultsOnPage(documentsOnPage.stream()
				.map(this::searchResult)
				.collect(Collectors.toList()));
		
		return (SearchResponseEvent) new SearchResponseEvent()
				.setQuery(topic.getQuery())
				.setResultPage(page)
				.setResponseModel(EnrichResponsesWithViewAspect.convertToModel(resultPage));
	}
	
	private SearchResult searchResult(int docId)
	{
		Document doc = judgedIndexCreator.getSearcherComponent().doc(docId);
		
		return new SearchResult(Triple.of(doc, docId, "Snippet..."));
	}
}
