package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.List;

public class MaxEntDatum implements Datum {

    int[] goldFeatures;
    List<int[]> features = new ArrayList<>();

    public void setGoldFeatures(int[] goldFeatures) {
        this.goldFeatures = goldFeatures;
    }

    @Override
    public void insertFeature(int[] feats) {
        features.add(feats);
    }
}