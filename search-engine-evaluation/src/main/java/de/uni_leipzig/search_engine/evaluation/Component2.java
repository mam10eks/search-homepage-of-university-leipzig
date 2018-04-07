package de.uni_leipzig.search_engine.evaluation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.uni_leipzig.search_engine.backend.lucene.SearcherComponent;
import de.uni_leipzig.search_engine.evaluation.topic.Topic;
import de.uni_leipzig.search_engine.events.EventTopics;
import de.uni_leipzig.search_engine.events.SearchResponseEvent;
import lombok.Data;
import lombok.SneakyThrows;

@Component
@Data
public class Component2
{
	private static final ObjectReader OBJECT_READER = new ObjectMapper().reader().forType(SearchResponseEvent.class);

	@Autowired
	private List<Topic> topics;
	
	private Map<String, List<SearchResponseEvent>> events = new HashMap<>();
	
	@Autowired
	private SearcherComponent searcherComponent;
	
	@SneakyThrows
	@KafkaListener(topics=EventTopics.ALL_SEARCH_EVENTS_TOPIC)
	public void add(String event)
	{
		SearchResponseEvent parsedEvent = OBJECT_READER.readValue(event);
		
		System.out.println("See Query: "+ parsedEvent.getQuery());
		if(topics.stream().map(topic -> topic.getQuery()).collect(Collectors.toList()).contains(parsedEvent.getQuery()))
		{
			if(!events.containsKey(parsedEvent.getQuery()))
			{
				events.put(parsedEvent.getQuery(), new ArrayList<>());
			}
			
			events.get(parsedEvent.getQuery()).add(parsedEvent);
		}
	}
	
	@SneakyThrows
	@EventListener
	public void eventHandler(ListenerContainerIdleEvent event)
	{
		JudgedDocumentIndexCreator indexCreator = new JudgedDocumentIndexCreator(searcherComponent);
		List<String> topicNames = topics.stream().map(t -> t.getQuery()).collect(Collectors.toList());
		
		System.out.println(topicNames);
		
		Path mainDirectory = Files.createTempDirectory("dummyStuff");
		System.out.println("Look at "+ mainDirectory);
		
		int i = 0;
		
		for(Topic topic : topics)
		{
			Path path = mainDirectory.resolve(String.valueOf(i++));

			System.out.println("Process: "+ path + " for "+ topic.getQuery());
			IndexWriter index = new IndexWriter(FSDirectory.open(path), new IndexWriterConfig());
			List<SearchResponseEvent> eventsForTopic = searchEventsForTopic(topic.getQuery());
			
			indexCreator.insertJudgedDocumentsIntoIndex(index, topic, eventsForTopic);
		}
		
		System.exit(0);
	}
	
	private List<SearchResponseEvent> searchEventsForTopic(String query)
	{
		if(events.get(query) == null)
		{
			System.out.println("The topic '"+ query +"' has no relevant docs at all.");
			return new ArrayList<>();
		}
		
		List<SearchResponseEvent> ret = new ArrayList<>(events.get(query));
		ret.sort((a,b) -> a.getResultPage() - b.getResultPage());
		
		SearchResponseEvent lastEvent = null;
		Iterator<SearchResponseEvent> it = ret.iterator();
		
		while(it.hasNext())
		{
			SearchResponseEvent next = it.next();
			if(lastEvent == null || lastEvent.getResultPage() != next.getResultPage())
			{
				lastEvent = next;
			}
			else
			{
				if(!lastEvent.getResponseModel().get("resultsOnPage").equals(next.getResponseModel().get("resultsOnPage")))
				{
					System.out.println("LAST: "+ lastEvent.toString() +"\n"+ lastEvent.getResponseModel());
					System.out.println("NEXT:"+ next.toString() +"\n"+ next.getResponseModel());
					throw new RuntimeException();
				}
				
				it.remove();
			}
		}
		
		return ret;
	}
}
