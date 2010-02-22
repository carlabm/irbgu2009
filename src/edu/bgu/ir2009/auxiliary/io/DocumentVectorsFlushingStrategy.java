package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.DocumentPostings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 17/02/2010
 * Time: 21:54:35
 */
public class DocumentVectorsFlushingStrategy implements FlushingStrategy {
    private final Configuration config;
    private final Map<String, Long> termDFMap;
    private final long totalDocCount;

    public DocumentVectorsFlushingStrategy(Configuration config, Map<String, Long> termDFMap, long totalDocCount) {
        this.config = config;
        this.termDFMap = termDFMap;
        this.totalDocCount = totalDocCount;
    }

    public String getTempFilePrefix() {
        return "tmp_docVectors_";
    }

    public void mergePreviousWithNew(Object toFlush, BufferedWriter flushWriter, String previous) throws IOException {
        throw new UnsupportedOperationException();
    }

    //FT933-2991:law-6,|scotland-10,|privatis-8,|mr-0,|ian-1,23,1232,|

    public void flushRemainingContent(Object toFlush, BufferedWriter flushWriter) throws IOException {
        LineIterator iterator = FileUtils.lineIterator(new File(config.getPostingsFileName()));
        try {
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                int start = 0;
                int end = line.indexOf(':');
                String docNo = line.substring(start, end);
                DocumentPostings documentPostings = new DocumentPostings(docNo);
                flushWriter.write(docNo);
                flushWriter.write(':');
                start = end + 1;
                Map<String, Double> tfMap = new LinkedHashMap<String, Double>();
                while (start < line.length()) {
                    int termFreq = 0;
                    end = line.indexOf('-', start);
                    String term = line.substring(start, end);
                    start = end + 1;
                    int currEnd = line.indexOf('|', start);
                    while (start < currEnd) {
                        end = line.indexOf(',', start);
                        long pos = Long.parseLong(line.substring(start, end));
                        documentPostings.addTerm(term, pos);
                        start = end + 1;
                    }
                    start++;
                }
                Map<String, Double> termWeights = calculateDocumentVector(termDFMap, totalDocCount, documentPostings);
                for (String term : termWeights.keySet()) {
                    flushWriter.write(term);
                    flushWriter.write('-');
                    flushWriter.write(String.valueOf(termWeights.get(term)));
                    flushWriter.write(',');
                }
                flushWriter.write('\n');
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    public static Map<String, Double> calculateDocumentVector(Map<String, Long> termDFMap, long totalDocCount, DocumentPostings doc) {
        double docLength = 0.0;
        Map<String, Set<Long>> terms = doc.getTerms();
        Map<String, Double> tfMap = new LinkedHashMap<String, Double>();
        for (String term : terms.keySet()) {
            int termFreq = terms.get(term).size();
            double tf_idf = calculateTF_IDF(termFreq, termDFMap.get(term), totalDocCount);
            docLength += tf_idf * tf_idf;
            tfMap.put(term, tf_idf);
        }
        docLength = Math.sqrt(docLength);
        Map<String, Double> documentVector = new HashMap<String, Double>();
        for (String term : terms.keySet()) {
            documentVector.put(term, tfMap.get(term) / docLength);
        }
        return documentVector;
    }

    private static double calculateTF_IDF(int termFreq, Long termDFreq, long totalDocs) {
        double res = 0.0;
        if (termDFreq != null) {
            res = termFreq * Math.log10((double) totalDocs / termDFreq);
        }
        return res;
    }

    public String getFinalFileName() {
        return config.getWorkingDir() + "/docVectors";
    }

    public String getRefFileName() {
        return config.getWorkingDir() + "/docVectorsRef";
    }

    public String getLineRefID(String line) {
        return line.substring(0, line.indexOf(':'));
    }

    public Configuration getConfig() {
        return config;
    }
}
