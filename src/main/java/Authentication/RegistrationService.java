package Authentication;

import com.mongodb.MongoClient;

import exceptions.InvalidUsernameException;
import exceptions.PasswordFormatException;
import exceptions.UserRegistrationException;
import models.UserRole;
import org.apache.commons.lang3.EnumUtils;
import models.User;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.regex.Pattern;

/**
 * A service which manages the registration of users.
 * Created by priyankitbangia on 15/10/17.
 */
public class RegistrationService {

    private Datastore datastore;

    public RegistrationService(MongoClient newConnection, Morphia dbMapper) {
        datastore = dbMapper.createDatastore(newConnection, "testdb");
    }

    /**
     * Register a user using a User object
     * @param user a user object that represents the user to register
     * @return the user that was created on registration
     */
    public User register(User user) {
        return register(user.getUsername(), user.getPassword(), user.getRole().toString());
    }

    /**
     * Register a user using the specified credentials.
     * @param username username of the user to register
     * @param password password of the user to register
     * @param role role of user to register
     * @return a user object which has been created on registration
     */
    public User register(String username, String password, String role) {
        validateDetails(username, password, role);
        User newUser = new User(username, password, UserRole.valueOf(role.toUpperCase()));
        datastore.save(newUser);
        return newUser;
    }

    /**
     * Check if the specified account credentials are valid
     * @param username username string to validate
     * @param password password string to validate
     * @param role specified role to validate
     */
    private void validateDetails(String username, String password, String role) {
        if (password.length() < 8) {
            throw new PasswordFormatException("Password needs to be at least 8 characters");
        }
        
        if (username.isEmpty()) {
            throw new InvalidUsernameException("User name can not be Empty");
        }

        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
        if (p.matcher(username).find()) {
            throw new InvalidUsernameException("Invalid Username");
        }

        if (findUser(username)!=null) {
            throw new InvalidUsernameException("Username already exists");
        }
        
        if (!EnumUtils.isValidEnum(UserRole.class, role)) {
            throw new UserRegistrationException("Role not supported");
        }
        
    }

    /**
     * DB wrapper method to be called when querying DB for user objects by ID.
     * @param id id of user object to retrieve
     * @return a User object that matches the specified ID
     */
    public User findUser(String id) {
        return datastore.find(User.class).field("_id").equal(id).get();
    }

}
