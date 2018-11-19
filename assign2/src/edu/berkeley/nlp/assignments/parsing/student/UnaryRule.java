package edu.berkeley.nlp.assignments.parsing.student;

public final class UnaryRule {
    private int head, tail;
    private double score;

    public UnaryRule(int head, int tail) {
        this.head = head;
        this.tail = tail;
    }

    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public int getTail() {
        return tail;
    }

    public void setTail(int tail) {
        this.tail = tail;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int hashCode(){
        long hid = head, tid = tail;
        return Long.hashCode((hid << 16) + tid);
    }

    public static long toLong(int head, int tail){
        long hid = head, tid = tail;
        return (hid << 16) + tid;
    }

    public long toLong(){
        long hid = head, tid = tail;
        return (hid << 16) + tid;
    }
}
