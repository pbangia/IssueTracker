package Authentication;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;

import exceptions.InvalidUsernameException;
import exceptions.PasswordMismatchException;
import models.User;
import models.UserStatus;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import static models.UserStatus.LOGGED_IN;
import static models.UserStatus.LOGGED_OUT;

/**
 * Created by priyankitbangia on 17/10/17.
 */
public class LoginService {

	private MongoClient connection;
	private Datastore ds;
	private User currentUser;

	public LoginService() throws UnknownHostException {
		// connect to mongodb
		this(new MongoClient("localhost", 27017), new Morphia());
	}

	public LoginService(MongoClient newConnection, Morphia dbMapper) {
		connection = newConnection;
		ds = dbMapper.createDatastore(connection, "testdb");
	}

	public boolean login(String username, String password) {
    	User user = getUser(username);

		if (!password.equals(user.getPassword())) {
    		throw new PasswordMismatchException("Password is incorrect");
    	}

    	user.setStatus(LOGGED_IN);
		ds.save(user);
		currentUser = user;
		return true;
    }
	
	public boolean logout(String username) {
		User user = getUser(username);
		if (LOGGED_IN.equals(user.getStatus())) {
			user.setStatus(LOGGED_OUT);
			ds.save(user);
			currentUser = null;
			return true;
		}
		return false;
	}

	public UserStatus checkStatus(String username) {
		User user = getUser(username);
		return user.getStatus();
	}
	
	private User getUser(String username) {
		User user1 = findUser(username);
		if (user1 == null) throw new InvalidUsernameException("Username not exists");
    	return user1;
	}

	public User findUser(String username) {
		return ds.find(User.class).field("_id").equal(username).get();
	}

	public User getCurrentUser() {
		return currentUser;
	}
}
