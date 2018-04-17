package de.uni_leipzig.search_engine.backend.aspects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Data;

/**
 * Writes log messages to kafka topics. Could work asynchronous.
 * 
 * @author Maik Fröbe
 *
 * @param <V>
 */
@Data
public class KafkaTopicProducer<V>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicProducer.class);
	
	private final String topic;
	
	private final KafkaTemplate<String, String> kafkaTemplate;

	private final ObjectWriter jsonWriter = jsonWriter();

	@Async
	public void logEvent(V logMessage) throws JsonProcessingException 
	{
		String value = jsonWriter.writeValueAsString(logMessage);
		
		kafkaTemplate.send(topic, value).addCallback(a -> {},
			throwable -> LOGGER.warn("Failed to send the following message to the kafka topic '{}': {}", topic, value,throwable));
	}
	
	private static ObjectWriter jsonWriter()
	{
		ObjectMapper ret = new ObjectMapper();
		ret.enable(SerializationFeature.INDENT_OUTPUT);
		
		return ret.writer();
	}
}
