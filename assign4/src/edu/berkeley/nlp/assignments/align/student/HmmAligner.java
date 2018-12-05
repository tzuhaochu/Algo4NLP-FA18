package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.*;

import java.util.Arrays;
import java.util.List;

public final class HmmAligner implements WordAligner {
    private static StringIndexer enIndexer = new StringIndexer();
    private static StringIndexer frIndexer = new StringIndexer();
    private static Indexer<Pair<String, String>> pairIndexer = new Indexer<>();
    private final static int MAX_ITER = 5;
    private final static double DISTORTION_LIKELIHOOD = 0.2;
    private final static int NID = -1;

    private int num_e, num_f, num_ef;
    private CounterMap<Integer, Integer> theta;

    public HmmAligner(Iterable<SentencePair> trainingData) {
        // Indexing words
        theta = new CounterMap<>();
        for (SentencePair pair : trainingData) {
            List<String> frWords = pair.getFrenchWords();
            List<String> enWords = pair.getEnglishWords();
            int Lf = pair.frenchWords.size();
            int Le = pair.englishWords.size();

            for (String e : enWords) enIndexer.add(e);
            for (String f : frWords) frIndexer.add(f);
            for (String f : frWords) {
                int fid = frIndexer.indexOf(f);
                theta.setCount(NID, fid, 1);
                for (String e : enWords) {
                    int eid = enIndexer.indexOf(e);
                    pairIndexer.add(new Pair<>(e, f));
                    theta.incrementCount(eid, fid, 1);
                }
            }
        }
        num_e = enIndexer.size();
        num_f = frIndexer.size();
        num_ef = pairIndexer.size();
        theta.normalize();
        System.out.println("e|f number: " + num_ef);
        System.out.println("e number: " + num_e);
        System.out.println("f number: " + num_f);

        // HMM training
        trainHMM(trainingData);
    }

    private double[][] trainBySentence(List<String> frWords, List<String> enWords) {
        int Lf = frWords.size();
        int Le = enWords.size();

        double[][] alpha = new double[Lf][Le + 1];
        double[][] beta = new double[Lf][Le + 1];
        double[] Z = new double[Le + 1];

        double[][] P = new double[Le + 1][Le + 1];
        for (int i = 0; i < Le; i++) {
            P[i][Le] = DISTORTION_LIKELIHOOD;
            P[Le][i] = (1 - DISTORTION_LIKELIHOOD) / Le;
            double norm = 0;
            for (int j = 0; j < Le; j++) {
                P[i][j] = Math.exp(- 2 * Math.abs(j - i - 1.1));
                norm += P[i][j];
            }
            for (int j = 0; j < Le; j++) {
                P[i][j] = P[i][j] * (1 - DISTORTION_LIKELIHOOD) / norm;
            }
        }

        // forward
        int fid0 = frIndexer.indexOf(frWords.get(0));
        double norm = 0;
        for (int j = 0; j <= Le; j++) {
            int eid = j == Le ? -1 : enIndexer.indexOf(enWords.get(j));
            alpha[0][j] = theta.getCount(eid, fid0) * P[j][Le];
            norm += alpha[0][j];
        }
        if (norm > 0) for (int j = 0; j <= Le; j++) alpha[0][j] /= norm;

        for (int i = 1; i < Lf; i++) {
            int fid = frIndexer.indexOf(frWords.get(i));
            norm = 0;
            for (int j1 = 0; j1 <= Le; j1++) {
                int eid = j1 == Le ? -1 : enIndexer.indexOf(enWords.get(j1));
                for (int j2 = 0; j2 <= Le; j2++) {
                    alpha[i][j1] += alpha[i - 1][j2] * P[j2][j1];
                }
                alpha[i][j1] *= theta.getCount(eid, fid);
                norm += alpha[i][j1];
            }
            if (norm > 0) {
                for (int j = 0; j <= Le; j++) {
                    alpha[i][j] /= norm;
                }
            }
        }

        System.arraycopy(alpha[Lf - 1], 0, beta[Lf - 1], 0, Le);

        // backward
        for (int i = Lf - 2; i >= 0; i--) {
            for (int j1 = 0; j1 <= Le; j1++) {
                for (int j2 = 0; j2 <= Le; j2++) {
                    Z[j1] += alpha[i][j2] * P[j2][j1];
                }
            }
            for (int j1 = 0; j1 <= Le; j1++) {
                for (int j2 = 0; j2 <= Le; j2++) {
                    if (Z[j2] == 0) {
                        beta[i][j1] = 0;
                    } else {
                        beta[i][j1] += alpha[i][j1] * P[j1][j2] * beta[i + 1][j2] / Z[j2];
                    }
                }
            }
        }

        return beta;
    }

    private void trainHMM(Iterable<SentencePair> trainingData) {
        System.out.println("Start training...");
        for (int iter = 0; iter < MAX_ITER; iter++) {
            CounterMap<Integer, Integer> tmpTheta = new CounterMap<>();
            for (SentencePair pair : trainingData) {
                List<String> frWords = pair.getFrenchWords();
                List<String> enWords = pair.getEnglishWords();
                double[][] gamma = trainBySentence(frWords, enWords);
                for (int i = 0; i < frWords.size(); i++) {
                    String f = frWords.get(i);
                    int fid = frIndexer.indexOf(f);
                    for (int j = 0; j < enWords.size(); j++) {
                        String e = enWords.get(j);
                        int eid = enIndexer.indexOf(e);
                        tmpTheta.incrementCount(eid, fid, gamma[i][j]);
                    }
                }
            }
            for (Integer k : tmpTheta.keySet()) {
                Counter<Integer> counter = tmpTheta.getCounter(k);
                if (counter.totalCount() > 0) {
                    counter.normalize();
                }
            }
            theta = tmpTheta;
            System.out.println(String.format("ITER: %02d/%02d", iter, MAX_ITER));
        }
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        List<String> frWords = sentencePair.getFrenchWords();
        List<String> enWords = sentencePair.getEnglishWords();
        int Le = sentencePair.englishWords.size();
        int Lf = sentencePair.frenchWords.size();
        Alignment alignment = new Alignment();
        double[][] gamma = trainBySentence(frWords, enWords);

        for (int i = 0; i < Lf; i++) {
            int maxEnPos = Le;
            double maxScore = gamma[i][Le];
            for (int j = 0; j < Le; j++) {
                if (gamma[i][j] > maxScore) {
                    maxEnPos = j;
                    maxScore = gamma[i][j];
                }
            }
            if (maxEnPos != Le) {
                alignment.addAlignment(maxEnPos, i, true);
            }
        }
        return alignment;
    }
}
