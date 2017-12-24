package de.uni_leipzig.search_engine.backend.controller.suggest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uni_leipzig.search_engine.backend.controller.suggest.dto.QuerySuggestion;
import de.uni_leipzig.search_engine.backend.lucene.SuggestionComponent;

@Controller
public class SuggestionController
{
	@Autowired
	private SuggestionComponent suggestionComponent;
	
	@RequestMapping(method=RequestMethod.GET, path="/suggest")
	@ResponseBody
	public Object suggest(@RequestParam String query)
	{
		return new QuerySuggestion(suggestionComponent.suggest(query));
	}
        
        @RequestMapping(method=RequestMethod.GET, path="/add")
	@ResponseBody
	public void add(@RequestParam String query)
	{
		suggestionComponent.add(query);
	}
        
        
}
