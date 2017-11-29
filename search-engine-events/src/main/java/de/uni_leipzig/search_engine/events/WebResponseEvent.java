package de.uni_leipzig.search_engine.events;

import java.util.Map;

import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class WebResponseEvent
{
	private Request request;
	
	private String clientId;
	
	private Map<String, Object> responseModel;
	
	public WebResponseEvent()
	{
		//default argument constructor for serialization
	}
	
	public WebResponseEvent(ServletRequestAttributes requestAttributes)
	{
		setClientId(requestAttributes.getSessionId());
		setRequest(new Request(requestAttributes.getRequest()));
	}
}
