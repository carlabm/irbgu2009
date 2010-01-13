package edu.bgu.ir2009.auxiliary;

import edu.bgu.ir2009.auxiliary.io.DocumentReader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693
 * Date: 01/01/2010
 * Time: 03:21:13
 */
public class InMemoryDocs {    //TODO change to work with DocumentReader
    private final static Logger logger = Logger.getLogger(InMemoryDocs.class);
    private final Configuration config;
    private final DocumentReader documentReader;
    private Map<String, Long> docsOffsets = new HashMap<String, Long>();

    public InMemoryDocs(Configuration config) throws FileNotFoundException {
        this.config = config;
        documentReader = new DocumentReader(config);
    }

    public void addDocument(String docNo, long offset) {
        docsOffsets.put(docNo, offset);
    }

    public void load() throws IOException {
        docsOffsets = documentReader.readRefFile();
    }

    public UnParsedDocument getDocData(String docNo) throws IOException {
        UnParsedDocument res = null;
        Long offset = docsOffsets.get(docNo);
        if (offset != null) {
            res = documentReader.read(offset);
        }
        return res;
    }

    public static void main(String[] args) throws IOException, ParseException {
        BasicConfigurator.configure();
        InMemoryDocs memoryDocs = new InMemoryDocs(new Configuration("2/conf.txt"));
        memoryDocs.load();
        UnParsedDocument data = memoryDocs.getDocData("FT933-495");
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
        Date date = f.parse("921210");
        int i = 0;
    }
}
