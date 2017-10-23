package Clustering;

import Authentication.LoginService;
import app.IssueTracker;
import com.mongodb.*;
import exceptions.AssignmentException;
import exceptions.ClusterException;
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
import static org.junit.Assert.*;
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
    public void init() throws UnknownHostException{


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
    public void returnClusteredForumPostIDsWhenPostsAreClustered() throws Exception {
        //Mock weka APIs for clustering
        DBSCAN dbscan = mock(DBSCAN.class);
        doNothing().when(dbscan).setMinPoints(anyInt());
        doNothing().when(dbscan).setEpsilon(anyInt());
        doNothing().when(dbscan).buildClusterer(any(Instances.class)); // Throws generic exception
        when(forum.getDBSCAN()).thenReturn(dbscan);
        ClusterEvaluation eval = spy(new ClusterEvaluation());
        when(forum.getEval()).thenReturn(eval);
        doNothing().when(eval).evaluateClusterer(any(Instances.class)); // Throws generic exception

        //perform expected mapping on test input
        doReturn(10).when(eval).getNumClusters();
        doReturn(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 , 8.0, 9.0, 0.0}).when(eval).getClusterAssignments();
        doReturn(getTestPosts()).when(forum).getAllPosts();
        forum.postsList = forum.getAllPosts();

        List<HashSet<Integer>> expectedIDs = getExpectedIDs();
        Map<Integer, Cluster> clusters = forum.getRelatedIssues();

        //Check equal number of clusters
        assertEquals(expectedIDs.size(), clusters.size());
        //check grouped forum post IDs within each cluster
        for (int i=0; i<clusters.size(); i++){
            Set<Integer> actual = clusters.get(i).getPostIDs();
            Set<Integer> expected = expectedIDs.get(i);
            assertEquals(expected.size(), actual.size());
            assertTrue(actual.containsAll(expected));
        }

    }

    //TODO:
    @Test
    public void showListOfIssueTitles(){
//        ArrayList<String> testList = getTestList();
//
//        List<String> issues = forum.getIssueTitles();
//
//        assertEquals(testList.size(),issues.size());
//        Iterator expected = testList.iterator();
//        for (String actualIssue: issues){
//            assertEquals(expected.next(), actualIssue);
//        }
    }

    @Test
    public void calculateCorrectNumberOfUniqueUsersAffectedForACluster(){
        Cluster c = new Cluster(53200);
        c.addForumPost(1000, "author 1"); //Account for multiple posts by same author
        c.addForumPost(1001, "author 1");
        c.addForumPost(1002, "author 2");
        c.addForumPost(1003, "author 3");
        c.addForumPost(1004, "author 4");

        doReturn(c).when(forum).getCluster(53200);

        Cluster returned = forum.getCluster(53200);
        assertEquals(4, returned.getNumAffectedUsers());
    }

    @Test
    public void calculateCorrectNumberOfPostsInACluster(){
        Cluster c = new Cluster(53200);
        c.addForumPost(1000, "author 1"); //Account for multiple posts by same author
        c.addForumPost(1001, "author 1");
        c.addForumPost(1002, "author 2");
        c.addForumPost(1003, "author 3");
        c.addForumPost(1004, "author 4");

        doReturn(c).when(forum).getCluster(53200);

        Cluster returned = forum.getCluster(53200);
        assertEquals(5, returned.getNumPosts());
    }

//    @Test
//    public void clusterSizeIncreasesWhenNewPostAddedToCluster(){
//        Cluster c = new Cluster(53200);
//        doReturn(c).when(forum).getCluster(53200);
//
//        //Check size before
//        Cluster cluster = forum.getCluster(53200);
//        assertEquals(0, cluster.getNumAffectedUsers());
//        assertEquals(0, cluster.getNumPosts());
//
//        //Check size after
//        cluster.addForumPost(53201, "author 1");
//        assertEquals(1, cluster.getNumAffectedUsers());
//        assertEquals(1, cluster.getNumPosts());
//
//    }

    @Test
    public void shouldThrowInvalidAuthStateExceptionWhenAddingPostToClusterIfNotAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(DEVELOPER);

        exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("Only admins have permission to add clusters");

        Cluster c = spy(new Cluster(1000));
        doReturn(c).when(forum).getCluster(1000);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn("author");
        when(f.getQuestionID()).thenReturn(90);

        forum.addForumPostToCluster(f, 1000);
    }

    @Test
    public void postIsAddedCorrectlyToClusterWhenPerformingAddAsAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        Cluster c = spy(new Cluster(1000));
        doReturn(c).when(forum).getCluster(1000);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn("author");
        when(f.getQuestionID()).thenReturn(90);
        when(f.getClusterID()).thenReturn(-1);
        doReturn(c).when(forum).getCluster(90);

        forum.addForumPostToCluster(f, 1000);
        verify(c).addForumPost(f.getQuestionID(), f.getAuthor());
        assertTrue(c.getPostIDs().contains(f.getQuestionID()));
        assertTrue(c.getUsersAffected().contains(f.getAuthor()));
        assertEquals(1, c.getNumPosts());
        assertEquals(1, c.getNumAffectedUsers());
    }

    @Test
    public void shouldThrowAssignmentExceptionWhenAddingPostToMultipleClusters() {
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        //Assign cluster to post
        ForumPost f = mock(ForumPost.class);
        when(f.getClusterID()).thenReturn(1000);

        exception.expect(AssignmentException.class);
        exception.expectMessage("Forum post is already assigned to a cluster");

        //Add to different cluster
        forum.addForumPostToCluster(f, 999);
    }

    @Test
    public void shouldThrowInvalidAuthStateExceptionWhenRemovingPostFromClusterIfNotAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(DEVELOPER);

        exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("Only admins have permission to remove clusters");

        Cluster c = spy(new Cluster(1000));
        doReturn(c).when(forum).getCluster(1000);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn("author");
        when(f.getQuestionID()).thenReturn(90);
        when(f.getClusterID()).thenReturn(1000);

        forum.removeForumPostFromCluster(f);
    }

    @Test
    public void postIsRemovedCorrectlyFromClusterWhenPerformingRemoveAsAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        //Initialise post data to remove from cluster
        Cluster c = spy(new Cluster(1000));
        doReturn(c).when(forum).getCluster(1000);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn("author");
        when(f.getQuestionID()).thenReturn(90);
        when(f.getClusterID()).thenReturn(1000);
        c.setNumAffectedUsers(2);
        c.setNumPosts(2);

        c.setUsersAffected(new ArrayList<>(Arrays.asList(f.getAuthor(), "author2")));
        c.setPostIDs(new HashSet<>(Arrays.asList(f.getQuestionID(),91)));
        doReturn(c).when(forum).getCluster(90);

        forum.removeForumPostFromCluster(f);

        //Check if relevant cluster data is in the expected state after remove
        verify(c).removeForumPost(f);
        assertFalse(c.getPostIDs().contains(f.getQuestionID()));
        assertFalse(c.getUsersAffected().contains(f.getAuthor()));
        assertEquals(1, c.getNumPosts());
        assertEquals(1, c.getNumAffectedUsers());
    }

    @Test
    public void shouldThrowAssignmentExceptionWhenRemovingUngroupedPost(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        exception.expect(AssignmentException.class);
        exception.expectMessage("Forum post not assigned to a cluster");

        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn("author");
        when(f.getQuestionID()).thenReturn(90);
        when(f.getClusterID()).thenReturn(-1);

        forum.removeForumPostFromCluster(f);
    }

    @Test
    public void shouldThrowInvalidAuthStateExceptionWhenDeletingClusterIfNotAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(DEVELOPER);
        exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("Only admins have permission to remove clusters");

        Cluster c = mock(Cluster.class);
        forum.deleteCluster(c);

    }

    @Test
    public void shouldThrowClusterExceptionWhenDeletingNonExistingCluster(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);
        exception.expect(ClusterException.class);
        exception.expectMessage("Cluster ID does not exist");

        Cluster c = mock(Cluster.class);
        when(c.getClusterID()).thenReturn(-1);
        forum.setClusters(new HashMap<>());

        forum.deleteCluster(c);
    }

    @Test
    public void forumPostsClusteredIndividuallyAfterDeletingCluster(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        //Initialise cluster with forum posts
        Cluster c = spy(new Cluster(1000));
        doReturn(c).when(forum).getCluster(1000);
        ForumPost f1 = mock(ForumPost.class);
        when(f1.getAuthor()).thenReturn("author1");
        when(f1.getQuestionID()).thenReturn(90);
        when(f1.getClusterID()).thenReturn(1000);
        ForumPost f2 = mock(ForumPost.class);
        when(f2.getAuthor()).thenReturn("author2");
        when(f2.getQuestionID()).thenReturn(91);
        when(f2.getClusterID()).thenReturn(1000);

        //Add forum post id info to cluster
        Set<Integer> testPostIds = new HashSet<>(Arrays.asList(f1.getQuestionID(), f2.getQuestionID()));
        when(c.getPostIDs()).thenReturn(testPostIds);

        // Use test cluster and forum post maps in forum service
        Map<Integer, Cluster> testClusterMap = new HashMap<>();
        testClusterMap.put(c.getClusterID(), c);
        forum.setClusters(testClusterMap);

        Map<Integer, ForumPost> testPostsMap = new HashMap<>();
        testPostsMap.put(f1.getQuestionID(), f1);
        testPostsMap.put(f2.getQuestionID(), f2);
        forum.setPostsMap(testPostsMap);

        // Delete cluster and check if existing forum posts assigned to new single clusters
        forum.deleteCluster(c);

        Map<Integer, Cluster> result = forum.getClusters();
        assertEquals(2, result.size());
        assertEquals(90, result.get(90).getClusterID());
        assertEquals(91, result.get(91).getClusterID());
        assertTrue(result.get(90).getPostIDs().contains(90));
        assertTrue(result.get(91).getPostIDs().contains(91));
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

            ForumPost f = new ForumPost(65);
            f.setAuthor("author");
            i.getForumService().addForumPostToCluster(f,0);

            
        }catch (UserRegistrationException e){
            System.out.println("User is already registered in real db");
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    public ArrayList<ForumPost> getTestPosts(){
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
}
