package Clustering;

import weka.core.Stopwords;

import java.util.*;

/**
 * Created by priyankitbangia on 25/10/17.
 */
public class TextSummariser {

    public Map<String, Integer> getWordCountMap(String words){
        Map<String, Integer> map = new HashMap<>();
        words = words.replace(",","");
        words = words.replace("\"","");

        String[] wordArray = words.split(" ");
        for (String w : wordArray) {

            Integer numOcurrences = map.get(w);
            numOcurrences = (numOcurrences == null) ? 1 : ++numOcurrences;
            map.put(w, numOcurrences);
        }
        return map;
    }

    public List<String> getSortedTopWords(String words, int numOfWords){
        Map<String, Integer> wordCounts = getWordCountMap(words);
        List<String> sortedWords = new ArrayList<String>(wordCounts.keySet());
        Collections.sort(sortedWords, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Integer firstWordCount = wordCounts.get(o1);
                Integer secondWordCount = wordCounts.get(o2);
                return secondWordCount.compareTo(firstWordCount);
            }
        });

        return sortedWords.subList(0, numOfWords);
    }
}
