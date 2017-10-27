package Assignment;

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

/**
 * A service which manages the assignment of Issues between admins and developers.
 */
public class AssignmentService {
	private Datastore datastore;

	public AssignmentService(MongoClient newConnection, Morphia dbMapper) {
		datastore = dbMapper.createDatastore(newConnection, "testdb");
	}

	/**
	 * Handles the assigning of an issue to a developer if permissions allow.
	 * @param assigner User object of the users who is assigning another user
	 * @param clusterID string of the issue ID to assign to
	 * @param assigneeID string of the ID of the assignee
     * @return a boolean representing successful assignment
     */
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
			datastore.save(cluster);
			return true;
		} else {
			throw new AssignmentException("The developer has already been assigned to the issue");
		}
	}

    /**
	 * Handles the unassigning of a developer by an Admin from an issue.
	 * @param currentUser the current User object of the user that is logged in
	 * @param clusterID cluster ID of the cluster to be unassigned
	 * @param assigneeID the string ID of user to be unassigned
	 * @return a boolean representing successful unassignment
     */
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
		datastore.save(cluster);
		return true;
	}

    /**
	 * Handles the marking of an issue as resolved if permissions allow.
	 * @param currentUser current user object of the user that is signed in
	 * @param clusterID cluster ID of the cluster to be marked as resolved
	 * @return a boolean representing successful resolving
     */
    public boolean resolveIssue(User currentUser, String clusterID) {
    	if (UserRole.ADMIN == currentUser.getRole()) {
			throw new InvalidAuthStateException("Only Developers have the permission to perform this operation");
		}

		Cluster cluster = findCluster(clusterID);
		if (IssueStatus.CLOSED == cluster.getStatus()) {
			throw new ClusterException("Issue already marked as Closed");
		}
		
		cluster.setStatus(IssueStatus.CLOSED);
		datastore.save(cluster);
	
		return true;
	}

	/**
	 * DB wrapper method to be called when querying DB for user objects by ID.
	 * @param id id of user object to retrieve
	 * @return a User object that matches the specified ID
	 */
    public User findUser(String id) {
		return datastore.find(User.class).field("_id").equal(id).get();
	}

	/**
	 * DB wrapper method to be called when querying DB for cluster objects by ID.
	 * @param id id of Cluster object to retrieve
	 * @return a Cluster object that matches the specified ID
     */
    public Cluster findCluster(String id) {
		return datastore.find(Cluster.class).field("_id").equal(id).get();
	}
	
}
