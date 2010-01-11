package edu.bgu.ir2009.auxiliary;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 26/12/2009
 * Time: 15:59:15
 */
public class PostingFileUtils {
    private static final Logger logger = Logger.getLogger(PostingFileUtils.class);

    public static InMemoryIndex saveIndex(Map<String, TermData> index, Map<String, Map<String, Double>> documentsVectors, Configuration config) throws IOException {
        logger.info("Saving index to indexFile: " + config.getIndexFileName() + " Reference indexFile: " + config.getIndexReferenceFileName());
        File indexFile = new File(config.getIndexFileName());
        if (indexFile.exists()) {
            FileUtils.deleteQuietly(indexFile);
        }
        File indexRefFile = new File(config.getIndexReferenceFileName());
        if (indexRefFile.exists()) {
            FileUtils.deleteQuietly(indexRefFile);
        }
        InMemoryIndex res = new InMemoryIndex(config);
        FileWriter indexWriter = new FileWriter(indexFile);
        FileWriter indexRefWriter = new FileWriter(indexRefFile);
        int saved = 0;
        int totalToSave = index.size() + documentsVectors.size();
        long pos = 0;
        for (TermData td : index.values()) {
            String termSerialized = td.getSavedString();
            indexWriter.write(termSerialized + '\n');
            res.addTerm(td.getTerm(), pos);
            indexRefWriter.write(td.getTerm() + "=" + pos + "\n");
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
            res.addDocVector(docNo, pos);
            indexRefWriter.write(docNo + "=" + pos + "\n");
            pos += serializedDocVector.length() + 1;
            saved++;
            UpFacade.getInstance().addIndexSavingEvent(saved, totalToSave);
        }
        indexWriter.close();
        indexRefWriter.close();
        logger.info("Finished saving index...");
        return res;
    }

    public static InMemoryIndex loadInMemoryIndex(Configuration config) throws IOException {
        InMemoryIndex res = new InMemoryIndex(config);
        res.newLoad();
        return res;
    }

    public static InMemoryDocs loadInMemoryDocs(Configuration config) throws IOException {
        InMemoryDocs res = new InMemoryDocs(config);
        res.load();
        return res;
    }

    public static InMemoryDocs saveParsedDocuments(final Set<ParsedDocument> docsCache, final Configuration config) throws IOException {
        logger.info("Saving parsed documents to: " + config.getSavedDocsFileName());
        InMemoryDocs res = new InMemoryDocs(config);
        File docFile = new File(config.getSavedDocsFileName());
        if (!docFile.exists()) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(docFile));
                int savedDocs = 0;
                long offset = 0;
                for (ParsedDocument doc : docsCache) {
                    String serializedDoc = doc.serialize();
                    writer.write(serializedDoc + "\n");
                    res.addDocument(doc.getDocNo(), offset);
                    offset += serializedDoc.length() + 1;
                    savedDocs++;
                    UpFacade.getInstance().addDocumentsSavingEvent(savedDocs, docsCache.size());
                }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
        logger.info("Finished saving documents. Saved " + docsCache.size() + " documents.");
        return res;
    }
}
