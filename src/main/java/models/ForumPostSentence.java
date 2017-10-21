package models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by priyankitbangia on 22/10/17.
 */
@Entity( value = "forumpostsentences")
public class ForumPostSentence {
    @Id
    private int sentenceID;

    private String content;
    private int categoryID;
    private int forumPostID;
    
    public ForumPostSentence(){}

    public ForumPostSentence(int sentenceID, String content, int categoryID, int forumPostID) {
        this.sentenceID = sentenceID;
        this.content = content;
        this.categoryID = categoryID;
        this.forumPostID = forumPostID;
    }
}
