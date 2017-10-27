package Authentication;

import static models.UserStatus.LOGGED_IN;
import static models.UserStatus.LOGGED_OUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import app.IssueTracker;
import exceptions.InvalidAuthStateException;
import exceptions.InvalidUsernameException;
import models.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.mongodb.MongoClient;

import exceptions.PasswordMismatchException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.net.UnknownHostException;

/**
 * Created by priyankitbangia on 17/10/17.
 */
public class LoginTest {
    private LoginService auth;
    IssueTracker issueTracker;
    Datastore ds;
    private String TEST_USERNAME = "testUsername";
    private String TEST_PASSWORD = "testPassword";
    private String INCORRECT_USERNAME = "incorrect_username";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void init() throws UnknownHostException {
        MongoClient connection = mock(MongoClient.class);
        Morphia dbMapper = mock(Morphia.class);
        ds = mock(Datastore.class);

        when(dbMapper.createDatastore(any(MongoClient.class),anyString())).thenReturn(ds);

        issueTracker = new IssueTracker(connection, dbMapper);

        auth = Mockito.spy(issueTracker.getLoginService());

    }

    @Test
    public void userStatusShouldBeLoggedInOnSignInIfCredentialsMatch(){
        User u = mock(User.class);
        when(u.getPassword()).thenReturn(TEST_PASSWORD);
        doReturn(u).when(auth).findUser(TEST_USERNAME);

        assertTrue(auth.login(TEST_USERNAME, TEST_PASSWORD));
        verify(u).setStatus(LOGGED_IN);
        verify(ds).save(u);
        assertEquals(auth.getCurrentUser(), u);
    }
    
    @Test
    public void shouldThrowInvalidUsernameExceptionOnSignInIfUsernameDoesNotExist() {

        doReturn(null).when(auth).findUser(INCORRECT_USERNAME);
        
        exception.expect(InvalidUsernameException.class);
        exception.expectMessage("Username not exists");
        
        auth.login(INCORRECT_USERNAME, TEST_PASSWORD);
    }
    
    @Test
    public void shouldThrowPasswordMismatchExceptionIfPasswordIsIncorrect() {

        User u = mock(User.class);
        when(u.getUsername()).thenReturn(TEST_USERNAME);
        when(u.getPassword()).thenReturn(TEST_PASSWORD);
        doReturn(u).when(auth).findUser(TEST_USERNAME);

        exception.expect(PasswordMismatchException.class);
        exception.expectMessage("Password is incorrect");
        
        auth.login(TEST_USERNAME, "incorrect_password");
    }
    
    @Test
    public void userStatusShouldBeLoggedOutOnLogOffIfCurrentlySignedIn() {

        User u = mock(User.class);
        when(u.getStatus()).thenReturn(LOGGED_IN);
        doReturn(u).when(auth).findUser(anyString());
        doReturn(null).when(ds).save(any(User.class));

        assertTrue(auth.logout(TEST_USERNAME));
        verify(u).setStatus(LOGGED_OUT);
        verify(ds).save(u);
        assertTrue(auth.getCurrentUser()==null);

    }

    @Test
    public void shouldThrowInvalidAuthStateExceptionWhenAccessingForumServiceIfNotLoggedIn(){
        when(auth.getCurrentUser()).thenReturn(null);

        exception.expect(InvalidAuthStateException.class);
        exception.expectMessage("No user currently logged in");
        issueTracker.getForumService();
    }
}
