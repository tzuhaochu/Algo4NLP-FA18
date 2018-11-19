package edu.berkeley.nlp.assignments.parsing.student;

import edu.berkeley.nlp.ling.Tree;

import java.util.ArrayList;
import java.util.List;

public final class CoarseBinarizer {

    private static BiTree binarizationHelper(Tree<String> tree, int index, String interLabel) {
        Tree<String> leftTree = tree.getChildren().get(index + 1);
        BiTree biTree = new BiTree(interLabel);
        biTree.setLeft(_binarize(leftTree));
        if (index < tree.getChildren().size() - 2) {
            biTree.setRight(binarizationHelper(tree, index + 1, interLabel));
        }
        return biTree;
    }

    private static BiTree _binarize(Tree<String> tree) {
        String label = tree.getLabel();
        if (tree.isLeaf()) return new BiTree(label);
        if (tree.getChildren().size() == 1) {
            BiTree bitree = new BiTree(label);
            bitree.setLeft(_binarize(tree.getChildren().get(0)));
            return bitree;
        }
        String interLabel = "@" + label;
        BiTree interTree = binarizationHelper(tree, 0, interLabel);
        BiTree biTree = new BiTree(label);
        biTree.setLeft(_binarize(tree.getChildren().get(0)));
        biTree.setRight(interTree);
        return biTree;
    }

    public static BiTree binarize(Tree<String> tree) {
        Tree<String> sTree = tree.getChildren().get(0);
        BiTree biTree = new BiTree("ROOT");
        TagEnricher.enrich(sTree);
        biTree.setLeft(_binarize(sTree));
        return biTree;
    }

}
