package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.Pair;
import edu.berkeley.nlp.util.StringIndexer;

import java.util.List;

public class HmmAligner implements WordAligner {
    private static StringIndexer enIndexer = new StringIndexer();
    private static StringIndexer frIndexer = new StringIndexer();
    private static Indexer<Pair<String, String>> pairIndexer = new Indexer<>();
    private final static double TOLERANCE0 = 1e-5;
    private final static double TOLERANCE1 = 1e-2;
    private final static int MAX_ITER = 20;
    private final static String NULL = "<NULL>";

    private int num_e, num_f, num_ef;

    private double[][] alpha, beta, gamma, psi;
    private double[][][] theta;

    public HmmAligner(Iterable<SentencePair> trainingData) {
        // Indexing words
        enIndexer.add(NULL);
        for (SentencePair pair : trainingData) {
            List<String> frWords = pair.getFrenchWords();
            List<String> enWords = pair.getEnglishWords();
            enWords.add(0, NULL);
            for (String e : enWords) enIndexer.add(e);
            for (String f : frWords) frIndexer.add(f);
            for (String f : frWords)
                for (String e : enWords)
                    pairIndexer.add(new Pair<>(e, f));
        }
        num_e = enIndexer.size();
        num_f = frIndexer.size();
        num_ef = pairIndexer.size();
        System.out.println("e|f number: " + num_ef);
        System.out.println("e number: " + num_e);
        System.out.println("f number: " + num_f);

        // HMM training
        HMM(trainingData);
    }

    private void HMM(Iterable<SentencePair> trainingData){
        theta = new double[num_f][num_e][]
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        return null;
    }
}
