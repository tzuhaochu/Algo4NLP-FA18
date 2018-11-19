package edu.berkeley.nlp.assignments.parsing.student;

import edu.berkeley.nlp.ling.Tree;

import java.util.ArrayList;
import java.util.List;

public final class FineBinarizer {

    private static void addParent(BiTree tree, String pLabel, String gpLabel) {
        if (tree.isPreTerminal()) {
            tree.setLabel(tree.getLabel() + "^" + pLabel);
            return;
        }
        String label = tree.getLabel();
        if (label.charAt(0) != '@') {
            tree.setLabel(label + "^" + pLabel);
            if (tree.hasLeft()) addParent(tree.getLeft(), label, pLabel);
            if (tree.hasRight()) addParent(tree.getRight(), label, pLabel);
        } else {
//            String[] subs = label.split("->");
//            tree.setLabel(subs[0] + "^" + gpLabel + "->" + subs[1]);
            if (tree.hasLeft()) addParent(tree.getLeft(), pLabel, gpLabel);
            if (tree.hasRight()) addParent(tree.getRight(), pLabel, gpLabel);
        }
        return;
    }

    private static BiTree binarizationHelper(Tree<String> tree, int index, String interLabel, String lastLabel) {
        String nbLabel = tree.getChildren().get(index).getLabel();
        Tree<String> leftTree = tree.getChildren().get(index + 1);
        BiTree biTree = new BiTree(interLabel + "_" + lastLabel + "_" + nbLabel);
        biTree.setLeft(_binarize(leftTree));
        if (index < tree.getChildren().size() - 2) {
            biTree.setRight(binarizationHelper(tree, index + 1, interLabel, nbLabel));
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
        String interLabel = "@" + label + "->";
        BiTree interTree = binarizationHelper(tree, 0, interLabel, "");
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
        addParent(biTree.getLeft(), "ROOT", "");
        return biTree;
    }
}
