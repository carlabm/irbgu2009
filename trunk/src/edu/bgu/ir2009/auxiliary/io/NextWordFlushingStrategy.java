package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: Jan 28, 2010
 * Time: 7:08:02 PM
 */
public class NextWordFlushingStrategy implements FlushingStrategy<Map<String, Map<String, Map<String, Set<Long>>>>> {
    private final Configuration config;

    public NextWordFlushingStrategy(Configuration config) {
        this.config = config;
    }

    public String getTempFilePrefix() {
        return "tmp_nwindex_";
    }

    public void mergePreviousWithNew(Map<String, Map<String, Map<String, Set<Long>>>> toFlush, BufferedWriter flushWriter, String previous) throws IOException {
        int start = 0;
        int end = previous.indexOf(':', start);
        String first = previous.substring(start, end);
        Map<String, Map<String, Set<Long>>> firstMap = toFlush.get(first);
        flushWriter.write(first + ':');
        while ((start = end + 1) < previous.length()) {
            end = previous.indexOf('{', start);
            String currNextWord = previous.substring(start, end);
            start = end;
            end = previous.indexOf('}', start);
            String toAppend = previous.substring(start, end);
            flushWriter.write(currNextWord + toAppend);
            if (firstMap != null) {
                Map<String, Set<Long>> secondMap = firstMap.get(currNextWord);
                if (secondMap != null) {
                    StringBuilder tmpBuilder = new StringBuilder();
                    buildPostingForDoc(tmpBuilder, secondMap);
                    firstMap.remove(currNextWord);
                    flushWriter.write(tmpBuilder.toString());
                }
            }
            flushWriter.write("},");
            end++;
        }
        if (firstMap != null && !firstMap.isEmpty()) {
            StringBuilder tmpBuilder = new StringBuilder();
            buildPostings(tmpBuilder, firstMap);
            flushWriter.write(tmpBuilder.toString());
        }
        toFlush.remove(first);
    }

    public void flushRemainingContent(Map<String, Map<String, Map<String, Set<Long>>>> toFlush, BufferedWriter flushWriter) throws IOException {
        for (String first : toFlush.keySet()) {
            String serialized = buildSerializedPosting(first, toFlush);
            flushWriter.write(serialized + '\n');
        }
        toFlush.clear();
    }

    public String getFinalFileName() {
        return config.getNextWordIndexFileName();
    }

    public String getRefFileName() {
        return config.getNextWordRefIndexFileName();
    }

    public String getLineRefID(String line) {
        return line.substring(0, line.indexOf(':'));
    }

    public Configuration getConfig() {
        return config;
    }

    private static String buildSerializedPosting(String first, Map<String, Map<String, Map<String, Set<Long>>>> index) {
        StringBuilder builder = new StringBuilder();
        builder.append(first).append(':');
        Map<String, Map<String, Set<Long>>> nextWordMap = index.get(first);
        buildPostings(builder, nextWordMap);
        return builder.toString();
    }

    private static void buildPostings(StringBuilder builder, Map<String, Map<String, Set<Long>>> nextWordMap) {
        for (String second : nextWordMap.keySet()) {
            builder.append(second).append('{');
            Map<String, Set<Long>> postingsMap = nextWordMap.get(second);
            buildPostingForDoc(builder, postingsMap);
            builder.append('}').append(',');
        }
    }

    private static void buildPostingForDoc(StringBuilder builder, Map<String, Set<Long>> postingsMap) {
        for (String docNo : postingsMap.keySet()) {
            builder.append(docNo).append('[');
            Set<Long> docPostingsSet = postingsMap.get(docNo);
            for (Long posting : docPostingsSet) {
                builder.append(posting).append(',');
            }
            builder.append(']').append(',');
        }
    }
}
