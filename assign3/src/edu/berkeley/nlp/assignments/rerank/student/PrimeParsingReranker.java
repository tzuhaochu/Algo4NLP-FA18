package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator.LabeledConstituentEval;
import edu.berkeley.nlp.util.Indexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class PrimeParsingReranker implements ParsingReranker {
    LabeledConstituentEval<String> eval;
    double scale = 1e-4;
    double[] weights = null;
    static Indexer<String> featIndexer = new Indexer<String>();
    MyFeatureExtractor featExtractor;
    Random rand;

    public PrimeParsingReranker() {
        this.eval = new LabeledConstituentEval<>(new HashSet<>(Arrays.asList("ROOT")), new HashSet<>());
        this.featExtractor = new MyFeatureExtractor();
        this.rand = new Random();
    }

    @Override
    public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
        Tree<String> bestTree = null;
        double bestScore = -1.0 * Double.MAX_VALUE;
        for (int index = 0; index < kbestList.getKbestTrees().size(); index++) {
            int[] feats = featExtractor.extractFeatures(kbestList, index, featIndexer, false);
            double score = 0;
            for (int feat : feats)
                score += weights[feat];
            if (score > bestScore) {
                bestScore = score;
                bestTree = kbestList.getKbestTrees().get(index);
            }
        }
        return bestTree;
    }

    protected double[] initializeWeights(int size) {
        double[] weights = new double[size];
        for (int index = 0; index < size; index++) weights[index] = rand.nextDouble() * scale;
        return weights;
    }

    protected int getBestIndex(KbestList kbest, Tree<String> gold) {
        double bestF1 = Double.MIN_VALUE;
        int bestTreeIndex = 0;

        for (int index = 0; index < kbest.getKbestTrees().size(); index++) {
            Tree<String> tree = kbest.getKbestTrees().get(index);
            if (gold.hashCode() == tree.hashCode())
                return index;
            else {
                double thisF1 = eval.evaluateF1(tree, gold);
                if (bestF1 < thisF1) {
                    bestF1 = thisF1;
                    bestTreeIndex = index;
                }
            }
        }
        return bestTreeIndex;
    }
}
