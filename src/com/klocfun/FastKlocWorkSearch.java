package com.klocfun;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class FastKlocWorkSearch {

    private Integer N;
    private String searchString;
    private int searchStringLength;
    private char searchStringLead;
    private StringBuilder buffer;

    private static String[] ATOMS = {"kloc", "work"};
    private static Integer ATOM_SIZE = 4;

    private WordSupplier supplier;

    private Map<Integer, String> numToIdxSequence;
    private Map<Integer, String> largeNumToIdxSequence;
    private Map<String, Long> counts;

    public FastKlocWorkSearch(Integer n, String subStr) {
        N = n;
        searchString = subStr;
        searchStringLength = searchString.length();
        searchStringLead = searchString.charAt(0);

        numToIdxSequence = new HashMap<>();
        largeNumToIdxSequence = new HashMap<>();
        counts = new HashMap<>();

        numToIdxSequence.put(0, "0");
        numToIdxSequence.put(1, "1");

        largeNumToIdxSequence.put(30, "30");
        largeNumToIdxSequence.put(31, "31");
        cacheSequence();
    }


    private class WordSupplier implements Supplier<String> {

        private String stream;
        private int pStreamIdx;
        private boolean finished = false;
        private int N;

        public WordSupplier(int n) {
            N = n;
        }

        @Override
        public String get() {
            if (finished) {
                return null;
            }
            if (stream == null) {
                stream = numToIdxSequence.get(N);
                pStreamIdx = -1;
            }

            pStreamIdx++;

            if (pStreamIdx == stream.length()) {
                finished = true;
                return null;
            } else {
                int wordIdx = (stream.charAt(pStreamIdx)) == '0' ? 0 : 1;
                return ATOMS[wordIdx];
            }
        }

        public boolean isFinished() {
            return finished;
        }
    }

    public long findOccurrences() {

        return findOccurrences(N);
    }

    public long findOccurrences(int n) {
        if (n <= 31) {
            buffer = null;
            supplier = new WordSupplier(n);
            long occurrences = 0;
            while (!searchIsComplete()) {
                advanceBuffer();
                if (isMatch()) {
                    occurrences++;
                }
            }
            return occurrences;
        } else {
            String seq = largeNumToIdxSequence.get(n);
            String current;
            String next;
            long occurrences = 0;
            for (int idx = 0; idx < seq.length(); idx = idx + 2) {
                current = seq.substring(idx, idx + 2);
                if (idx >= seq.length() - 4) {
                    next = null;
                } else {
                    next = seq.substring(idx + 2, idx + 4);
                }

                occurrences += counts.get(current);
                if (next != null) {
                    occurrences += counts.get(current + next);
                }
            }
            return occurrences;
        }

    }

    private boolean searchIsComplete() {
        return supplier.isFinished();
    }

    private void advanceBuffer() {
        if (buffer == null) {
            initializeBuffer();
        } else {
            do {
                buffer.deleteCharAt(0);
                if (buffer.length() < searchStringLength) {
                    String nextWord = supplier.get();
                    if (nextWord != null) {
                        buffer.append(nextWord);
                    } else {
                        break;
                    }
                }
            } while (buffer.charAt(0) != searchStringLead);
        }
    }

    private boolean isMatch() {
        if (buffer.length() < searchStringLength) {
            return false;
        } else {
            return buffer.substring(0, searchStringLength).equals(searchString);
        }
    }

    private void initializeBuffer() {
        int numWordsInBuffer = (int)java.lang.Math.ceil(searchStringLength / 4.0) + 1;
        int bufferLen = numWordsInBuffer*ATOM_SIZE;
        buffer = new StringBuilder(bufferLen);
        for (int i = 1; i <= numWordsInBuffer; i++) {
            String word = supplier.get();
            if (word == null) {
                break;
            }
            buffer.append(word);
        }
    }

    private void cacheSequence() {
        if (N <= 31) {
            computeSequence(N);
        } else {
            computeSequence(31);

            counts.put("31", findOccurrences(31));
            counts.put("30", findOccurrences(30));

            counts.put("3130", getBoundaryCounts(31, 30));
            counts.put("3131", getBoundaryCounts(31, 31));
            counts.put("3031", getBoundaryCounts(30, 31));
            counts.put("3030", getBoundaryCounts(30, 30));

            computeLargeSequence(N);
        }
    }

    private long getBoundaryCounts(int n1, int n2) {
        int boundaryStringLength = searchStringLength - 1;
        String n1Str = numToIdxSequence.get(n1);
        String n2Str = numToIdxSequence.get(n2);
        String s1 = n1Str.substring(n1Str.length() - 5, n1Str.length());
        String s2 = n2Str.substring(0, 5);

        String s1Real = "";
        String s2Real = "";

        StringBuilder boundarySb = new StringBuilder();

        for (int i = 0; i < s1.length(); i++) {
            char ch = s1.charAt(i);
            int wordNum = ch == '0'? 0 : 1;
            s1Real += ATOMS[wordNum];
        }

        for (int j = 0; j < s2.length(); j++) {
            char ch = s2.charAt(j);
            int wordNum = ch == '0'? 0 : 1;
            s2Real += ATOMS[wordNum];
        }


        for (int i = (s1Real.length() - boundaryStringLength); i < s1Real.length(); i++) {
            char ch = s1Real.charAt(i);
            boundarySb.append(ch);
        }

        for (int j = 0; j < boundaryStringLength; j++) {
            char ch = s2Real.charAt(j);
            boundarySb.append(ch);
        }
        return occurrences(boundarySb.toString(), searchString);
    }

    private long occurrences(String str, String subString) {

        if (subString.length() <= 0) return (str.length() + 1);

        long n = 0;
        long pos = 0;
        long step = 1;

        while (true) {
            pos = (long) str.indexOf(subString, (int)pos);
            if (pos >= 0) {
                n++;
                pos += step;
            } else {
                break;
            }
        }
        return n;
    }

    public String computeSequence(int n) {
        if (numToIdxSequence.containsKey(n)) {
            return numToIdxSequence.get(n);
        } else {
            String sequence = computeSequence(n - 1) + computeSequence(n - 2);
            numToIdxSequence.put(n, sequence);
            return sequence;
        }
    }

    private String computeLargeSequence(int n) {
        if (largeNumToIdxSequence.containsKey(n)) {
            return largeNumToIdxSequence.get(n);
        } else {
            String sequence = computeLargeSequence(n - 1) + computeLargeSequence(n - 2);
            largeNumToIdxSequence.put(n, sequence);
            return sequence;
        }
    }


    // Testing method
    public void printStream() {
        String s;
        while ( (s = supplier.get()) != null) {
            System.out.print(s);
        }
        System.out.println();
    }
}