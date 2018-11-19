package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.util.IntCounter;

import java.util.ArrayList;

public class SVMDatum implements Datum {
    int goldIndex;
    ArrayList<IntCounter> features;
    ArrayList<Double> oneMinusF1;

    public SVMDatum(int goldIndex) {
        this.goldIndex = goldIndex;
        this.features = new ArrayList<>();
        this.oneMinusF1 = new ArrayList<>();
    }

    @Override
    public void insertFeature(int[] feats) {
        IntCounter featsIC = new IntCounter();
        for (int i = 0; i < feats.length; i++) {
            featsIC.incrementCount(feats[i], 1);
        }
        this.features.add(featsIC);
    }

    public void insertOneMinusF1(double thisOneMinusF1) {
        this.oneMinusF1.add(thisOneMinusF1);
    }

}
