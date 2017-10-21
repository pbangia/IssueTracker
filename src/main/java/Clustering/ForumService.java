package Clustering;

import com.mongodb.*;
import models.Cluster;
import models.ForumPost;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DBSCAN;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;


/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ForumService {
    Instances data;
    ArrayList<ForumPost> posts = new ArrayList<>();
    private ClusterEvaluation eval;
    double[] assignments;
    Map<Integer, Cluster> clusters;

    private MongoClient connection;
    private Datastore ds;
    //private DB db;
    //private DBCollection dbCollection;

    public ForumService() throws UnknownHostException {
        this(new MongoClient("localhost", 27017));
    }

    public ForumService(MongoClient newConnection) {

        loadIssueList();
        //connect to mongodb
        connection = newConnection;

        //get db
        //db = this.connection.getDB("testdb");

        //get collection from db
        //dbCollection = db.getCollection("clusters");

        Morphia morphia = new Morphia();
        ds = morphia.createDatastore(connection, "testdb");
        morphia.map(Cluster.class);
    }

    public List<String> getIssueTitles() {

        ArrayList<String> titles = new ArrayList<>();
        for (ForumPost post: posts) titles.add(post.getTitle());
        return titles;
    }

    private void loadIssueList() {
        try {
            ArffLoader loader = new ArffLoader();
            loader.setSource(new File("sample.arff"));
            data = loader.getDataSet();

            for (int i=0; i<data.numInstances();i++){
                ForumPost post = new ForumPost(data.instance(i));
                posts.add(post);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            DBSCAN dbscan = new DBSCAN();
            dbscan.setEpsilon(1);
            dbscan.setMinPoints(1);
            dbscan.buildClusterer(data);
            eval = new ClusterEvaluation();
            eval.setClusterer(dbscan);
            eval.evaluateClusterer(data);
            System.out.println(eval.clusterResultsToString());

        } catch (Exception e) { e.printStackTrace(); }
    }
}
