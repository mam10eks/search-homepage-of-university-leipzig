package de.uni_leipzig.search_engine.events;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestParameterUtil
{
	public static List<NameValuePair> extractQueryParametersFromRequest(Request request)
	{
		if(request == null || request.getRequestUrl() == null)
		{
			return new ArrayList<>();
		}
		
		return URLEncodedUtils.parse(URI.create(request.getRequestUrl()), StandardCharsets.UTF_8);
	}
	
	public static List<NameValuePair> extractQueryParametersFromRequest(String url)
	{
		return extractQueryParametersFromRequest(new Request().setRequestUrl(url));
	}
	
	public static NameValuePair extractFirstPairWithName(String name, List<NameValuePair> params)
	{
		return extractAllPairsWithName(name, params).stream()
			.findFirst().orElse(null);
	}
	
	public static URI parseUriFailsave(String uri)
	{
		if(uri != null)
		{
			try
			{
				return URI.create(uri);
			}
			catch(IllegalArgumentException doNothing)
			{
				
			}
		}
			
		return null;
	}
	
	public static List<NameValuePair> extractAllPairsWithName(String name, List<NameValuePair> params)
	{
		if(name == null || params == null)
		{
			return new ArrayList<>();
		}
		
		return params.stream()
				.filter(pair -> pair != null && name.equals(pair.getName()))
				.collect(Collectors.toList());
	}
}
