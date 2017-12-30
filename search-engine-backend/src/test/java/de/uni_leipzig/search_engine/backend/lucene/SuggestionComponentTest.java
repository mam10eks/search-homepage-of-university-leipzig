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
    
    @Test
    public void checkLocalUserQuery(){
        SuggestionComponent qsc = new SuggestionComponent();
        
        String user1 = "user1";
        String user2 = "user2";
        
        qsc.addOrUpdate(user1, "Windows 10");
        qsc.addOrUpdate(user2, "Windows 8");
        
        Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggestByUpdated(user1, "win"));
        Assert.assertEquals(Arrays.asList("windows 8"), qsc.suggestByUpdated(user2, "win"));
        
        qsc.addOrUpdate(user2, "Windows 7");
        
        Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggestByUpdated(user1, "win"));
        Assert.assertEquals(Arrays.asList("windows 8", "windows 7"), qsc.suggestByUpdated(user2, "win"));
    }
    
    @Test
    public void checkGlobalUserQuery(){
        SuggestionComponent qsc = new SuggestionComponent();
        
        String user1 = "user1";
        String user2 = "user2";
        String user3 = "user3";
        String user4 = "user4";
        
        qsc.addOrUpdate(user1, "Windows 10");
        qsc.addOrUpdate(user2, "Windows 8");
        qsc.addOrUpdate(user3, "Windows 7");
        qsc.addOrUpdate(user3, "Windows 10");
        
        Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggestByUpdated(user1, "win"));
        Assert.assertEquals(Arrays.asList("windows 8", "windows 10"), qsc.suggestByUpdated(user2, "win"));
        Assert.assertEquals(Arrays.asList("windows 7", "windows 10"), qsc.suggestByUpdated(user3, "win"));
        Assert.assertEquals(Arrays.asList("windows 10"), qsc.suggestByUpdated(user4, "win"));
    }
    
    @Test
    public void checkGlobalAndLocalUserQuery(){
        SuggestionComponent qsc = new SuggestionComponent();
        
        String user1 = "user1";
        String user2 = "user2";
        String user3 = "user3";
        String user4 = "user4";
        
        // fill global index
        List<String> query_list = Arrays.asList("Windows 8", "Windows 7" ,"Windows Vista", "Windows XP", "Windows 2000", "Windows ME", "Windows 98", "Windows 95", "Windows DOS"); 
        for (int i = 0; i < query_list.size(); i++){
            qsc.add(query_list.get(i));
            for (int j = 0; j < (query_list.size() - i); j++){
                qsc.addOrUpdate(query_list.get(i));
            }
        }
                
        qsc.addOrUpdate(user1, "Windows 10");
        qsc.addOrUpdate(user1, "Windows 8.1");
        qsc.addOrUpdate(user2, "Windows 10");
        qsc.addOrUpdate(user2, "Linux vs Windows");
        qsc.addOrUpdate(user3, "Windows 10");
        
        Assert.assertEquals(Arrays.asList("windows 8.1", "windows 8", "windows 7" ,"windows vista", "windows xp", "windows 2000", "windows me", "windows 98", "windows 95", "windows dos"), qsc.suggestByUpdated(user1, "win"));
        Assert.assertEquals(Arrays.asList("linux vs windows", "windows 8", "windows 7" ,"windows vista", "windows xp", "windows 2000", "windows me", "windows 98", "windows 95", "windows dos"), qsc.suggestByUpdated(user2, "win"));
        Assert.assertEquals(Arrays.asList("windows 8", "windows 7" ,"windows vista", "windows xp", "windows 2000", "windows me", "windows 98", "windows 95", "windows dos", "windows 10"), qsc.suggestByUpdated(user3, "win"));
        Assert.assertEquals(Arrays.asList("windows 8", "windows 7" ,"windows vista", "windows xp", "windows 2000", "windows me", "windows 98", "windows 95", "windows dos", "windows 10"), qsc.suggestByUpdated(user4, "win"));
        
        qsc.addOrUpdate(user4, "Microsoft Windows");
        
        Assert.assertEquals(Arrays.asList("microsoft windows", "windows 8", "windows 7" ,"windows vista", "windows xp", "windows 2000", "windows me", "windows 98", "windows 95", "windows dos"), qsc.suggestByUpdated(user4, "win"));
        
        qsc.addOrUpdate(user4, "Windows 1");
        qsc.addOrUpdate(user4, "Windows 2");
        
        Assert.assertEquals(Arrays.asList("microsoft windows", "windows 1", "windows 2", "windows 8", "windows 7" ,"windows vista", "windows xp", "windows 2000", "windows me", "windows 98"), qsc.suggestByUpdated(user4, "win"));
        
        qsc.addOrUpdate(user1, "Windows 7");
        qsc.addOrUpdate(user2, "Windows 7");
        
        Assert.assertEquals(Arrays.asList("microsoft windows", "windows 1", "windows 2", "windows 7", "windows 8" ,"windows vista", "windows xp", "windows 2000", "windows me", "windows 98"), qsc.suggestByUpdated(user4, "win"));
    }
    
}
