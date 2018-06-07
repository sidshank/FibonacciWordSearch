package com.klocfun;

import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KlocWorkSearch {

    public static final String[] WORDS = {"kloc", "work"};
    private static final Integer WORD_SIZE = Math.max(WORDS[0].length(), WORDS[1].length());

    private static final Integer DEFAULT_PARTITION_N = 31;
    private static final Integer MIN_PARTITION_N = 11;

    private int fPartitionN;
    private String fSearchString;
    private Integer fSequenceSize;
    private boolean fValidSearchString;

    private Map<Integer, String> fSizeToSingleWordSequence;
    private Map<Integer, String> fSizeToMultiWordSequence;
    private Map<String, Long> fCompoundWordsToOccurrences;

    public KlocWorkSearch(Integer n, String subStr, int partitionN) {
        fPartitionN = Math.max(partitionN, MIN_PARTITION_N);
        fSequenceSize = n;
        fSearchString = subStr;

        fSizeToSingleWordSequence = new HashMap<>();
        fSizeToSingleWordSequence.put(0, "0");
        fSizeToSingleWordSequence.put(1, "1");

        String searchSpace = (WORDS[0] + WORDS[1]);

        fValidSearchString = true;
        for (int cIdx = 0; cIdx < fSearchString.length(); cIdx++) {
            if (searchSpace.indexOf(fSearchString.charAt(cIdx)) == -1) {
                fValidSearchString = false;
                break;
            }
        }

        if (fValidSearchString) {
            generateWordSequence(fSequenceSize);
        }
    }

    public KlocWorkSearch(Integer n, String subStr) {
        this(n, subStr, DEFAULT_PARTITION_N);
    }

    // WordSupplier takes a simple stream (consisting of 1 and 0)
    // representation, and converts each element in the stream to
    // one of the "atomic" words (kloc and work). This allows us
    // to store compressed / simple streams, and expand each element
    // in that stream into a word only when a search buffer is ready to
    // process it.
    private static class WordSupplier implements Supplier<String> {

        private String fStreamRepr;
        private int fStreamIdx;
        private boolean fDone = false;

        public WordSupplier(String streamRepr) {
            fStreamRepr = streamRepr;
            fStreamIdx = -1;
        }

        @Override
        public String get() {
            if (fDone) {
                return null;
            }

            fStreamIdx++;

            if (fStreamIdx == fStreamRepr.length()) {
                fDone = true;
                return null;
            } else {
                int wordIdx = (fStreamRepr.charAt(fStreamIdx)) == '0' ? 0 : 1;
                return WORDS[wordIdx];
            }
        }

        public boolean isFinished() {
            return fDone;
        }
    }

    // This class takes a simple stream of word indices (1s and 0s) and
    // and a substring to be found in the stream. It then continuously loads
    // words into a buffer, corresponding the word indices in the stream, to
    // enable string comparisons to find a match.
    private static class StreamingSearch {

        private long fResultCount;
        private String fSearchString;
        private String fWordIndexStream;
        private char fSearchStringLead;
        private int fSearchStringLength;
        private WordSupplier fWordSupplier;
        private StringBuilder fBuffer;

        public StreamingSearch(String wordIdxString, String subString) {
            fResultCount = -1;
            fWordIndexStream = wordIdxString;
            fSearchString = subString;
            fSearchStringLead = fSearchString.charAt(0);
            fSearchStringLength = fSearchString.length();
        }

        public StreamingSearch search() {
            fResultCount = 0;
            fBuffer = null;
            fWordSupplier = new WordSupplier(fWordIndexStream);

            while (!searchIsComplete()) {
                advanceBuffer();
                if (isMatch()) {
                    fResultCount++;
                }
            }
            return this;
        }

        public long getResultCount() {
            return fResultCount;
        }

        private boolean searchIsComplete() {
            return fWordSupplier.isFinished();
        }

        /**
         * Move the search buffer forward.
         */
        private void advanceBuffer() {
            if (fBuffer == null) {
                initializeBuffer();
            } else {
                /**
                 * Instead of simply moving forward by 1 element, we will
                 * move forward until we encounter a character that
                 * matches the first element of our searchString.
                 */
                do {
                    fBuffer.deleteCharAt(0);
                    if (fBuffer.length() < fSearchStringLength) {
                        String nextWord = fWordSupplier.get();
                        if (nextWord != null) {
                            fBuffer.append(nextWord);
                        } else {
                            break;
                        }
                    }
                } while (fBuffer.charAt(0) != fSearchStringLead);
            }
        }

        private boolean isMatch() {
            if (fBuffer.length() < fSearchStringLength) {
                return false;
            } else {
                return fBuffer.substring(0, fSearchStringLength).equals(fSearchString);
            }
        }

        private void initializeBuffer() {
            int numWordsInBuffer = (int)java.lang.Math.ceil(
                fSearchStringLength / (double) WORD_SIZE) + 1;
            int bufferLen = numWordsInBuffer*WORD_SIZE;
            fBuffer = new StringBuilder(bufferLen);
            for (int i = 1; i <= numWordsInBuffer; i++) {
                String word = fWordSupplier.get();
                if (word == null) {
                    break;
                }
                fBuffer.append(word);
            }
        }

        /**
         * Static utility method doing a plain old string comparison match
         * to find occurrences of the second string, in the first string.
         */
        public static long simpleSearch(String searchSpace, String searchString) {

            if (searchString.length() <= 0) {
                return (searchSpace.length() + 1);
            }

            long count = 0;
            long pos = 0;
            long step = 1;

            while (true) {
                pos = (long) searchSpace.indexOf(searchString, (int)pos);
                if (pos >= 0) {
                    count++;
                    pos += step;
                } else {
                    break;
                }
            }
            return count;
        }
    }

    public long computeOccurrences() {
        if (fValidSearchString) {
            return computeOccurrences(fSequenceSize);
        } else {
            return 0;
        }
    }

    /**
     * Compute the number of occurrences of the search string
     * in a word sequence produced by kw(n).
     */
    private long computeOccurrences(int n) {

        if (n <= fPartitionN) {
            StreamingSearch finder = new StreamingSearch(
                fSizeToSingleWordSequence.get(n), fSearchString);
            return finder.search().getResultCount();

        } else {
            String seq = fSizeToMultiWordSequence.get(n);
            String current;
            String next;
            long occurrences = 0;

            // Given a sequence like 313031:
            // The total number of occurrences of the substring in this sequence
            // can be determined by adding:
            // 1. The number of occurrences in N = 31
            // 2. The number of occurrences at the boundary of strings
            //    N = 31/30
            // 3. The number of occurrences in N = 30
            // 4. Num occurrences in boundary of 30/31
            // 5. Num occurrences in 31

            for (int idx = 0; idx < seq.length(); idx += 2) {
                current = seq.substring(idx, idx + 2);
                if (idx == seq.length() - 2) {
                    next = null;
                } else {
                    next = seq.substring(idx + 2, idx + 4);
                }

                occurrences += fCompoundWordsToOccurrences.get(current);
                if (next != null) {
                    occurrences += fCompoundWordsToOccurrences.get(current + next);
                }
            }
            return occurrences;
        }
    }

    /**
     * For a given n, this function generates the word sequence kw(n) that
     * is to be searched:
     * 1. For n < partition_n, the word sequence consists of simple / atomic
     *    elements, like 1 and 0, corresponding to the indices of the WORDS array.
     * 2. For n > partition_n, the word sequence becomes combinations of
     *    "compound words" i.e. the partition number, and it's predecessor.
     */
    private void generateWordSequence(int n) {
        Map<Integer, String> sequenceMap;
        int offset;
        if (n <= fPartitionN) {
            offset = 1;
            sequenceMap = fSizeToSingleWordSequence;
        } else {
            generateWordSequence(fPartitionN);

            fSizeToMultiWordSequence = new HashMap<>();
            fCompoundWordsToOccurrences = new HashMap<>();

            fSizeToMultiWordSequence.put(fPartitionN - 1, "" + (fPartitionN - 1));
            fSizeToMultiWordSequence.put(fPartitionN, "" + fPartitionN);

            String partStr = Integer.toString(fPartitionN);
            String partLessOneStr = Integer.toString(fPartitionN - 1);

            fCompoundWordsToOccurrences.put(partStr, computeOccurrences(fPartitionN));
            fCompoundWordsToOccurrences.put(partLessOneStr, computeOccurrences(fPartitionN - 1));

            computeBoundaryCounts();

            offset = fPartitionN;
            sequenceMap = fSizeToMultiWordSequence;
        }

        for (int seqIdx = (offset + 1); seqIdx <= n; seqIdx++) {
            String sequence = sequenceMap.get(seqIdx - 1) + sequenceMap.get(seqIdx - 2);
            sequenceMap.put(seqIdx, sequence);

            // At any given time, we only need the previous two map entries,
            // So it is safe to get rid of all previous map entries.
            if (sequenceMap.containsKey(seqIdx - 3)) {
                sequenceMap.remove(seqIdx - 3);
            }
        }
    }

    private void computeBoundaryCounts() {
        String partStr = Integer.toString(fPartitionN);
        String partLessOneStr = Integer.toString(fPartitionN - 1);

        // Best explained via an example:
        // Let's say the partition count (partition_n) is 31. Then for N > 31,
        // the generated word sequences will be a combination of 30 and 31
        // (partition_n and partition_n - 1). Since the 4 possible combinations are
        // 30/30, 30/31, 31/30 and 31/31, we need to find and cache away the occurrence
        // count of the search string at the intersection of these combinations
        // (referred to as boundary counts).
        fCompoundWordsToOccurrences.put(partStr + partStr, getBoundaryCounts(fPartitionN, fPartitionN));
        fCompoundWordsToOccurrences.put(partStr + partLessOneStr, getBoundaryCounts(fPartitionN, fPartitionN - 1));
        fCompoundWordsToOccurrences.put(partLessOneStr + partStr, getBoundaryCounts(fPartitionN - 1, fPartitionN));
        fCompoundWordsToOccurrences.put(partLessOneStr + partLessOneStr, getBoundaryCounts(fPartitionN - 1, fPartitionN - 1));
    }

    private long getBoundaryCounts(int n1, int n2) {
        // When searching for a substring (size M) at the intersection point of two larger
        // strings, the search string begins to span both strings from when (M-1) of its
        // characters are in the first string (n1), to when (M-1) characters are in the second
        // string (n2). Therefore, we compute a "boundaryStringLength" value of M - 1 i.e. to
        // find the number of occurrences of a search string at the intersection of two strings,
        // we only need to search 2M - 2 characters distributed over the intersection.
        int boundaryStringLength = fSearchString.length() - 1;
        String n1Str = fSizeToSingleWordSequence.get(n1);
        String n2Str = fSizeToSingleWordSequence.get(n2);

        int numWordsInBoundarySpace = (int) Math.ceil((fSearchString.length() - 1) / (double) WORD_SIZE);
        String s1 = n1Str.substring(n1Str.length() - numWordsInBoundarySpace, n1Str.length());
        String s2 = n2Str.substring(0, numWordsInBoundarySpace);

        StringBuilder boundarySb = new StringBuilder();

        Function<Stream<String>, String> streamToString = (streamSeq) ->
            streamSeq.map(s -> Integer.parseInt(s))
            .map(i -> WORDS[i])
            .collect(Collectors.joining());

        String s1Real = streamToString.apply(Stream.of(s1.split("")));
        String s2Real = streamToString.apply(Stream.of(s2.split("")));

        String countKey = boundarySb.append(s1Real.substring(s1Real.length() - boundaryStringLength, s1Real.length()))
                                    .append(s2Real.substring(0, boundaryStringLength))
                                    .toString();

        if (fCompoundWordsToOccurrences.containsKey(countKey)) {
            // Sometimes, boundary strings are identical, in that case
            // just fetch occurrences from the map, instead of recomputing.
            return fCompoundWordsToOccurrences.get(countKey);
        } else {
            return StreamingSearch.simpleSearch(countKey, fSearchString);
        }
    }
}