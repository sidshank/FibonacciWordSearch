package com.klocfun;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class KlocWorkSearch {

    private Integer N;
    private String searchString;
    private int searchStringLength;
    private char searchStringLead;
    private StringBuilder buffer;

    private static String[] ATOMS = {"kloc", "work"};
    private static Integer ATOM_SIZE = 4;

    private WordSupplier supplier;

    public KlocWorkSearch(Integer n, String subStr) {
        N = n;
        searchString = subStr;
        searchStringLength = searchString.length();
        searchStringLead = searchString.charAt(0);
        numToIdxSequence = new HashMap<>();
        largeNumToIdxSequence = new HashMap<>();
        numToIdxSequence.put(0, "0");
        numToIdxSequence.put(1, "1");

        largeNumToIdxSequence.put(31, "31");
        largeNumToIdxSequence.put(32, "32");

        supplier = new WordSupplier();

        cacheSequence();
    }


    private class WordSupplier implements Supplier<String> {

        private String primaryStream;
        private int pStreamIdx;

        private String secondaryStream;
        private int sStreamIdx;
        private boolean finished = false;

        @Override
        public String get() {
            if (finished) {
                return null;
            }
            int wordLoc;
            if (primaryStream == null) {
                if (N <= 30) {
                    primaryStream = numToIdxSequence.get(N);
                } else {
                    secondaryStream = largeNumToIdxSequence.get(N);
                    sStreamIdx = 0;
                    wordLoc = Integer.parseInt(secondaryStream.substring(sStreamIdx, sStreamIdx + 2));
                    primaryStream = numToIdxSequence.get(wordLoc - 1) + numToIdxSequence.get(wordLoc - 2);
                    sStreamIdx += 2;
                }
                pStreamIdx = -1;
            }

            pStreamIdx++;

            if (pStreamIdx == primaryStream.length()) {
                if (N <= 30) {
                    finished = true;
                    return null;
                } else {

                    if (sStreamIdx == secondaryStream.length()) {
                        // Done processing secondary stream too.
                        finished = true;
                        return null;
                    } else {
                        wordLoc = Integer.parseInt(secondaryStream.substring(sStreamIdx, sStreamIdx + 2));
                        primaryStream = numToIdxSequence.get(wordLoc - 1) + numToIdxSequence.get(wordLoc - 2);
                        sStreamIdx += 2;
                        pStreamIdx = 0;
                    }

                }

            }
            int wordIdx = (primaryStream.charAt(pStreamIdx)) == '0' ? 0 : 1;
            return ATOMS[wordIdx];
        }

        public boolean isFinished() {
            return finished;
        }
    }

    public long findOccurrences() {

        long occurrences = 0;
        while (!searchIsComplete()) {
            advanceBuffer();
            if (isMatch()) {
                occurrences++;
            }
        }
        return occurrences;
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
        if (N <= 30) {
            computeSequence(N);
        } else {
            computeSequence(30);
            computeLargeSequence(N);
        }
    }

    private Map<Integer, String> numToIdxSequence;
    private Map<Integer, String> largeNumToIdxSequence;

    public String computeSequence(int n) {
        if (n > 31) {
            return computeLargeSequence(n);
        } else if (numToIdxSequence.containsKey(n)) {
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