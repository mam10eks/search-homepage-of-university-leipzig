package de.uni_leipzig.search_engine.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;

import de.uni_leipzig.search_engine.backend.lucene.SearcherComponent;
import de.uni_leipzig.search_engine.evaluation.topic.RelevanceJudgement;
import de.uni_leipzig.search_engine.evaluation.topic.Topic;
import de.uni_leipzig.search_engine.events.RequestParameterUtil;
import de.uni_leipzig.search_engine.events.SearchResponseEvent;
import lombok.Data;

@Data
public class JudgedDocumentIndexCreator
{
	public static final String DOCUMENT_FIELD_RELEVANCE_JUDGEMENT = "rj";
	
	private final SearcherComponent searcherComponent;
	
	public void insertJudgedDocumentsIntoIndex(IndexWriter indexWriter, Topic topic,
			List<SearchResponseEvent> searchEvents) throws IOException
	{
		if(topic == null || topic.getRelevanceGainVector() == null ||
				Arrays.stream(topic.getRelevanceGainVector()).allMatch(RelevanceJudgement.NOT_RELEVANT::equals))
		{
			indexWriter.commit();
			return;
		}
		
		List<Integer> judgedDocumentIds = getDocumentIdsOrFailIfInvalid(searchEvents, topic);
		failIfJudgementsDifferFromAvailableDocuments(topic, judgedDocumentIds);
		
		for(int i=0; i<topic.getRelevanceGainVector().length; i++)
		{
			IndexableField judgement = new StringField(DOCUMENT_FIELD_RELEVANCE_JUDGEMENT, 
					topic.getRelevanceGainVector()[i].name(), Store.YES);
			
			Document doc = searcherComponent.doc(judgedDocumentIds.get(i));
			doc.add(judgement);
			
			indexWriter.addDocument(doc);
			
		}
		
		indexWriter.commit();
	}
	
	private static void failIfJudgementsDifferFromAvailableDocuments(Topic topic, List<Integer> judgedDocumentIds)
	{
		int judgements = topic != null && topic.getRelevanceGainVector() != null ? topic.getRelevanceGainVector().length : 0;
		
		if(judgements > judgedDocumentIds.size())
		{
			throw new RuntimeException("I have "+ judgements +" for "+ judgedDocumentIds.size() +" Documents");
		}
	}
	
	private static List<Integer> getDocumentIdsOrFailIfInvalid(List<SearchResponseEvent> searchEvents, Topic topic)
	{
		List<Integer> ret = new ArrayList<>();
		
		if(searchEvents != null && !searchEvents.isEmpty())
		{
			if( topicQueryDoesNotMatchToAllSearchEventQueries(searchEvents, topic) 
					||
				searchEventsAreNotConsecutiveStartingOnFirstPage(searchEvents))
			{
				throw new RuntimeException("FIXME: good message");
			}
			

			for(SearchResponseEvent searchEvent : searchEvents)
			{
				ret.addAll(getAllDocumentIdsListetIn(searchEvent));
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Integer> getAllDocumentIdsListetIn(SearchResponseEvent searchEvent)
	{
		List<Integer> ret = new ArrayList<>();
		
		List<Map<String, Object>> bla = (List<Map<String, Object>>) searchEvent.getResponseModel().get("resultsOnPage");
		
		for(Map<String, Object> bl : bla)
		{
			ret.add(extractDocumentIdFromLink((String) ((Map<String, Object>) bl.get("targetUrl")).get("href")));
		}
		
		return ret;
	}
	
	private static Integer extractDocumentIdFromLink(String url)
	{
		List<NameValuePair> params = RequestParameterUtil.extractQueryParametersFromRequest(url);
		
		return Integer.valueOf(RequestParameterUtil.extractFirstPairWithName("documentId", params).getValue());
	}
	
	private static boolean searchEventsAreNotConsecutiveStartingOnFirstPage(List<SearchResponseEvent> searchEvents)
	{
		for(int i=1; i<= searchEvents.size(); i++)
		{
			if(i != searchEvents.get(i-1).getResultPage())
			{
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}
	
	private static boolean topicQueryDoesNotMatchToAllSearchEventQueries(List<SearchResponseEvent> searchEvents, Topic topic)
	{
		return !searchEvents.stream().allMatch(event -> event.getQuery().equals(topic.getQuery()));
	}
}
