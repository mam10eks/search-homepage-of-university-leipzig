package de.uni_leipzig.search_engine_uni.aspects.dto;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class RequestDto
{
	private Map<String, String> headers;
	
	private String requestUrl;
	
	private String remoteAddr;
	
	private String remoteHost;
	
	private Integer remotePort;
	
	public RequestDto(HttpServletRequest request)
	{
		setRequestUrl(request.getRequestURL().toString());
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
