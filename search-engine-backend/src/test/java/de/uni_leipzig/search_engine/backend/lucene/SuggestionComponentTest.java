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
    public void checkThatFullPhrasesAreReturned()
    {
        SuggestionComponent qsc = new SuggestionComponent();

        qsc.add("Windows 10");
        qsc.add("Alles ist gut");

        Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggest("win"));
        Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggest("Win"));
        Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggest("wIn"));
        Assert.assertEquals(Arrays.asList("alles ist gut"), qsc.suggest("all"));
        Assert.assertEquals(Arrays.asList("alles ist gut"), qsc.suggest("ist"));
    }

    @Test
    public void checkThatFullPhrasesAndWordsAreReturned()
    {
        SuggestionComponent qsc = new SuggestionComponent();

        qsc.add("Linux");
        qsc.add("Windows 7");
        qsc.add("Windows 8");
        qsc.add("Ubuntu Linux");
        qsc.add("Windows 10");
        qsc.add("Linux vs Windows");
        qsc.add("Linux latest version");

        Assert.assertEquals(Arrays.asList("windows 7", "windows 8", "windows 10", "linux vs windows"), qsc.suggest("win"));
        Assert.assertEquals(Arrays.asList("linux", "ubuntu linux", "linux vs windows", "linux latest version"), qsc.suggest("lin"));
    }

    @Test
    @Ignore("FIXME: suggester runs other as we think, use the other method")
    public void checkThatPhrasesWithHigherFrequenzAreFirstReturned()
    {
        SuggestionComponent qsc = new SuggestionComponent();

        qsc.add("Linux");
        qsc.add("Windows 7");
        qsc.add("Windows 8");
        qsc.add("Ubuntu Linux");
        qsc.add("Windows 10");
        qsc.add("Linux vs Windows");
        qsc.add("Microsoft Windows 10");
        qsc.add("Windows 10");
        qsc.add("Linux latest version");
        
        Assert.assertEquals(Arrays.asList("windows 10", "windows 7", "windows 8", "linux vs windows", "microsoft windows 10"), qsc.suggest("win"));
    }
    
    @Test
    public void checkThatPhrasesWithHigherFrequenzAreFirstReturned_otherMethod(){
        SuggestionComponent qsc = new SuggestionComponent();

        qsc.addOrUpdate("Linux");
        qsc.addOrUpdate("Windows 8");
        qsc.addOrUpdate("Windows 10");
        qsc.addOrUpdate("Windows 10");
        qsc.addOrUpdate("Windows 7");
        qsc.addOrUpdate("Windows 10");
        qsc.addOrUpdate("Windows 8");
        qsc.addOrUpdate("Ubuntu Linux");

        Assert.assertEquals(Arrays.asList("windows 10", "windows 8", "windows 7"), qsc.suggestByUpdated("win"));
    }
    
    @Test
    public void checkThatNothingAreReturned()
    {
        SuggestionComponent qsc = new SuggestionComponent();

        qsc.add("Linux");
        qsc.add("Windows 7");
        qsc.add("Windows 8");
        qsc.add("Ubuntu Linux");
        
        Assert.assertEquals(Arrays.asList(), qsc.suggest("cv"));
    }
    
    @Test
    public void checkThatPhrasesAreNotSplitted()
    {
        SuggestionComponent qsc = new SuggestionComponent();
        qsc.add("Windows 10");

        Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggest("1"));
    }

    @Test
    public void checkInitIndex(){
        SuggestionComponent qsc = new SuggestionComponent();
        
        qsc.initIndex();
        
        Assert.assertEquals(Arrays.asList("gerik scheuermann informatik", "prof dr gerik scheuermann"), qsc.suggest("scheuermann"));
    }
    
}
