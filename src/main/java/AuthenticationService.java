import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import exceptions.PasswordFormatException;

import java.net.UnknownHostException;

/**
 * Created by priyankitbangia on 15/10/17.
 */
public class AuthenticationService {

    private MongoClient connection;
    private DB db;
    private DBCollection dbCollection;

    public AuthenticationService() throws UnknownHostException {
        //connect to mongodb
        this(new MongoClient("localhost", 27017));
    }

    public AuthenticationService(MongoClient newConnection) {
        //connect to mongodb
        connection = newConnection;

        //get db
        db = this.connection.getDB("testdb");

        //get collection from db
        dbCollection = db.getCollection("users");

    }


    public boolean register(User user) {
        return register(user.getUsername(), user.getPassword());
    }

    public boolean register(String username, String password) {
        BasicDBObject document = new BasicDBObject();
        document.put("username", username);
        document.put("password", password);

        if (password.length() < 8) {
            throw new PasswordFormatException("Password needs to be at least 8 characters");
        }

        if (checkExistingUsers(username)) {
            System.out.println("Already exists");
            return false;
        }

        return (dbCollection.insert(document).getError() == null);

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
