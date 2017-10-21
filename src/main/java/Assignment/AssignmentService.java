package Assignment;

import java.net.UnknownHostException;
import java.util.Set;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import exceptions.AdminCannotBeenAssignedException;
import exceptions.DeveloperAlreadyAssignedException;
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
	
	public boolean assignIssue(User assigner, Cluster cluster, User assignee) {
		if (!UserStatus.LOGIN.equals(assigner.getStatus())) {
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
	
}