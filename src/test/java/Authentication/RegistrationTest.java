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
 * Created by priyankitbangia on 15/10/17.
 */
public class RegistrationTest {
    private User u;
    private RegistrationService auth;
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

        auth = Mockito.spy(new RegistrationService(connection));
    }

    @Test
    public void shouldThrowFormatPasswordExceptionIfPasswordLengthLessThan8() {
        when(u.getPassword()).thenReturn("1234567");

        exception.expect(PasswordFormatException.class);
        exception.expectMessage("Password needs to be at least 8 characters");

        auth.register(u);
    }

    @Test
    public void developerCanRegisterIfNewUser(){
        when(u.getRole()).thenReturn(UserRole.DEVELOPER);

        //return false when query to check db for already existing name is run
        DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(false);

        //return a result for when db checks if write was successful
        when(dbCollection.insert(any(BasicDBObject.class))).thenReturn(mock(WriteResult.class));

        //expect true on successful registration
        assertTrue(auth.register(u));

    }

    @Test
    public void adminCanRegisterIfNewUser(){
        //return false when query to check db for already existing name is run
        DBCursor queriedUsers = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(queriedUsers);
        when(queriedUsers.hasNext()).thenReturn(false);

        //return a result for when db checks if write was successful
        when(dbCollection.insert(any(BasicDBObject.class))).thenReturn(mock(WriteResult.class));

        //expect true on successful registration
        assertTrue(auth.register(u));

    }

    @Test
    public void userCannotRegisterIfAlreadyExists(){
        //return true when query to check db for already existing name is run
        DBCursor index = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(index);
        when(index.hasNext()).thenReturn(true);

        exception.expect(UserRegistrationException.class);
        exception.expectMessage("Username already exists");

        auth.register(u);

    }

    //Must set up mongoDB server first on your device
    @Ignore
    @Test
    public void testRealDatabase(){

        try {
            RegistrationService a = new RegistrationService();
            a.register("realUsername","realPassword", "ADMIN");

            DBCursor indexes = a.getDB().getCollection("users").find();
            for (DBObject user: indexes){
                System.out.println(user);
            }

        }catch (Exception e){

        }
    }
}
