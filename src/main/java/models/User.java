package models;

/**
 * Created by priyankitbangia on 15/10/17.
 */
public class User {

    private String password;
    private String username;
    private UserRole role;
    private UserStatus status;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        status = UserStatus.NOTEXIST;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() { return role; }
    
    public UserStatus getStatus() { return status; }
    
    public void setStatus(UserStatus status) { this.status = status; }
}
