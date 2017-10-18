package models;

import weka.core.Instance;

import java.util.Date;

/**
 * Created by priyankitbangia on 18/10/17.
 */
public class ForumPost {


    private int questionID;
    private String title;
    private String date;
    private String author;
    private int detailsID;
    private String content;
    private String url;

    private int clusterID;

    public ForumPost(){

    }

    public ForumPost(Instance rawData){
        questionID = (int)rawData.value(0);
        title = rawData.stringValue(1);
        date = rawData.stringValue(2);
        author = rawData.stringValue(3);
        detailsID = (int) rawData.value(4);
        content = rawData.stringValue(5);
        url = rawData.stringValue(6);
        clusterID = -1;

    }

    public ForumPost(String[] fields){

    }

    @Override
    public String toString(){
        return Integer.toString(questionID);
    }

    public int getDetailsID() {
        return detailsID;
    }

    public void setDetailsID(int detailsID) {
        this.detailsID = detailsID;
    }

    public int getQuestionID() {
        return questionID;
    }

    public void setQuestionID(int questionID) {
        this.questionID = questionID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getClusterID() {
        return clusterID;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }
}
