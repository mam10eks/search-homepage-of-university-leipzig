package de.uni_leipzig.search_engine_uni.backend.aspects.dto;

import groovy.transform.EqualsAndHashCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@EqualsAndHashCode(callSuper=true)
public class SearchResponseEvent extends WebResponseEvent
{
	private String query;
	
	private int resultPage;
}
