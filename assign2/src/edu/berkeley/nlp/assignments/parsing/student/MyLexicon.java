package edu.berkeley.nlp.assignments.parsing.student;

import java.util.List;
import java.util.Set;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.CounterMap;

public final class MyLexicon {

    CounterMap<String, String> wordToTagCounters = new CounterMap<String, String>();

    int totalTokens = 0;

    int totalWordTypes = 0;

    Counter<String> tagCounter = new Counter<String>();

    Counter<String> wordCounter = new Counter<String>();

    Counter<String> typeTagCounter = new Counter<String>();

    public MyLexicon(List<Tree<String>> trainTrees) {
        for (Tree<String> trainTree : trainTrees) {
            List<String> words = trainTree.getYield();
            List<String> tags = trainTree.getPreTerminalYield();
            for (int position = 0; position < words.size(); position++) {
                String word = words.get(position);
                String tag = tags.get(position);
                tallyTagging(word, tag);
            }
        }
    }

    public Set<String> getAllTags() {
        return tagCounter.keySet();
    }

    public boolean isKnown(String word) {
        return wordCounter.keySet().contains(word);
    }

    public double scoreTagging(String word, String tag) {
        int totalTokens = this.getTotalTokens();
        double p_tag = this.getTagCounter().getCount(tag) / totalTokens;
        double c_word = this.getWordCounter().getCount(word);
        double c_tag_and_word = this.getWordToTagCounters().getCount(word, tag);
        if (c_word < 10) {
            c_word += 1.0;
            c_tag_and_word += this.getTypeTagCounter().getCount(tag) / this.getTotalWordTypes();
        }
        double p_word = (0.75 + c_word) / (totalTokens + 1.0);
        double p_tag_given_word = c_tag_and_word / c_word;
        double score = Math.log(p_tag_given_word / p_tag * p_word);
        return score;
    }

    private void tallyTagging(String word, String tag) {
        if (!isKnown(word)) {
            totalWordTypes += 1.0;
            typeTagCounter.incrementCount(tag, 1.0);
        }
        totalTokens += 1.0;
        tagCounter.incrementCount(tag, 1.0);
        wordCounter.incrementCount(word, 1.0);
        wordToTagCounters.incrementCount(word, tag, 1.0);
    }


    private int getTotalTokens() {
        return totalTokens;
    }

    private Counter<String> getTypeTagCounter() {
        return typeTagCounter;
    }

    private double getTotalWordTypes() {
        return totalWordTypes;
    }

    private Counter<String> getTagCounter() {
        return tagCounter;
    }

    private Counter<String> getWordCounter() {
        return wordCounter;
    }

    private CounterMap<String, String> getWordToTagCounters() {
        return wordToTagCounters;
    }

}