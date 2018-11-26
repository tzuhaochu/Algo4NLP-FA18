package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Pair;

import java.util.List;

public class HeuristicAligner implements WordAligner {
    Counter<Pair<String, String>> feCounter;
    Counter<String> fCounter;
    Counter<String> eCounter;

    public HeuristicAligner(Iterable<SentencePair> trainingData) {
        feCounter = new Counter<>();
        fCounter = new Counter<>();
        eCounter = new Counter<>();
        for (SentencePair pair : trainingData) {
            List<String> frWords = pair.getFrenchWords();
            List<String> enWords = pair.getEnglishWords();
            for (String frWord : frWords) {
                fCounter.incrementCount(frWord, 1.0);
            }
            for (String enWord : enWords) {
                eCounter.incrementCount(enWord, 1.0);
                for (String frWord : frWords) {
                    feCounter.incrementCount(new Pair<>(frWord, enWord), 1.0);
                }
            }
        }
    }

    @Override
    public Alignment alignSentencePair(SentencePair sentencePair) {
        Alignment alignment = new Alignment();
        List<String> frWords = sentencePair.getFrenchWords();
        List<String> enWords = sentencePair.getEnglishWords();
        for (int i = 0; i < frWords.size(); i++) {
            String frWord = frWords.get(i);
            double maxP = 0.0;
            int maxEPos = 0;
            for (int j = 0; j < enWords.size(); j++) {
                String enWord = enWords.get(j);
                Pair<String, String> pair = new Pair<>(frWord, enWord);
                double P = feCounter.getCount(pair) / (eCounter.getCount(enWord) * fCounter.getCount(frWord));
                if(maxP < P){
                    maxP = P;
                    maxEPos = j;
                }
            }
            alignment.addAlignment(maxEPos, i, true);
        }
        return alignment;
    }
}
