package de.uni_leipzig.search_engine.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SuggesterController {

    DBController searchDB = new DBController();
        
    public SuggesterController() {
    }

    @RequestMapping(value = "/getQueries", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public List<String> getQueries(@RequestParam String search) {
        
        return searchDB.search(search);

    }
    
    @RequestMapping(value = "/addQuery", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public void addQuery(@RequestParam String search) {
        
        searchDB.addQuery(search);
        
    }
    
}
