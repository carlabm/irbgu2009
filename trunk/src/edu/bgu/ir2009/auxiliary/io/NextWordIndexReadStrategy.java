package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: Jan 28, 2010
 * Time: 10:46:00 PM
 */
public class NextWordIndexReadStrategy implements IndexReadStrategy<Map<String, Set<Long>>, String> {
    private final Configuration config;

    public NextWordIndexReadStrategy(Configuration config) {
        this.config = config;
    }

    public String getIndexFileName() {
        return config.getNextWordIndexFileName();
    }

    public String getRefFileName() {
        return config.getNextWordRefIndexFileName();
    }

    public Map<String, Set<Long>> processLine(String line, String second) {
        int start = line.indexOf(':') + 1;
        int end = line.indexOf('{');
        String currNextWord = line.substring(start, end);
        while (!second.equals(currNextWord) && (start = line.indexOf('}', end) + 2) < line.length()) {
            end = line.indexOf('{', start);
            currNextWord = line.substring(start, end);
        }
        HashMap<String, Set<Long>> res = null;
        if (second.equals(currNextWord)) {
            res = new HashMap<String, Set<Long>>();
            start = end + 1;
            end = line.indexOf('}', start);
            while (start < end) {
                Set<Long> postings = new LinkedHashSet<Long>();
                int currEnd = line.indexOf('[', start);
                String docNo = line.substring(start, currEnd);
                start = currEnd + 1;
                currEnd = line.indexOf(']', start);
                while (start < currEnd) {
                    int anotherCurrEnd = line.indexOf(',', start);
                    postings.add(Long.parseLong(line.substring(start, anotherCurrEnd)));
                    start = anotherCurrEnd + 1;
                }
                res.put(docNo, postings);
                start = currEnd + 2;
            }
        }
        return res;
    }


    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        IndexReader<Map<String, Set<Long>>, String> reader = new IndexReader<Map<String, Set<Long>>, String>(new NextWordIndexReadStrategy(new Configuration()));
        Map<String, Set<Long>> map = reader.read("vanessa", "besll");
        int i = 0;
    }
}
