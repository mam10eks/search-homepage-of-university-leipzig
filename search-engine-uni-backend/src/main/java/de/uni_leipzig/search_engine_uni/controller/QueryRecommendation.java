package de.uni_leipzig.search_engine_uni.controller;

import java.util.List;

import lombok.Data;

@Data
public class QueryRecommendation
{
	private final List<String> recommendations;
}
