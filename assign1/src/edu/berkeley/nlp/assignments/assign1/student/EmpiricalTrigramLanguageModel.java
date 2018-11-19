package edu.berkeley.nlp.assignments.assign1.student;

import edu.berkeley.nlp.assignments.assign1.student.util.BiCounter;
import edu.berkeley.nlp.assignments.assign1.student.util.TriCounter;
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;

import java.util.ArrayList;
import java.util.List;

public final class EmpiricalTrigramLanguageModel implements NgramLanguageModel {

    private double d1 = 0.75, d2 = 0.75;
    private int[] uniCntr, uniHead, uniRear, uniMid;
    private int uniNum = 0;
    private int OFFSET = 19;
    private float LF = 0.85f;
    private BiCounter biCntr, biHead, biRear;
    private TriCounter triCntr;
    private long biNum, triNum;
    private long uniSum = 0;
    private double epsilon;

    public EmpiricalTrigramLanguageModel(Iterable<List<String>> trainingData) {
        int sizeOfData = 0;
        for (List<String> l : trainingData)
            if (sizeOfData > 2000) break;
            else sizeOfData++;
        if (sizeOfData < 2000) {
            OFFSET = 12;
        }
        uniCntr = new int[1 << OFFSET];
        uniHead = new int[1 << OFFSET];
        uniRear = new int[1 << OFFSET];
        uniMid = new int[1 << OFFSET];
        biCntr = new BiCounter(OFFSET, LF);
        biHead = new BiCounter(OFFSET, LF);
        biRear = new BiCounter(OFFSET, LF);
        triCntr = new TriCounter(OFFSET, LF);

        for (List<String> sentence : trainingData) {
            ArrayList<String> l = new ArrayList<>(sentence);
            l.add(0, NgramLanguageModel.START);
            l.add(NgramLanguageModel.STOP);
            int _uid0 = EnglishWordIndexer.getIndexer().addAndGetIndex(l.get(0));
            int _uid1 = EnglishWordIndexer.getIndexer().addAndGetIndex(l.get(1));
            uniCntr[_uid0] += 1;
            uniCntr[_uid1] += 1;
            uniSum += 2;
            long _uid0L = _uid0, _uid1L = _uid1;
            long _bid0 = (_uid0L << OFFSET) + _uid1L;
            int hash0 = biCntr.inc(_bid0);

            if (biCntr.getValueByHash(hash0) == 1) {
                uniHead[_uid0] += 1;
                uniRear[_uid1] += 1;
            }
            for (int i = 2; i < l.size(); i++) {
                String w0 = l.get(i - 2);
                String w1 = l.get(i - 1);
                String w2 = l.get(i);
                int uid0 = EnglishWordIndexer.getIndexer().addAndGetIndex(w0);
                int uid1 = EnglishWordIndexer.getIndexer().addAndGetIndex(w1);
                int uid2 = EnglishWordIndexer.getIndexer().addAndGetIndex(w2);
                uniCntr[uid2] += 1;
                uniSum += 1;
                long uid0L = uid0, uid1L = uid1, uid2L = uid2;
                long bid0 = (uid0L << OFFSET) + uid1L;
                long bid1 = (uid1L << OFFSET) + uid2L;
                long tid = (bid0 << OFFSET) + uid2L;
                int hash1 = biCntr.inc(bid1);
                int hash2 = triCntr.inc(tid);
                if (biCntr.getValueByHash(hash1) == 1) {
                    uniHead[uid1] += 1;
                    uniRear[uid2] += 1;
                }
                if (triCntr.getValueByHash(hash2) == 1) {
                    uniMid[uid1] += 1;
                    biHead.inc(bid0);
                    biRear.inc(bid1);
                }
            }
        }
        while (uniCntr[uniNum] != 0) uniNum++;
        biNum = biCntr.size();
        epsilon = 2.0/uniNum;
    }

    @Override
    public int getOrder() {
        return 3;
    }

    private double getTrigramProbability(int uid1, int uid2, long hBid0, long hBid1, long hTid) {
        double p, a2;
        if (uniCntr[uid2] == 0) return epsilon;
        if (triCntr.getValue(hTid) > 0) {
            a2 = d2 * biHead.getValue(hBid0) / biCntr.getValue(hBid0);
            long tmp1 = (long) uniHead[uid1] * biHead.getValue(hBid0);
            long tmp2 = (long) uniMid[uid1] * biNum;
            p = d1 * tmp1 / tmp2 + Math.max(biRear.getValue(hBid1) - d1, 0.0) / uniMid[uid1];
            p = a2 * p + Math.max(triCntr.getValue(hTid) - d2, 0.0) / biCntr.getValue(hBid0);
            return p;
        } else return getBigramProbability(uid1, uid2, hBid1);
    }

    private double getBigramProbability(int uid0, int uid1, long hBid) {
        double p;
        if (uniCntr[uid1] == 0) return epsilon;
        if (biCntr.getValue(hBid) > 0) {
            long tmp1 = (long) uniHead[uid0] * uniRear[uid1];
            long tmp2 = (long) uniCntr[uid0] * biNum;
            p = d1 * tmp1 / tmp2 + Math.max(biCntr.getValue(hBid) - d1, 0.0) / uniCntr[uid0];
            return p;
        } else return getUnigramProbability(uid1);
    }

    private double getUnigramProbability(int uid) {
        return uniCntr[uid] == 0 ? (double) uniCntr[uid] / uniSum : epsilon;
    }

    @Override
    public double getNgramLogProbability(int[] ids, int lo, int hi) {
        double p;
        int len = hi - lo;
        if (len == 3) {
            long uid0 = ids[lo], uid1 = ids[lo+1], uid2 = ids[lo+2];
            long hBid0 = (uid0 << OFFSET) + uid1, hBid1 = (uid1 << OFFSET) + uid2;
            long hTid = (hBid0 << OFFSET) + uid2;
            p = getTrigramProbability((int) uid1, (int) uid2, hBid0, hBid1, hTid);
        } else if (len == 2) {
            long uid0 = ids[lo], uid1 = ids[lo+1];
            long hBid = (uid0 << OFFSET) + uid1;
            p = getBigramProbability((int) uid0, (int) uid1, hBid);
        } else if (len == 1) {
            p = getUnigramProbability(ids[lo]);
        } else p = epsilon;
        return Math.log(p);
    }

    @Override
    public long getCount(int[] ints) {
        if (ints.length == 1) return uniCntr[ints[0]];
        if (ints.length == 2) {
            long id0 = ints[0], id1 = ints[1];
            long bid = (id0 << OFFSET) + id1;
            return biCntr.getValue(bid);
        }
        if (ints.length == 3) {
            long id0 = ints[0], id1 = ints[1], id2 = ints[2];
            long bid = (id0 << OFFSET) + id1;
            long tid = (bid << OFFSET) + id2;
            return triCntr.getValue(tid);
        }
        return 0;
    }
}
