package edu.berkeley.nlp.assignments.assign1.student.util;

import edu.berkeley.nlp.util.CollectionUtils;

import java.util.Arrays;

public final class Indexer {
    private long[] keys;
    private int magic1 = 2;
    private int magic2 = 37;
    private long offset;
    private int L;
    private int size = -1;

    public Indexer(int length, int bits) {
        keys = new long[length];
        offset = bits / 2;
        L = length;
        Arrays.fill(keys, -1);
    }

    public int addIndex(long key) {
        int hash = (int) ((key ^ (key >>> offset)) % L);
        int k = magic1;
        while (keys[hash] != key) {
            if (keys[hash] == -1) {
                keys[hash] = key;
                break;
            }
            hash = (int) (((long) k * k + hash) % L);
            k += magic1;
        }
        return hash;
    }

    public int getIndex(long key) {
        if (key < 0) return -1;
        int hash = (int) ((key ^ (key >>> offset)) % L);
        int k = magic1;
        while (keys[hash] != key) {
            if (keys[hash] == -1) return -1;
            hash = (int) (((long) k * k + hash) % L);
            k += magic1;
        }
        return hash;
    }

    public int size() {
        if (size == -1) {
            int cnt = 0;
            for (int i = 0; i < keys.length; i++) {
                cnt += keys[i] == -1 ? 0 : 1;
            }
            this.size = cnt;
        }
        return size;
    }

//    public double avgFindNum() {
//        return (double) cntI / cntS;
//    }
}
