package Clustering;

import Authentication.LoginService;
import app.IssueTracker;
import com.mongodb.*;
import exceptions.AssignmentException;
import exceptions.InvalidAuthStateException;
import exceptions.UserRegistrationException;
import models.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DBSCAN;
import weka.core.Instances;

import java.net.UnknownHostException;
import java.util.*;

import static models.UserRole.ADMIN;
import static models.UserRole.DEVELOPER;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    IssueTracker issueTracker;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUpUserAuthenticationMockObjects() throws UnknownHostException{


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

    public ArrayList<ForumPost> populatePosts(){
        ArrayList<ForumPost> posts = new ArrayList<>();
        posts.add(new ForumPost(44330));
        posts.add(new ForumPost(44331));
        posts.add(new ForumPost(44332));
        posts.add(new ForumPost(44333));
        posts.add(new ForumPost(44334));
        posts.add(new ForumPost(44335));
        posts.add(new ForumPost(44336));
        posts.add(new ForumPost(44337));
        posts.add(new ForumPost(44338));
        posts.add(new ForumPost(44339));
        posts.add(new ForumPost(44330));
        return posts;
    }

    @Test
    public void clusterRelatedForumPosts() throws Exception {

        DBSCAN dbscan = mock(DBSCAN.class);
        doNothing().when(dbscan).setMinPoints(anyInt());
        doNothing().when(dbscan).setEpsilon(anyInt());
        doNothing().when(dbscan).buildClusterer(any(Instances.class)); // Throws generic exception
        when(forum.getDBSCAN()).thenReturn(dbscan);

        ClusterEvaluation eval = spy(new ClusterEvaluation());
        when(forum.getEval()).thenReturn(eval);
        //doNothing().when(eval).setClusterer(dbscan);
        doNothing().when(eval).evaluateClusterer(any(Instances.class)); // Throws generic exception

        doReturn(10).when(eval).getNumClusters();
        doReturn(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 , 8.0, 9.0, 0.0}).when(eval).getClusterAssignments();
        doReturn(populatePosts()).when(forum).getAllPosts();
        forum.posts = forum.getAllPosts();

        List<HashSet<Integer>> expectedIDs = getExpectedIDs();
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
        titles.add("Delete ALL postData and start again?");
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
        when(forum.getAccessPrivilege()).thenReturn(DEVELOPER);

        exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("Only admins have permission to modify clusters");

        Cluster c = spy(new Cluster(0));
        doReturn(c).when(forum).getCluster(0);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn("author");
        when(f.getQuestionID()).thenReturn(0);

        forum.addForumPostToCluster(f, 0);
    }

    @Test
    public void userIsAbleToAddPostsToClusterIfAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        Cluster c = spy(new Cluster(0));
        doReturn(c).when(forum).getCluster(0);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn("author");
        when(f.getQuestionID()).thenReturn(0);
        when(f.getClusterID()).thenReturn(-1);

        forum.addForumPostToCluster(f, 0);
    }

    @Test
    public void shouldThrowExceptionWhenAdminAddsForumPostOfExistingClusterToAnotherCluster() {
        ForumPost f = mock(ForumPost.class);
        when(f.getClusterID()).thenReturn(1);
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        exception.expect(AssignmentException.class);
        exception.expectMessage("Forum post is already assigned to a cluster");

        forum.addForumPostToCluster(f, 0);
    }



    @Ignore
    @Test
    public void testRealDatabaseWithClusterPersistence(){

        try {
            IssueTracker i = new IssueTracker();
            //RegistrationService r = i.getRegistrationService();
            LoginService l = i.getLoginService();
            //r.register("user1","password","ADMIN");
            l.login("user1","password");
            i.getForumService().getRelatedIssues();
            i.getForumService().saveClusters();
            i.getForumService().saveForumPosts();
        }catch (UserRegistrationException e){
            System.out.println("User is already registered in real db");
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }
}
