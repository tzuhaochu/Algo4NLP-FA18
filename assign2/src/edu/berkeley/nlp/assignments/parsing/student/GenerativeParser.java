package edu.berkeley.nlp.assignments.parsing.student;

import edu.berkeley.nlp.assignments.parsing.*;
import edu.berkeley.nlp.assignments.parsing.BinaryRule;
import edu.berkeley.nlp.assignments.parsing.UnaryRule;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

import java.util.ArrayList;
import java.util.List;

public class GenerativeParser implements Parser {

    Indexer<String> indexer;
    MyLexicon lexicon;
    MyGrammar grammar;
    UnaryClosure closure;
    double[][][] uScores, bScores;
    int[][][] uBacks, bSplitBacks, bLeftBacks, bRightBacks;
    int maxLen = 42;
    int numLbl;
    double NEG_INF = Double.NEGATIVE_INFINITY;

    public GenerativeParser(List<Tree<String>> trainTrees) {

        List<Tree<String>> trees = new ArrayList<>();
        for (Tree<String> tree : trainTrees) {
            trees.add(FineBinarizer.binarize(tree).toTree());
        }
        grammar = new MyGrammar(trees);
        lexicon = new MyLexicon(trees);

        indexer = grammar.getLabelIndexer();

        System.out.println(indexer.size());
        System.out.println(grammar.getUnaryRules().size());
        System.out.println(grammar.getBinaryRules().size());

        closure = new UnaryClosure(grammar.getLabelIndexer(), grammar.getUnaryRules());

        numLbl = indexer.size();

        uScores = new double[maxLen][maxLen][numLbl];
        bScores = new double[maxLen][maxLen][numLbl];

        uBacks = new int[maxLen][maxLen][numLbl];
        bSplitBacks = new int[maxLen][maxLen][numLbl];
        bLeftBacks = new int[maxLen][maxLen][numLbl];
        bRightBacks = new int[maxLen][maxLen][numLbl];
    }

    public Tree<String> backtrackU(int i, int j, int a, List<String> list) {
        int b = uBacks[i][j][a];
        Tree<String> treeAsB = backtrackB(i, j, b, list);
        List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();

        List<Integer> pathAtoB = closure.getPath(new UnaryRule(a, indexer.indexOf(treeAsB.getLabel())));
        if (pathAtoB.size() == 1) {
            treesUnderA.addAll(treeAsB.getChildren());
        } else if (pathAtoB.size() == 2) {
            treesUnderA.add(treeAsB);
        } else if (pathAtoB.size() > 2) {

            Tree<String> prev = treeAsB;

            for (int ind = pathAtoB.size() - 2; ind >= 1; ind--) {
                int intermediate = pathAtoB.get(ind);
                List<Tree<String>> inters = new ArrayList<Tree<String>>();
                inters.add(prev);
                prev = new Tree<String>(indexer.get(intermediate), inters);
            }
            treesUnderA.add(prev);
        }
        Tree<String> tree = new Tree<String>(indexer.get(a), treesUnderA);
        return tree;
    }

    private Tree<String> backtrackB(int i, int j, int a, List<String> list) {
        if (j == i + 1) {
            List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();
            treesUnderA.add(new Tree<String>(list.get(i)));
            return new Tree<String>(indexer.get(a), treesUnderA);
        }
        int s = bSplitBacks[i][j][a];
        int lc = bLeftBacks[i][j][a];
        int rc = bRightBacks[i][j][a];
        List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();
        Tree<String> left = backtrackU(i, s, lc, list);
        Tree<String> right = backtrackU(s, j, rc, list);
        treesUnderA.add(left);
        treesUnderA.add(right);
        Tree<String> tree = new Tree<String>(indexer.get(a), treesUnderA);
        return tree;
    }

    @Override
    public Tree<String> getBestParse(List<String> list) {
        int N = list.size();
        for (int i = 0; i <= N; i++) {
            for (int j = i; j <= N; j++) {
                for (int k = 0; k < numLbl; k++) {
                    bScores[i][j][k] = NEG_INF;
                    uScores[i][j][k] = NEG_INF;
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < numLbl; j++) {
                String word = list.get(i), tag = indexer.get(j);
                double score = lexicon.getAllTags().contains(tag) ? lexicon.scoreTagging(word, tag) : NEG_INF;
                bScores[i][i + 1][j] = score != Double.NaN && score != NEG_INF ? score : NEG_INF;
            }
        }
        for (int i = 0; i < N; i++) {
            for (int k = 0; k < numLbl; k++) {
                if (bScores[i][i + 1][k] != NEG_INF) {
                    for (UnaryRule rule : closure.getClosedUnaryRulesByChild(k)) {
                        int p = rule.getParent();
                        double prob = rule.getScore() + bScores[i][i + 1][k];
                        if (uScores[i][i + 1][p] < prob) {
                            uScores[i][i + 1][p] = prob;
                            uBacks[i][i + 1][p] = k;
                        }
                    }
                }
            }
        }


        for (int d = 2; d <= N; d++) {
            for (int i = 0; i <= (N - d); i++) {
                for (int s = i + 1; s < i + d; s++) {
                    for (int b = 0; b < numLbl; b++) {
                        if (uScores[i][s][b] != NEG_INF) {
                            for (BinaryRule rule : grammar.getBinaryRulesByLeftChild(b)) {
                                int lc = rule.getLeftChild();
                                int rc = rule.getRightChild();
                                int p = rule.getParent();
                                if (uScores[s][i + d][rc] != NEG_INF) {
                                    double prob = rule.getScore() + uScores[i][s][lc] + uScores[s][i + d][rc];
                                    if (prob > bScores[i][i + d][p]) {
                                        bScores[i][i + d][p] = prob;
                                        bSplitBacks[i][i + d][p] = s;
                                        bLeftBacks[i][i + d][p] = lc;
                                        bRightBacks[i][i + d][p] = rc;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i <= (N - d); i++) {
                for (int b = 0; b < numLbl; b++) {
                    if (bScores[i][i + d][b] != NEG_INF) {
                        for (UnaryRule rule : closure.getClosedUnaryRulesByChild(b)) {
                            int p = rule.getParent();
                            double prob = rule.getScore() + bScores[i][i + d][b];
                            if (uScores[i][i + d][p] < prob) {
                                uScores[i][i + d][p] = prob;
                                uBacks[i][i + d][p] = b;
                            }
                        }
                    }
                }

            }
        }
        Tree<String> tree = backtrackU(0, N, 0, list);
        return TreeAnnotations.unAnnotateTree(tree);
    }
}
