package models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import weka.core.Instance;

/**
 * A forum post object that contains the fields of forum post data.
 * ForumPost objects are stored in the "forumposts" collection of the database.
 * Created by priyankitbangia on 18/10/17.
 */
@Entity(value = "forumposts")
public class ForumPost {

    @Id
    private int questionID;

    private String title;
    private String date;
    private String author = "";
    private int detailsID;
    private String content;
    private String url;
    private String clusterID;

    /* Default constructor required by dbMapper */
    public ForumPost(){}

    public ForumPost(Instance rawData){
        questionID = (int)rawData.value(0);
        title = rawData.stringValue(1);
        date = rawData.stringValue(2);
        author = rawData.stringValue(3);
        detailsID = (int) rawData.value(4);
        content = rawData.stringValue(5);
        url = rawData.stringValue(6);
    }

    public ForumPost(int id) {
        questionID=id;
    }

    @Override
    public String toString(){
        return Integer.toString(questionID);
    }

    public int getQuestionID() {
        return questionID;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }
}
