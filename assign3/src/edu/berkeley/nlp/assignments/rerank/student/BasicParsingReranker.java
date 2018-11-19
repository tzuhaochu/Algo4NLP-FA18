package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.PrimalSubgradientSVMLearner;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class BasicParsingReranker extends PrimeParsingReranker {
    double stepSize = 0.1;
    double C = 0.001;
    int maxIter = 30;

    public BasicParsingReranker(Iterable<Pair<KbestList, Tree<String>>> kbestListsAndGoldTrees) {
        super();
        List<SVMDatum> trainingData = new ArrayList<SVMDatum>();
        for (Pair<KbestList, Tree<String>> pair : kbestListsAndGoldTrees) {
            KbestList kbest = pair.getFirst();
            int bestIndex = getBestIndex(kbest, pair.getSecond());
            SVMDatum datum = new SVMDatum(bestIndex);

            for (int index = 0; index < kbest.getKbestTrees().size(); index++) {
                int[] feats = featExtractor.extractFeatures(kbest, index, featIndexer, true);
                double omf1 = 1.0d - eval.evaluateF1(kbest.getKbestTrees().get(index), kbest.getKbestTrees().get(bestIndex));
                datum.insertFeature(feats);
                datum.insertOneMinusF1(omf1);
            }
            trainingData.add(datum);
        }

        weights = initializeWeights(featIndexer.size());

        IntCounter wCounter = new IntCounter();
        for (int i = 0; i < weights.length; i++) {
            wCounter.incrementCount(i, 0);
        }
        PrimalSubgradientSVMLearner learner = new PrimalSubgradientSVMLearner(stepSize, C, featIndexer.size());
        wCounter = learner.train(wCounter, new SVMModel(), trainingData, maxIter);
        weights = wCounter.toArray(wCounter.size());
    }
}
