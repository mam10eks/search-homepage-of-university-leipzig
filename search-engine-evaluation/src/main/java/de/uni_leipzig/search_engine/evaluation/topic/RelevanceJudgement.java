package de.uni_leipzig.search_engine.evaluation.topic;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum RelevanceJudgement
{
	NOT_RELEVANT,
	RELEVANT,
	RELEVANT_FOR_MRR;

	public static RelevanceJudgement max(RelevanceJudgement a, RelevanceJudgement b)
	{
		return a.ordinal() >= b.ordinal() ? a : b;
	}

	static RelevanceJudgement[] parseRelevanceGainVector(String rawBinaryRelevanceVector, String rawReciprocalRankRelevanceVector)
	{
		RelevanceJudgement[] binaryRelevanceVector = parseBinaryRelevanceVectorOrFailIfInvalid(rawBinaryRelevanceVector);
		RelevanceJudgement[] reciprocalRankRelevanceVector = parseReciprocalRankRelevanceVectorOrFailIfInvalid(rawReciprocalRankRelevanceVector);
		
		return IntStream.range(0, binaryRelevanceVector.length)
			.mapToObj(i -> RelevanceJudgement.max(binaryRelevanceVector[i], reciprocalRankRelevanceVector[i]))
			.collect(Collectors.toList())
			.toArray(new RelevanceJudgement[binaryRelevanceVector.length]);
	}
	
	private static RelevanceJudgement[] parseRelevanceJudgementsFromDigits(String number)
	{
		return number.chars()
				.mapToObj(character -> RelevanceJudgement.values()[Character.getNumericValue((char) character)])
				.collect(Collectors.toList())
				.toArray(new RelevanceJudgement[number.length()]);
	}
	
	private static RelevanceJudgement[] parseBinaryRelevanceVectorOrFailIfInvalid(String rawBinaryRelevanceVector)
	{
		RelevanceJudgement[] ret = RelevanceJudgement.parseRelevanceJudgementsFromDigits(rawBinaryRelevanceVector);
		
		if(ret.length != rawBinaryRelevanceVector.length() || 
				Arrays.stream(ret).anyMatch(jugement -> jugement.equals(RelevanceJudgement.RELEVANT_FOR_MRR)))
		{
			throw new RuntimeException("Couldnt parse the relevance vector '"+ rawBinaryRelevanceVector 
					+"'. Only digits 0 and 1 are allowed.");
		}

		return ret;
	}
	
	private static RelevanceJudgement[] parseReciprocalRankRelevanceVectorOrFailIfInvalid(String rawReciprocalRankRelevanceVector)
	{
		RelevanceJudgement[] ret = RelevanceJudgement.parseRelevanceJudgementsFromDigits(rawReciprocalRankRelevanceVector);
		
		if(ret.length != rawReciprocalRankRelevanceVector.length() ||
				Arrays.stream(ret).anyMatch(jugement -> jugement.equals(RelevanceJudgement.RELEVANT)) ||
				Arrays.stream(ret).filter(jugement -> jugement.equals(RelevanceJudgement.RELEVANT_FOR_MRR)).count() > 1)
		{
			throw new RuntimeException("Couldnt parse the reciprocal rank relevance vector '"+ 
					rawReciprocalRankRelevanceVector +"'. Only digits 0 and one single 2 are allowed.");
		}
			
		return ret;
	}
}
