package com.klocfun;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.ArrayList;

public class FasterKlocWorkSearch {

    private Integer N;
    private String searchString;
    private int searchStringLength;
    private char searchStringLead;
    private StringBuilder buffer;

    private static String[] ATOMS = {"kloc", "work"};
    private static Integer ATOM_SIZE = 4;

    private ArrayList<Map<Integer, String>> maps;


    private WordSupplier supplier;

    public FasterKlocWorkSearch(Integer n, String subStr) {
        N = n;
        searchString = subStr;
        searchStringLength = searchString.length();
        searchStringLead = searchString.charAt(0);

        int numMaps = (int) Math.ceil( n / 10.0 );
        maps = new ArrayList<>();
        for (int i = 0; i < numMaps; i++) {
            Map<Integer, String> m = new HashMap<>();
            m.put(i * 10, "" + i*10);
            m.put(i * 10 + 1, "" + (i*10 + 1));
            maps.add(m);
        }

        computeSequences();
    }

    Map<Integer, String> getMap(int n) {
        int mapIdx = (int) (n / 10);
        return maps.get(mapIdx);
    }

    private class WordSupplier implements Supplier<String> {

        private String primaryStream;
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
            if (primaryStream == null) {
                primaryStream = getMap(N).get(N);
                pStreamIdx = -1;
            }

            pStreamIdx++;

            if (pStreamIdx == primaryStream.length()) {
                finished = true;
                return null;
            } else {
                int wordIdx = (primaryStream.charAt(pStreamIdx)) == '0' ? 0 : 1;
                return ATOMS[wordIdx];
            }
        }

        public String getAll() {
            StringBuilder sb = new StringBuilder();
            while (!finished) {
                String w = get();
                sb.append(w == null ? "" : w);
            }
            return sb.toString();
        }

        public boolean isFinished() {
            return finished;
        }
    }

    public long findOccurrences() {
        return findOccurrences(N);
    }

    public long findOccurrences(int n) {
        supplier = new WordSupplier(n);
        buffer = null;
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
            int numWordsInBuffer = (int) java.lang.Math.ceil(searchStringLength / 4.0) + 1;
            int bufferLen = numWordsInBuffer * ATOM_SIZE;
            buffer = new StringBuilder(bufferLen);
            for (int i = 1; i <= numWordsInBuffer; i++) {
                String word = supplier.get();
                if (word == null) {
                    break;
                }
                buffer.append(word);
            }
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

    private void computeSequences() {
        computeSequence(N);
    }

    public String computeSequence(int n) {
        
        if (getMap(n).containsKey(n)) {
            return getMap(n).get(n);
        } else {
            String sequence = computeSequence(n - 1) + computeSequence(n - 2);
            getMap(n).put(n, sequence);

            if ( n == 10 ) {
                System.out.println("Num occurrences in string of length 10");
                System.out.println(findOccurrences(n));
            }

            if (n == 11) {
                System.out.println("Num occurrences in string of length 11");
                System.out.println(findOccurrences(n));
                findOccurrencesInBoundary();
            }

            return sequence;
        }
    }

    public void findOccurrencesInBoundary() {
        int len = searchStringLength - 1;
        String seq1 = (new WordSupplier(10)).getAll();
        String seq2 = (new WordSupplier(11)).getAll();
        String seq1end = seq1.substring(seq1.length() - len, seq1.length());
        String seq1begin = seq1.substring(0, len);
        String seq2end = seq2.substring(seq2.length() - len, seq2.length());
        String seq2begin = seq2.substring(0, len);
        String seq3 = seq1end + seq2begin;
        String seq4 = seq2end + seq1begin;
        String seq5 = seq1end + seq1begin;
        String seq6 = seq2end + seq2begin;
        System.out.println(seq3);
        System.out.println(occurrences(seq3, searchString));
        System.out.println(seq4);
        System.out.println(occurrences(seq4, searchString));
        System.out.println(seq5);
        System.out.println(occurrences(seq5, searchString));
        System.out.println(seq6);
        System.out.println(occurrences(seq6, searchString));
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

    // Testing method
    public void printStream() {
        String s;
        while ( (s = supplier.get()) != null) {
            System.out.print(s);
        }
        System.out.println();
    }
}