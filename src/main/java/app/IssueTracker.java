package app;

import java.net.UnknownHostException;

import models.UserRole;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import Assignment.AssignmentService;
import Authentication.LoginService;
import Authentication.RegistrationService;
import Clustering.ForumService;
import exceptions.InvalidAuthStateException;
import models.User;
import models.UserStatus;

/**
 * The controller of the app. Handles the interaction between the different services and checks user
 * permissions when accessing services. IssueTracker object to be instantiated as first entry point into app.
 * Created by priyankitbangia on 21/10/17.
 */
public class IssueTracker {

    private Datastore datastore;
    private ForumService forumService;
    private LoginService loginService;
    private RegistrationService registrationService;
    private AssignmentService assignmentService;
    private User currentUser;

    /**
     * Sets up the application with a connection to the database.
     * @throws UnknownHostException when connection cannot be established with database.
     */
    public IssueTracker() throws UnknownHostException {
        this(new MongoClient("localhost", 27017), new Morphia());
    }

    /**
     * Start the services required.
     * @param connection a MongoDB client connection
     * @param morphia a database-to-java class mapper/serializer
     */
    public IssueTracker(MongoClient connection, Morphia morphia) {
        forumService = new ForumService(connection, morphia);
        loginService = new LoginService(connection, morphia);
        registrationService = new RegistrationService(connection, morphia);
        assignmentService = new AssignmentService(connection, morphia);
        datastore = morphia.createDatastore(connection, "testdb");
    }

    /**
     * Retrieve the forum service if authentication state is valid.
     * @return the forum service to be used in performing operations on the forum data
     */
    public ForumService getForumService() {
        currentUser = loginService.getCurrentUser();
        if (currentUser == null) {
            throw new InvalidAuthStateException("No user currently logged in");
        }
        forumService.setAccessPrivilege(currentUser.getRole());
        return forumService;
    }

    /**
     * Pipes registration requests to the registration service
     * @param username a string that represents the username
     * @param password a string that represents the password
     * @param role a string that represents the role of the user
     */
    public void register(String username, String password, UserRole role){
        registrationService.register(new User(username, password, role));
    }

    /**
     * Pipes login requests to the login service
     * @param username a string that represents the username
     * @param password a string that represents the password
     */
    public void login(String username, String password){
        loginService.login(username, password);
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public LoginService getLoginService() {
        return loginService;
    }

    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    public RegistrationService getRegistrationService() {
        return registrationService;
    }

	public AssignmentService getAssignmentService() {
		return assignmentService;
	}
}
