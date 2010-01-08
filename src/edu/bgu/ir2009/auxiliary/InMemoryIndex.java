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
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 29/12/2009
 * Time: 20:25:01
 */
public class InMemoryIndex {
    private final static Logger logger = Logger.getLogger(InMemoryIndex.class);
    private final Map<String, Long> termOffsets = new HashMap<String, Long>();
    private final Configuration config;

    public InMemoryIndex(Configuration config) {
        this.config = config;
    }

    public void load() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(config.getIndexFileName()));
        long offset = 0L;
        String line;
        while ((line = reader.readLine()) != null) {
            String term = line.substring(0, line.indexOf(':'));
            termOffsets.put(term, offset);
            offset += line.length() + 1;
        }
        reader.close();
    }

    public void addTerm(String term, long offset) {
        termOffsets.put(term, offset);
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

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        InMemoryIndex memoryIndex = new InMemoryIndex(new Configuration("project.cfg"));
        memoryIndex.load();
        TermData termData = memoryIndex.getTermData("beatti");
        int i = 0;
    }
}
