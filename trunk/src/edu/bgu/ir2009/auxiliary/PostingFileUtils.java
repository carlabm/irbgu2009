package edu.bgu.ir2009.auxiliary;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

    public static void writeDocumentVectors(Configuration config, Map<String, Long> termDFMap, long totalDocCount) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(config.getWorkingDir() + "/docVectors"));
        LineIterator iterator = FileUtils.lineIterator(new File(config.getPostingsFileName()));
        try {
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                int start = 0;
                int end = line.indexOf(':');
                writer.write(line.substring(start, end));
                writer.write(':');
                start = end + 1;
                while (start < line.length()) {
                    int termFreq = 0;
                    end = line.indexOf('-', start);
                    String term = line.substring(start, end);
                    start = end + 1;
                    int currEnd = line.indexOf('|', start);
                    while (start < currEnd) {
                        end = line.indexOf(',', start);
                        start = end + 1;
                        termFreq++;
                    }
                    writer.write(term);
                    writer.write('-');
                    writer.write(String.valueOf(calculateTF_IDF(termFreq, termDFMap.get(term), totalDocCount)));
                    writer.write(',');
                    start++;
                }
                writer.write('\n');
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    private static double calculateTF_IDF(int termFreq, Long termDFreq, long totalDocs) {
        return termFreq * Math.log10((double) totalDocs / termDFreq);
    }

    /* public static InMemoryIndex saveIndex(Map<String, TermData> index, Map<String, Map<String, Double>> documentsVectors, Configuration config) throws IOException {
            logger.info("Saving index to indexFile: " + config.getIndexFileName() + " Reference indexFile: " + config.getIndexReferenceFileName());
            File indexFile = new File(config.getIndexFileName());
            File indexRefFile = new File(config.getIndexReferenceFileName());
            InMemoryIndex res = new InMemoryIndex(config, false);
            FileWriter indexWriter = new FileWriter(indexFile);
            FileWriter indexRefWriter = new FileWriter(indexRefFile);
            int saved = 0;
            int totalToSave = index.size() + documentsVectors.size();
            long pos = 0;
            for (TermData td : index.values()) {
                String termSerialized = td.getSavedString();
                indexWriter.write(termSerialized + '\n');
                res.addTerm(td.getTerm(), pos, termSerialized.length());
                indexRefWriter.write(td.getTerm() + ":" + pos + ":" + termSerialized.length() + "\n");
                pos += termSerialized.length() + 1;
                saved++;
                UpFacade.getInstance().addIndexSavingEvent(saved, totalToSave);
            }
            indexWriter.write('\n');
            indexRefWriter.write('\n');
            pos++;
            for (String docNo : documentsVectors.keySet()) {
                StringBuilder builder = new StringBuilder();
                builder.append(docNo).append(':');
                Map<String, Double> docVector = documentsVectors.get(docNo);
                for (String term : docVector.keySet()) {
                    builder.append(term).append('=').append(docVector.get(term)).append(',');
                }
                String serializedDocVector = builder.toString();
                indexWriter.write(serializedDocVector + '\n');
                res.addDocVector(docNo, pos, serializedDocVector.length());
                indexRefWriter.write(docNo + ":" + pos + ":" + serializedDocVector.length() + "\n");
                pos += serializedDocVector.length() + 1;
                saved++;
                UpFacade.getInstance().addIndexSavingEvent(saved, totalToSave);
            }
            indexWriter.close();
            indexRefWriter.close();
            logger.info("Finished saving index...");
            return res;
        }
    */

    public static InMemoryDocs loadInMemoryDocs(Configuration config) throws IOException {
        InMemoryDocs res = new InMemoryDocs(config);
        res.load();
        return res;
    }


    public static void main(String[] args) throws IOException {
        Configuration configuration = new Configuration("8/conf.txt");
        Map<String, Long> map = getTermDFMap(configuration);
        writeDocumentVectors(configuration, map, 17047);
        int i = 0;
    }
}
