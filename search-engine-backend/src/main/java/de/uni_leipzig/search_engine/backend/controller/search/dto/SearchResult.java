package de.uni_leipzig.search_engine.backend.controller.search.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.lucene.document.Document;
import org.springframework.hateoas.Link;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 
 * @author Maik Fröbe
 *
 */
@Data
@NoArgsConstructor
@Accessors(chain=true)
public class SearchResult
{
	public static final String INDEX_FIELD_TITLE = "title";
	
	public static final String INDEX_FIELD_CONTENT = "content";
	
	public static final String INDEX_FIELD_LINK = "url";
	
	public static final String INDEX_FIELD_ANCHOR = "anchor";
	
	public static final String INDEX_FIELD_LINKS_TO_DUPLICATES = "duplicatesUrl";
	
	private String title;
	
	private String snippet;
	
	private Link targetUrl;
	
	private List<Link> linksToDuplicates;
	
	public SearchResult(Triple<Document, Integer, String> result)
	{
		setSnippet(result.getRight());
		setTargetUrl(new DocumentLink(result.getMiddle(), -1, result.getLeft().get(INDEX_FIELD_LINK)));
		setLinksToDuplicates(createLinksToDuplicates(result.getMiddle(), result.getLeft()));
		setTitle(result.getLeft().get(INDEX_FIELD_TITLE));
		
		
		if(getTitle() == null || getTitle().isEmpty())
		{
			setTitle(StringUtils.abbreviate(result.getLeft().get(INDEX_FIELD_CONTENT), 40));
		}
	}

	
	private static List<Link> createLinksToDuplicates(Integer docID, Document doc)
	{
		List<Link> ret = new ArrayList<>();
		String[] duplicateUrls = null;
		
		if(doc != null && (duplicateUrls = doc.getValues(INDEX_FIELD_LINKS_TO_DUPLICATES)) != null
				&& duplicateUrls.length > 0)
		{
			for(int i=0; i< duplicateUrls.length; i++)
			{
				ret.add(new DocumentLink(docID, i, duplicateUrls[i]));
			}
		}
		
		return ret;
	}
}
