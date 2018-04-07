package de.uni_leipzig.search_engine.evaluation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import de.uni_leipzig.search_engine.backend.aspects.KafkaTopicProducer;
import de.uni_leipzig.search_engine.backend.lucene.SearcherComponent;
import de.uni_leipzig.search_engine.evaluation.topic.CsvTopicParser;
import de.uni_leipzig.search_engine.evaluation.topic.Topic;
import de.uni_leipzig.search_engine.events.EventTopics;
import de.uni_leipzig.search_engine.events.WebResponseEvent;
import lombok.SneakyThrows;

@SpringBootApplication
public class Main
{
	public static void main(String[] args)
	{
		SpringApplication.run(Main.class, args);
	}
	
	@Configuration
	@EnableKafka
	public static class ApplicationConfiguration
	{
		@Bean
		@SneakyThrows
		public static List<Topic> topics(@Value("${csvFile:/home/maik/Downloads/IR_UL_Search_Evaluation - Tabellenblatt1.csv}")
			String csvFile)
		{
			System.out.println("USE csvFile "+ csvFile);
			List<Topic> ret = CsvTopicParser.parseTopicsFromCsvString(new String(Files.readAllBytes(Paths.get(csvFile)), StandardCharsets.UTF_8));
			
			ret.forEach(i-> System.out.println(i.getQuery()));
			
			return ret;
		}

		@Bean
		public KafkaTopicProducer<WebResponseEvent> kafkaLoggingProducer(KafkaTemplate<String, String> kafkaTemplate)
		{
			return new KafkaTopicProducer<>(EventTopics.ALL_WEB_EVENTS_TOPIC, kafkaTemplate);
		}
		
		@Bean
		@SneakyThrows
		public SearcherComponent searcherComponent(
				@Value("${indexDirectory:../example_indices/lips_informatik_uni_leipzig}")
				String indexDirectory)
		{
			System.out.println("USE INDEX"+ indexDirectory);
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory))));
			
			return new SearcherComponent(new StandardAnalyzer(), searcher);
		}
		
		@Bean
		public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> cf) {
		    ConcurrentKafkaListenerContainerFactory<String, String> factory =
		                new ConcurrentKafkaListenerContainerFactory<>();
		    
		    factory.getContainerProperties().setIdleEventInterval(1000L);
		    factory.setConsumerFactory(cf);
		    

		    return factory;
		}
	}
}
