package edu.berkeley.nlp.assignments.assign1.student.util;

public final class BiCounter implements Counter {
    static public Indexer indexer;
    public int[] content;

    public BiCounter(int offset, double loadFactor) {
        int shift = (int) (offset / 19.0 * 23.0);
        int length = (int) ((1L << shift) / loadFactor);
        content = new int[length];
        indexer = new Indexer(length, offset * 2);
    }

    @Override
    public int inc(long key) {
        int hash = indexer.addIndex(key);
        content[hash] += 1;
        return hash;
    }

    @Override
    public int getValue(long key) {
        int hash = getHash(key);
        return hash >= 0 ? content[hash] : 0;
    }

    @Override
    public int getValueByHash(int hash) {
        return content[hash];
    }

    @Override
    public int getHash(long key) {
        return indexer.getIndex(key);
    }

    @Override
    public int size() {
        return indexer.size();
    }
}
