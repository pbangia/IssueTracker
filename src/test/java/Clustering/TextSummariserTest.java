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
import java.util.Set;

import static org.junit.Assert.assertEquals;
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
        Cluster c = initialiseClusterWithMockPostTitles();
        String summarisedTitle = forum.summariseClusterTitle(c, 5);
        c.setTitle(summarisedTitle);

        String expectedSummarisedTitle = "login Invoice Report credentials query";
        assertEquals(expectedSummarisedTitle, c.getTitle());

    }

    private Cluster initialiseClusterWithMockPostTitles(){
        Cluster c = spy(new Cluster("1000"));
        ArrayList<String> fakeTitles = getTestTitles();
        Set<Integer> fakePostIds = new HashSet<>();
        int id = 0;
        for (String title: fakeTitles){
            ForumPost f = mock(ForumPost.class);
            int fakeID = id++;
            when(f.getTitle()).thenReturn(title);
            when(f.getQuestionID()).thenReturn(fakeID);
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


}
