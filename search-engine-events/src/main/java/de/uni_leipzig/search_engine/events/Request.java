package de.uni_leipzig.search_engine.events;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class Request
{
	private Map<String, String> headers;
	
	private String requestUrl;
	
	private String remoteAddr;
	
	private String remoteHost;
	
	private Integer remotePort;
	
	public Request()
	{
		//default constructor for serialization/deserialization
	}
	
	public Request(HttpServletRequest request)
	{
		setRequestUrl(request.getRequestURL().toString());
		
		if(request.getQueryString() != null && !request.getQueryString().isEmpty())
		{
			setRequestUrl(getRequestUrl() +"?"+ request.getQueryString());
		}
		
		setRemoteAddr(request.getRemoteAddr());
		setRemoteHost(request.getRemoteHost());
		setRemotePort(request.getRemotePort());

		Enumeration<String> headerEnum = request.getHeaderNames();
		
		headers = new HashMap<>();
		
		while(headerEnum.hasMoreElements())
		{
			String headerName = headerEnum.nextElement();
			headers.put(headerName, request.getHeader(headerName));
		}
	}
}
