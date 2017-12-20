package de.uni_leipzig.search_engine.backend.lucene;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.store.Directory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.uni_leipzig.search_engine.backend.lucene.SuggestionComponent;

public class SuggestionComponentTest
{
	@Test
	public void checkThatControllerExists()
	{
		new SuggestionComponent();
	}
	
	@Test
	public void checkAddQuery()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("abc");
	}
	
	@Test
	public void checkThatAbcCouldBeRetreaved()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("abc");
		qsc.add("abcd");
		
		Assert.assertEquals(Arrays.asList("abc", "abcd"), qsc.suggest("abc"));
	}
	
	@Test
	public void checkThatAbcCouldBeRetreavedInReversedOrder()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("abcd");
		qsc.add("abc");
		
		Assert.assertEquals(Arrays.asList("abc", "abcd"), qsc.suggest("abc"));
	}
	
	@Test
	public void checkThatAbcdCouldBeRetreaved()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("abcd");
		qsc.add("abc");
		
		Assert.assertEquals(Arrays.asList("abcd"), qsc.suggest("abcd"));
	}
	
	@Test
	public void checkThatNothingIsReturnedForNonExistingQuery()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("abcd");
		qsc.add("abc");
		
		Assert.assertEquals(Arrays.asList(), qsc.suggest("b"));
	}
	
	@Test
	public void checkFuzzySearchIsUsable()
	{
		Function<Directory, Lookup> suggestionFactory = (dir -> new FuzzySuggester(dir, "vsaas", new StandardAnalyzer()));
		SuggestionComponent qsc = new SuggestionComponent(suggestionFactory);
		qsc.add("abcd");
		qsc.add("abc");
		
		Assert.assertEquals(Arrays.asList("abc", "abcd"), qsc.suggest("abb"));
	}
	
	@Test (expected=RuntimeException.class)
	public void checkFuzzySearchIsNotUsable()
	{
		Function<Directory, Lookup> suggestionFactory = (dir -> {throw new RuntimeException();});
		SuggestionComponent qsc = new SuggestionComponent(suggestionFactory);
		qsc.suggest("abc");
	}
	
	@Test
	public void checkThatUpperCaseQueryMatchAgainstAllCasesDoesNotMatter()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("ABCDEFGHIJKLMNOP");
		qsc.add("ABCDEFGHIJKLMNOPQRSTUVW");
		List<String> expected = Arrays.asList("abcdefghijklmnop", "abcdefghijklmnopqrstuvw");
		
		Assert.assertEquals(expected, qsc.suggest("AB"));
		Assert.assertEquals(expected, qsc.suggest("ab"));
	}
	
	@Test
	public void checkThatLowerCaseQueryMatchAgainstAllCasesDoesNotMatter()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("abcdefghijklmnop");
		qsc.add("abcdefghijklmnopqrstuvw");
		List<String> expected = Arrays.asList("abcdefghijklmnop", "abcdefghijklmnopqrstuvw");
		
		Assert.assertEquals(expected, qsc.suggest("AB"));
		Assert.assertEquals(expected, qsc.suggest("ab"));
	}
	
	@Test
	public void checkThatAddingTheSameQueryMultipleTimesDoesNotYieldToDuplicatedSuggestions()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("abcd");
		qsc.add("abcd");
		qsc.add("abcd");
		qsc.add("abc");
		qsc.add("abc");
		qsc.add("abc");
		
		Assert.assertEquals(Arrays.asList("abc", "abcd"), qsc.suggest("abc"));
	}
	
	@Test
	@Ignore("FIXME: make the test green")
	public void checkThatFullPhrasesAreReturned()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("Windows 10");
		qsc.add("Alles ist gut");
		
		//Fixme I am not sure what the valid behaviour should be here, but for the moment i think
		//that full phrases should be returned in that cases
		Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggest("win"));
		Assert.assertEquals(Arrays.asList("alles ist gut"), qsc.suggest("all"));
	}
	
	@Test
	@Ignore("FIXME: make the test green")
	public void checkThatPhrasesAreNotSplitted()
	{
		SuggestionComponent qsc = new SuggestionComponent();
		qsc.add("Windows 10");
		
		//Fixme I am not sure what the valid behaviour should be here, but for the moment i think
		//that full phrases should be returned in that cases
		Assert.assertEquals(Arrays.asList(), qsc.suggest("1"));
	}
}
