package de.uni_leipzig.search_engine.events;

import java.util.Date;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class WebResponseEvent
{
	private RequestDto request;
	
	private String clientId;
	
	private Map<String, Object> responseModel;
	
	private Date responseTimestamp;
}
