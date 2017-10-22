package Assignment;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
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
import exceptions.PermissionDeniedException;
import models.Cluster;
import models.User;
import models.UserRole;
import models.UserStatus;

public class UnassginmentTest {
	private User u;
	private AssignmentService assign;
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
        when(u.getStatus()).thenReturn(UserStatus.LOGIN);
    }
	
	@Test
    public void administratorCanUnassignDeveloperFromAnAssignedIssue() {
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer1");
    	
    	//mock an issue
    	Cluster c = spy(new Cluster(0));
    	Set<String> assignees = new HashSet<String>(Arrays.asList("developer1","developer2"));
    	doReturn(assignees).when(c).getAssigneeIDs();
  
    	assertTrue(assign.unassignIssue(u,0,"developer1"));  	
    	assertTrue(c.getAssigneeIDs().size() == 1);
    	assertTrue(c.getAssigneeIDs().contains("developer2"));
    	
	}
	
	@Test
    public void administratorCannnotUnassignDeveloperWhoisNotAssignedToIssue() {
    	//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer1");
    	
    	//mock an issue
    	Cluster c = spy(new Cluster(0));
    	Set<String> assignees = new HashSet<String>(Arrays.asList("developer2"));
    	doReturn(assignees).when(c).getAssigneeIDs();
		
    	exception.expect(DeveloperNotAssignedException.class);
        exception.expectMessage("The developer has not been assigned to the issue");
    	
		assign.unassignIssue(u,0,"developer1");
		
	}
	
	@Test
    public void developerCannotPerformUnassignment() {
		//mock a developer
    	User developer = mock(User.class);
    	when(developer.getRole()).thenReturn(UserRole.DEVELOPER);
    	when(developer.getUsername()).thenReturn("developer2");
		
    	//mock an issue
    	Cluster c = spy(new Cluster(0));
    	Set<String> assignees = new HashSet<String>(Arrays.asList("developer3"));
    	doReturn(assignees).when(c).getAssigneeIDs();
		
		when(u.getRole()).thenReturn(UserRole.DEVELOPER);
		
		exception.expect(PermissionDeniedException.class);
        exception.expectMessage("You do not have the permission to perform this operation");
		
		assign.unassignIssue(u,0,"developer3");
	}
}
