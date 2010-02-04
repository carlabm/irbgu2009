package edu.bgu.ir2009.auxiliary.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 15:34:13
 */
public class DocumentInputStream extends InputStream {
    private static final char HEADER[] = {'<', 'r', 'o', 'o', 't', '>'};
    private static final char FOOTER[] = {'<', '/', 'r', 'o', 'o', 't', '>'};
    private final FileChannel channel;
    private int headerPos;
    private int footerPos;
    private MappedByteBuffer buffer;

    public DocumentInputStream(String name) throws IOException {
        channel = new FileInputStream(name).getChannel();
        buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
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
            if (buffer.remaining() > 0) {
                res = buffer.get();
                if (res == '\n' || res == '\r') {
                    res = ' ';
                }
            } else {
                if (footerPos < FOOTER.length) {
                    res = FOOTER[footerPos];
                    footerPos++;
                } else {
                    res = -1;
                }
            }
        }
        return res;
    }

    public void close() throws IOException {
        channel.close();
    }
}
