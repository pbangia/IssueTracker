package Assignment;

import java.net.UnknownHostException;
import java.util.Set;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import exceptions.AdminCannotBeenAssignedException;
import exceptions.DeveloperAlreadyAssignedException;
import exceptions.DeveloperNotAssignedException;
import exceptions.IssueAlreadyClosedException;
import exceptions.PermissionDeniedException;
import models.Cluster;
import models.Cluster.IssueStatus;
import models.User;
import models.UserRole;
import models.UserStatus;

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
	
	public boolean assignIssue(User assigner, int clusterID, String assigneeID) {
		User assignee = findUser(assigneeID);
		Cluster cluster = findCluster(clusterID);
		
		if (!UserStatus.LOGGED_IN.equals(assigner.getStatus())) {
			return false;
		}
		
		if (!UserRole.ADMIN.equals(assigner.getRole())) {
			throw new PermissionDeniedException("You do not have the permission to perform this operation");
		}
		
		if (!UserRole.DEVELOPER.equals(assignee.getRole())) {
			throw new AdminCannotBeenAssignedException("An administrator cannot been assigned to an issue");
		}
		
		if (IssueStatus.CLOSED.equals(cluster.getStatus())) {
			throw new IssueAlreadyClosedException("The issue has already been closed");
		}
		
		Set<String> assignees = cluster.getAssigneeIDs();
		if (!assignees.contains(assignee.getUsername())) {
			assignees.add(assignee.getUsername());
			ds.save(cluster);
			return true;
		} else {
			throw new DeveloperAlreadyAssignedException("The developer has already been assigned to the issue");
		}
	}
	
    public boolean unassignIssue(User currentUser, int clusterID, String assigneeID) {
		Cluster cluster = findCluster(clusterID);
		
		if (!UserStatus.LOGGED_IN.equals(currentUser.getStatus())) {
			return false;
		}
		
		if (!UserRole.ADMIN.equals(currentUser.getRole())) {
			throw new PermissionDeniedException("You do not have the permission to perform this operation");
		}
		
		Set<String> assignees = cluster.getAssigneeIDs();
		if (!assignees.contains(assigneeID)){
			throw new DeveloperNotAssignedException("The developer has not been assigned to the issue");
		}
		
		assignees.remove(assigneeID);
		ds.save(cluster);
		return true;
	}
	
	public User findUser(String username) {
		return ds.find(User.class).field("_id").equal(username).get();
	}
	
	public Cluster findCluster(int id) {
		return ds.find(Cluster.class).field("_id").equal(id).get();
	}
	
}
