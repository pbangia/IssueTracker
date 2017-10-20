package models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by priyankitbangia on 18/10/17.
 */
@Entity(value = "clusters")
public class Cluster implements Serializable {


    @Id
    private int clusterID;
    Set<Integer> postIDs = new HashSet<>();
    private String title;
    private String summary;
    private int numUsers;
    private String context;
    private Set<String> assigneeIDs;
    private String status;

    public Cluster(int id){
        this.clusterID = id;
    }

    public void addForumPost(int postID){
        postIDs.add(postID);
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString(){
        return postIDs.toString();
    }
}
