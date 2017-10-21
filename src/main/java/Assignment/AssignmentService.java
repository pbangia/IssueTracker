package Assignment;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import models.Cluster;
import models.User;

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
		return true;
	}
	
}
