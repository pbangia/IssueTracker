package Clustering;

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
import java.util.List;

/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ForumService {
    Instances data;
    ArrayList<ForumPost> posts = new ArrayList<>();

    public ForumService(){

    }

    public List<String> getIssueTitles() {
        loadIssueList();

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
}
