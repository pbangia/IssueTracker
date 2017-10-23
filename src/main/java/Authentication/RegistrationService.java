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

import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Created by priyankitbangia on 15/10/17.
 */
public class RegistrationService {

    private MongoClient connection;
    private Datastore ds;

    public RegistrationService() throws UnknownHostException {
        //connect to mongodb
        this(new MongoClient("localhost", 27017), new Morphia());
    }

    public RegistrationService(MongoClient newConnection, Morphia dbMapper) {
        //connect to mongodb
        connection = newConnection;
        ds = dbMapper.createDatastore(connection, "testdb");
    }

    public User register(User user) {
        return register(user.getUsername(), user.getPassword(), user.getRole().toString());
    }

    public User register(String username, String password, String role) {
        validateDetails(username, password, role);
        User newUser = new User(username, password, UserRole.valueOf(role.toUpperCase()));
        ds.save(newUser);
        return newUser;
    }

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

    public User findUser(String username) {
        return ds.find(User.class).field("_id").equal(username).get();
    }


    public Datastore getDataStore(){
        return ds;
    }
}
