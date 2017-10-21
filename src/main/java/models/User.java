package models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by priyankitbangia on 15/10/17.
 */
@Entity(value = "users")
public class User {
    @Id
    private String username;

    private String password;
    private UserRole role;
    private UserStatus status;

    public User() {}

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        status = UserStatus.LOGOUT;
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
