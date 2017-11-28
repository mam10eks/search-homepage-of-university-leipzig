package de.uni_leipzig.search_engine_uni.aspects.dto;

import groovy.transform.EqualsAndHashCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@EqualsAndHashCode(callSuper=true)
public class SearchResultSelectedEvent extends WebResponseEvent
{
	private String query;
	
	private int searchPage = 1;
	
	private int resultId;
	
	private String documentUri;
}
