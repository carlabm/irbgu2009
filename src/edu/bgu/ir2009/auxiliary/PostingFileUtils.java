package edu.bgu.ir2009.auxiliary;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 26/12/2009
 * Time: 15:59:15
 */
public class PostingFileUtils {
    private static final Logger logger = Logger.getLogger(PostingFileUtils.class);

    public static Map<String, Long> getTermDFMap(Configuration config) throws IOException {
        Map<String, Long> res = new HashMap<String, Long>();
        LineIterator iterator = FileUtils.lineIterator(new File(config.getIndexFileName()));
        try {
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                String term = line.substring(0, line.indexOf(':'));
                long docFreq = 0;
                int start = 0;
                while ((start = line.indexOf('{', start)) != -1) {
                    docFreq++;
                    start++;
                }
                res.put(term, docFreq);
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
        return res;
    }

    private static double calculateTF_IDF(int termFreq, Long termDFreq, long totalDocs) {
        return termFreq * Math.log10((double) totalDocs / termDFreq);
    }
}
