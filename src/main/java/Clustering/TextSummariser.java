package Clustering;

import weka.core.Stopwords;

import java.util.*;

/**
 * Processes a string of words and creates a summarised string.
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

    /**
     * Takes in a string of words and extract the most popular words to form a summarised string.
     * @param words a string of words to be processed
     * @param numOfWords number of max words to set for the summarised string
     * @return a list that is ordered by frequency of words
     */
    public String getSortedTopWords(String words, int numOfWords){
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

        List<String> wordList = sortedWords.subList(0, maxSize);
        return String.join(" ", wordList);
    }
}
