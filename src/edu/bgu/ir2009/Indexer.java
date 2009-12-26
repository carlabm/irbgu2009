package edu.bgu.ir2009;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 25/12/2009
 * Time: 16:20:46
 */
public class Indexer {
    private Map<String, TermData> index = new HashMap<String, TermData>();
    private PostingFileUtils postingFileUtils;

    public Indexer(Configuration config) {
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
        postingFileUtils.saveParsedDocument(doc);
    }
}
