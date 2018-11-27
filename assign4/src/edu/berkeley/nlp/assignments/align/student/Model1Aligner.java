package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.Pair;
import edu.berkeley.nlp.util.StringIndexer;

import java.util.Arrays;
import java.util.List;

public class Model1Aligner implements WordAligner {

    private static StringIndexer enIndexer = new StringIndexer();
    private static StringIndexer frIndexer = new StringIndexer();
    private static Indexer<Pair<String, String>> pairIndexer = new Indexer<>();
    private final static double TOLERANCE = 1e-5;

    private double[] t_ef, total, s_total, count;
    private int num_e, num_f, num_ef;

    public Model1Aligner(Iterable<SentencePair> trainingData){
        for(SentencePair pair : trainingData){
            List<String> frWords = pair.getFrenchWords();
            List<String> enWords = pair.getEnglishWords();
            for(String e: enWords) enIndexer.add(e);
            for(String f: frWords) frIndexer.add(f);
            for(String f: frWords)
                for(String e: enWords)
                    pairIndexer.add(new Pair<>(e, f));
        }
        num_e = enIndexer.size();
        num_f = frIndexer.size();
        num_ef = pairIndexer.size();
        System.out.println("e|f number: " + num_ef);
        System.out.println("e number: " + num_e);
        System.out.println("f number: " + num_f);
        count = new double[num_ef];
        total = new double[num_f];
        s_total = new double[num_e];
        t_ef = new double[num_ef];
        Arrays.fill(t_ef, 1.0/num_ef);
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        return null;
    }
}
