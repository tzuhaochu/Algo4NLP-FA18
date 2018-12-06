package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.assignments.align.AlignmentTester;
import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Pair;

import java.util.Set;

public class CoModel1Aligner implements WordAligner {

    private Model1Aligner aligner1, aligner2;

    public CoModel1Aligner(Iterable<SentencePair> trainingData) {
        System.out.println("French to English...");
        aligner1 = new Model1Aligner(trainingData);
        System.out.println("English to French...");
        aligner2 = new Model1Aligner(AlignmentTester.reverse(trainingData));
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        Alignment alignment = new Alignment();
        Set<Pair<Integer, Integer>> alignment1 = aligner1.alignSentencePair(sentencePair).getSureAlignments();
        Set<Pair<Integer, Integer>> alignment2 = aligner2.alignSentencePair(sentencePair.getReversedCopy()).getSureAlignments();
        for (Pair<Integer, Integer> pair : alignment1){
            if (alignment2.contains(new Pair<>(pair.getSecond(), pair.getFirst()))){
                alignment.addAlignment(pair.getFirst(), pair.getSecond(), true);
            }
        }
        return alignment;
    }
}