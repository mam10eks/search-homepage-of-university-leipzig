package de.uni_leipzig.search_engine.events;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;

import lombok.Data;
import lombok.EqualsAndHashCode;
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

	public static SearchResultSelectedEvent fromWebResponseEvent(WebResponseEvent webResponseEvent)
	{
		if(!eventHasAllMembers(webResponseEvent) || !isRedirectEndpoint(webResponseEvent.getRequest()))
		{
			return null;
		}
		
		SearchResultSelectedEvent ret = new SearchResultSelectedEvent();
		ret.setClientId(webResponseEvent.getClientId());
		ret.setRequest(webResponseEvent.getRequest());
		ret.setResponseModel(webResponseEvent.getResponseModel());
		
		URI referer = RequestParameterUtil.parseUriFailsave(ret.getRequest().getHeaders().get("referer"));
		URI redirectUri = RequestParameterUtil.parseUriFailsave(ret.getRequest().getHeaders().get("Location"));
		
		if(referer == null || redirectUri == null)
		{
			return null;
		}
		
		List<NameValuePair> refererParams = RequestParameterUtil.extractQueryParametersFromRequest(referer.toString());
		
		ret.setDocumentUri(redirectUri.toString());
		ret.setQuery(SearchResponseEvent.extractQueryParameter(refererParams));
		ret.setSearchPage(SearchResponseEvent.extractResultPage(refererParams));
		
		List<NameValuePair> params = RequestParameterUtil.extractQueryParametersFromRequest(ret.getRequest());
		ret.setResultId(extractDocumentId(params));
		
		return ret.getQuery() == null || ret.getQuery().isEmpty() ? null : ret;
	}
	
	private static boolean eventHasAllMembers(WebResponseEvent webResponseEvent)
	{
		return webResponseEvent != null && webResponseEvent.getRequest() != null
				&& webResponseEvent.getRequest().getRequestUrl() != null
				&& webResponseEvent.getRequest().getHeaders() != null
				&& webResponseEvent.getRequest().getHeaders().containsKey("referer");
	}
	
	static int extractDocumentId(List<NameValuePair> params)
	{
		NameValuePair documentIdParameter = RequestParameterUtil.extractFirstPairWithName("documentId", params);
		
		if(documentIdParameter != null && documentIdParameter.getValue() != null)
		{
			try
			{
				return Integer.parseInt(documentIdParameter.getValue());
			}
			catch(NumberFormatException doNothing)
			{
				
			}
		}
		
		return 0;
	}
	
	private static boolean isRedirectEndpoint(Request request)
	{
		URI uri = RequestParameterUtil.parseUriFailsave(request.getRequestUrl());
		
		return uri != null
			&& StringUtils.removeAll(uri.getPath(), "/").toLowerCase().equals("url");
	}
}
