package Clustering;

import Authentication.LoginService;
import com.mongodb.*;
import models.User;
import models.UserRole;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ClusteringTest {
    private User u;
    private LoginService auth;
    private DBCollection dbCollection;
    private ForumService forum;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUpUserAuthenticationMockObjects(){
        u = mock(User.class);
        when(u.getUsername()).thenReturn("testUsername");
        when(u.getPassword()).thenReturn("testPassword");
        when(u.getRole()).thenReturn(UserRole.ADMIN);

        MongoClient connection = mock(MongoClient.class);
        DB db = mock(DB.class);
        dbCollection = mock(DBCollection.class);

        doReturn(db).when(connection).getDB(anyString());
        doReturn(dbCollection).when(db).getCollection(anyString());

        auth = Mockito.spy(new LoginService(connection));

        try {
            forum = new ForumService();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void clusterRelatedForumPosts(){
        //TODO: Use mock objects and change return result to proper objects
        String relatedPosts = forum.getRelatedIssues();
        assertEquals(getExpectedIDs(), relatedPosts);
        //assertEquals("[44330, 44330]", Arrays.toString(relatedPosts.get(0)));


    }

    @Test
    public void showListOfIssueTitles(){
        ArrayList<String> testList = getTestList();

        List<String> issues = forum.getIssueTitles();

        assertEquals(testList.size(),issues.size());
        Iterator expected = testList.iterator();
        for (String actualIssue: issues){
            assertEquals(expected.next(), actualIssue);
        }
    }

    public ArrayList<String> getTestList() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add("xero-php API, how to structure query");
        titles.add("Delivery Address on Invoice");
        titles.add("Getting zero dollar value categories included in Profit and Loss Report");
        titles.add("Link Manual Journal Entry Transactions to an Invoice or Bill");
        titles.add("Delete ALL data and start again?");
        titles.add("Hi there,I am unable to login with my login credentials today.Can I know what could be the reason?");
        titles.add("Invoice");
        titles.add("Private authentication problem");
        titles.add("link api to a xero instance");
        titles.add("Xero Interface with PMS system");
        titles.add("xero-php API, how to structure query");
        return titles;
    }

    public String getExpectedIDs() {
        return "[[44330, 44330], [44331], [44332], [44333], [44334], [44335], [44336], [44337], [44338], [44339]]";
    }
}
