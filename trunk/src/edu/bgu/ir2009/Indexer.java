package edu.bgu.ir2009;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 25/12/2009
 * Time: 16:20:46
 */
public class Indexer {
    private Map<String, TermData> index = new HashMap<String, TermData>();
    private Set<ParsedDocument> docs = new HashSet<ParsedDocument>();
    private PostingFileUtils postingFileUtils;
    private Configuration config;

    public Indexer(Configuration config) {
        this.config = config;
        postingFileUtils = new PostingFileUtils(config);
    }

    public void addParsedDocument(ParsedDocument doc) throws IOException {
        String docNo = doc.getDocNo();
        Map<String, Set<Long>> docTerms = doc.getTerms();
        for (String term : docTerms.keySet()) {
            TermData termData = index.get(docNo);
            if (termData == null) {
                termData = new TermData(term);
                index.put(term, termData);
            }
            termData.addPosting(docNo, docTerms.get(term));
        }
        docs.add(doc);
    }

    public void saveIndex() throws IOException {
        File file = new File("index.txt");
        if (file.exists()) {
            file.delete();
        }
        FileWriter writer = new FileWriter(file);
        for (TermData td : index.values()) {
            writer.write(td.getSavedString() + "\n");
        }
        writer.close();
    }
}
