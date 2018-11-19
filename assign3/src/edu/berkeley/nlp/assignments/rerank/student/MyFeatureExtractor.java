package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.SimpleFeatureExtractor;
import edu.berkeley.nlp.ling.AnchoredTree;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

import java.util.ArrayList;
import java.util.List;

public class MyFeatureExtractor extends SimpleFeatureExtractor {

    @Override
    public int[] extractFeatures(KbestList kbestList, int idx, Indexer<String> featureIndexer,
                                 boolean addFeaturesToIndexer) {
        Tree<String> tree = kbestList.getKbestTrees().get(idx);
        AnchoredTree<String> anchoredTree = AnchoredTree.fromTree(tree);
        List<String> poss = tree.getPreTerminalYield();
        List<String> words = tree.getYield();

        List<Integer> feats = new ArrayList<>();
        addFeature("Posn=" + idx, feats, featureIndexer, addFeaturesToIndexer);

        for (AnchoredTree<String> subtree : anchoredTree.toSubTreeList()) {
            if (!subtree.isPreTerminal() && !subtree.isLeaf()) {
                String rule = "Rule=" + subtree.getLabel() + " ->";
                for (AnchoredTree<String> child : subtree.getChildren()) {
                    rule += " " + child.getLabel();
                }
                addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
                String l = getBucket(subtree.getSpanLength()) + ", " + rule;

                addFeature(l, feats, featureIndexer, addFeaturesToIndexer);
                int si = subtree.getStartIdx();
                int ei = subtree.getEndIdx();

                String spanShape = "";
                for (int j = si; j < ei; j++) {
                    String word = words.get(j);
                    if (word.charAt(0) >= 'A' && word.charAt(0) <= 'Z')
                        spanShape = spanShape + "X";
                    else if (word.charAt(0) >= 'a' && word.charAt(0) <= 'z')
                        spanShape = spanShape + "x";
                    else if (Character.isDigit(word.charAt(0)))
                        spanShape = spanShape + "1";
                    else if (word.length() == 1)
                        spanShape = spanShape + word;
                }

                addFeature(rule + "=" + spanShape, feats, featureIndexer, addFeaturesToIndexer);

                String firstword = words.get(si);
                addFeature(firstword + "_1_" + rule, feats, featureIndexer, addFeaturesToIndexer);

                String firstPOS = poss.get(si);
                addFeature(firstPOS + "_sPOS_" + rule, feats, featureIndexer, addFeaturesToIndexer);

                String lastword = words.get(ei - 1);
                addFeature(lastword + "_e_" + rule, feats, featureIndexer, addFeaturesToIndexer);

                String lastPOS = poss.get(ei - 1);
                addFeature(lastPOS + "_ePOS_" + rule, feats, featureIndexer, addFeaturesToIndexer);

                if (si - 1 >= 0) {
                    String previousword = words.get(si - 1);
                    addFeature(previousword + ">" + rule, feats, featureIndexer, addFeaturesToIndexer);
                }
                if (ei < words.size()) {
                    String nextword = words.get(ei);
                    addFeature(nextword + "<" + rule, feats, featureIndexer, addFeaturesToIndexer);

                }
                int nc = subtree.getChildren().size();
                AnchoredTree rightmostnode = subtree;
                while (nc >= 1) {
                    rightmostnode = (AnchoredTree) rightmostnode.getChildren().get(nc - 1);
                    nc = rightmostnode.getChildren().size();
                }
            }
        }

        int[] featsArr = new int[feats.size()];
        for (int i = 0; i < feats.size(); i++) {
            featsArr[i] = feats.get(i).intValue();
        }
        return featsArr;
    }

    private void addFeature(String feat, List<Integer> feats, Indexer<String> featureIndexer, boolean addNew) {
        if (addNew || featureIndexer.contains(feat)) {
            feats.add(featureIndexer.addAndGetIndex(feat));
        }
    }

    private String getBucket(int l) {
        if (l <= 5)
            return Integer.toString(l);
        if (l <= 10)
            return "10";
        if (l <= 20)
            return "20";
        return ">=21";
    }
}
