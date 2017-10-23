package models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by priyankitbangia on 18/10/17.
 */
@Entity(value = "clusters")
public class Cluster implements Serializable {

    @Id
    private int clusterID;
    Set<Integer> postIDs = new HashSet<>();
    List<String> usersAffected = new ArrayList<>();
    private int numPosts;
    private String title = "placeholder title";
    private String summary = "placeholder summary";
    private int numAffectedUsers;
    private String context = "placeholder summary";
    private Set<String> assigneeIDs = new HashSet<>();
    private IssueStatus status = IssueStatus.OPEN;

    public Cluster(int id){
        this.clusterID = id;
    }

    public void addForumPost(int postID, String author){
        postIDs.add(postID);
        numPosts=postIDs.size();
        numAffectedUsers = (usersAffected.contains(author)) ? numAffectedUsers : numAffectedUsers + 1;
        usersAffected.add(author);
    }

    public void removeForumPost(ForumPost forumPost) {
        postIDs.remove(forumPost.getQuestionID());
        numPosts = postIDs.size();
        usersAffected.remove(forumPost.getAuthor());
        numAffectedUsers = (usersAffected.contains(forumPost.getAuthor())) ? numAffectedUsers : numAffectedUsers - 1;

        System.out.println(numAffectedUsers);
        forumPost.setClusterID(-1);
    }

    public void setClusterID(int id) {
        this.clusterID = id;
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Set<String> getAssigneeIDs() {
        return assigneeIDs;
    }

    public void setAssigneeIDs(Set<String> assigneeIDs) {
        this.assigneeIDs = assigneeIDs;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public int getClusterID() {
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

    public enum IssueStatus { OPEN, CLOSED, IN_PROGRESS };

    public enum ClusterCategory { NUMPOSTS };
}
