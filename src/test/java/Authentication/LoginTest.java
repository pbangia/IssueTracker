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
    private User u;
    private LoginService auth;
    private DBCollection dbCollection;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUpUserAuthenticationMockObjects(){
        u = mock(User.class);
        when(u.getUsername()).thenReturn("testUsername");
        when(u.getPassword()).thenReturn("testPassword");
        when(u.getRole()).thenReturn(UserRole.ADMIN);

        MongoClient connection = mock(MongoClient.class);
        DB db = mock(DB.class);
        dbCollection = mock(DBCollection.class);

        doReturn(db).when(connection).getDB(anyString());
        doReturn(dbCollection).when(db).getCollection(anyString());

        auth = Mockito.spy(new LoginService(connection));
    }

}
