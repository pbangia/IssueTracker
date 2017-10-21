package app;

import java.net.UnknownHostException;

import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import Assignment.AssignmentService;
import Authentication.LoginService;
import Authentication.RegistrationService;
import Clustering.ForumService;
import com.mongodb.MongoClient;
import exceptions.InvalidAuthStateException;
import models.User;
import models.UserStatus;

/**
 * Created by priyankitbangia on 21/10/17.
 */
public class IssueTracker {

    private ForumService forumService;
    private LoginService loginService;
    private RegistrationService registrationService;
<<<<<<< HEAD
    private AssignmentService assignmentService;
    //private User currentUser;
=======
    private User currentUser;
>>>>>>> master

    public IssueTracker() throws UnknownHostException {
        this(new MongoClient("localhost", 27017), new Morphia());
    }

    public IssueTracker(MongoClient connection, Morphia morphia) {

        forumService = new ForumService(connection, morphia);
        loginService = new LoginService(connection, morphia);
        registrationService = new RegistrationService(connection, morphia);
        assignmentService = new AssignmentService(connection, morphia);
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

	public AssignmentService getAssignmentService() {
		return assignmentService;
	}

	public void setAssignmentService(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}
}
