package Authentication;

import static models.UserStatus.LOGIN;
import static models.UserStatus.LOGOUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import app.IssueTracker;
import models.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

import exceptions.PasswordMismatchException;
import exceptions.UsernameNotExistException;
import models.UserRole;
import models.UserStatus;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.net.UnknownHostException;

/**
 * Created by priyankitbangia on 17/10/17.
 */
public class LoginTest {
    private DBObject document;
    private LoginService auth;
    private DBCollection dbCollection;
    IssueTracker issueTracker;
    Datastore ds;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUpUserAuthenticationMockObjects() throws UnknownHostException {
        MongoClient connection = mock(MongoClient.class);
        Morphia morphia = mock(Morphia.class);
        ds = mock(Datastore.class);

        when(morphia.createDatastore(any(MongoClient.class),anyString())).thenReturn(ds);

        issueTracker = new IssueTracker(connection, morphia);

        auth = Mockito.spy(issueTracker.getLoginService());

    }

    @Test
    public void userCanLoginIfUserExist(){
    	document.put("status", UserStatus.LOGOUT);
        //return false when query to check db for already existing name is run
        DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(true);
        when(queriedUsers.next()).thenReturn(document);
        //return a result for when db checks if write was successful
        when(dbCollection.insert(any(BasicDBObject.class))).thenReturn(mock(WriteResult.class));

        //expect true on successful registration
        assertTrue(auth.login("testUsername", "testPassword"));
        assertTrue(UserStatus.LOGIN.equals(auth.checkStatus("testUsername")));

    }
    
    @Test
    public void shouldThrowUserNotExistExceptionIfUsernameNotExist() {
    	document.put("status", UserStatus.LOGOUT);
    	//return false when query to check db for already existing name is run
        DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(false);
        
        exception.expect(UsernameNotExistException.class);
        exception.expectMessage("Username not exists");
        
        auth.login("testUsername1", "testPassword");
    }
    
    @Test
    public void shouldThrowPasswordMismatchExceptionIfPasswordIsIncorrect() {
    	document.put("status", UserStatus.LOGOUT);
    	DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(true);
        when(queriedUsers.next()).thenReturn(document);
        
        exception.expect(PasswordMismatchException.class);
        exception.expectMessage("Password is incorrect");
        
        auth.login("testUsername", "testPassword1");
    }
    
    @Test
    public void userCanSucessfullyLogOutIfCurrentlyLogin() {
    	document.put("status", UserStatus.LOGIN);
    	DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(true);
        when(queriedUsers.next()).thenReturn(document);
        
        //return a result for when db checks if write was successful
        when(dbCollection.insert(any(BasicDBObject.class))).thenReturn(mock(WriteResult.class));

        //expect true on successful registration
        assertTrue(auth.logout("testUsername"));
        assertTrue(UserStatus.LOGOUT.equals(auth.checkStatus("testUsername")));
    }
}
