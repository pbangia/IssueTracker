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

        String expectedSummarisedTitle = "login Invoice Report credentials query";
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

        String expectedSummarisedTitle = "notacommonword_1 notacommonword_2";
        assertEquals(expectedSummarisedTitle, c.getTitle());
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

    public ArrayList<String> getTestTitles() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add("xero-php API, how to structure query");
        titles.add("Getting zero dollar value categories included in Profit and Loss Report");
        titles.add("Link Manual Journal Entry Transactions to an Invoice or Bill");
        titles.add("Hi there,I am unable to login with my login credentials today.Can I know what could be the reason?");
        return titles;
    }

    public List<String> getTestTitlesWithCommonWords() {
        ArrayList<String> fakeTitles = new ArrayList<>();
        fakeTitles.add("this that how we be will notacommonword_1");
        fakeTitles.add("this we that a be I can");
        fakeTitles.add("a then how this what a a a a notacommonword_2");
        return fakeTitles;
    }
}
