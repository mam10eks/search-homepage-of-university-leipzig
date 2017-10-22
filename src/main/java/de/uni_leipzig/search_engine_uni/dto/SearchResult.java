package de.uni_leipzig.search_engine_uni.dto;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.document.Document;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import de.uni_leipzig.search_engine_uni.controller.RedirectController;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain=true)
public class SearchResult
{
	private String title;
	
	private String snippet;
	
	private Link targetUrl;
	
	public SearchResult(Pair<Document, Integer> result)
	{
		setTitle(result.getLeft().get("title"));
		setSnippet(result.getLeft().get("content"));
		setTargetUrl(createTargetLink(result.getRight()));
	}
	
	private static Link createTargetLink(Integer docID)
	{
		return ControllerLinkBuilder.linkTo(
			ControllerLinkBuilder.methodOn(RedirectController.class).redirect(docID))
			.withRel("targetUrl");
	}
}
