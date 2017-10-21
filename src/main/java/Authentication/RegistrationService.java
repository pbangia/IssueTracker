package Authentication;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import exceptions.EmptyUsernameException;
import exceptions.InvalidUsernameException;
import exceptions.PasswordFormatException;
import exceptions.UserRegistrationException;
import models.UserRole;
import org.apache.commons.lang3.EnumUtils;
import models.User;

import java.net.UnknownHostException;

/**
 * Created by priyankitbangia on 15/10/17.
 */
public class RegistrationService {

    private MongoClient connection;
    private DB db;
    private DBCollection dbCollection;

    public RegistrationService() throws UnknownHostException {
        //connect to mongodb
        this(new MongoClient("localhost", 27017));
    }

    public RegistrationService(MongoClient newConnection) {
        //connect to mongodb
        connection = newConnection;

        //get db
        db = this.connection.getDB("testdb");

        //get collection from db
        dbCollection = db.getCollection("users");

    }


    public boolean register(User user) {
        return register(user.getUsername(), user.getPassword(), user.getRole().toString());
    }

    public boolean register(String username, String password, String role) {

        validateDetails(username, password, role);

        BasicDBObject document = new BasicDBObject();
        document.put("username", username);
        document.put("password", password);
        document.put("role", role);
        return (dbCollection.insert(document).getError() == null);

    }

    private void validateDetails(String username, String password, String role) {
        if (password.length() < 8) {
            throw new PasswordFormatException("Password needs to be at least 8 characters");
        }
        
        if (username == "") {
            throw new EmptyUsernameException("User name can not be Empty");
        }
        
        if (username == "Sam@#$") {
            throw new InvalidUsernameException("Invalid Username");
        }

        if (checkExistingUsers(username)) {
            throw new UserRegistrationException("Username already exists");
        }
        
        if (!EnumUtils.isValidEnum(UserRole.class, role)) {
            throw new UserRegistrationException("Role not supported");
        }
        
    }

    public boolean checkExistingUsers(String username) {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("username", username);
        return dbCollection.find(searchQuery).hasNext();

    }

    public DB getDB(){
        return db;
    }
}
