package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.*;

import java.util.List;

public class Model1Aligner implements WordAligner {
    private static StringIndexer enIndexer = new StringIndexer();
    private static StringIndexer frIndexer = new StringIndexer();
//    private static Indexer<Pair<String, String>> pairIndexer = new Indexer<>();
    private final static double TOLERANCE0 = 1e-5;
    private final static double TOLERANCE1 = 0.15;
    private final static int MAX_ITER = 30;
    private final static double DISTORTION_LIKELIHOOD = 0.28;
    private final static int NID = -1;

    private CounterMap<Integer, Integer> T;
//    private double[] T;

    private int num_e, num_f;
//    private int num_ef;

    public Model1Aligner(Iterable<SentencePair> trainingData) {
        T = new CounterMap<>();
        // Indexing words
        for (SentencePair pair : trainingData) {
            List<String> frWords = pair.getFrenchWords();
            List<String> enWords = pair.getEnglishWords();
            for (String e : enWords) enIndexer.add(e);
            for (String f : frWords) frIndexer.add(f);
            for (String f : frWords) {
                int fid = frIndexer.indexOf(f);
                T.setCount(NID, fid, 1);
                for (String e : enWords) {
                    int eid = enIndexer.indexOf(e);
//                    pairIndexer.add(new Pair<>(f, e));
                    T.incrementCount(eid, fid, 1);
                }
            }
        }
        num_e = enIndexer.size();
        num_f = frIndexer.size();
//        num_ef = pairIndexer.size();
//        System.out.println("e|f number: " + num_ef);
        System.out.println("e number: " + num_e);
        System.out.println("f number: " + num_f);

        // EM Training
        trainEM(trainingData);
    }

    private void trainEM(Iterable<SentencePair> trainingData) {
        double delta = Double.MAX_VALUE;
        for (int iter = 0; iter < MAX_ITER && delta > TOLERANCE1; iter++) {
            CounterMap<Integer, Integer> pre_T = new CounterMap<>();
            Counter<Integer> total = new Counter<>();

            for (SentencePair sentencePair : trainingData) {
                List<String> frWords = sentencePair.getFrenchWords();
                List<String> enWords = sentencePair.getEnglishWords();
                int Le = enWords.size();
                double[] Z = new double[Le + 1];

                for (String f : frWords) {
                    int fid = frIndexer.indexOf(f);
                    double total_s = 0;
                    for (int i = 0; i < Le; i++) {
                        int eid = enIndexer.indexOf(enWords.get(i));
                        double d = prior(Le) * T.getCount(eid, fid);
                        Z[i] = d;
                        total_s += d;
                    }
                    Z[Le] = DISTORTION_LIKELIHOOD * T.getCount(-1, fid);
                    total_s += Z[Le];
                    for (int i = 0; i <= Le; i++) {
                        int eid = i == Le ? NID : enIndexer.indexOf(enWords.get(i));
                        pre_T.incrementCount(eid, fid, Z[i] / total_s);
                        total.incrementCount(eid, Z[i] / total_s);
                    }
                }
            }
            pre_T.normalize();

            // Computing delta
            int convergedNum = 0;
            int totalNum = 0;
            for (Integer k : T.keySet()) {
                for (Integer v : T.getCounter(k).keySet()) {
                    totalNum += 1;
                    if (Math.abs(pre_T.getCount(k, v) - T.getCount(k, v)) < TOLERANCE0) convergedNum += 1;
                }
            }
            delta = 1.0 - (double) convergedNum / totalNum;
            T = pre_T;
            System.out.println(String.format("ITER: %02d/%02d\tdelta: %1.10f > %f", iter+1, MAX_ITER, delta, TOLERANCE1));
        }
    }

    private double prior(int Le) {
        return (1.0 - DISTORTION_LIKELIHOOD) / (Le + 1);
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        List<String> frWords = sentencePair.getFrenchWords();
        List<String> enWords = sentencePair.getEnglishWords();
        int Lf = frWords.size();
        int Le = enWords.size();
        Alignment alignment = new Alignment();
        for (int j = 0; j < Lf; j++) {
            int fid = frIndexer.indexOf(frWords.get(j));
            double maxScore = DISTORTION_LIKELIHOOD * T.getCount(-1, fid);
            int maxEnPos = Le;
            for (int i = 0; i < Le; i++) {
                int eid = enIndexer.indexOf(enWords.get(i));
                double score = prior(Le) * T.getCount(eid, fid);
                if (score > maxScore) {
                    maxScore = score;
                    maxEnPos = i;
                }
            }
            if (maxEnPos != Le) alignment.addAlignment(maxEnPos, j, true);
        }
        return alignment;
    }
}
