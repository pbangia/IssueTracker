package Clustering;

import app.ForumPostReader;
import com.mongodb.*;
import exceptions.AssignmentException;
import exceptions.ClusterException;
import exceptions.InvalidAuthStateException;
import models.Cluster;
import models.ClusterSortBy;
import models.ForumPost;
import models.UserRole;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DBSCAN;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.*;

import static models.UserRole.ADMIN;


/**
 * A service to manage clustering tasks of processing forum post data.
 * Created by priyankitbangia on 18/10/17.
 */
public class ForumService {
    Instances postData;
    ArrayList<ForumPost> postsList = new ArrayList<>();
    Map<Integer, ForumPost> postsMap = new HashMap<>();
    public ClusterEvaluation eval;
    double[] assignments;
    Map<String, Cluster> clusters;
    public Cluster[] clusterIndexes;

    private Datastore datastore;
    private UserRole accessPrivilege;
    private String POST_FILE_NAME = "data/testforumPosts.arff";

    public ForumService(MongoClient newConnection, Morphia dbMapper) {
        postData = new ForumPostReader().loadData(POST_FILE_NAME);
        postsList = getAllPosts();

        datastore = dbMapper.createDatastore(newConnection, "testdb");
    }

    /**
     * Generates related issues an parses information from weka clustering APIs into java objects to store in memory.
     * @return  a map of cluster objects with their IDs as keys.
     */
    public Map<String, Cluster> getRelatedIssues(){

        //run cluster algorithm
        clusterPosts();

        //create cluster array
        clusters = new HashMap<>();
        clusterIndexes = new Cluster[eval.getNumClusters()];
        for (int i=0; i<eval.getNumClusters(); i++){
            Cluster c = new Cluster();
            clusters.put(c.getClusterID() , c);
            clusterIndexes[i] = c;
        }

        //perform mapping between cluster assignment and forum postsList
        assignments = eval.getClusterAssignments();
        for (int i = 0; i<assignments.length; i++){
            int clusterNum = (int) assignments[i];
            ForumPost post = postsList.get(i);

            Cluster c = clusterIndexes[clusterNum];
            post.setClusterID(c.getClusterID());
            c.addForumPost(post.getQuestionID(), post.getAuthor());
            datastore.save(c);
            datastore.save(post);
        }

        System.out.println("Cluster assignments: "+ Arrays.toString(eval.getClusterAssignments()));
        System.out.println("All clusters (with forum post IDs): "+clusters.toString());

        setClusterText();

        return clusters;
    }

    /**
     * Cluster all forum posts that have been loaded in memory. Uses the Weka clustering APIs.
     */
    public void clusterPosts() {
        try {
            StringToWordVector s = new StringToWordVector();
            s.setInputFormat(postData);
            postData = Filter.useFilter(postData, s);
            DBSCAN dbscan = getDBSCAN();
            dbscan.setEpsilon(1);
            dbscan.setMinPoints(1);
            dbscan.buildClusterer(postData);
            eval = getEval();
            eval.setClusterer(dbscan);
            eval.evaluateClusterer(postData);
            System.out.println(eval.clusterResultsToString());

        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Converts 'Instances' objects from the Weka library output, to build a list of Forum Post objects.
     * Stores a reference to the forum post objects in a map.
     * @return an arraylist of all forum posts.
     */
    public ArrayList<ForumPost> getAllPosts(){
        ArrayList<ForumPost> posts = new ArrayList<>();
        for (int i = 0; i< postData.numInstances(); i++){
            ForumPost post = new ForumPost(postData.instance(i));
            posts.add(post);
            postsMap.put(post.getQuestionID(), post);
        }
        return posts;
    }

    /**
     * Iterates through all clusters, sets their summarised titles and content.
     * Saves state of cluster objects after title and content summaries have been set.
     */
    public void setClusterText() {
        for (Cluster c: clusters.values()) {
            c.setTitle(summariseClusterTitle(c, 5));
            c.setSummary(summariseClusterTitle(c, 5));
            datastore.save(c);
        }
    }

    /**
     * Takes forum post object and cluster id, adds forum post reference to the cluster of specified cluster id.
     * Checks if permission is allowed, and if a cluster object of the specified cluster id exists.
     * @param forumPost forum post object to be added
     * @param clusterId ID of cluster object to add forum post to
     */
    public void addForumPostToCluster(ForumPost forumPost, String clusterId) {
        if (getAccessPrivilege()!= ADMIN) throw new InvalidAuthStateException("Only admins have permission to add clusters");
        if (forumPost.getClusterID() != null) throw new AssignmentException("Forum post is already assigned to a cluster");
        Cluster c = getCluster(clusterId);
        c.addForumPost(forumPost.getQuestionID(), forumPost.getAuthor());
        forumPost.setClusterID(clusterId);

        datastore.save(c);
        datastore.save(forumPost);
    }

    /**
     * Removes the specified forum post from it's cluster if permission allows and if it is currently assigned.
     * @param forumPost the forum post object to remove
     */
    public void removeForumPostFromCluster(ForumPost forumPost) {
        if (getAccessPrivilege()!= ADMIN) throw new InvalidAuthStateException("Only admins have permission to remove clusters");
        if (forumPost.getClusterID() == null) throw new AssignmentException("Forum post not assigned to a cluster");
        Cluster cluster = getCluster(forumPost.getClusterID());
        cluster.removeForumPost(forumPost);

        datastore.save(forumPost);
        datastore.save(cluster);
    }

    /**
     * Deletes a cluster object if permission allows. Removes existing forum posts from this cluster, places them into
     * their own individual new clusters.
     * @param c the cluster object to be deleted
     */
    public void deleteCluster(Cluster c) {
        if (getAccessPrivilege()!= ADMIN) throw new InvalidAuthStateException("Only admins have permission to remove clusters");
        if (!clusters.containsKey(c.getClusterID())) throw new ClusterException("Cluster ID does not exist");
        datastore.delete(c);
        clusters.remove(c.getClusterID());
        for (int fid : c.getPostIDs()) {
            Cluster newCluster = new Cluster();
            clusters.put(newCluster.getClusterID(), newCluster);

            ForumPost fp = postsMap.get(fid);
            fp.setClusterID(newCluster.getClusterID());
            newCluster.addForumPost(fp.getQuestionID(), fp.getAuthor());
            datastore.save(fp);
            datastore.save(newCluster);
        }
    }

    /**
     * Combines all titles from forum posts within a cluster object.
     * Summarises the key words into a string of a specified number of words.
     * @param c cluster object containing the forum posts to summarise
     * @param length length of words to set for summarised string
     * @return a string of specified number of words representing issue title
     */
    public String summariseClusterTitle(Cluster c, int length) {
        ArrayList<Integer> postIDs = new ArrayList<>(c.getPostIDs());
        StringBuilder sb = new StringBuilder();

        for (int id: postIDs) {
            ForumPost fp = getForumPost(id);
            sb.append(fp.getTitle()+" ");
        }

        return generateSummaryText(sb.toString().trim(), length);
    }

    /**
     * Combines all forum post descriptions within a cluster object.
     * Summarises key words into a string of a specified number of words.
     * @param c cluster object containing the forum posts to summarise
     * @param length length of words to set for summarised string
     * @return a string of specified number of words representing issue content
     */
    public String summariseClusterContent(Cluster c, int length) {
        ArrayList<Integer> postIDs = new ArrayList<>(c.getPostIDs());
        StringBuilder sb = new StringBuilder();

        for (int id: postIDs) {
            ForumPost fp = getForumPost(id);
            sb.append(fp.getContent()+" ");
        }

        return generateSummaryText(sb.toString().trim(), length);
    }

    /**
     * Performs key word extraction on a given string by passing input to the text summariser.
     * @param words string representing the words to summarise
     * @param summaryLength number of key words to set
     * @return a string of specified length representing a summarised string
     */
    public String generateSummaryText(String words, int summaryLength){

        if (words.equals("null") || words.isEmpty()) {
            return "No summary text available";
        }

        TextSummariser summariser = new TextSummariser();
        return summariser.getSortedTopWords(words, summaryLength);

    }

    /**
     * DB wrapper method to be called when querying DB for forum post objects by ID.
     * @param id id of forum post to retrieve
     * @return a ForumPost object that matches the specified ID
     */
    public ForumPost getForumPost(int id) {
        return datastore.find(ForumPost.class).field("_id").equal(id).get();
    }

    public List<Cluster> getSortedClusters(ClusterSortBy category, boolean asc) {
        List<Cluster> list = getClustersAsList();
        Collections.sort(list, category);
        if (asc) Collections.reverse(list);
        return list;
    }

    public Cluster getCluster(String i) {
        return clusters.get(i);
    }

    public void setAccessPrivilege(UserRole accessPrivilege) {
        this.accessPrivilege = accessPrivilege;
    }

    public UserRole getAccessPrivilege() {
        return accessPrivilege;
    }

    public DBSCAN getDBSCAN() {
        return new DBSCAN();
    }

    public ClusterEvaluation getEval() {
        return new ClusterEvaluation();
    }

    public List<Cluster> getClustersAsList() {
        return new ArrayList<>(clusters.values());
    }

    public void setClusters(Map<String, Cluster> clusters) {
        this.clusters = clusters;
    }

    public void setPostsMap(Map<Integer,ForumPost> postsMap) {
        this.postsMap = postsMap;
    }

    public Map<String,Cluster> getClusters() {
        return clusters;
    }

}
