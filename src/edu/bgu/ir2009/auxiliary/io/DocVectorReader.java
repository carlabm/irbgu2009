package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 19/02/2010
 * Time: 17:03:18
 */
public class DocVectorReader implements IndexReadStrategy<Map<String, Double>, Object> {
    private final String indexFileName;
    private final String indexRefFileName;

    public DocVectorReader(Configuration config) {
        indexFileName = config.getWorkingDir() + "/docVectors";
        indexRefFileName = config.getWorkingDir() + "/docVectorsRef";
    }

    public String getIndexFileName() {
        return indexFileName;
    }

    public String getRefFileName() {
        return indexRefFileName;
    }
//FT933-2980:to-0.07066715160000156,govern-0.5125653872521136,for-0.18227983623084,

    public Map<String, Double> processLine(String line, Object o) {
        Map<String, Double> res = new HashMap<String, Double>();
        int start = line.indexOf(':') + 1;
        while (start < line.length()) {
            int end = line.indexOf('-', start);
            String term = line.substring(start, end);
            start = end + 1;
            end = line.indexOf(',', start);
            double w = Double.parseDouble(line.substring(start, end));
            res.put(term, w);
            start = end + 1;
        }
        return res;
    }
}
