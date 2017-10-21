package Clustering;

import com.mongodb.*;
import exceptions.InvalidAuthStateException;
import models.Cluster;
import models.ForumPost;
import models.UserRole;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DBSCAN;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import static models.UserRole.ADMIN;


/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ForumService {
    Instances data;
    ArrayList<ForumPost> posts = new ArrayList<>();
    public ClusterEvaluation eval;
    double[] assignments;
    Map<Integer, Cluster> clusters;

    private MongoClient connection;
    private Datastore ds;
    private UserRole accessPrivilege;
    private String FILE_NAME = "sample.arff";
    //private DB db;
    //private DBCollection dbCollection;

    public ForumService() throws UnknownHostException {
        this(new MongoClient("localhost", 27017), new Morphia());
    }

    public ForumService(MongoClient newConnection, Morphia dbMapper) {
        data = loadIssueList(FILE_NAME);
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
        for (int i=0; i<data.numInstances();i++){
            ForumPost post = new ForumPost(data.instance(i));
            posts.add(post);
        }
        return posts;
    }

    public Instances loadIssueList(String filename) {
        Instances instances = null;
        try {
            instances =  new Instances(new BufferedReader(new FileReader(filename)));
        } catch (IOException e) { e.printStackTrace(); }

        return instances;
    }

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

            clusters.get(clusterNum).setClusterID(clusterNum);
            clusters.get(clusterNum).addForumPost(posts.get(i).getQuestionID(), posts.get(i).getAuthor());

        }

        System.out.println("Cluster assignments: "+ Arrays.toString(eval.getClusterAssignments()));
        System.out.println("All clusters (with forum post IDs): "+clusters.toString());

        //TODO: split saving clusters below into another unit test
//        saveForumPosts();
//        saveClusters();
        return clusters;
    }

    public void saveClusters() {
        ds.save(clusters);
    }

    public void saveForumPosts() {
        for (ForumPost f: posts){
            ds.save(f);
        }
    }

    public void clusterPosts() {
        try {
            StringToWordVector s = new StringToWordVector();
            s.setInputFormat(data);
            data = Filter.useFilter(data, s);
            DBSCAN dbscan = getDBSCAN();
            dbscan.setEpsilon(1);
            dbscan.setMinPoints(1);
            dbscan.buildClusterer(data);
            eval = getEval();
            eval.setClusterer(dbscan);
            eval.evaluateClusterer(data);
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
        if (getAccessPrivilege()!= ADMIN) {
            throw new InvalidAuthStateException("Only admins have permission to modify clusters");
        }
        getCluster(i).addForumPost(forumPost.getQuestionID(), forumPost.getAuthor());
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
