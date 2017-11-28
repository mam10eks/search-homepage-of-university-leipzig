package de.uni_leipzig.search_engine_uni;

import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.suggest.DocumentDictionary;
import org.apache.lucene.store.FSDirectory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import de.uni_leipzig.search_engine_uni.dto.SearchResult;
import lombok.SneakyThrows;

@SpringBootApplication
public class Application
{
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}
	
	@Configuration
	@EnableSpringHttpSession
	public static class ApplicationConfiguration
	{
		@Bean
		public CookieSerializer cookieSerializer()
		{
			DefaultCookieSerializer ret = new DefaultCookieSerializer();
			
			ret.setCookieName("client_id"); 
			ret.setCookiePath("/"); 
			ret.setCookieMaxAge(Integer.MAX_VALUE);
			
			return ret;
		}

		@Bean
		public static SessionRepository<?> sessionStore()
		{
			return new MapSessionRepository();
		}
		
		@Bean
		@SneakyThrows
		public static Dictionary dictionary()
		{
			return new DocumentDictionary(DirectoryReader.open(FSDirectory.open(Paths.get("lucene_index"))), SearchResult.INDEX_FIELD_TITLE, SearchResult.INDEX_FIELD_TITLE);
		}
	}
}
