package Assignment;

import java.net.UnknownHostException;
import java.util.Set;

import exceptions.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import models.Cluster;
import models.Cluster.IssueStatus;
import models.User;
import models.UserRole;
import models.UserStatus;

import static models.Cluster.IssueStatus.IN_PROGRESS;

public class AssignmentService {
	private MongoClient connection;
	private Datastore ds;
	
	public AssignmentService() throws UnknownHostException {
		// connect to mongodb
		this(new MongoClient("localhost", 27017), new Morphia());
	}

	public AssignmentService(MongoClient newConnection, Morphia dbMapper) {
		connection = newConnection;
		ds = dbMapper.createDatastore(connection, "testdb");
	}
	
	public boolean assignIssue(User assigner, String clusterID, String assigneeID) {
		User assignee = findUser(assigneeID);
		Cluster cluster = findCluster(clusterID);
		
		if (!UserStatus.LOGGED_IN.equals(assigner.getStatus())) {
			return false;
		}
		
		if (!UserRole.ADMIN.equals(assigner.getRole())) {
			throw new InvalidAuthStateException("Developers cannot assign issues to other users");
		}
		
		if (!UserRole.DEVELOPER.equals(assignee.getRole())) {
			throw new AssignmentException("An administrator cannot been assigned to an issue");
		}
		
		if (IssueStatus.CLOSED.equals(cluster.getStatus())) {
			throw new ClusterException("The issue has already been closed");
		}
		
		Set<String> assignees = cluster.getAssigneeIDs();
		if (!assignees.contains(assignee.getUsername())) {
			assignees.add(assignee.getUsername());
			cluster.setStatus(IN_PROGRESS);
			ds.save(cluster);
			return true;
		} else {
			throw new AssignmentException("The developer has already been assigned to the issue");
		}
	}
	
    public boolean unassignIssue(User currentUser, String clusterID, String assigneeID) {
		Cluster cluster = findCluster(clusterID);
		
		if (!UserStatus.LOGGED_IN.equals(currentUser.getStatus())) {
			return false;
		}
		
		if (!UserRole.ADMIN.equals(currentUser.getRole())) {
			throw new InvalidAuthStateException("You do not have the permission to perform this operation");
		}
		
		Set<String> assignees = cluster.getAssigneeIDs();
		if (!assignees.contains(assigneeID)){
			throw new AssignmentException("The developer has not been assigned to the issue");
		}
		
		assignees.remove(assigneeID);
		ds.save(cluster);
		return true;
	}
    
    public boolean resolveIssue(User currentUser, String clusterID) {
    	if (UserRole.ADMIN == currentUser.getRole()) {
			throw new InvalidAuthStateException("Only Developers have the permission to perform this operation");
		}

		Cluster cluster = findCluster(clusterID);
		if (IssueStatus.CLOSED == cluster.getStatus()) {
			throw new ClusterException("Issue already marked as Closed");
		}
		
		cluster.setStatus(IssueStatus.CLOSED);
		ds.save(cluster);
	
		return true;
	}
	
	public User findUser(String username) {
		return ds.find(User.class).field("_id").equal(username).get();
	}
	
	public Cluster findCluster(String id) {
		return ds.find(Cluster.class).field("_id").equal(id).get();
	}
	
}
