package Authentication;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import exceptions.PasswordFormatException;
import exceptions.UserRegistrationException;
import models.UserRole;
import models.UserStatus;

import org.apache.commons.lang3.EnumUtils;
import models.User;

import java.net.UnknownHostException;

/**
 * Created by priyankitbangia on 17/10/17.
 */
public class LoginService {

    private MongoClient connection;
    private DB db;
    private DBCollection dbCollection;

    public LoginService() throws UnknownHostException {
        //connect to mongodb
        this(new MongoClient("localhost", 27017));
    }

    public LoginService(MongoClient newConnection) {
        //connect to mongodb
        connection = newConnection;

        //get db
        db = this.connection.getDB("testdb");

        //get collection from db
        dbCollection = db.getCollection("users");

    }
    
    public boolean login(String username, String password) {
    	return true;
    }
    
    public UserStatus checkStatus(String username) {
    	return UserStatus.LOGIN;
    }
}
