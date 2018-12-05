package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.util.CounterMap;

public final class Utils {
    public static double computeDiff(CounterMap<Integer, Integer> A, CounterMap<Integer, Integer> B, double tol){
        int convergedNum = 0;
        int totalNum = 0;
        for (Integer k : A.keySet()) {
            for (Integer v : A.getCounter(k).keySet()) {
                totalNum += 1;
                if (Math.abs(B.getCount(k, v) - A.getCount(k, v)) < tol) convergedNum += 1;
            }
        }
        return  1.0 - (double) convergedNum / totalNum;
    }
}
