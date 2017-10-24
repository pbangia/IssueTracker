package Clustering;

import Authentication.LoginService;
import app.IssueTracker;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import models.Cluster;
import models.ClusterSortBy;
import models.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.net.UnknownHostException;
import java.util.*;

import static models.UserRole.ADMIN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

/**
 * Created by g.tiongco on 23/10/17.
 */
public class ClusterSortingTest {

    private User u;
    private ForumService forum;
    private IssueTracker issueTracker;
    private ArrayList<Cluster> testClusterList;
    private Cluster c1;
    private Cluster c2;
    private Cluster c3;


    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUpUserAuthenticationMockObjects() throws UnknownHostException {
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

        when(forum.getAccessPrivilege()).thenReturn(ADMIN);

        c1 = spy(new Cluster("1000"));
        c2 = spy(new Cluster("2000"));
        c3 = spy(new Cluster("3000"));

        when(c1.getNumPosts()).thenReturn(1);
        when(c2.getNumPosts()).thenReturn(2);
        when(c3.getNumPosts()).thenReturn(3);

        when(c1.getNumAffectedUsers()).thenReturn(1);
        when(c2.getNumAffectedUsers()).thenReturn(2);
        when(c3.getNumAffectedUsers()).thenReturn(3);

        when(c1.getTitle()).thenReturn("aaa");
        when(c2.getTitle()).thenReturn("bbb");
        when(c3.getTitle()).thenReturn("ccc");

        testClusterList = new ArrayList<>(Arrays.asList(c1, c2, c3));
        doReturn(testClusterList).when(forum).getClustersAsList();

    }

    @Test
    public void shouldSortIssuesInDescendingOrderBasedOnNumberOfPosts(){
        List<Cluster> sortedClusters = forum.getSortedClusters(ClusterSortBy.NUMPOSTS, false);

        assertEquals(sortedClusters.get(0), c3);
        assertEquals(sortedClusters.get(1), c2);
        assertEquals(sortedClusters.get(2), c1);
    }

    @Test
    public void shouldSortIssuesInAscendingOrderBasedOnNumberOfPosts(){
        List<Cluster> sortedClusters = forum.getSortedClusters(ClusterSortBy.NUMPOSTS, true);

        assertEquals(sortedClusters.get(0), c1);
        assertEquals(sortedClusters.get(1), c2);
        assertEquals(sortedClusters.get(2), c3);
    }

    @Test
    public void shouldSortIssuesInDescendingOrderBasedOnNumberOfAffectedUsers(){
        List<Cluster> sortedClusters = forum.getSortedClusters(ClusterSortBy.NUM_AFFECTED_USERS, false);

        assertEquals(sortedClusters.get(0), c3);
        assertEquals(sortedClusters.get(1), c2);
        assertEquals(sortedClusters.get(2), c1);
    }

    @Test
    public void shouldSortIssuesInAscendingOrderBasedOnNumberOfAffectedUsers(){
        List<Cluster> sortedClusters = forum.getSortedClusters(ClusterSortBy.NUM_AFFECTED_USERS, true);

        assertEquals(sortedClusters.get(0), c1);
        assertEquals(sortedClusters.get(1), c2);
        assertEquals(sortedClusters.get(2), c3);
    }

    @Test
    public void shouldSortIssuesInDescendingOrderBasedOnTitle(){
        List<Cluster> sortedClusters = forum.getSortedClusters(ClusterSortBy.TITLE, false);

        assertEquals(sortedClusters.get(0), c3);
        assertEquals(sortedClusters.get(1), c2);
        assertEquals(sortedClusters.get(2), c1);
    }

    @Test
    public void shouldSortIssuesInAscendingOrderBasedOnTitle(){
        List<Cluster> sortedClusters = forum.getSortedClusters(ClusterSortBy.TITLE, true);

        assertEquals(sortedClusters.get(0), c1);
        assertEquals(sortedClusters.get(1), c2);
        assertEquals(sortedClusters.get(2), c3);
    }
}
