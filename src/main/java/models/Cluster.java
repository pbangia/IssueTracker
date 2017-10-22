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
//    Set<String> usersAffected = new HashSet<>();
    List<String> usersAffected = new ArrayList<>();
    private int numPosts;
    private String title = "placeholder title";
    private String summary = "placeholder summary";
    private int numUsers;
    private String context = "placeholder summary";
    private Set<String> assigneeIDs = new HashSet<>();
    private IssueStatus status = IssueStatus.OPEN;

    public Cluster(int id){
        this.clusterID = id;
    }

    public void addForumPost(int postID, String author){
        postIDs.add(postID);
        numPosts=postIDs.size();
        usersAffected.add(author);
        numUsers = (usersAffected.contains(author)) ? numPosts : numPosts++;
    }

    public void removeForumPost(ForumPost forumPost) {
        postIDs.remove(forumPost.getQuestionID());
        numPosts = postIDs.size();
        usersAffected.remove(forumPost.getAuthor());
        numUsers = (usersAffected.contains(forumPost.getAuthor())) ? numPosts : numPosts--;

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

    public int getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(int numUsers) {
        this.numUsers = numUsers;
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

    public enum IssueStatus { OPEN, CLOSED, IN_PROGRESS };
}
