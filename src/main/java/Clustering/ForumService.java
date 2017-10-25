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
import weka.filters.unsupervised.attribute.InterquartileRange;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.net.UnknownHostException;
import java.util.*;

import static models.UserRole.ADMIN;


/**
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

    private MongoClient connection;
    private Datastore ds;
    private UserRole accessPrivilege;
    private String POST_FILE_NAME = "data/testforumPosts.arff";
    private String SENTENCE_FILE_NAME = "data/testforumSentences.arff";
    //private DB db;
    //private DBCollection dbCollection;

    public ForumService() throws UnknownHostException {
        this(new MongoClient("localhost", 27017), new Morphia());
    }

    public ForumService(MongoClient newConnection, Morphia dbMapper) {
        postData = new ForumPostReader().loadData(POST_FILE_NAME);
        postsList = getAllPosts();

        connection = newConnection;
        ds = dbMapper.createDatastore(connection, "testdb");
    }

    public List<String> getIssueTitles() {
        ArrayList<String> titles = new ArrayList<>();
        for (ForumPost post: postsList) titles.add(post.getTitle());
        return titles;
    }

    public ArrayList<ForumPost> getAllPosts(){
        ArrayList<ForumPost> posts = new ArrayList<>();
        for (int i = 0; i< postData.numInstances(); i++){
            ForumPost post = new ForumPost(postData.instance(i));
            posts.add(post);
            postsMap.put(post.getQuestionID(), post);
        }
        return posts;
    }

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
            ds.save(c);
            ds.save(post);
        }

        System.out.println("Cluster assignments: "+ Arrays.toString(eval.getClusterAssignments()));
        System.out.println("All clusters (with forum post IDs): "+clusters.toString());

        setClusterTitles();

        return clusters;
    }

    public void setClusterTitles() {
        for (Cluster c: clusters.values()) {
            c.setTitle(summariseClusterTitle(c, 5));
            ds.save(c);
        }
    }

    private void saveState() {
        saveForumPosts();
        saveClusters();
    }

    public void saveClusters() {
        for (Cluster c: clusters.values()) {
            ds.save(c);
        }
    }

    public void saveForumPosts() {
        for (ForumPost f: postsList) {
            ds.save(f);
        }
    }

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

    public void addForumPostToCluster(ForumPost forumPost, String id) {
        if (getAccessPrivilege()!= ADMIN) throw new InvalidAuthStateException("Only admins have permission to add clusters");
        if (forumPost.getClusterID() != null) throw new AssignmentException("Forum post is already assigned to a cluster");
        Cluster c = getCluster(id);
        c.addForumPost(forumPost.getQuestionID(), forumPost.getAuthor());
        forumPost.setClusterID(id);

        ds.save(c);
        ds.save(forumPost);
    }

    public void removeForumPostFromCluster(ForumPost forumPost) {
        if (getAccessPrivilege()!= ADMIN) throw new InvalidAuthStateException("Only admins have permission to remove clusters");
        if (forumPost.getClusterID() == null) throw new AssignmentException("Forum post not assigned to a cluster");
        Cluster cluster = getCluster(forumPost.getClusterID());
        cluster.removeForumPost(forumPost);

        ds.save(forumPost);
        ds.save(cluster);
    }

    public void deleteCluster(Cluster c) {
        if (getAccessPrivilege()!= ADMIN) throw new InvalidAuthStateException("Only admins have permission to remove clusters");
        if (!clusters.containsKey(c.getClusterID())) throw new ClusterException("Cluster ID does not exist");
        ds.delete(c);
        clusters.remove(c.getClusterID());
        for (int fid : c.getPostIDs()) {
            Cluster newCluster = new Cluster();
            clusters.put(newCluster.getClusterID(), newCluster);

            ForumPost fp = postsMap.get(fid);
            fp.setClusterID(newCluster.getClusterID());
            newCluster.addForumPost(fp.getQuestionID(), fp.getAuthor());
            ds.save(fp);
            ds.save(newCluster);
        }
    }

    public String summariseClusterTitle(Cluster c, int length) {
        ArrayList<Integer> postIDs = new ArrayList<>(c.getPostIDs());
        StringBuilder sb = new StringBuilder();

        for (int id: postIDs) {
            ForumPost fp = getForumPost(id);
            sb.append(fp.getTitle()+" ");
        }

        TextSummariser summariser = new TextSummariser();
        List<String> wordList = summariser.getSortedTopWords(sb.toString(), length);
        String summary = String.join(" ", wordList);

        return summary;
    }

    public ForumPost getForumPost(int id) {
        return ds.find(ForumPost.class).field("_id").equal(id).get();
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

    public Map<Integer,ForumPost> getPostsMap() {
        return postsMap;
    }
}
