package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HmmAligner implements WordAligner {
    private static StringIndexer enIndexer = new StringIndexer();
    private static StringIndexer frIndexer = new StringIndexer();
    private static Indexer<Pair<String, String>> pairIndexer = new Indexer<>();
    private final static int MAX_ITER = 20;
    private final static double DISTORTION_LIKELIHOOD = 0.2;
    private final static int NID = -1;

    private int num_e, num_f, num_ef;
    private CounterMap<Integer, Integer> efCounter;

    public HmmAligner(Iterable<SentencePair> trainingData) {
        efCounter = new CounterMap<>();
        // Indexing words
        for (SentencePair pair : trainingData) {
            List<String> frWords = pair.getFrenchWords();
            List<String> enWords = pair.getEnglishWords();
            int Lf = pair.frenchWords.size();
            int Le = pair.englishWords.size();

            for (String e : enWords) enIndexer.add(e);
            for (String f : frWords) frIndexer.add(f);
            for (String f : frWords) {
                int fid = frIndexer.indexOf(f);
                efCounter.setCount(NID, fid, 1);
                for (String e : enWords) {
                    int eid = enIndexer.indexOf(e);
                    pairIndexer.add(new Pair<>(e, f));
                    efCounter.incrementCount(eid, fid, 1);
                }
            }
        }
        num_e = enIndexer.size();
        num_f = frIndexer.size();
        num_ef = pairIndexer.size();
        efCounter.normalize();
        System.out.println("e|f number: " + num_ef);
        System.out.println("e number: " + num_e);
        System.out.println("f number: " + num_f);

        // HMM training
        trainHMM(trainingData);
    }

    private double[][] getGamma(List<String> frWords, List<String> enWords) {
        int Lf = frWords.size();
        int Le = enWords.size();

        double[][] alpha = new double[Lf][Le + 1];
        double[][] gamma = new double[Lf][Le + 1];

        double[][] trans = new double[Le + 1][Le + 1];
        for (int i = 0; i < Le; i++) {
            trans[i][Le] = DISTORTION_LIKELIHOOD;
            trans[Le][i] = (1 - DISTORTION_LIKELIHOOD) / Le;
            double norm = 0;
            for (int j = 0; j < Le; j++) {
                trans[i][j] = Math.exp(- 2 * Math.abs(j - i - 1.1));
                norm += trans[i][j];
            }
            for (int j = 0; j < Le; j++) {
                trans[i][j] = trans[i][j] * (1 - DISTORTION_LIKELIHOOD) / norm;
            }
        }

        int fstFid = frIndexer.addAndGetIndex(frWords.get(0));

        // Generate the English word index. Use the last one as NULL
        // alignment. Also set the initial alpha.
        double normAlphaZero = 0;
        for (int j = 0; j < Le; j++) {
            String e = enWords.get(j);
            int eid = enIndexer.indexOf(e);
            alpha[0][j] = efCounter.getCount(eid, fstFid) * trans[j][Le];
            normAlphaZero += alpha[0][j];
        }

        alpha[0][Le] = efCounter.getCount(-1, fstFid) * trans[Le][Le];
        normAlphaZero += alpha[0][Le];
        if (normAlphaZero != 0) {
            for (int j = 0; j <= Le; j++) {
                alpha[0][j] /= normAlphaZero;
            }
        }

        // Calculate alpha by going forward.
        for (int i = 1; i < Lf; i++) {
            String f = frWords.get(i);
            int fid = frIndexer.indexOf(f);
            double norm = 0;

            for (int j1 = 0; j1 <= Le; j1++) {
                int eid = j1 == Le ? -1 : enIndexer.indexOf(enWords.get(j1));
                for (int j2 = 0; j2 <= Le; j2++) {
                    alpha[i][j1] += alpha[i - 1][j2] * trans[j2][j1];
                }
                alpha[i][j1] *= efCounter.getCount(eid, fid);
                norm += alpha[i][j1];
            }
            if (norm > 0) {
                for (int j = 0; j <= Le; j++) {
                    alpha[i][j] /= norm;
                }
            }
        }

        System.arraycopy(alpha[Lf - 1], 0, gamma[Lf - 1], 0, Le);
        double[] normFactors = new double[Le + 1];

        for (int i = Lf - 2; i >= 0; i--) {
            for (int j1 = 0; j1 <= Le; j1++) {
                for (int j2 = 0; j2 <= Le; j2++) {
                    normFactors[j1] += alpha[i][j2] * trans[j2][j1];
                }
            }
            for (int j1 = 0; j1 <= Le; j1++) {
                for (int j2 = 0; j2 <= Le; j2++) {
                    if (normFactors[j2] == 0) {
                        gamma[i][j1] = 0;
                    } else {
                        gamma[i][j1] += alpha[i][j1] * trans[j1][j2] * gamma[i + 1][j2] / normFactors[j2];
                    }
                }
            }
        }

        return gamma;
    }

    private void trainHMM(Iterable<SentencePair> trainingData) {
        for (int iter = 0; iter < MAX_ITER; iter++) {
            CounterMap<Integer, Integer> tmpCounter = new CounterMap<>();
            for (SentencePair pair : trainingData) {
                List<String> frWords = pair.getFrenchWords();
                List<String> enWords = pair.getEnglishWords();
                double[][] gamma = getGamma(frWords, enWords);
                for (int i = 0; i < frWords.size(); i++) {
                    String f = frWords.get(i);
                    int fid = frIndexer.indexOf(f);
                    for (int j = 0; j < enWords.size(); j++) {
                        String e = enWords.get(j);
                        int eid = enIndexer.indexOf(e);
                        tmpCounter.incrementCount(eid, fid, gamma[i][j]);
                    }
                }
            }
            for (Integer k : tmpCounter.keySet()) {
                Counter<Integer> counter = tmpCounter.getCounter(k);
                if (counter.totalCount() > 0) {
                    counter.normalize();
                }
            }
            efCounter = tmpCounter;
        }
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        List<String> frWords = sentencePair.getFrenchWords();
        List<String> enWords = sentencePair.getEnglishWords();
        int Le = sentencePair.englishWords.size();
        int Lf = sentencePair.frenchWords.size();
        Alignment alignment = new Alignment();
        double[][] gamma = getGamma(frWords, enWords);

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
