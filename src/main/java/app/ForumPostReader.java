package app;

import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by priyankitbangia on 22/10/17.
 */
public class ForumPostReader {

    public ForumPostReader(){}

    public Instances loadData(String filename) {
        Instances instances = null;
        try {
            instances =  new Instances(new BufferedReader(new FileReader(filename)));
        } catch (IOException e) { e.printStackTrace(); }
        return instances;
    }

}
