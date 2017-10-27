package Authentication;

import app.IssueTracker;
import exceptions.PasswordFormatException;
import exceptions.UserRegistrationException;
import exceptions.InvalidUsernameException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import com.mongodb.*;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.UnknownHostException;

import static models.UserRole.ADMIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import models.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Created by priyankitbangia on 15/10/17.
 */
public class RegistrationTest {
    private RegistrationService auth;
    Datastore ds;
    IssueTracker issueTracker;
    private String TEST_USERNAME = "testUsername";
    private String TEST_PASSWORD = "testPassword";
    private String TEST_ROLE_ADMIN = "ADMIN";
    private String TEST_ROLE_DEV = "DEVELOPER";
    private User u;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void init(){

        MongoClient connection = mock(MongoClient.class);
        Morphia morphia = mock(Morphia.class);
        ds = mock(Datastore.class);
        u = mock(User.class);

        when(morphia.createDatastore(any(MongoClient.class),anyString())).thenReturn(ds);

        issueTracker = new IssueTracker(connection, morphia);

        auth = spy(issueTracker.getRegistrationService());

    }

    @Test
    public void shouldCreateNewUserWhenRegisteringAsAdminIfNewUser(){

        doReturn(null).when(auth).findUser(TEST_USERNAME);

        assertTrue(auth.register(TEST_USERNAME, TEST_PASSWORD,TEST_ROLE_ADMIN)!=null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(ds).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(capturedUser.getUsername(), TEST_USERNAME);
        assertEquals(capturedUser.getPassword(), TEST_PASSWORD);
        assertEquals(capturedUser.getRole().toString(), TEST_ROLE_ADMIN);

    }

    @Test
    public void shouldThrowInvalidUsernameExceptionWhenRegisteringIfUsernameAlreadyExists(){

        exception.expect(InvalidUsernameException.class);
        exception.expectMessage("Username already exists");

        User u = mock(User.class);
        doReturn(u).when(auth).findUser(TEST_USERNAME);

        auth.register(TEST_USERNAME, TEST_PASSWORD,TEST_ROLE_ADMIN);
    }


    @Test
    public void shouldThrowPasswordFormatExceptionIfPasswordLengthLessThan8() {
        when(u.getUsername()).thenReturn(TEST_USERNAME);
        when(u.getPassword()).thenReturn("1234567");
        when(u.getRole()).thenReturn(ADMIN);

        exception.expect(PasswordFormatException.class);
        exception.expectMessage("Password needs to be at least 8 characters");

        auth.register(u);
    }
     
    @Test
    public void shouldThrowInvalidUsernameExceptionIfUsernameIsEmpty() {
        when(u.getUsername()).thenReturn("");
        when(u.getPassword()).thenReturn(TEST_PASSWORD);
        when(u.getRole()).thenReturn(ADMIN);

        exception.expect(InvalidUsernameException.class);
        exception.expectMessage("User name can not be Empty");
     
        auth.register(u);
    }
    
    @Test
    public void shouldThrowInvalidUsernameExceptionIfUsernameContainsSpecialChars() {
        when(u.getUsername()).thenReturn(TEST_USERNAME);
        when(u.getPassword()).thenReturn(TEST_PASSWORD);
        when(u.getRole()).thenReturn(ADMIN);
        when(u.getUsername()).thenReturn("Sam@#$");

        exception.expect(InvalidUsernameException.class);
        exception.expectMessage("Invalid Username");
     
        auth.register(u);
    }

    @Test
    public void shouldCreateNewUserWhenRegisteringAsDeveloperIfNewUser(){

        doReturn(null).when(auth).findUser(TEST_USERNAME);

        assertTrue(auth.register(TEST_USERNAME, TEST_PASSWORD,TEST_ROLE_DEV)!=null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(ds).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(capturedUser.getUsername(), TEST_USERNAME);
        assertEquals(capturedUser.getPassword(), TEST_PASSWORD);
        assertEquals(capturedUser.getRole().toString(), TEST_ROLE_DEV);
    }

    @Test
    public void shouldThrowUserRegistrationExceptionWhenRegisteringIfRoleNotDeveloperOrAdmin(){

        exception.expect(UserRegistrationException.class);
        exception.expectMessage("Role not supported");
        doReturn(null).when(auth).findUser(TEST_USERNAME);

        auth.register(TEST_USERNAME, TEST_PASSWORD,"invalid role");
    }

}
