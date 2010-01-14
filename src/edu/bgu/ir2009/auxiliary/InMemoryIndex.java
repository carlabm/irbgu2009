package edu.bgu.ir2009.auxiliary;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 29/12/2009
 * Time: 20:25:01
 */
public class InMemoryIndex {
    private final static Logger logger = Logger.getLogger(InMemoryIndex.class);
    private final Map<String, Pair<Long, Long>> termOffsets = new HashMap<String, Pair<Long, Long>>();
    private final Map<String, Pair<Long, Long>> docOffsets = new HashMap<String, Pair<Long, Long>>();
    private final Configuration config;
    private final LRUCache<String, TermData> termDataCache;
    private final LRUCache<String, Map<String, Double>> docsVectorCache;
    private RandomAccessFile indexFile;

    public InMemoryIndex(Configuration config, boolean existingIndex) throws FileNotFoundException {
        this.config = config;
        termDataCache = new LRUCache<String, TermData>(config.getInMemoryIndexCacheSize());
        docsVectorCache = new LRUCache<String, Map<String, Double>>(config.getInMemoryDocsCacheSize());
        if (existingIndex) {
            indexFile = new RandomAccessFile(config.getIndexFileName(), "r");
        }
    }

    public void load() throws IOException {
        FileChannel channel = new FileInputStream(config.getIndexReferenceFileName()).getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        StringBuilder builder = new StringBuilder();
        while (buffer.remaining() > 0) {
            char singleChar = (char) buffer.get();
            if (singleChar != '\n') {
                builder.append(singleChar);
            } else {
                String line = builder.toString();
                if ("".equals(line)) {
                    break;
                }
                int end = line.indexOf(':');
                String term = line.substring(0, end);
                int start = end + 1;
                end = line.indexOf(':', start);
                long offset = Long.parseLong(line.substring(start, end));
                long length = Long.parseLong(line.substring(end + 1, line.length()));
                termOffsets.put(term, new Pair<Long, Long>(offset, length));
                builder.delete(0, builder.length());
            }
        }
        while (buffer.remaining() > 0) {
            char singleChar = (char) buffer.get();
            if (singleChar != '\n') {
                builder.append(singleChar);
            } else {
                String line = builder.toString();
                int end = line.indexOf(':');
                String docNo = line.substring(0, end);
                int start = end + 1;
                end = line.indexOf(':', start);
                long offset = Long.parseLong(line.substring(start, end));
                long length = Long.parseLong(line.substring(end + 1, line.length()));
                docOffsets.put(docNo, new Pair<Long, Long>(offset, length));
            }
        }
        channel.close();
    }

    public void addTerm(String term, long offset, long length) {
        termOffsets.put(term, new Pair<Long, Long>(offset, length));
    }

    public void addDocVector(String docNo, long offset, long length) {
        docOffsets.put(docNo, new Pair<Long, Long>(offset, length));
    }

    public TermData getTermData(String term) throws IOException {
        TermData res;
        if ((res = termDataCache.get(term)) == null) {
            Pair<Long, Long> pair = termOffsets.get(term);
            if (pair != null) {
                MappedByteBuffer buffer = indexFile.getChannel().map(FileChannel.MapMode.READ_ONLY, pair.getFirst(), pair.getSecond());
                StringBuilder builder = new StringBuilder();
                while (buffer.remaining() > 0) {
                    builder.append((char) buffer.get());
                }
                String line = builder.toString();
                try {
                    res = new TermData(term, line);
                } catch (RuntimeException e) {
                    res = null;
                    logger.error("Error while parsing term data from file: term - " + term + " . Read from file - " + line);
                }
                termDataCache.put(term, res);
            }
        }
        return res;
    }

    public Map<String, Double> getDocumentVector(String docNo) throws IOException {
        Map<String, Double> res;
        if ((res = docsVectorCache.get(docNo)) == null) {
            Pair<Long, Long> pair = docOffsets.get(docNo);
            if (pair != null) {
                MappedByteBuffer buffer = indexFile.getChannel().map(FileChannel.MapMode.READ_ONLY, pair.getFirst(), pair.getSecond());
                StringBuilder builder = new StringBuilder();
                while (buffer.remaining() > 0) {
                    builder.append((char) buffer.get());
                }
                res = parseDocumentVector(builder.toString());
                docsVectorCache.put(docNo, res);
            }
        }
        return res;
    }

    private Map<String, Double> parseDocumentVector(String serialized) {
        Map<String, Double> res = new HashMap<String, Double>();
        int start = serialized.indexOf(":") + 1;
        int end = serialized.length();
        while (start < end) {
            int currEnd = serialized.indexOf('=', start);
            String currTerm = serialized.substring(start, currEnd);
            start = currEnd + 1;
            currEnd = serialized.indexOf(',', start);
            Double currTermWeight = Double.parseDouble(serialized.substring(start, currEnd));
            res.put(currTerm, currTermWeight);
            start = currEnd + 1;
        }
        return res;
    }
}
