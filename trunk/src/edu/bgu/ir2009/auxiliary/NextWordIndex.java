package edu.bgu.ir2009.auxiliary;

import edu.bgu.ir2009.auxiliary.io.FlushWriter;
import edu.bgu.ir2009.auxiliary.io.NextWordFlushingStrategy;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 13/01/2010
 * Time: 02:02:04
 */
public class NextWordIndex {
    private final Logger logger = Logger.getLogger(NextWordIndex.class);
    private final Map<String, Map<String, Map<String, Set<Long>>>> index = new HashMap<String, Map<String, Map<String, Set<Long>>>>();
    private final Map<String, Pair<Long, Long>> offsets = new HashMap<String, Pair<Long, Long>>();
    private final FlushWriter<Map<String, Map<String, Map<String, Set<Long>>>>> nwWriter;
    private final Configuration config;
    private final Object lock = new Object();
    private int currentUnflushedCount = 0;
    private RandomAccessFile indexFile;


    public NextWordIndex(Configuration config, boolean loadFromFile) throws IOException {
        this.config = config;
        if (loadFromFile) {
            nwWriter = null;
            indexFile = new RandomAccessFile(config.getNextWordIndexFileName(), "r");
            load();
        } else {
            nwWriter = new FlushWriter<Map<String, Map<String, Map<String, Set<Long>>>>>(new NextWordFlushingStrategy(config), config);
        }
    }

    public Map<String, Set<Long>> getNextWordPostings(String first, String second) throws IOException {
        Map<String, Set<Long>> res = null;
        Pair<Long, Long> pair = offsets.get(first);
        if (pair != null) {
            StringBuilder builder = new StringBuilder();
            FileChannel channel = indexFile.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, pair.getFirst(), pair.getSecond());
            while (buffer.remaining() > 0) {
                builder.append((char) buffer.get());
            }
            String line = builder.toString();
            int start = line.indexOf(':') + 1;
            int end = line.indexOf('{');
            String currNextWord = line.substring(start, end);
            while (!second.equals(currNextWord) && (start = line.indexOf('}', end) + 2) < line.length()) {
                end = line.indexOf('{', start);
                currNextWord = line.substring(start, end);
            }
            if (second.equals(currNextWord)) {
                res = new HashMap<String, Set<Long>>();
                start = end + 1;
                end = line.indexOf('}', start);
                while (start < end) {
                    Set<Long> postings = new LinkedHashSet<Long>();
                    int currEnd = line.indexOf('[', start);
                    String docNo = line.substring(start, currEnd);
                    start = currEnd + 1;
                    currEnd = line.indexOf(']', start);
                    while (start < currEnd) {
                        int anotherCurrEnd = line.indexOf(',', start);
                        postings.add(Long.parseLong(line.substring(start, anotherCurrEnd)));
                        start = anotherCurrEnd + 1;
                    }
                    res.put(docNo, postings);
                    start = currEnd + 2;
                }
            }
        }
        return res;
    }

    public void load() throws IOException {
        FileChannel channel = new RandomAccessFile(config.getNextWordRefIndexFileName(), "r").getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        StringBuilder builder = new StringBuilder();
        while (buffer.remaining() > 0) {
            char singleChar = (char) buffer.get();
            if (singleChar != '\n') {
                builder.append(singleChar);
            } else {
                String line = builder.toString();
                int first = line.indexOf(':');
                int second = line.indexOf(':', first + 1);
                String firstWord = line.substring(0, first);
                Long offset = Long.parseLong(line.substring(first + 1, second));
                Long size = Long.parseLong(line.substring(second + 1, line.length()));
                offsets.put(firstWord, new Pair<Long, Long>(offset, size));
                builder.delete(0, builder.length());
            }
        }
        channel.close();
    }

    public synchronized void addWordPair(String doc, String first, String second, Long pos) {
        currentUnflushedCount++;
        if (currentUnflushedCount > 1000000) {
            currentUnflushedCount = 0;
            try {
                logger.info("Flushing next word records...");
                nwWriter.flush(index);
                logger.info("Done flushing next word records...");
            } catch (IOException e) {
                logger.error(e, e);
            }
        }
        Map<String, Map<String, Set<Long>>> nextWordMap = index.get(first);
        if (nextWordMap == null) {
            nextWordMap = new HashMap<String, Map<String, Set<Long>>>();
            index.put(first, nextWordMap);
        }
        Map<String, Set<Long>> postingsMap = nextWordMap.get(second);
        if (postingsMap == null) {
            postingsMap = new HashMap<String, Set<Long>>();
            nextWordMap.put(second, postingsMap);
        }
        Set<Long> docPostings = postingsMap.get(doc);
        if (docPostings == null) {
            docPostings = new LinkedHashSet<Long>();
            postingsMap.put(doc, docPostings);
        }
        docPostings.add(pos);
    }

    public synchronized void close() throws IOException {
        logger.info("Flushing and Closing remaining unflushed next words...");
        nwWriter.flush(index);
        nwWriter.close();
    }
}
