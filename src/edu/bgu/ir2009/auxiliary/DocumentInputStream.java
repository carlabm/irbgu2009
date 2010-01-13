package edu.bgu.ir2009.auxiliary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 15:34:13
 */
public class DocumentInputStream extends InputStream {
    private static final char HEADER[] = {'<', 'r', 'o', 'o', 't', '>'};
    private static final char FOOTER[] = {'<', '/', 'r', 'o', 'o', 't', '>'};
    private FileInputStream is;
    private int headerPos;
    private int footerPos;

    public DocumentInputStream(String name) throws FileNotFoundException {
        is = new FileInputStream(name);
        headerPos = 0;
        footerPos = 0;
    }

    @Override
    public int read() throws IOException {
        int res;
        if (headerPos < HEADER.length) {
            res = HEADER[headerPos];
            headerPos++;
        } else {
            res = is.read();
            if (res == '\n' || res == '\r') {
                res = ' ';
            }
            if (res == -1) {
                if (footerPos < FOOTER.length) {
                    res = FOOTER[footerPos];
                    footerPos++;
                }
            }
        }
        return res;
    }

    public void close() throws IOException {
        is.close();
    }
}
