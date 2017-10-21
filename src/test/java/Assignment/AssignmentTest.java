package Assignment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import Authentication.LoginService;
import Clustering.ForumService;
import app.IssueTracker;
import models.Cluster;
import models.Cluster.IssueStatus;
import models.User;
import models.UserRole;
import models.UserStatus;

public class AssignmentTest {
	private User u;
    private AssignService assign;

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
        assign = Mockito.spy(issueTracker.getAssignService());
        
        u = spy(new User());
        when(u.getRole()).thenReturn(UserRole.ADMIN);
        when(u.getStatus()).thenReturn(UserStatus.LOGIN);
    }
    
    @Test
    public void administratorCanAssignDeveloperToAnOpenIssue() {
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	
    	assertTrue(assign.assignIssue(u, c, developer));
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
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);
    	
    	assertTrue(assign.assignIssue(u, c, developer));
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
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.CLOSED);
    	
    	assign.assignIssue(u, c, developer);
    	   	
    	exception.expect(IssueAlreadyClosedException.class);
        exception.expectMessage("The issue has already been closed");
    }
    
    @Test
    public void developerCannotAssignDeveloperToAnIssue() {
    	when(u.getRole()).thenReturn(UserRole.DEVELOPER);
    	
    	//mock a developer
    	User developer = mock(User.class);
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	
    	assign.assignIssue(u, c, developer);
    	   	
    	exception.expect(PermissionDeniedException.class);
        exception.expectMessage("You do not have the permission to perform this operation");
    }
    
    @Test
    public void adminstratorCannotAssignDeveloperAlreadyAssignedToTheSameIssue() {   	
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);
    	when(c.getAssigneeIDs()).thenReturn(new HashSet<String>(Arrays.asList(developer.getUsername())));
    	
    	assign.assignIssue(u, c, developer);
    	   	
    	exception.expect(DeveloperAlreadyAssignedException.class);
        exception.expectMessage("The developer has already been assigned to the issue");
    }
    
    @Test
    public void adminstratorCannotAssignDeveloperAlreadyAssignedToTheSameIssue() {   	
    	//mock a developer
    	User admin = mock(User.class);
    	when(admin.getRole()).thenReturn(UserRole.ADMIN);
    	when(admin.getUsername()).thenReturn("admin1");
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster(0));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	
    	assign.assignIssue(u, c, admin);
    	   	
    	exception.expect(AdminCannotBeenAssignedException.class);
        exception.expectMessage("An administrator cannot been assigned to an issue");
    }
}
