package de.uni_leipzig.search_engine.evaluation.topic;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CsvTopicParser
{
	private static final String CSV_FIELD_CATEGORY = "Kategorie";
	
	private static final String CSV_FIELD_CREATOR = "Ersteller";
	
	private static final String CSV_FIELD_EVALUATOR = "Bewerter";
	
	private static final String CSV_FIELD_DESCRIPTION = "Information Need";
	
	private static final String CSV_FIELD_NARRATIVE = "Criteria for Relevance";
	
	private static final String CSV_FIELD_QUERY = "Query";
	
	private static final String CSV_FIELD_BINARY_RELEVANCE_VECTOR = "Relevanz Vektor";
	
	private static final String CSV_FIELD_RECIPROCAL_RANK_RELEVANCE_VECTOR = "RR Vektor";
	
	public static List<Topic> parseTopicsFromCsvString(String csvString) throws IOException
	{
		CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
				.parse(new StringReader(csvString));
		
		return csvParser.getRecords().stream()
				.map(CsvTopicParser::mapCsvRecordToTopicOrFailIfInvalid)
				.collect(Collectors.toList());
	}
	
	private static Topic mapCsvRecordToTopicOrFailIfInvalid(CSVRecord record)
	{
		return new Topic()
				.setCategory(record.get(CSV_FIELD_CATEGORY))
				.setCreator(record.get(CSV_FIELD_CREATOR))
				.setEvaluator(record.get(CSV_FIELD_EVALUATOR))
				.setDescription(record.get(CSV_FIELD_DESCRIPTION))
				.setNarrative(record.get(CSV_FIELD_NARRATIVE))
				.setQuery(record.get(CSV_FIELD_QUERY))
				.setRelevanceGainVector(RelevanceJudgement.parseRelevanceGainVector(
						record.get(CSV_FIELD_BINARY_RELEVANCE_VECTOR),
						record.get(CSV_FIELD_RECIPROCAL_RANK_RELEVANCE_VECTOR)));
	}
}
