package Authentication;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import exceptions.PasswordMismatchException;
import exceptions.UsernameNotExistException;
import models.UserStatus;

/**
 * Created by priyankitbangia on 17/10/17.
 */
public class LoginService {

	private MongoClient connection;
	private DB db;
	private DBCollection dbCollection;

	public LoginService() throws UnknownHostException {
		// connect to mongodb
		this(new MongoClient("localhost", 27017));
	}

	public LoginService(MongoClient newConnection) {
		// connect to mongodb
		connection = newConnection;

		// get db
		db = this.connection.getDB("testdb");

		// get collection from db
		dbCollection = db.getCollection("users");

	}

	public boolean login(String username, String password) {
		DBObject document = getUser(username);  	
    	if (!password.equals(document.get("password"))) {
    		throw new PasswordMismatchException("Password is incorrect");
    	}
    	
    	document.put("status", UserStatus.LOGIN);
    	return (dbCollection.insert(document).getError() == null);
    }
	
	public boolean logout(String username) {
		return true;
	}

	public UserStatus checkStatus(String username) {
		DBObject document = getUser(username);
		return (UserStatus) document.get("status");
	}
	
	private DBObject getUser(String username) {
		BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("username", username);
        DBCursor cursor = dbCollection.find(searchQuery);
    	
    	if (!cursor.hasNext()) {
    		throw new UsernameNotExistException("Username not exists");
    	}
    	
    	return cursor.next();
	}
}
