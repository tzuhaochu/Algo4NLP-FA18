package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.*;

import java.util.List;

public final class HmmAligner implements WordAligner {
    private static StringIndexer enIndexer = new StringIndexer();
    private static StringIndexer frIndexer = new StringIndexer();
    private final static int MAX_ITER = 20;
    private final static double TOLERANCE0 = 1e-5;
    private final static double TOLERANCE1 = 5e-2;
    private final static double EPSILON = 0.9;
    private final static int NID = -1;
    private final static double A = 1.7;
    private final static double B = 1.1;

    private CounterMap<Integer, Integer> theta;

    public HmmAligner(Iterable<SentencePair> trainingData) {
        // Indexing words
        theta = new CounterMap<>();
        for (SentencePair pair : trainingData) {
            List<String> frWords = pair.getFrenchWords();
            List<String> enWords = pair.getEnglishWords();

            for (String e : enWords) enIndexer.add(e);
            for (String f : frWords) frIndexer.add(f);
            for (String f : frWords) {
                int fid = frIndexer.indexOf(f);
                theta.setCount(NID, fid, 1);
                for (String e : enWords) {
                    int eid = enIndexer.indexOf(e);
                    theta.incrementCount(eid, fid, 1);
                }
            }
        }
        theta.normalize();

        // HMM training
        trainHMM(trainingData);
    }

    private double[][] computeScore(List<String> frWords, List<String> enWords) {
        int Lf = frWords.size();
        int Le = enWords.size();
        double[][] alpha = new double[Lf][Le + 1];
        double[][] score = new double[Lf][Le + 1];
        double[] Z = new double[Le + 1];
        double[][] psi = new double[Le + 1][Le + 1];

        for (int i = 0; i < Le; i++) {
            psi[i][Le] = EPSILON;
            psi[Le][i] = (1 - EPSILON) / Le;
            double norm = 0;
            for (int j = 0; j < Le; j++) {
                psi[i][j] = Math.exp(- A * Math.abs(j - i - B));
                norm += psi[i][j];
            }
            for (int j = 0; j < Le; j++) {
                psi[i][j] = psi[i][j] * (1 - EPSILON) / norm;
            }
        }

        // forward
        for (int i = 0; i < Lf; i++) {
            int fid = frIndexer.indexOf(frWords.get(i));
            double norm = 0;
            for (int j1 = 0; j1 <= Le; j1++) {
                int eid = j1 == Le ? NID : enIndexer.indexOf(enWords.get(j1));
                if (i == 0) alpha[i][j1] = psi[j1][Le];
                else for (int j2 = 0; j2 <= Le; j2++) alpha[i][j1] += alpha[i - 1][j2] * psi[j2][j1];
                alpha[i][j1] *= theta.getCount(eid, fid);
                norm += alpha[i][j1];
            }
            if (norm > 0) for (int j = 0; j <= Le; j++) alpha[i][j] /= norm;
        }

        System.arraycopy(alpha[Lf - 1], 0, score[Lf - 1], 0, Le);

        // backward
        for (int i = Lf - 2; i >= 0; i--) {
            for (int j1 = 0; j1 <= Le; j1++)
                for (int j2 = 0; j2 <= Le; j2++)
                    Z[j1] += alpha[i][j2] * psi[j2][j1];
            for (int j1 = 0; j1 <= Le; j1++)
                for (int j2 = 0; j2 <= Le; j2++)
                    score[i][j1] = Z[j2] == 0 ? 0 : score[i][j1] + alpha[i][j1] * psi[j1][j2] * score[i + 1][j2] / Z[j2];
        }

        return score;
    }

    private void trainHMM(Iterable<SentencePair> trainingData) {
        double delta = Double.MAX_VALUE;
        for (int iter = 0; iter < MAX_ITER  && delta > TOLERANCE1; iter++) {
            CounterMap<Integer, Integer> new_theta = new CounterMap<>();
            for (SentencePair pair : trainingData) {
                List<String> frWords = pair.getFrenchWords();
                List<String> enWords = pair.getEnglishWords();
                double[][] score = computeScore(frWords, enWords);
                for (int i = 0; i < frWords.size(); i++) {
                    int fid = frIndexer.indexOf(frWords.get(i));
                    for (int j = 0; j < enWords.size(); j++) {
                        int eid = enIndexer.indexOf(enWords.get(j));
                        new_theta.incrementCount(eid, fid, score[i][j]);
                    }
                }
            }
            for (Integer k : new_theta.keySet()) {
                Counter<Integer> counter = new_theta.getCounter(k);
                if (counter.totalCount() > 0) {
                    counter.normalize();
                }
            }

            // Computing delta
            delta = Utils.computeDiff(theta, new_theta, TOLERANCE0);
            System.out.println(String.format("ITER: %02d/%02d\tdelta: %1.10f > %f", iter+1, MAX_ITER, delta, TOLERANCE1));
            theta = new_theta;
        }
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        List<String> frWords = sentencePair.getFrenchWords();
        List<String> enWords = sentencePair.getEnglishWords();
        int Lf = frWords.size();
        int Le = enWords.size();
        Alignment alignment = new Alignment();
        double[][] score = computeScore(frWords, enWords);
        for (int i = 0; i < Lf; i++) {
            int maxEnPos = Le;
            double maxScore = score[i][Le];
            for (int j = 0; j < Le; j++) {
                if (score[i][j] > maxScore) {
                    maxEnPos = j;
                    maxScore = score[i][j];
                }
            }
            if (maxEnPos != Le) alignment.addAlignment(maxEnPos, i, true);
        }
        return alignment;
    }
}
