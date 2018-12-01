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
    private final static double TOLERANCE0 = 1e-5;
    private final static double TOLERANCE1 = 1e-2;
    private final static int MAX_ITER = 20;
    private final static String NULL = "<NULL>";

    private double[] T;
    private int num_e, num_f, num_ef;

    public Model1Aligner(Iterable<SentencePair> trainingData) {
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

        // EM Training
        em1(trainingData);
    }

    private void em1(Iterable<SentencePair> trainingData){
        double[] c_p = new double[num_ef];
        double[] c_e = new double[num_e];
        T = new double[num_ef];
        Arrays.fill(T, 1.0 / num_f);

        double delta = Double.MAX_VALUE;
        for (int iter = 0; iter < MAX_ITER && delta > TOLERANCE1; iter++) {
            Arrays.fill(c_p, 0.0);
            double[] pre_T = T.clone();
            for (SentencePair pair : trainingData) {
                List<String> frWords = pair.getFrenchWords();
                List<String> enWords = pair.getEnglishWords();
                enWords.add(0, NULL);
                double[] z = new double[num_f];
                for (String f : frWords) {
                    int fid = frWords.indexOf(f);
                    for (String e : enWords) {
                        int eid = enIndexer.indexOf(e);
                        int pid = pairIndexer.indexOf(new Pair<>(e, f));
                        z[fid] += prior(eid, enWords.size()) * T[pid];
                    }
                }

                for (String e : enWords) {
                    int eid = enIndexer.indexOf(e);
                    for (String f : frWords) {
                        int fid = frWords.indexOf(f);
                        int pid = pairIndexer.indexOf(new Pair<>(e, f));
                        double d = prior(eid, enWords.size()) * T[pid] / z[fid];
                        c_p[pid] += d;
                        c_e[eid] += d;
                    }
                }
            }
            for (int fid = 0; fid < num_f; fid++) {
                for (int eid = 0; eid < num_e; eid++) {
                    int pid = pairIndexer.indexOf(new Pair<>(enIndexer.get(eid), frIndexer.get(fid)));
                    if (pid < 0) continue;
                    T[pid] = c_p[pid] / c_e[eid];
                }
            }
            // Computing delta
            int convergedNum = 0;
            for (int i = 0; i < T.length; i++) {
                if (Math.abs(pre_T[i] - T[i]) < TOLERANCE0) convergedNum += 1;
            }
            delta = 1.0 - (double) convergedNum / T.length;
            System.out.println(String.format("ITER: %02d/%02d\tdelta: %1.10f > %f", iter, MAX_ITER, delta, TOLERANCE1));
        }
    }

    private void em2(Iterable<SentencePair> trainingData){
        double[] count = new double[num_ef];
        double[] total = new double[num_f];
        double[] total_s = new double[num_e];
        T = new double[num_ef];
        Arrays.fill(T, 1.0 / num_ef);

        double delta = Double.MAX_VALUE;
        for(int iter = 0; iter < MAX_ITER && delta > TOLERANCE1; iter++){
            Arrays.fill(count, 0.0);
            Arrays.fill(total, 0.0);
            double[] pre_T = T.clone();
            for (SentencePair pair : trainingData) {
                List<String> frWords = pair.getFrenchWords();
                List<String> enWords = pair.getEnglishWords();
                enWords.add(0, NULL);
                for (String e : enWords) {
                    int eid = enIndexer.indexOf(e);
                    total_s[eid] = 0;
                    for (String f : frWords) {
                        int fid = frWords.indexOf(f);
                        int pid = pairIndexer.indexOf(new Pair<>(e, f));
                        total_s[eid] += T[pid];
                    }
                }
                for (String e : enWords) {
                    int eid = enIndexer.indexOf(e);
                    for (String f : frWords) {
                        int fid = frWords.indexOf(f);
                        int pid = pairIndexer.indexOf(new Pair<>(e, f));
                        count[pid] += T[pid] / total_s[eid];
                        total[fid] += T[pid] / total_s[eid];
                    }
                }
            }
            for (int fid = 0; fid < num_f; fid++) {
                for (int eid = 0; eid < num_e; eid++) {
                    int pid = pairIndexer.indexOf(new Pair<>(enIndexer.get(eid), frIndexer.get(fid)));
                    if (pid < 0 || total[fid] == 0.0) continue;
                    T[pid] = count[pid] / total[fid];
                }
            }
            // Computing delta
            int convergedNum = 0;
            for(int i = 0; i < T.length; i++){
                if(Math.abs(pre_T[i] - T[i]) < TOLERANCE0) convergedNum += 1;
            }
            delta = 1.0 - (double) convergedNum / T.length;
            System.out.println(String.format("ITER: %02d/%02d\tdelta: %1.10f > %f", iter, MAX_ITER, delta, TOLERANCE1));
        }
    }

    private double prior(int i, int len) {
        return i == enIndexer.indexOf(NULL) ? 0.2 : 0.8 / len;
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        List<String> frWords = sentencePair.getFrenchWords();
        List<String> enWords = sentencePair.getEnglishWords();
        enWords.add(0, NULL);
        Alignment alignment = new Alignment();
        for (int i = 0; i < frWords.size(); i++) {
            String f = frWords.get(i);
            double maxScore = 0.0;
            int maxEnPos = 0;
            for (int j = 0; j < enWords.size(); j++) {
                String e = enWords.get(j);
                int eid = enIndexer.indexOf(e);
                int pid = pairIndexer.indexOf(new Pair<>(e, f));
                if (maxScore < T[pid]) {
                    maxScore = T[pid];
                    maxEnPos = j;
                }
            }
            alignment.addAlignment(maxEnPos, i, true);
        }
        return alignment;
    }
}
