package de.uni_leipzig.search_engine.backend.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.lucene.document.Document;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import de.uni_leipzig.search_engine.backend.controller.RedirectController;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
	
	//FIXME remove this: only fast and ugly
	private String duplicateContent;
	
	public SearchResult(Triple<Document, Integer, String> result)
	{
		setTitle(result.getLeft().get(INDEX_FIELD_TITLE));
		setSnippet(result.getRight());
		setTargetUrl(createTargetLink(result.getMiddle()));
		setLinksToDuplicates(createLinksToDuplicates(result.getMiddle(), result.getLeft()));
		
		if(!getLinksToDuplicates().isEmpty())
		{
			duplicateContent = "<ul>"+ getLinksToDuplicates().stream()
					.map(l -> "<li><a href=\""+ l.getHref() +"\">"+ l.getRel() +"</a></li>")
					.collect(Collectors.joining(" "))
		
					+"</ul>";
			System.out.println(duplicateContent);
		}
	}
	
	private static Link createTargetLink(Integer docID)
	{
		return createTargetLink(docID, -1);
	}
	
	private static Link createTargetLink(Integer docID, Integer duplicate)
	{
		return ControllerLinkBuilder.linkTo(
			ControllerLinkBuilder.methodOn(RedirectController.class).redirect(docID, duplicate))
			.withRel("targetUrl");
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
				Link link = createTargetLink(docID, i);
				link = link.withRel(duplicateUrls[i]);
				
				ret.add(link);
			}
		}
		
		return ret;
	}
}
