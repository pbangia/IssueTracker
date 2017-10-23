package Assignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import app.IssueTracker;
import exceptions.AdminCannotBeenAssignedException;
import exceptions.DeveloperAlreadyAssignedException;
import exceptions.IssueAlreadyClosedException;
import exceptions.PermissionDeniedException;
import models.Cluster;
import models.Cluster.IssueStatus;
import models.User;
import models.UserRole;
import models.UserStatus;

public class AssignmentTest {
	private User u;
    private AssignmentService assign;

    Set<Integer> postIDs = new HashSet<>();
    IssueTracker issueTracker;
    Datastore ds;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Before
    public void setUpUserAuthenticationMockObjects() throws UnknownHostException{
    	MongoClient connection = mock(MongoClient.class);
        Morphia morphia = mock(Morphia.class);
        ds = mock(Datastore.class);

        when(morphia.createDatastore(any(MongoClient.class),anyString())).thenReturn(ds);

        issueTracker = new IssueTracker(connection, morphia);
        assign = Mockito.spy(issueTracker.getAssignmentService());
        
        u = mock(User.class);
        when(u.getRole()).thenReturn(UserRole.ADMIN);
        when(u.getStatus()).thenReturn(UserStatus.LOGGED_IN);
    }
    
    @Test
    public void administratorCanAssignDeveloperToAnOpenIssue() {
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer1");
    	doReturn(developer).when(assign).findUser("developer1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	doReturn(c).when(assign).findCluster(0);
    	
    	assertTrue(assign.assignIssue(u, 0, "developer1"));
    	List<String> assignees = new ArrayList<String>(c.getAssigneeIDs());
    	assertEquals(assignees.get(0), "developer1");
    	verify(ds).save(c);
    }
    
    @Test
    public void administratorCanAssignDeveloperToAnInProgressIssue() {
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer1");
    	doReturn(developer).when(assign).findUser("developer1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);
    	doReturn(c).when(assign).findCluster(0);
    	
    	assertTrue(assign.assignIssue(u, 0, "developer1"));
    	List<String> assignees = new ArrayList<String>(c.getAssigneeIDs());
    	assertEquals(assignees.get(0), "developer1");
    	verify(ds).save(c);
    }
    
    @Test
    public void administratorCannotAssignDeveloperToClosedIssue() {
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer1");
    	doReturn(developer).when(assign).findUser("developer1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.CLOSED);
    	doReturn(c).when(assign).findCluster(0);
    	
    	exception.expect(IssueAlreadyClosedException.class);
        exception.expectMessage("The issue has already been closed");
    	
        assign.assignIssue(u, 0, "developer1");
    }
    
    @Test
    public void developerCannotAssignDeveloperToAnIssue() {
    	when(u.getRole()).thenReturn(UserRole.DEVELOPER);
    	
    	//mock a developer
    	User developer = mock(User.class);
    	doReturn(developer).when(assign).findUser("developer1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	doReturn(c).when(assign).findCluster(0);
    	
    	exception.expect(PermissionDeniedException.class);
        exception.expectMessage("You do not have the permission to perform this operation");
    	
        assign.assignIssue(u, 0, "developer1");
    }
    
    @Test
    public void adminstratorCannotAssignDeveloperAlreadyAssignedToTheSameIssue() {   	
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer1");
    	doReturn(developer).when(assign).findUser("developer1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);
    	doReturn(new HashSet<String>(Arrays.asList(developer.getUsername()))).when(c).getAssigneeIDs();
    	doReturn(c).when(assign).findCluster(0);
    		   	
	    exception.expect(DeveloperAlreadyAssignedException.class);
        exception.expectMessage("The developer has already been assigned to the issue");
    	
    	assign.assignIssue(u, 0, "developer1");
    }
    
    @Test
    public void adminstratorCannotBeAssignedToAnIssue() {   	
    	//mock a developer
    	User admin = mock(User.class);
    	when(admin.getRole()).thenReturn(UserRole.ADMIN);
    	when(admin.getUsername()).thenReturn("admin1");
    	doReturn(admin).when(assign).findUser("admin1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	doReturn(c).when(assign).findCluster(0);
    	
    	exception.expect(AdminCannotBeenAssignedException.class);
        exception.expectMessage("An administrator cannot been assigned to an issue");
    	
    	assign.assignIssue(u, 0, "admin1");
    }
}
