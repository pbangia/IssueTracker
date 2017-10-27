package Authentication;

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
 * A service that manages the login state of the user using the system.
 * Created by priyankitbangia on 17/10/17.
 */
public class LoginService {
	private Datastore datastore;
	private User currentUser;

	public LoginService(MongoClient newConnection, Morphia dbMapper) {
		datastore = dbMapper.createDatastore(newConnection, "testdb");
	}

	/**
	 * Handles signing into the system for a specified username that exists and matches a
	 * valid password.
	 * @param username string of the username to sign in with
	 * @param password string of the user's password
     * @return a boolean representing succesful login
     */
    public boolean login(String username, String password) {
    	User user = getUser(username);

		if (!password.equals(user.getPassword())) {
    		throw new PasswordMismatchException("Password is incorrect");
    	}

    	user.setStatus(LOGGED_IN);
		datastore.save(user);
		currentUser = user;
		return true;
    }

    /**
	 * Signs out the specified user from the system.
	 * @param username the username of the user logging out
	 * @return a boolean representing successful logout
     */
	public boolean logout(String username) {
		User user = getUser(username);
		if (LOGGED_IN.equals(user.getStatus())) {
			user.setStatus(LOGGED_OUT);
			datastore.save(user);
			currentUser = null;
			return true;
		}
		return false;
	}

	/**
	 * Checks if the requested user object exists
	 * @param username
	 * @return the requested user object
     */
    private User getUser(String username) {
		User user1 = findUser(username);
		if (user1 == null) throw new InvalidUsernameException("Username not exists");
    	return user1;
	}

	/**
	 * DB wrapper method to be called when querying DB for user objects by ID.
	 * @param id id of user object to retrieve
	 * @return a User object that matches the specified ID
	 */
	public User findUser(String id) {
		return datastore.find(User.class).field("_id").equal(id).get();
	}

	public User getCurrentUser() {
		return currentUser;
	}
}
