package Clustering;

import weka.core.Stopwords;

import java.util.*;

/**
 * Created by priyankitbangia on 25/10/17.
 */
public class TextSummariser {

    public Map<String, Integer> getWordCountMap(String words){
        Map<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        words = words.replace(",","");
        words = words.replace("\"","");

        String[] wordArray = words.split(" ");
        for (String w : wordArray) {
            if (Stopwords.isStopword(w)) continue;

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
        int maxSize = Math.min(numOfWords, sortedWords.size());

        return sortedWords.subList(0, maxSize);
    }
}
