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

    Set<ForumPost> posts = new HashSet<>();

    public Cluster(int id){
        this.clusterID = id;
    }

    public void addForumPost(ForumPost post){
        post.setClusterID(clusterID);
        posts.add(post);
    }

    public void setClusterID(int id) {
        this.clusterID = id;
    }

    public Set<ForumPost> getPosts(){
        return posts;
    }

    @Override
    public String toString(){
        return posts.toString();
    }
}
