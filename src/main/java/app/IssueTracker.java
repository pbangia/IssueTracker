package app;

import Authentication.LoginService;
import Authentication.RegistrationService;
import Clustering.ForumService;
import com.mongodb.MongoClient;
import exceptions.InvalidAuthStateException;
import models.User;
import models.UserStatus;
import org.mongodb.morphia.Morphia;

import java.net.UnknownHostException;

/**
 * Created by priyankitbangia on 21/10/17.
 */
public class IssueTracker {

    private ForumService forumService;
    private LoginService loginService;
    private RegistrationService registrationService;
    private User currentUser;

    public IssueTracker() throws UnknownHostException {
        this(new MongoClient("localhost", 27017), new Morphia());
    }

    public IssueTracker(MongoClient connection, Morphia morphia) {

        forumService = new ForumService(connection, morphia);
        loginService = new LoginService(connection, morphia);
        registrationService = new RegistrationService(connection, morphia);
    }

    public ForumService getForumService() {
        currentUser = loginService.getCurrentUser();
        if (currentUser == null) {
            throw new InvalidAuthStateException("No user currently logged in");
        }
        forumService.setAccessPrivilege(currentUser.getRole());
        return forumService;
    }

    public void setForumService(ForumService forumService) {
        this.forumService = forumService;
    }

    public LoginService getLoginService() {
        return loginService;
    }

    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    public UserStatus checkUserLoggedIn(String username){
        return loginService.checkStatus(username);
    }


    public  void checkCurrentUserRole() {
        return;
    }

    public void authenticate(String username, String password) {
        //currentUser = loginService.login(username,password);
    }

    public RegistrationService getRegistrationService() {
        return registrationService;
    }
}
