package Authentication;

import exceptions.PasswordFormatException;
import exceptions.UserRegistrationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import com.mongodb.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import models.*;

/**
 * Created by priyankitbangia on 17/10/17.
 */
public class LoginTest {
    private DBObject document;
    private LoginService auth;
    private DBCollection dbCollection;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUpUserAuthenticationMockObjects(){
    	document = new BasicDBObject();
        document.put("username", "testUsername");
        document.put("password", "testPassword");
        document.put("role", UserRole.ADMIN);
        document.put("status", UserStatus.LOGOUT);
        MongoClient connection = mock(MongoClient.class);
        DB db = mock(DB.class);
        dbCollection = mock(DBCollection.class);

        doReturn(db).when(connection).getDB(anyString());
        doReturn(dbCollection).when(db).getCollection(anyString());

        auth = Mockito.spy(new LoginService(connection));
    }

    @Test
    public void adminCanLoginIfUserExist(){
        //return false when query to check db for already existing name is run
        DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(true).thenReturn(false);
        when(queriedUsers.next()).thenReturn(document);
        //return a result for when db checks if write was successful
        when(dbCollection.insert(any(BasicDBObject.class))).thenReturn(mock(WriteResult.class));

        //expect true on successful registration
        assertTrue(auth.login("testUsername", "testPassword"));
        assertTrue(UserStatus.LOGIN.equals(auth.checkStatus("testUsername")));

    }
    
    @Test
    public void shouldThrowUserNotExistExceptionIfUsernameNotExist() {
    	//return false when query to check db for already existing name is run
        DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(false);
        
        exception.expect(UernameNotExistException.class);
        exception.expectMessage("Username not exists");
        
        auth.login("testUsername1", "testPassword");
    }
    
    @Test
    public void shouldThrowPasswordMismatchExceptionIfPasswordIsIncorrect() {
    	DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(false);
        when(queriedUsers.next()).thenReturn(document);
        
        exception.expect(PasswordMismatchException.class);
        exception.expectMessage("Password is incorrect");
    }
}
