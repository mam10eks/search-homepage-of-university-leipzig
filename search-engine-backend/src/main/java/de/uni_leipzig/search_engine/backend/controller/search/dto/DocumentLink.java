package de.uni_leipzig.search_engine.backend.controller.search.dto;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import de.uni_leipzig.search_engine.backend.controller.redirect.RedirectController;
import groovy.transform.EqualsAndHashCode;
import lombok.Data;

/**
 * A {@link Link} which contains a
 * {@link Link#getHref() tracking link} and a {@link DocumentLink#displayLink display link}.
 * 
 * @author Maik Fröbe
 *
 */
@Data
@EqualsAndHashCode(callSuper=true)
class DocumentLink extends Link
{
	private static final long serialVersionUID = 1L;

	private String displayLink;
	
	DocumentLink(Integer docID, Integer duplicate, String displayLink)
	{
		super(createTargetLink(docID, duplicate).getHref(), duplicate < 0 ? Link.REL_SELF : "duplicate");
		this.displayLink = displayLink;
	}
	
	private static Link createTargetLink(Integer docID, Integer duplicate)
	{
		return ControllerLinkBuilder.linkTo(
			ControllerLinkBuilder.methodOn(RedirectController.class).redirect(docID, duplicate))
			.withRel(Link.REL_SELF);
	}
}
