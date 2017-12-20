package de.uni_leipzig.search_engine.backend.controller.suggest.dto;

import java.util.List;

import lombok.Data;

@Data
public class QuerySuggestion
{
	private final List<String> recommendations;
}
