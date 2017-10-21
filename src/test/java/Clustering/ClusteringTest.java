package Clustering;

import Authentication.LoginService;
import com.mongodb.*;
import models.Cluster;
import models.ForumPost;
import models.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import weka.clusterers.ClusterEvaluation;
import weka.core.Instance;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ClusteringTest {
    private User u;
    private LoginService auth;
    private DBCollection dbCollection;
    private ForumService forum;

    Set<Integer> postIDs = new HashSet<>();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUpUserAuthenticationMockObjects(){


//        MongoClient connection = mock(MongoClient.class);
//        DB db = mock(DB.class);
//        dbCollection = mock(DBCollection.class);
//
//        doReturn(db).when(connection).getDB(anyString());
//        doReturn(dbCollection).when(db).getCollection(anyString());
//
//       // auth = Mockito.spy(new LoginService(connection));

        MongoClient connection = mock(MongoClient.class);
        Morphia morphia = mock(Morphia.class);
        Datastore ds = mock(Datastore.class);

        when(morphia.createDatastore(any(MongoClient.class),anyString())).thenReturn(ds);

        forum = spy(new ForumService(connection,morphia));
    }

    }

    @Test
    public void clusterRelatedForumPosts(){
        List<HashSet<Integer>> expectedIDs = getExpectedIDs();

        //TODO: Use mock objects and change return result to proper objects
        Map<Integer, Cluster> clusters = forum.getRelatedIssues();

        //Check equal number of clusters
        assertEquals(expectedIDs.size(), clusters.size());

        //check posts within clusters
        for (int i=0; i<clusters.size(); i++){
            Set<Integer> actual = clusters.get(i).getPostIDs();
            Set<Integer> expected = expectedIDs.get(i);
            assertEquals(expected.size(), actual.size());
            assertTrue(actual.containsAll(expected));
        }

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

    public List<HashSet<Integer>> getExpectedIDs() {
        List<HashSet<Integer>> idList = new ArrayList<>();
        idList.add(new HashSet<Integer>(Arrays.asList(44330)));
        idList.add(new HashSet<Integer>(Arrays.asList(44331)));
        idList.add(new HashSet<Integer>(Arrays.asList(44332)));
        idList.add(new HashSet<Integer>(Arrays.asList(44333)));
        idList.add(new HashSet<Integer>(Arrays.asList(44334)));
        idList.add(new HashSet<Integer>(Arrays.asList(44335)));
        idList.add(new HashSet<Integer>(Arrays.asList(44336)));
        idList.add(new HashSet<Integer>(Arrays.asList(44337)));
        idList.add(new HashSet<Integer>(Arrays.asList(44338)));
        idList.add(new HashSet<Integer>(Arrays.asList(44339)));
        return idList;
    }

    @Test
    public void testNumberUsersAndPosts(){
        Cluster c = new Cluster(53200);
        c.addForumPost(1000, "author 1");
        c.addForumPost(1001, "author 1");
        c.addForumPost(1002, "author 2");
        c.addForumPost(1003, "author 3");
        c.addForumPost(1004, "author 4");

        doReturn(c).when(forum).getCluster(53200);

        Cluster returned = forum.getCluster(53200);
        assertEquals(4, returned.getNumUsers());
        assertEquals(5, returned.getNumPosts());
    }

    @Test
    public void clusterSizeIncreasesWhenNewForumPostAddedToCluster(){
//        Cluster c = new Cluster(53200);
//        c.addForumPost(1000, "author 1");
        Cluster c = new Cluster(53200);
        doReturn(c).when(forum).getCluster(53200);

        Cluster cluster = forum.getCluster(53200);
        assertEquals(0, cluster.getNumUsers());
        assertEquals(0, cluster.getNumPosts());

        cluster.addForumPost(53201, "author 1");
        assertEquals(1, cluster.getNumUsers());
        assertEquals(1, cluster.getNumPosts());

    }

    @Test
    public void throwExceptionWhenAddPostToClusterIfNotAdmin(){

    }
}
