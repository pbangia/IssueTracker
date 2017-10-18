package Clustering;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import models.Cluster;
import models.ForumPost;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DBSCAN;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ForumService {
    Instances data;
    ArrayList<ForumPost> posts = new ArrayList<>();
    ClusterEvaluation eval;
    double[] assignments;
    ArrayList<Cluster> clusters;

    public ForumService(){
        loadIssueList();
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

    public String getRelatedIssues(){

        //run cluster algorithm
        clusterPosts();

        return "";
    }

    private void clusterPosts() {
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
