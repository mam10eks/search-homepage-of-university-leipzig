package de.uni_leipzig.search_engine.evaluation.topic;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class Topic
{
	private String query;
	
	private String description;
	
	private String narrative;
	
	private String category;
	
	private String creator;
	
	private String evaluator;
	
	private RelevanceJudgement[] relevanceGainVector;
}
