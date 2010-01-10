package edu.bgu.ir2009.auxiliary;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public InMemoryIndex(Configuration config) {
        this.config = config;
    }

    public void load() throws IOException {
        LineIterator iterator = null;
        try {
            iterator = FileUtils.lineIterator(new File(config.getIndexFileName()));
            long offset = 0L;
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                if ("".equals(line)) {
                    break;
                }
                String term = line.substring(0, line.indexOf(':'));
                termOffsets.put(term, offset);
                offset += line.length() + 1;
            }
            offset++;
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                String docNo = line.substring(0, line.indexOf(':'));
                docOffsets.put(docNo, offset);
                offset += line.length() + 1;
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    public void newLoad() throws IOException {
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
        TermData res = null;
        Long termOffset = termOffsets.get(term);
        if (termOffset != null) {
            RandomAccessFile file = new RandomAccessFile(config.getIndexFileName(), "r");
            file.seek(termOffset);
            res = new TermData(term, file.readLine());
            file.close();
        }
        return res;
    }

    public Map<String, Double> getDocumentVector(String docNo, Set<String> termsSet) throws IOException {
        Map<String, Double> res = null;
        Long docOffset = docOffsets.get(docNo);
        if (docOffset != null) {
            RandomAccessFile file = new RandomAccessFile(config.getIndexFileName(), "r");
            file.seek(docOffset);
            res = parseDocumentVector(file.readLine(), termsSet);
            file.close();
        }
        return res;
    }

    private Map<String, Double> parseDocumentVector(String serialized, Set<String> termsSet) {
        Map<String, Double> res = new HashMap<String, Double>();
        int start = serialized.indexOf(":") + 1;
        int end = serialized.length();
        while (start < end) {
            int currEnd = serialized.indexOf('=', start);
            String currTerm = serialized.substring(start, currEnd);
            start = currEnd + 1;
            if (termsSet.contains(currTerm)) {
                currEnd = serialized.indexOf(',', start);
                Double currTermWeight = Double.parseDouble(serialized.substring(start, currEnd));
                res.put(currTerm, currTermWeight);
                start = currEnd + 1;
            } else {
                start = serialized.indexOf(',', start) + 1;
            }
        }
        return res;
    }
}
