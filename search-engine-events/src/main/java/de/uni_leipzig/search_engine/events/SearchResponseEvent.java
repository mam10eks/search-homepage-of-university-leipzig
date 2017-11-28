package de.uni_leipzig.search_engine.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@EqualsAndHashCode(callSuper=true)
public class SearchResponseEvent extends WebResponseEvent
{
	private String query;
	
	private int resultPage;
}
