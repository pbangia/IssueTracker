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
import org.mockito.ArgumentCaptor;
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
    private ForumService forum;
    IssueTracker issueTracker;

    private String CLUSTER_ID = "53200";
    private String INCORRECT_CLUSTER_ID = "999";
    private String AUTHOR1 = "author 1";
    private String AUTHOR2 = "author 2";
    private String AUTHOR3 = "author 3";
    private String AUTHOR4 = "author 4";
    private String AUTHOR5 = "author 5";

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
        doNothing().when(forum).setClusterText();

        //perform expected mapping on test input
        doReturn(10).when(eval).getNumClusters();
        doReturn(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 , 8.0, 9.0, 0.0}).when(eval).getClusterAssignments();
        doReturn(getTestPosts()).when(forum).getAllPosts();
        forum.postsList = forum.getAllPosts();

        List<HashSet<Integer>> expectedIDs = getExpectedIDs();
        Map<String, Cluster> clusters = forum.getRelatedIssues();
        Cluster[] clusterIndexes = forum.clusterIndexes;

        //Check equal number of clusters
        assertEquals(expectedIDs.size(), clusters.size());

        //check grouped forum post IDs within each cluster
        for (int i=0; i<clusters.size(); i++){
            Set<Integer> actual = clusterIndexes[i].getPostIDs();
            Set<Integer> expected = expectedIDs.get(i);
            assertEquals(expected.size(), actual.size());
            assertTrue(actual.containsAll(expected));
        }

    }

    @Test
    public void calculateCorrectNumberOfUniqueUsersAffectedForACluster(){
        Cluster c = new Cluster(CLUSTER_ID);
        c.addForumPost(1000, AUTHOR1); //Account for multiple posts by same author
        c.addForumPost(1001, AUTHOR1);
        c.addForumPost(1002, AUTHOR2);
        c.addForumPost(1003, AUTHOR3);
        c.addForumPost(1004, AUTHOR4);

        doReturn(c).when(forum).getCluster(CLUSTER_ID);

        Cluster returned = forum.getCluster(CLUSTER_ID);
        assertEquals(4, returned.getNumAffectedUsers());
    }

    @Test
    public void calculateCorrectNumberOfPostsInACluster(){
        Cluster c = new Cluster(CLUSTER_ID);
        c.addForumPost(1000, AUTHOR1); //Account for multiple posts by same author
        c.addForumPost(1001, AUTHOR1);
        c.addForumPost(1002, AUTHOR2);
        c.addForumPost(1003, AUTHOR3);
        c.addForumPost(1004, AUTHOR4);

        doReturn(c).when(forum).getCluster(CLUSTER_ID);

        Cluster returned = forum.getCluster(CLUSTER_ID);
        assertEquals(5, returned.getNumPosts());
    }

    @Test
    public void shouldThrowInvalidAuthStateExceptionWhenAddingPostToClusterIfNotAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(DEVELOPER);

        exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("Only admins have permission to add clusters");

        Cluster c = spy(new Cluster(CLUSTER_ID));
        doReturn(c).when(forum).getCluster(CLUSTER_ID);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn(AUTHOR1);
        when(f.getQuestionID()).thenReturn(90);

        forum.addForumPostToCluster(f, CLUSTER_ID);
    }

    @Test
    public void postIsAddedCorrectlyToClusterWhenPerformingAddAsAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        Cluster c = spy(new Cluster(CLUSTER_ID));
        doReturn(c).when(forum).getCluster(CLUSTER_ID);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn(AUTHOR1);
        when(f.getQuestionID()).thenReturn(90);
        when(f.getClusterID()).thenReturn(null);
        doReturn(c).when(forum).getCluster("90");

        forum.addForumPostToCluster(f, CLUSTER_ID);
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
        when(f.getClusterID()).thenReturn(CLUSTER_ID);

        exception.expect(AssignmentException.class);
        exception.expectMessage("Forum post is already assigned to a cluster");

        //Add to different cluster
        forum.addForumPostToCluster(f, INCORRECT_CLUSTER_ID);
    }

    @Test
    public void shouldThrowInvalidAuthStateExceptionWhenRemovingPostFromClusterIfNotAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(DEVELOPER);

        exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("Only admins have permission to remove clusters");

        Cluster c = spy(new Cluster(CLUSTER_ID));
        doReturn(c).when(forum).getCluster(CLUSTER_ID);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn(AUTHOR1);
        when(f.getQuestionID()).thenReturn(90);
        when(f.getClusterID()).thenReturn(CLUSTER_ID);

        forum.removeForumPostFromCluster(f);
    }

    @Test
    public void postIsRemovedCorrectlyFromClusterWhenPerformingRemoveAsAdmin(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        //Initialise post data to remove from cluster
        Cluster c = spy(new Cluster(CLUSTER_ID));
        doReturn(c).when(forum).getCluster(CLUSTER_ID);
        ForumPost f = mock(ForumPost.class);
        when(f.getAuthor()).thenReturn(AUTHOR1);
        when(f.getQuestionID()).thenReturn(90);
        when(f.getClusterID()).thenReturn(CLUSTER_ID);
        c.setNumAffectedUsers(2);
        c.setNumPosts(2);

        c.setUsersAffected(new ArrayList<>(Arrays.asList(f.getAuthor(), AUTHOR2)));
        c.setPostIDs(new HashSet<>(Arrays.asList(f.getQuestionID(),91)));
        doReturn(c).when(forum).getCluster(CLUSTER_ID);

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
        when(f.getAuthor()).thenReturn(AUTHOR1);
        when(f.getQuestionID()).thenReturn(90);
        when(f.getClusterID()).thenReturn(null);

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
        when(c.getClusterID()).thenReturn(null);
        forum.setClusters(new HashMap<>());

        forum.deleteCluster(c);
    }



    @Test
    public void forumPostsClusteredIndividuallyAfterDeletingCluster(){
        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        // Initialise cluster with 2 fake posts inside, with ids=90 and 91
        Cluster clusterToDelete = initialiseClusterWithMockPosts();
        Map<String, Cluster> testClusterMap = new HashMap<>();
        testClusterMap.put(clusterToDelete.getClusterID(), clusterToDelete);

        //set forum service to use this mocked data
        forum.setClusters(spy(testClusterMap));

        // Delete cluster and check if existing forum posts assigned to new single clusters
        forum.deleteCluster(clusterToDelete);

        //Check 2 new clusters have been made, record ids
        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        verify(forum.clusters, times(2)).put(argCaptor.capture(), any(Cluster.class));

        //Use recorded ids to get new clusters, check if they contain post 90 and 91
        Map<String, Cluster> result = forum.getClusters();
        assertEquals(2, result.size());
        String firstClusterId = argCaptor.getAllValues().get(0);
        String secondClusterId = argCaptor.getAllValues().get(1);
        assertTrue(result.get(firstClusterId).getPostIDs().contains(90));
        assertTrue(result.get(secondClusterId).getPostIDs().contains(91));
    }

    private Cluster initialiseClusterWithMockPosts(){
        //Initialise cluster with forum posts
        Cluster c = spy(new Cluster(CLUSTER_ID));
        doReturn(c).when(forum).getCluster(CLUSTER_ID);
        ForumPost f1 = mock(ForumPost.class);
        when(f1.getAuthor()).thenReturn(AUTHOR1);
        when(f1.getQuestionID()).thenReturn(90);
        when(f1.getClusterID()).thenReturn(CLUSTER_ID);
        ForumPost f2 = mock(ForumPost.class);
        when(f2.getAuthor()).thenReturn(AUTHOR2);
        when(f2.getQuestionID()).thenReturn(91);
        when(f2.getClusterID()).thenReturn(CLUSTER_ID);
        Set<Integer> testPostIds = new HashSet<>(Arrays.asList(f1.getQuestionID(), f2.getQuestionID()));
        when(c.getPostIDs()).thenReturn(testPostIds);

        Map<Integer, ForumPost> testPostsMap = new HashMap<>();
        testPostsMap.put(f1.getQuestionID(), f1);
        testPostsMap.put(f2.getQuestionID(), f2);
        forum.setPostsMap(testPostsMap);

        return c;
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

    /**
     * Test to run real environment. Ensures implementation works in production and data
     * persists. Requires setup of a Mongo server connected to local host.
     * Follow instructions below to examine entries made in the MongoDB.
     */
    @Ignore
    @Test
    public void testRealDatabaseWithClusterPersistence(){

        try {
            IssueTracker issueTracker = new IssueTracker();
            issueTracker.getDatastore().getCollection(User.class).drop(); //clear db to be able to run sample test
            issueTracker.getDatastore().getCollection(ForumPost.class).drop();
            issueTracker.getDatastore().getCollection(Cluster.class).drop();

            // Authenticate
            issueTracker.register("user1","password", ADMIN);
            issueTracker.register("user2","password", DEVELOPER);
            issueTracker.login("user1","password");

            // Load cluster info
            ForumService forum = issueTracker.getForumService();
            Map<String, Cluster> clusters = forum.getRelatedIssues();

            // Use cluster info
            User user = issueTracker.getLoginService().getCurrentUser();
            for (Cluster c: clusters.values()) {
                issueTracker.getAssignmentService().assignIssue(user,c.getClusterID(),"user2");
                System.out.println("Cluster ID: " + c.getClusterID());
            }

            /* Check mongo db server entries. execute the commands in the server shell:
             * 1. use testdb
             * 2. db.users.find()       -> lists all users persisted in db
             * 3. db.clusters.find()    -> lists all clusters persisted in db
             * 4. db.forumposts.find()  -> lists all forum posts persisted in db
             *
             * Querying clusters by ID example: db.clusters.find( "_id" : "<INSERT ID>" )
             * Querying forum posts by assigned Cluster ID example: db.forumposts.find( "clusterID" : "<INSERT CID>" )
             */

        }catch (UserRegistrationException e){
            System.out.println("User is already registered in real db");
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }
}
