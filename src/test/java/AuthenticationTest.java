import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.mongodb.*;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by priyankitbangia on 15/10/17.
 */
public class AuthenticationTest {
    private User u;
    private AuthenticationService auth;
    private DBCollection dbCollection;


    @Before
    public void setUpUserAuthenticationMockObjects(){
        u = mock(User.class);
        when(u.getUsername()).thenReturn("testUsername");
        when(u.getPassword()).thenReturn("testPassword");

        MongoClient connection = mock(MongoClient.class);
        DB db = mock(DB.class);
        dbCollection = mock(DBCollection.class);

        doReturn(db).when(connection).getDB(anyString());
        doReturn(dbCollection).when(db).getCollection(anyString());

        auth = spy(new AuthenticationService(connection));
    }

    @Test
    public void userCanRegisterIfNewUser(){

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
    public void userCanNotRegisterIfAlreadyExists(){
        //return true when query to check db for already existing name is run
        DBCursor index = mock(DBCursor.class);
        when(dbCollection.find(any(BasicDBObject.class))).thenReturn(index);
        when(index.hasNext()).thenReturn(true);

        assertFalse(auth.register(u));

    }

    //Must set up mongoDB server first on your device
    @Ignore
    @Test
    public void testRealDatabase(){

        try {
            AuthenticationService a = new AuthenticationService();
            a.register("realUsername","realPassword");

            DBCursor indexes = a.getDB().getCollection("users").find();
            for (DBObject user: indexes){
                System.out.println(user);
            }

        }catch (Exception e){

        }
    }
}
