/**
 * Created by priyankitbangia on 15/10/17.
 */
public class User {

    private String password;
    private String username;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
}
