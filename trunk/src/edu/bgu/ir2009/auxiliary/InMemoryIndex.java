package edu.bgu.ir2009.auxiliary;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 29/12/2009
 * Time: 20:25:01
 */
public class InMemoryIndex {
    private final static Logger logger = Logger.getLogger(InMemoryIndex.class);
    private final Map<String, Long> termOffsets = new HashMap<String, Long>();
    private final Map<String, Long> docOffsets = new HashMap<String, Long>();
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
        LineIterator iterator = null;
        try {
            iterator = FileUtils.lineIterator(new File(config.getIndexReferenceFileName()));
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                if ("".equals(line)) {
                    break;
                }
                int end = line.indexOf('=');
                String term = line.substring(0, end);
                termOffsets.put(term, Long.parseLong(line.substring(end + 1, line.length())));
            }
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                int end = line.indexOf('=');
                String docNo = line.substring(0, end);
                docOffsets.put(docNo, Long.parseLong(line.substring(end + 1, line.length())));
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    public void addTerm(String term, long offset) {
        termOffsets.put(term, offset);
    }

    public void addDocVector(String docNo, long offset) {
        docOffsets.put(docNo, offset);
    }

    public TermData getTermData(String term) throws IOException {
        TermData res;
        if ((res = termDataCache.get(term)) == null) {
            Long termOffset = termOffsets.get(term);
            if (termOffset != null) {
                indexFile.seek(termOffset);
                String line = indexFile.readLine();
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
            Long docOffset = docOffsets.get(docNo);
            if (docOffset != null) {
                indexFile.seek(docOffset);
                res = parseDocumentVector(indexFile.readLine());
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
