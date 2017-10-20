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

    @Override
    public String toString(){
        return postIDs.toString();
    }
}
