package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.LBFGSMinimizer;
import edu.berkeley.nlp.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class AwesomeParsingReranker extends PrimeParsingReranker {
    double tolerence = 1e-3;
    double lambda = 1e-6;

    public AwesomeParsingReranker(Iterable<Pair<KbestList, Tree<String>>> kbestListsAndGoldTrees) {
        super();
        List<MaxEntDatum> trainingData = new ArrayList<MaxEntDatum>();
        for (Pair<KbestList, Tree<String>> pair : kbestListsAndGoldTrees) {
            MaxEntDatum datum = new MaxEntDatum();
            KbestList kbest = pair.getFirst();
            int bestIndex = getBestIndex(kbest, pair.getSecond());

            for (int index = 0; index < kbest.getKbestTrees().size(); index++) {
                int[] feats = featExtractor.extractFeatures(kbest, index, featIndexer, true);
                datum.features.add(feats);
                if (index == bestIndex)
                    datum.setGoldFeatures(feats);
            }
            trainingData.add(datum);
        }

        weights = initializeWeights(featIndexer.size());

        LBFGSMinimizer minimizer = new LBFGSMinimizer();
        MaxEntLoss maxEntLoss = new MaxEntLoss(trainingData, lambda);
        weights = minimizer.minimize(maxEntLoss, weights, tolerence);
    }
}
