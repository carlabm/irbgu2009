package edu.bgu.ir2009.auxiliary;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693
 * Date: 01/01/2010
 * Time: 03:21:13
 */
public class InMemoryDocs {
    private final static Logger logger = Logger.getLogger(InMemoryDocs.class);
    private final Map<String, Long> docsOffsets = new HashMap<String, Long>();
    private final Configuration config;

    public InMemoryDocs(Configuration config) {
        this.config = config;
    }

    public void addDocument(String docNo, long offset) {
        docsOffsets.put(docNo, offset);
    }

    public void load() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(config.getSavedDocsFileName()));
        long offset = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            String docNo = line.substring(0, line.indexOf('|'));
            docsOffsets.put(docNo, offset);
            offset += line.length() + 1;
        }
        reader.close();
    }

    public ParsedDocument getDocData(String docNo) throws IOException {
        ParsedDocument res = null;
        Long docOffset = docsOffsets.get(docNo);
        if (docOffset != null) {
            RandomAccessFile file = new RandomAccessFile(config.getSavedDocsFileName(), "r");
            file.seek(docOffset);
            res = new ParsedDocument(file.readLine());
            file.close();
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        InMemoryDocs memoryDocs = new InMemoryDocs(new Configuration("2/conf.txt"));
        memoryDocs.load();
        ParsedDocument data = memoryDocs.getDocData("FT933-495");
        int i = 0;
    }
}
