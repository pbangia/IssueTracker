import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by priyankitbangia on 15/10/17.
 */
public class AuthenticationTest {

    @Test
    public void userCanRegisterIfNewUser(){
        User user = new User("username","password");
        AuthenticationDB auth = new AuthenticationDB();
        assertTrue(auth.register(user));
    }

    @Test
    public void userCanNotRegisterIfAlreadyExists(){
        User user = new User("username","password");
        User sameUser = new User("username","password");

        AuthenticationDB auth = new AuthenticationDB();
        auth.register(user);
        assertFalse(auth.register(sameUser));
    }
}
