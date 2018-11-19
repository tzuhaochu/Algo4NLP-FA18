package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel;
import edu.berkeley.nlp.util.IntCounter;

public class SVMModel implements LossAugmentedLinearModel {

    public double score(IntCounter weights, IntCounter features) {
        double score = 0;
        for (int index : features.keySet()) {
            score = score + weights.get(index) * features.get(index);
        }
        return score;
    }

    @Override
    public UpdateBundle getLossAugmentedUpdateBundle(Object datumObject, IntCounter weights) {
        SVMDatum datum = (SVMDatum) datumObject;
        int goldIndex = datum.goldIndex;
        double goldScore = score(weights, datum.features.get(goldIndex));
        double maxScore = goldScore;
        int maxIndex = goldIndex;
        int lossOfGuess = 0;

        for (int i = 1; i < datum.features.size(); i++) {
            if (i != goldIndex) {
                double thisScore = score(weights, datum.features.get(i));
                double thisOneMinusF1Loss = datum.oneMinusF1.get(i);

                if (thisScore + thisOneMinusF1Loss > maxScore) {
                    maxIndex = i;
                    maxScore = thisScore;
                }
            }
        }
        if (maxIndex != goldIndex) lossOfGuess = 1;
        return new UpdateBundle(datum.features.get(goldIndex), datum.features.get(maxIndex), lossOfGuess);
    }

}