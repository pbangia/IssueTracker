package Clustering;

import app.ForumPostReader;
import com.mongodb.*;
import exceptions.AssignmentException;
import exceptions.InvalidAuthStateException;
import models.Cluster;
import models.ForumPost;
import models.UserRole;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DBSCAN;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.net.UnknownHostException;
import java.util.*;

import static models.UserRole.ADMIN;


/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ForumService {
    private Instances sentenceData;
    Instances postData;
    ArrayList<ForumPost> posts = new ArrayList<>();
    public ClusterEvaluation eval;
    double[] assignments;
    Map<Integer, Cluster> clusters;

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
        //sentenceData = new ForumPostReader().loadData(SENTENCE_FILE_NAME);
        posts = getAllPosts();

        connection = newConnection;
        ds = dbMapper.createDatastore(connection, "testdb");
    }

    public List<String> getIssueTitles() {
        ArrayList<String> titles = new ArrayList<>();
        for (ForumPost post: posts) titles.add(post.getTitle());
        return titles;
    }

    public ArrayList<ForumPost> getAllPosts(){
        ArrayList<ForumPost> posts = new ArrayList<>();
        for (int i = 0; i< postData.numInstances(); i++){
            ForumPost post = new ForumPost(postData.instance(i));
            posts.add(post);
        }
        return posts;
    }

//    public Instances loadIssueList(String filename) {
//        Instances instances = null;
//        try {
//            instances =  new Instances(new BufferedReader(new FileReader(filename)));
//        } catch (IOException e) { e.printStackTrace(); }
//
//        return instances;
//    }

    public Map<Integer, Cluster> getRelatedIssues(){

        //run cluster algorithm
        clusterPosts();

        //create cluster array
        clusters = new HashMap<>();
        for (int i=0; i<eval.getNumClusters(); i++){
            clusters.put(i , new Cluster(i));
        }

        //perform mapping between cluster assignment and forum posts
        assignments = eval.getClusterAssignments();
        for (int i = 0; i<assignments.length; i++){
            int clusterNum = (int) assignments[i];
            ForumPost post = posts.get(i);
            post.setClusterID(clusterNum);
            clusters.get(clusterNum).setClusterID(clusterNum);
            clusters.get(clusterNum).addForumPost(post.getQuestionID(), post.getAuthor());

        }

        System.out.println("Cluster assignments: "+ Arrays.toString(eval.getClusterAssignments()));
        System.out.println("All clusters (with forum post IDs): "+clusters.toString());

        //TODO: split saving clusters below into another unit test
//        saveForumPosts();
//        saveClusters();
        return clusters;
    }

    public void saveClusters() {
        for (Cluster c: clusters.values()) ds.save(c);
    }

    public void saveForumPosts() {
        for (ForumPost f: posts){
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

    public Cluster getCluster(int i) {
        return clusters.get(i);
    }

    public void setAccessPrivilege(UserRole accessPrivilege) {
        this.accessPrivilege = accessPrivilege;
    }

    public void addForumPostToCluster(ForumPost forumPost, int i) {
        if (getAccessPrivilege()!= ADMIN) throw new InvalidAuthStateException("Only admins have permission to add clusters");
        if (forumPost.getClusterID() != -1) throw new AssignmentException("Forum post is already assigned to a cluster");
        Cluster c = getCluster(i);
        c.addForumPost(forumPost.getQuestionID(), forumPost.getAuthor());
        forumPost.setClusterID(i);

        ds.save(c);
        ds.save(forumPost);
    }

    public void removeForumPostFromCluster(ForumPost forumPost, int clusterId) {

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
}
