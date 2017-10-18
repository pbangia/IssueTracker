package Clustering;

import Authentication.LoginService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import models.User;
import models.UserRole;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ClusteringTest {
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

    @Test
    public void showListOfIssueTitles(){
        ForumService forum = new ForumService();
        List<String> issues = forum.getIssueTitles();
        assertEquals(10,issues.size());
        System.out.println(issues);

    }
}
