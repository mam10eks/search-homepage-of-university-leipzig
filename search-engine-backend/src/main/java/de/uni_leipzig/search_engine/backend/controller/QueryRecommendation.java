package de.uni_leipzig.search_engine.backend.controller;

import java.util.List;

import lombok.Data;

@Data
public class QueryRecommendation
{
	private final List<String> recommendations;
}
