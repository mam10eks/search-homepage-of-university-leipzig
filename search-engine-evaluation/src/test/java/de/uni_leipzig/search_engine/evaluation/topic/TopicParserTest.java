package de.uni_leipzig.search_engine.evaluation.topic;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.approvaltests.Approvals;
import org.junit.Test;

import de.uni_leipzig.search_engine.evaluation.topic.CsvTopicParser;
import lombok.SneakyThrows;

public class TopicParserTest
{
	@Test
	@SneakyThrows
	public void checkThatTopicWithOneLineCouldBeParsed()
	{
		String csvString = readFile("src/test/resources/one_line_search_evaluation.csv");
		
		Approvals.verify(CsvTopicParser.parseTopicsFromCsvString(csvString));
	}
	
	@Test
	@SneakyThrows
	public void checkThatTopicWithMultipleLinesCouldBeParsed()
	{
		String csvString = readFile("src/test/resources/multi_line_search_evaluation.csv");
		
		Approvals.verify(CsvTopicParser.parseTopicsFromCsvString(csvString));
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatTopicWithInvalidReciprocalRankJudgementCouldNotBeParsed()
	{
		String csvString = readFile("src/test/resources/one_line_search_evaluation_with_invalid_rr_range.csv");
		
		CsvTopicParser.parseTopicsFromCsvString(csvString);
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatTopicWithMultipleRelevantReciprocalRankJudgementCouldNotBeParsed()
	{
		String csvString = readFile("src/test/resources/one_line_search_evaluation_with_multiple_valid_rr_entries.csv");
		
		CsvTopicParser.parseTopicsFromCsvString(csvString);
	}
	
	@Test(expected=RuntimeException.class)
	@SneakyThrows
	public void checkThatTopicWithInvalidRelevanceJudgmentCouldNotBeParsed()
	{
		String csvString = readFile("src/test/resources/one_line_search_evaluation_with_invalid_relevance_vector.csv");
		
		CsvTopicParser.parseTopicsFromCsvString(csvString);
	}
	
	@SneakyThrows
	private static final String readFile(String path)
	{
		return new String(Files.readAllBytes(Paths.get(path)));
	}
}
