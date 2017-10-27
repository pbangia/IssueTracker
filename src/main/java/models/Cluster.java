package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A cluster object to represent an Issue. Contains forum post IDs of posts that have been assigned.
 * Cluster objects are stored in the "clusters" collection of the database.
 * Created by priyankitbangia on 18/10/17.
 */
@Entity(value = "clusters")
public class Cluster implements Serializable {

    @Id
    private String clusterID;

    private Set<Integer> postIDs = new HashSet<>();
    private List<String> usersAffected = new ArrayList<>();
    private int numPosts;
    private String title;
    private String summary;
    private int numAffectedUsers;
    private Set<String> assigneeIDs = new HashSet<>();
    private IssueStatus status = IssueStatus.OPEN;

    public Cluster(String id){
        this.clusterID = id;
    }

    public Cluster(){
        this.clusterID = generateID();
    }

    private String generateID() {
        return new ObjectId().toString();
    }

    /**
     * Handles adding a forum post to this cluster. Updates other objects that are affected.
     * @param postID ID of forum post to be assigned to this cluster
     * @param author String representing the author of the forum post
     */
    public void addForumPost(int postID, String author){
        postIDs.add(postID);
        numPosts=postIDs.size();
        numAffectedUsers = (usersAffected.contains(author)) ? numAffectedUsers : numAffectedUsers + 1;
        usersAffected.add(author);
    }

    /**
     * Handles removing a forum post from this cluster. Updates other objects that are affected.
     * @param forumPost
     */
    public void removeForumPost(ForumPost forumPost) {
        postIDs.remove(forumPost.getQuestionID());
        numPosts = postIDs.size();
        usersAffected.remove(forumPost.getAuthor());
        numAffectedUsers = (usersAffected.contains(forumPost.getAuthor())) ? numAffectedUsers : numAffectedUsers - 1;
        forumPost.setClusterID(null);
    }

    public Set<Integer> getPostIDs(){
        return postIDs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getNumAffectedUsers() {
        return numAffectedUsers;
    }

    public void setNumAffectedUsers(int numAffectedUsers) {
        this.numAffectedUsers = numAffectedUsers;
    }

    public Set<String> getAssigneeIDs() {
        return assigneeIDs;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public String getClusterID() {
        return clusterID;
    }

    @Override
    public String toString(){
        return postIDs.toString();
    }

    public int getNumPosts() {
        return numPosts;
    }

    public void setNumPosts(int numPosts) {
        this.numPosts = numPosts;
    }

    public void setUsersAffected(ArrayList<String> usersAffected) {
        this.usersAffected = usersAffected;
    }

    public void setPostIDs(HashSet<Integer> postIDs) {
        this.postIDs = postIDs;
    }

    public List<String> getUsersAffected() {
        return usersAffected;
    }

    /**
     * An enum which represents the state of the Issue.
     */
    public enum IssueStatus { OPEN, CLOSED, IN_PROGRESS };
}
