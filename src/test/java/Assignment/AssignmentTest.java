package Assignment;

import static models.Cluster.IssueStatus.CLOSED;
import static models.Cluster.IssueStatus.IN_PROGRESS;
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

import exceptions.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import app.IssueTracker;
import models.Cluster;
import models.Cluster.IssueStatus;
import models.User;
import models.UserRole;
import models.UserStatus;

public class AssignmentTest {

	private User u;
    private AssignmentService assign;
    private IssueTracker issueTracker;
    private Datastore ds;

    private String DEVELOPER1 = "developer1";
    private String DEVELOPER2 = "developer2";
    private String ADMIN = "admin";

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
    public void issueStatusSetToInProgressWhenAssigningDeveloperToOpenIssue() {
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn(DEVELOPER1);
    	doReturn(developer).when(assign).findUser(DEVELOPER1);

    	Cluster c = spy(new Cluster("0"));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	doReturn(c).when(assign).findCluster("0");
    	
    	assertTrue(assign.assignIssue(u, "0", DEVELOPER1));
    	List<String> assignees = new ArrayList<String>(c.getAssigneeIDs());
    	assertEquals(assignees.get(0), DEVELOPER1);
    	verify(ds).save(c);
    	verify(c).setStatus(IN_PROGRESS);
    }
    
    @Test
    public void shouldThrowAssignmentExceptionWhenAssigningDeveloperToClosedIssue() {
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn(DEVELOPER1);
    	doReturn(developer).when(assign).findUser(DEVELOPER1);
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster("0"));
    	when(c.getStatus()).thenReturn(CLOSED);
    	doReturn(c).when(assign).findCluster("0");
    	
    	exception.expect(ClusterException.class);
        exception.expectMessage("The issue has already been closed");
    	
        assign.assignIssue(u, "0", DEVELOPER1);
    }
    
    @Test
    public void shouldThrowAssignmentExceptionWhenAssigningAnIssueAsDeveloper() {
    	when(u.getRole()).thenReturn(UserRole.DEVELOPER);
    	
    	//mock a developer
    	User developer = mock(User.class);
    	doReturn(developer).when(assign).findUser(DEVELOPER1);
    	
    	//mock an open issue
    	Cluster c = spy(new Cluster("0"));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	doReturn(c).when(assign).findCluster("0");
    	
    	exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("Developers cannot assign issues to other users");
    	
        assign.assignIssue(u, "0", DEVELOPER1);
    }
    
    @Test
    public void shouldThrowAssignmentExceptionWhenAssigningIssueToDeveloperThatIsAlreadyAssignedToThatIssue() {
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn(DEVELOPER1);
    	doReturn(developer).when(assign).findUser(DEVELOPER1);

    	Cluster c = spy(new Cluster("0"));
    	when(c.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);
    	doReturn(new HashSet<>(Arrays.asList(developer.getUsername()))).when(c).getAssigneeIDs();
    	doReturn(c).when(assign).findCluster("0");
    		   	
	    exception.expect(AssignmentException.class);
        exception.expectMessage("The developer has already been assigned to the issue");
    	
    	assign.assignIssue(u, "0", DEVELOPER1);
    }
    
    @Test
    public void shouldThrowAssignmentExceptionWhenAssigningAdministratorToIssue() {
    	when(u.getRole()).thenReturn(UserRole.ADMIN);
    	when(u.getUsername()).thenReturn(ADMIN);
    	doReturn(u).when(assign).findUser(ADMIN);

    	Cluster c = spy(new Cluster("0"));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	doReturn(c).when(assign).findCluster("0");
    	
    	exception.expect(AssignmentException.class);
        exception.expectMessage("An administrator cannot been assigned to an issue");
    	
    	assign.assignIssue(u, "0", ADMIN);
    }
    
    
    @Test
    public void issueStatusIsMarkedResolvedWhenResolvingIssueAsDeveloper(){
    	when(u.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(u.getUsername()).thenReturn(DEVELOPER1);

    	Cluster c = spy(new Cluster("1000"));
    	when(c.getStatus()).thenReturn(IssueStatus.OPEN);
    	doReturn(c).when(assign).findCluster("1000");

    	assign.resolveIssue(u, "1000");
    	verify(c).setStatus(CLOSED);
    }
    
    
    @Test
    public void shouldThrowInvalidAuthStateExceptionWhenClosingIssueAsAdmin(){
    	when(u.getRole()).thenReturn(UserRole.ADMIN);
    	
    	exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("Only Developers have the permission to perform this operation");
        assign.resolveIssue(u, "1000");
    }

    
    @Test
    public void shouldThrowClusterExceptionWhenMarkingIssueAsClosedIfAlreadyMarkedAsClosed(){
		when(u.getRole()).thenReturn(UserRole.DEVELOPER);
		Cluster c = Mockito.spy(new Cluster("1000"));
		doReturn(c).when(assign).findCluster("1000");
		when(c.getStatus()).thenReturn(CLOSED);

		exception.expect(ClusterException.class);
		exception.expectMessage("Issue already marked as Closed");
		assign.resolveIssue(u, "1000");
    }

	@Test
	public void administratorCanUnassignDeveloperFromAnIssueTheyAreAssignedTo() {
		User developer = mock(User.class);
		when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
		when(developer.getUsername()).thenReturn(DEVELOPER1);
		doReturn(developer).when(assign).findUser(DEVELOPER1);

		Cluster c = spy(new Cluster("0"));
		Set<String> assignees = new HashSet<>(Arrays.asList(DEVELOPER1,DEVELOPER2));
		doReturn(assignees).when(c).getAssigneeIDs();
		doReturn(c).when(assign).findCluster("0");

		assertTrue(assign.unassignIssue(u,"0",DEVELOPER1));
		assertTrue(c.getAssigneeIDs().size() == 1);
		assertTrue(c.getAssigneeIDs().contains(DEVELOPER2));

	}

	@Test
	public void administratorCannotUnassignDeveloperWhoisNotAssignedToIssue() {
		User developer = mock(User.class);
		when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
		when(developer.getUsername()).thenReturn(DEVELOPER1);
		doReturn(developer).when(assign).findUser(DEVELOPER1);

		Cluster c = spy(new Cluster("0"));
		Set<String> assignees = new HashSet<>(Arrays.asList(DEVELOPER2));
		doReturn(assignees).when(c).getAssigneeIDs();
		doReturn(c).when(assign).findCluster("0");

		exception.expect(AssignmentException.class);
		exception.expectMessage("The developer has not been assigned to the issue");

		assign.unassignIssue(u,"0",DEVELOPER1);

	}

	@Test
	public void shouldThrowInvalidAuthStateExceptionWhenDeveloperAttemptsToPerformIssueUnassignment() {
		User developer = mock(User.class);
		when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
		when(developer.getUsername()).thenReturn(DEVELOPER2);
		doReturn(developer).when(assign).findUser(DEVELOPER2);

		Cluster c = spy(new Cluster("0"));
		Set<String> assignees = new HashSet<>(Arrays.asList(DEVELOPER2));
		doReturn(assignees).when(c).getAssigneeIDs();
		doReturn(c).when(assign).findCluster("0");

		when(u.getRole()).thenReturn(UserRole.DEVELOPER);

		exception.expect(InvalidAuthStateException.class);
		exception.expectMessage("You do not have the permission to perform this operation");

		assign.unassignIssue(u,"0",DEVELOPER2);
	}
}
