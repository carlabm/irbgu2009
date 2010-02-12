package edu.bgu.ir2009.auxiliary;

import edu.bgu.ir2009.auxiliary.io.FlushWriter;
import edu.bgu.ir2009.auxiliary.io.NextWordFlushingStrategy;
import org.apache.log4j.Logger;

import java.io.IOException;
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
    private final FlushWriter<Map<String, Map<String, Map<String, Set<Long>>>>> nwWriter;
    private int currentUnflushedCount = 0;


    public NextWordIndex(Configuration config) throws IOException {
        nwWriter = new FlushWriter<Map<String, Map<String, Map<String, Set<Long>>>>>(new NextWordFlushingStrategy(config), config);
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
