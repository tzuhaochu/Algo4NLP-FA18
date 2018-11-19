package edu.berkeley.nlp.assignments.parsing.student;

import edu.berkeley.nlp.ling.Tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BiTree {
    private BiTree left, right;
    private String label;

    public BiTree(String label) {
        this.label = label;
        this.left = null;
        this.right = null;
    }

    public BiTree getLeft() {
        return left;
    }

    public void setLeft(BiTree left) {
        this.left = left;
    }

    public BiTree getRight() {
        return right;
    }

    public void setRight(BiTree right) {
        this.right = right;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isLeaf() {
        return this.left == null && this.right == null;
    }

    public boolean isPreTerminal() {
        if (this.right != null) return false;
        if (this.left == null) return false;
        return this.left.isLeaf();
    }

    public boolean hasLeft() {
        return this.left != null;
    }

    public boolean hasRight() {
        return this.right != null;
    }

    public String toString() {
        if (isLeaf()) return this.label;
        if (hasLeft() && hasRight())
            return "(" + this.label + " " + this.left.toString() + " " + this.right.toString() + ")";
        if (hasLeft()) return "(" + this.label + " " + this.left.toString() + ")";
        if (hasRight()) return "(" + this.label + " " + this.right.toString() + ")";
        return "";
    }

    public Tree<String> toTree() {
        Tree<String> tree = new Tree<String>(label);
        if (isLeaf()) return tree;
        List<Tree<String>> children = new ArrayList<>();
        if (hasLeft()) children.add(left.toTree());
        if (hasRight()) children.add(right.toTree());
        tree.setChildren(children);
        return tree;
    }

    public List<String> getLeaves() {
        List<String> leaves = new ArrayList<>();
        appendLeaves(this, leaves);
        return leaves;
    }

    public List<String> getPreTerminals() {
        List<String> preterms = new ArrayList<>();
        appendPreTerminals(this, preterms);
        return preterms;
    }

    private static void appendLeaves(BiTree biTree, List<String> list) {
        if (biTree.isLeaf()) {
            list.add(biTree.getLabel());
            return;
        }
        appendLeaves(biTree.getLeft(), list);
        if (biTree.hasRight()) appendLeaves(biTree.getRight(), list);
    }

    private static void appendPreTerminals(BiTree biTree, List<String> list) {
        if (biTree.isPreTerminal()) {
            list.add(biTree.getLabel());
            return;
        }
        appendPreTerminals(biTree.getLeft(), list);
        if (biTree.hasRight()) appendPreTerminals(biTree.getRight(), list);
    }
}
