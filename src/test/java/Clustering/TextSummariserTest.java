package Clustering;

import Authentication.LoginService;
import app.IssueTracker;
import com.mongodb.MongoClient;
import models.Cluster;
import models.ForumPost;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

/**
 * Created by priyankitbangia on 25/10/17.
 */
public class TextSummariserTest {

    private User u;
    private ForumService forum;
    IssueTracker issueTracker;

    @Before
    public void init() throws UnknownHostException {
        
        MongoClient connection = mock(MongoClient.class);
        Morphia morphia = mock(Morphia.class);
        Datastore ds = mock(Datastore.class);

        when(morphia.createDatastore(any(MongoClient.class),anyString())).thenReturn(ds);

        issueTracker = new IssueTracker(connection, morphia);
        LoginService auth = mock(LoginService.class);

        u = mock(User.class);
        when(auth.getCurrentUser()).thenReturn(u);

        issueTracker.setLoginService(auth);
        forum = spy(issueTracker.getForumService());

    }

    @Test
    public void shouldSummariseClusteredPostTitlesAndSetAsIssueTitle() {
        List<String> fakeTitles = getTestTitles();
        Cluster c = initialiseClusterWithMockPostTitles(fakeTitles);
        String summarisedTitle = forum.summariseClusterTitle(c, 5);
        c.setTitle(summarisedTitle);

        String expectedSummarisedTitle = "API Invoice login query xero-php";
        assertEquals(expectedSummarisedTitle, c.getTitle());

    }

    @Test
    public void shouldSummariseTitleWithSpecifiedLengthGreaterThanMaxLengthOfPostTitles() {
        List<String> fakeTitles = getTestTitles();
        Cluster c = initialiseClusterWithMockPostTitles(fakeTitles);

        int lengthGreaterThanNumberOfWords = 10000;

        String summarisedTitle = forum.summariseClusterTitle(c, lengthGreaterThanNumberOfWords);
        c.setTitle(summarisedTitle);

        String[] returnedWords = c.getTitle().split(" ");
        assertTrue(returnedWords.length < lengthGreaterThanNumberOfWords);

    }

    @Test
    public void shouldIgnoreCommonEnglishWordsWhenGeneratingIssueTitleFromForumPosts() {
        List<String> fakeTitles = getTestTitlesWithCommonWords();
        Cluster c = initialiseClusterWithMockPostTitles(fakeTitles);
        String summarisedTitle = forum.summariseClusterTitle(c, 2);
        c.setTitle(summarisedTitle);

        String expectedSummarisedTitle = "TEXT SUMMARISED";
        assertEquals(expectedSummarisedTitle, c.getTitle());
    }

    @Test
    public void shouldNotCountRepeatedWordsOfDifferentCaseWhenGeneratingIssueTitle() {
        List<String> fakeTitles = getTestTitlesWithMixedCase();
        Cluster c = initialiseClusterWithMockPostTitles(fakeTitles);
        String summarisedTitle = forum.summariseClusterTitle(c, 2);
        c.setTitle(summarisedTitle);

        String expectedSummarisedTitle = "shouldbe1st ShouldBe2nd";
        assertEquals(expectedSummarisedTitle, c.getTitle());
    }

    @Test
    public void shouldSummariseClusteredPostContentAndSetAsIssueSummary() {
        List<String> fakeContent = getTestContent();
        Cluster c = initialiseClusterWithMockPostContent(fakeContent);
        String summarisedTitle = forum.summariseClusterContent(c, 11);
        c.setSummary(summarisedTitle);

        String expectedSummarisedContent = "fox dog jumps brown lazy higher jump lives pet talk wild";
        assertEquals(expectedSummarisedContent, c.getSummary());

    }

    @Test
    public void shouldSetADefaultClusterTitleIfClusteredForumPostsHaveEmptyTitles() {
        List<String> emptyFakeTitles = getTestEmptyText();
        Cluster c = initialiseClusterWithMockPostTitles(emptyFakeTitles);
        String summarisedTitle = forum.summariseClusterTitle(c, 5);
        c.setTitle(summarisedTitle);

        String expectedDefaultTitle = "Issue #1000";
        assertEquals(expectedDefaultTitle, c.getTitle());

    }

    @Test
    public void shouldSetADefaultClusterSummaryIfClusteredForumPostsHaveEmptyContent() {
        List<String> emptyFakeContent = getTestEmptyText();
        Cluster c = initialiseClusterWithMockPostContent(emptyFakeContent);
        String summarisedContent = forum.summariseClusterContent(c, 5);
        c.setSummary(summarisedContent);

        String expectedDefaultContent = "Issue #1000";
        assertEquals(expectedDefaultContent, c.getSummary());

    }


    private Cluster initialiseClusterWithMockPostTitles(List<String> fakeTitles){
        Cluster c = spy(new Cluster("1000"));
        Set<Integer> fakePostIds = new HashSet<>();
        int id = 0;
        for (String title: fakeTitles){
            ForumPost f = mock(ForumPost.class);
            int fakeID = id++;
            when(f.getTitle()).thenReturn(title);
            when(f.getQuestionID()).thenReturn(fakeID);
            doReturn(f).when(forum).getForumPost(fakeID);
            fakePostIds.add(fakeID);
        }

        doReturn(fakePostIds).when(c).getPostIDs();
        return c;
    }

    private Cluster initialiseClusterWithMockPostContent(List<String> fakeContent){
        Cluster c = spy(new Cluster("1000"));
        Set<Integer> fakePostIds = new HashSet<>();
        int id = 0;
        for (String content: fakeContent){
            ForumPost f = mock(ForumPost.class);
            int fakeID = id++;
            when(f.getContent()).thenReturn(content);
            when(f.getQuestionID()).thenReturn(fakeID);
            doReturn(f).when(forum).getForumPost(fakeID);
            fakePostIds.add(fakeID);
        }

        doReturn(fakePostIds).when(c).getPostIDs();
        return c;
    }

    public ArrayList<String> getTestTitles() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add("xero-php API, how to structure query");
        titles.add("xero-php API query");
        titles.add("Getting zero dollar value categories included in Profit and Loss Report");
        titles.add("Link Manual Journal Entry Transactions to an Invoice or Bill");
        titles.add("Want to see Invoice");
        titles.add("Hi there,I am unable to login with my login credentials today.Can I know what could be the reason?");
        return titles;
    }

    public List<String> getTestTitlesWithCommonWords() {
        ArrayList<String> fakeTitles = new ArrayList<>();
        fakeTitles.add("the TEXT should only be SUMMARISED");
        fakeTitles.add("TEXT the this hello we that a a be I can a I how when");
        fakeTitles.add("TEXT to be SUMMARISED");
        return fakeTitles;
    }

    public List<String> getTestTitlesWithMixedCase() {
        ArrayList<String> fakeTitles = new ArrayList<>();
        fakeTitles.add("ShouldBe2nd ShouldBe2nd");
        fakeTitles.add("shouldbe1st");
        fakeTitles.add("sHouldBe1st SHOULDbe1st");
        return fakeTitles;
    }

    public List<String> getTestContent() {
        ArrayList<String> testContent = new ArrayList<>();
        testContent.add("this should talk about a brown fox who jumps over a lazy dog");
        testContent.add("the brown fox jumps over a dog who is very lazy");
        testContent.add("fox lives in the wild but dog is a pet");
        testContent.add("both fox and dog like to jump but the fox jumps higher");
        return testContent;
    }

    public List<String> getTestEmptyText() {
        ArrayList<String> fakeEmptyTextData = new ArrayList<>();
        fakeEmptyTextData.add("");
        fakeEmptyTextData.add(null);
        return fakeEmptyTextData;
    }
}
