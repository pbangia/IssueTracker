package models;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by priyankitbangia on 18/10/17.
 */
public class Cluster {



    private int id;
    Set<ForumPost> posts = new HashSet<>();

    public Cluster(int id){
        this.id = id;
    }

    public void addForumPost(ForumPost post){
        post.setClusterID(id);
        posts.add(post);
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<ForumPost> getPosts(){
        return posts;
    }

    @Override
    public String toString(){
        return posts.toString();
    }
}
