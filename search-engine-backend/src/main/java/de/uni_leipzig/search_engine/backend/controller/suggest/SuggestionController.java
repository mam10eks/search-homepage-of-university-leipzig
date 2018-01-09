package de.uni_leipzig.search_engine.backend.controller.suggest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import de.uni_leipzig.search_engine.backend.controller.suggest.dto.QuerySuggestion;
import de.uni_leipzig.search_engine.backend.lucene.SuggestionComponent;
import de.uni_leipzig.search_engine.events.EventTopics;
import de.uni_leipzig.search_engine.events.SearchResponseEvent;
import lombok.SneakyThrows;

@Controller
public class SuggestionController
{
	private static final ObjectReader OBJECT_READER = new ObjectMapper().reader().forType(SearchResponseEvent.class);
	
	@Autowired
	private SuggestionComponent suggestionComponent;
	
	@RequestMapping(method=RequestMethod.GET, path="/suggest")
	@ResponseBody
	public Object suggest(@RequestParam String query)
	{
		return new QuerySuggestion(suggestionComponent.suggestByUpdated(
			RequestContextHolder.currentRequestAttributes().getSessionId(),
			query));
	}
	
	@SneakyThrows
	@KafkaListener(topics=EventTopics.ALL_SEARCH_EVENTS_TOPIC)
	public void add(String event)
	{
		SearchResponseEvent parsedEvent = OBJECT_READER.readValue(event);
		
		suggestionComponent.addOrUpdate(parsedEvent.getClientId(), parsedEvent.getQuery());
	}
}
