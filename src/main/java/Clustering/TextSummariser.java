package Clustering;

import weka.core.Stopwords;

import java.util.*;

/**
 * Created by priyankitbangia on 25/10/17.
 */
public class TextSummariser {

    public Map<String, Integer> getWordCountMap(String words){
        Map<String, Integer> map = new HashMap<>();

        return map;
    }

    public List<String> getSortedTopWords(String words, int numOfWords){
        Map<String, Integer> wordCounts = getWordCountMap(words);
        List<String> sortedWords = new ArrayList<String>(wordCounts.keySet());
       

        return sortedWords.subList(0, numOfWords);
    }
}
