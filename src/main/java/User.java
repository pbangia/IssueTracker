/**
 * Created by priyankitbangia on 15/10/17.
 */
public class User {

    private String password;
    private String username;
    private UserRole role;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() { return role; }
}
