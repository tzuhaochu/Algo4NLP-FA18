package edu.berkeley.nlp.assignments.parsing.student;

public final class BinaryRule {
    private int head, left, right;
    private double score;

    public BinaryRule(int head, int left, int right) {
        this.head = head;
        this.left = left;
        this.right = right;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int hashCode(){
        long hid = head, lid = left, rid = right;
        return Long.hashCode((hid << 32) + (lid << 16) + rid);
    }

    public static long toLong(int head, int left, int right){
        long hid = head, lid = left, rid = right;
        return (hid << 32) + (lid << 16) + rid;
    }

    public long toLong(){
        long hid = head, lid = left, rid = right;
        return (hid << 32) + (lid << 16) + rid;
    }
}
