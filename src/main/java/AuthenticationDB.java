import java.util.HashSet;
import java.util.Set;

/**
 * Created by priyankitbangia on 15/10/17.
 */
public class AuthenticationDB {

    private Set<String> db = new HashSet<String>();

    public boolean register(User user) {

        return db.add(user.getUsername());

    }
}
