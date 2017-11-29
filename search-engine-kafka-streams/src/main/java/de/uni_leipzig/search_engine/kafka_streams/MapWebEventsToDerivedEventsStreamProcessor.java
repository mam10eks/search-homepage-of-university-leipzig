package de.uni_leipzig.search_engine.kafka_streams;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.uni_leipzig.search_engine.events.EventTopics;
import de.uni_leipzig.search_engine.events.SearchResponseEvent;
import de.uni_leipzig.search_engine.events.SearchResultSelectedEvent;
import de.uni_leipzig.search_engine.events.WebResponseEvent;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MapWebEventsToDerivedEventsStreamProcessor
{
	private static final ObjectWriter JSON_WRITER = jsonWriter();
	
	private static final ObjectReader WEB_RESPONSE_EVENT_READER = new ObjectMapper().readerFor(WebResponseEvent.class);
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MapWebEventsToDerivedEventsStreamProcessor.class);
	
	public static void main(String[] args)
	{
		Properties streamsConfiguration = new Properties();
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-function-lambda-example");
	    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
	    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
	    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    
	    final Serde<String> stringSerde = Serdes.String();
	    final Serde<byte[]> byteArraySerde = Serdes.ByteArray();
	    
	    StreamsBuilder builder = new StreamsBuilder();
	    
	    KStream<byte[], String> stream = builder.stream(EventTopics.ALL_WEB_EVENTS_TOPIC, Consumed.with(byteArraySerde, stringSerde));
	    
	    stream.flatMapValues(MapWebEventsToDerivedEventsStreamProcessor::flatMapWebEventToSearchEvent)
	    	.to(EventTopics.ALL_SEARCH_EVENTS_TOPIC);
	    
	    stream.flatMapValues(MapWebEventsToDerivedEventsStreamProcessor::flatMapWebEventToSelectSearchResultEvent)
    		.to(EventTopics.ALL_SEARCH_RESULT_SELECTED_EVENTS_TOPIC);
	    
	    KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
	    streams.cleanUp();
	    streams.start();

	    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}
	
	private static Iterable<String> flatMapWebEventToSearchEvent(String webEventAsJson)
	{
		return flatMapWebEventTo(webEventAsJson, SearchResponseEvent::fromWebResponseEvent);
	}
	
	private static Iterable<String> flatMapWebEventToSelectSearchResultEvent(String webEventAsJson)
	{
		return flatMapWebEventTo(webEventAsJson, SearchResultSelectedEvent::fromWebResponseEvent);
	}
	
	private static <V extends WebResponseEvent> Iterable<String> flatMapWebEventTo(String webEventAsJson, Function<WebResponseEvent, V> transformer)
	{
		WebResponseEvent event = parseWebResponseEvent(webEventAsJson);
		List<String> ret = new ArrayList<>();
		V mappedEvent = null;
		
		if(event != null && (mappedEvent = transformer.apply(event)) != null)
		{
			try
			{
				ret.add(JSON_WRITER.writeValueAsString(mappedEvent));
			}
			catch(Exception exception)
			{
				LOGGER.info("Couldnt transform the WebResponseEvent since the mapped event {} could not be written as json.", mappedEvent, exception);
			}
		}
		
		return ret;
	}
	
	private static WebResponseEvent parseWebResponseEvent(String webEventAsJson)
	{
		try
		{
			return WEB_RESPONSE_EVENT_READER.readValue(webEventAsJson);
		}
		catch(Exception exception)
		{
			LOGGER.info("Couldnt transform the WebResponseEvent since the source event '{}'" +
				"could not be parsed.", webEventAsJson, exception);
		}
		
		return null;
	}
	
	private static ObjectWriter jsonWriter()
	{
		ObjectMapper ret = new ObjectMapper();
		ret.enable(SerializationFeature.INDENT_OUTPUT);
		
		return ret.writer();
	}
}
