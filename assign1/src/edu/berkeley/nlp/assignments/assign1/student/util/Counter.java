package edu.berkeley.nlp.assignments.assign1.student.util;

public interface Counter {
    int inc(long key);

    int getValue(long key);

    int getValueByHash(int hash);

    int getHash(long key);

    int size();
}
