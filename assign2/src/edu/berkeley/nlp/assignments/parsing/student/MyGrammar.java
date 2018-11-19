package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.BinaryRule;
import edu.berkeley.nlp.assignments.parsing.UnaryRule;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.Indexer;

public final class MyGrammar {

    Indexer<String> labelIndexer;

    List<edu.berkeley.nlp.assignments.parsing.BinaryRule>[] binaryRulesByLeftChild;
    List<edu.berkeley.nlp.assignments.parsing.BinaryRule>[] binaryRulesByRightChild;
    List<edu.berkeley.nlp.assignments.parsing.BinaryRule>[] binaryRulesByParent;
    List<edu.berkeley.nlp.assignments.parsing.BinaryRule> binaryRules = new ArrayList<edu.berkeley.nlp.assignments.parsing.BinaryRule>();

    List<edu.berkeley.nlp.assignments.parsing.UnaryRule>[] unaryRulesByChild;
    List<edu.berkeley.nlp.assignments.parsing.UnaryRule>[] unaryRulesByParent;
    List<edu.berkeley.nlp.assignments.parsing.UnaryRule> unaryRules = new ArrayList<edu.berkeley.nlp.assignments.parsing.UnaryRule>();

    public Indexer<String> getLabelIndexer() {
        return labelIndexer;
    }

    public List<edu.berkeley.nlp.assignments.parsing.BinaryRule> getBinaryRulesByLeftChild(int leftChildIdx) {
        return binaryRulesByLeftChild[leftChildIdx];
    }

    public List<edu.berkeley.nlp.assignments.parsing.BinaryRule> getBinaryRulesByRightChild(int rightChildIdx) {
        return binaryRulesByRightChild[rightChildIdx];
    }

    public List<edu.berkeley.nlp.assignments.parsing.BinaryRule> getBinaryRulesByParent(int parentIdx) {
        return binaryRulesByParent[parentIdx];
    }

    public List<edu.berkeley.nlp.assignments.parsing.BinaryRule> getBinaryRules() {
        return binaryRules;
    }

    public List<edu.berkeley.nlp.assignments.parsing.UnaryRule> getUnaryRulesByChild(int childIdx) {
        return unaryRulesByChild[childIdx];
    }

    public List<edu.berkeley.nlp.assignments.parsing.UnaryRule> getUnaryRulesByParent(int parentIdx) {
        return unaryRulesByParent[parentIdx];
    }

    public List<edu.berkeley.nlp.assignments.parsing.UnaryRule> getUnaryRules() {
        return unaryRules;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<String> ruleStrings = new ArrayList<String>();
        for (int parent = 0; parent < binaryRulesByParent.length; parent++) {
            for (edu.berkeley.nlp.assignments.parsing.BinaryRule binaryRule : getBinaryRulesByParent(parent)) {
                ruleStrings.add(binaryRule.toString(labelIndexer));
            }
        }
        for (int parent = 0; parent < unaryRulesByParent.length; parent++) {
            for (edu.berkeley.nlp.assignments.parsing.UnaryRule fastUnaryRule : getUnaryRulesByParent(parent)) {
                ruleStrings.add(fastUnaryRule.toString(labelIndexer));
            }
        }
        for (String ruleString : CollectionUtils.sort(ruleStrings)) {
            sb.append(ruleString);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void addTreeLabels(Tree<String> tree) {
        if (!tree.isLeaf()) {
            labelIndexer.addAndGetIndex(tree.getLabel());
            if (!tree.getChildren().isEmpty()) {
                for (Tree<String> child : tree.getChildren()) {
                    addTreeLabels(child);
                }
            }
        }
    }

    private void addBinary(edu.berkeley.nlp.assignments.parsing.BinaryRule binaryRule) {
        binaryRules.add(binaryRule);
        binaryRulesByParent[binaryRule.getParent()].add(binaryRule);
        binaryRulesByLeftChild[binaryRule.getLeftChild()].add(binaryRule);
        binaryRulesByRightChild[binaryRule.getRightChild()].add(binaryRule);
    }

    private void addUnary(edu.berkeley.nlp.assignments.parsing.UnaryRule unaryRule) {
        unaryRules.add(unaryRule);
        unaryRulesByChild[unaryRule.getChild()].add(unaryRule);
        unaryRulesByParent[unaryRule.getParent()].add(unaryRule);
    }

    public MyGrammar(List<Tree<String>> trainTrees) {
        this.labelIndexer = new Indexer<String>();
        for (Tree<String> trainTree : trainTrees) {
            addTreeLabels(trainTree);
        }
        this.binaryRulesByLeftChild = new List[labelIndexer.size()];
        this.binaryRulesByRightChild = new List[labelIndexer.size()];
        this.binaryRulesByParent = new List[labelIndexer.size()];
        this.unaryRulesByChild = new List[labelIndexer.size()];
        this.unaryRulesByParent = new List[labelIndexer.size()];
        for (int i = 0; i < labelIndexer.size(); i++) {
            this.binaryRulesByLeftChild[i] = new ArrayList<edu.berkeley.nlp.assignments.parsing.BinaryRule>();
            this.binaryRulesByRightChild[i] = new ArrayList<edu.berkeley.nlp.assignments.parsing.BinaryRule>();
            this.binaryRulesByParent[i] = new ArrayList<edu.berkeley.nlp.assignments.parsing.BinaryRule>();
            this.unaryRulesByChild[i] = new ArrayList<edu.berkeley.nlp.assignments.parsing.UnaryRule>();
            this.unaryRulesByParent[i] = new ArrayList<edu.berkeley.nlp.assignments.parsing.UnaryRule>();
        }
        Counter<edu.berkeley.nlp.assignments.parsing.UnaryRule> unaryRuleCounter = new Counter<edu.berkeley.nlp.assignments.parsing.UnaryRule>();
        Counter<edu.berkeley.nlp.assignments.parsing.BinaryRule> binaryRuleCounter = new Counter<edu.berkeley.nlp.assignments.parsing.BinaryRule>();
        Counter<Integer> symbolCounter = new Counter<Integer>();
        for (Tree<String> trainTree : trainTrees) {
            tallyTree(trainTree, symbolCounter, unaryRuleCounter, binaryRuleCounter);
        }
        for (edu.berkeley.nlp.assignments.parsing.UnaryRule unaryRule : unaryRuleCounter.keySet()) {
            double c_rule = unaryRuleCounter.getCount(unaryRule);
            double s_num = symbolCounter.getCount(unaryRule.getParent());
            double unaryProbability = c_rule/ s_num;
            unaryRule.setScore(Math.log(unaryProbability));
            addUnary(unaryRule);
        }
        for (edu.berkeley.nlp.assignments.parsing.BinaryRule binaryRule : binaryRuleCounter.keySet()) {
            double c_rule = binaryRuleCounter.getCount(binaryRule);
            double s_num = symbolCounter.getCount(binaryRule.getParent());
            double binaryProbability = c_rule/ s_num;
            binaryRule.setScore(Math.log(binaryProbability));
            addBinary(binaryRule);
        }
    }

    private void tallyTree(Tree<String> tree, Counter<Integer> symbolCounter, Counter<edu.berkeley.nlp.assignments.parsing.UnaryRule> unaryRuleCounter, Counter<edu.berkeley.nlp.assignments.parsing.BinaryRule> binaryRuleCounter) {
        if (tree.isLeaf()) return;
        if (tree.isPreTerminal()) return;
        if (tree.getChildren().size() == 1) {
            symbolCounter.incrementCount(labelIndexer.indexOf(tree.getLabel()), 1.0);
            unaryRuleCounter.incrementCount(makeUnaryRule(tree), 1.0);
        }
        if (tree.getChildren().size() == 2) {
            symbolCounter.incrementCount(labelIndexer.indexOf(tree.getLabel()), 1.0);
            binaryRuleCounter.incrementCount(makeBinaryRule(tree), 1.0);
        }
        if (tree.getChildren().size() < 1 || tree.getChildren().size() > 2) {
            throw new RuntimeException("Attempted to construct a Grammar with an illegal tree (unbinarized?): " + tree);
        }
        for (Tree<String> child : tree.getChildren()) {
            tallyTree(child, symbolCounter, unaryRuleCounter, binaryRuleCounter);
        }
    }

    private edu.berkeley.nlp.assignments.parsing.UnaryRule makeUnaryRule(Tree<String> tree) {
        return new UnaryRule(labelIndexer.indexOf(tree.getLabel()), labelIndexer.indexOf(tree.getChildren().get(0).getLabel()));
    }

    private edu.berkeley.nlp.assignments.parsing.BinaryRule makeBinaryRule(Tree<String> tree) {
        return new BinaryRule(labelIndexer.indexOf(tree.getLabel()), labelIndexer.indexOf(tree.getChildren().get(0).getLabel()), labelIndexer.indexOf(tree.getChildren().get(1).getLabel()));
    }
}