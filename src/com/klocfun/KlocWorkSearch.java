package com.klocfun;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;

public class KlocWorkSearch {

    private Integer N;
    private String subString;
    private StringBuilder buffer;
    
    private static String[] ATOMS = {"kloc", "work"};
    private static Integer ATOM_SIZE = 4;

    public KlocWorkSearch(Integer n, String subStr) {
        N = n;
        subString = subStr;
        int bufferLen = (int)java.lang.Math.ceil(subStr.length() / ATOM_SIZE)*ATOM_SIZE + ATOM_SIZE;
        buffer = new StringBuilder(bufferLen);
        numToIdxSequence = new HashMap<>();
        largeNumToIdxSequence = new HashMap<>();
        numToIdxSequence.put(0, "0");
        numToIdxSequence.put(1, "1");

        largeNumToIdxSequence.put(30, "30");
        largeNumToIdxSequence.put(31, "31");
    }

    // public long findOccurrences() {
    //     initialize();
    //     long occurrences = 0;
    //     while (!searchIsComplete()) {
    //         advanceBuffer();
    //         if (isMatch()) {
    //             occurrences++;
    //         }
    //     }
    //     return occurrences;
    // }

    private Map<Integer, String> numToIdxSequence;
    private Map<Integer, String> largeNumToIdxSequence;

    public String getSequence(int n) {
        if (n > 30) {
            return getLargeNumberSequence(n);
        } else if (numToIdxSequence.containsKey(n)) {
            return numToIdxSequence.get(n);
        } else {
            String sequence = getSequence(n - 1) + getSequence(n - 2);
            numToIdxSequence.put(n, sequence);
            return sequence;
        }
    }

    private String getLargeNumberSequence(int n) {
        if (largeNumToIdxSequence.containsKey(n)) {
            return largeNumToIdxSequence.get(n);
        } else {
            String sequence = getLargeNumberSequence(n - 1) + getLargeNumberSequence(n - 2);
            largeNumToIdxSequence.put(n, sequence);
            return sequence;
        }
    }

    private void initialize() {
        
    }
}