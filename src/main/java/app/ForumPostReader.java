package app;

import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Handles the reading of forum .arff files.
 * Created by priyankitbangia on 22/10/17.
 */
public class ForumPostReader {

    public ForumPostReader(){}

    /**
     * Loads the data from file to Instances object.
     * @param filename the path of the file to be read
     * @return an Instances object containing the processed file data
     */
    public Instances loadData(String filename) {
        Instances instances = null;
        try {
            instances =  new Instances(new BufferedReader(new FileReader(filename)));
        } catch (IOException e) { e.printStackTrace(); }
        return instances;
    }

}
