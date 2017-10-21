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
        User u = mock(User.class);
        when(u.getPassword()).thenReturn("testPassword");
        doReturn(u).when(auth).findUser("testUsername");

        assertTrue(auth.login("testUsername", "testPassword"));
        verify(u).setStatus(LOGIN);
        verify(ds).save(u);
    }
    
    @Test
    public void shouldThrowUserNotExistExceptionIfUsernameNotExist() {

        doReturn(null).when(auth).findUser("incorrect_username");
        
        exception.expect(UsernameNotExistException.class);
        exception.expectMessage("Username not exists");
        
        auth.login("incorrect_username", "testPassword");
    }
    
    @Test
    public void shouldThrowPasswordMismatchExceptionIfPasswordIsIncorrect() {

        User u = mock(User.class);
        when(u.getUsername()).thenReturn("testUsername");
        when(u.getPassword()).thenReturn("testPassword");
        doReturn(u).when(auth).findUser("testUsername");

        exception.expect(PasswordMismatchException.class);
        exception.expectMessage("Password is incorrect");
        
        auth.login("testUsername", "incorrect_password");
    }
    
    @Test
    public void userCanSucessfullyLogOutIfCurrentlyLogin() {

        User u = mock(User.class);
        when(u.getStatus()).thenReturn(LOGIN);
        doReturn(u).when(auth).findUser(anyString());
        doReturn(null).when(ds).save(any(User.class));

        assertTrue(auth.logout("testUsername"));
        verify(u).setStatus(LOGOUT);
        verify(ds).save(u);

    }
}
