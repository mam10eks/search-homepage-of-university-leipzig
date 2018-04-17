package de.uni_leipzig.search_engine.events;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;

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

	public static SearchResponseEvent fromWebResponseEvent(WebResponseEvent webResponseEvent)
	{
		if(!eventHasAllMembers(webResponseEvent) || !isSearchResponseEndpoint(webResponseEvent.getRequest()))
		{
			return null;
		}
		
		SearchResponseEvent ret = new SearchResponseEvent();
		ret.setClientId(webResponseEvent.getClientId());
		ret.setRequest(webResponseEvent.getRequest());
		ret.setResponseModel(webResponseEvent.getResponseModel());
		
		List<NameValuePair> params = RequestParameterUtil.extractQueryParametersFromRequest(ret.getRequest());
		ret.setResultPage(extractResultPage(params));
		ret.setQuery(extractQueryParameter(params));
		
		return ret.getQuery() == null || ret.getQuery().isEmpty() ? null : ret;
	}
	
	static String extractQueryParameter(List<NameValuePair> params)
	{
		return RequestParameterUtil.extractAllPairsWithName("q", params).stream()
				.map(pair -> pair.getValue())
				.collect(Collectors.joining(","));
	}
	
	static int extractResultPage(List<NameValuePair> params)
	{
		NameValuePair resultPageParameter = RequestParameterUtil.extractFirstPairWithName("p", params);
		
		if(resultPageParameter != null && resultPageParameter.getValue() != null)
		{
			try
			{
				return Integer.parseInt(resultPageParameter.getValue());
			}
			catch(NumberFormatException doNothing)
			{
				
			}
		}
		
		return 1;
	}
	
	private static boolean eventHasAllMembers(WebResponseEvent webResponseEvent)
	{
		return webResponseEvent != null && webResponseEvent.getRequest() != null
				&& webResponseEvent.getRequest().getRequestUrl() != null;
	}
	
	private static boolean isSearchResponseEndpoint(Request request)
	{
		URI uri = RequestParameterUtil.parseUriFailsave(request.getRequestUrl());
		
		return uri != null
			&& StringUtils.removeAll(uri.getPath(), "/api/v1/search").isEmpty();
	}
}
