package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.LRUCache;
import edu.bgu.ir2009.auxiliary.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: Jan 28, 2010
 * Time: 9:57:31 PM
 */
public class IndexReader<ReadResult, ExtraParam> {
    private final IndexReadStrategy<ReadResult, ExtraParam> is;
    private final Map<String, Pair<Integer, Integer>> offsets = new HashMap<String, Pair<Integer, Integer>>();
    private final MappedByteBuffer indexBuffer;
    private final FileChannel indexChannel;
    private final LRUCache<String, ReadResult> cache = new LRUCache<String, ReadResult>(100);

    public IndexReader(IndexReadStrategy<ReadResult, ExtraParam> is) throws IOException {
        this.is = is;
        indexChannel = new RandomAccessFile(is.getIndexFileName(), "r").getChannel();
        indexBuffer = indexChannel.map(FileChannel.MapMode.READ_ONLY, 0, indexChannel.size());
        readRefFile();
    }

    private void readRefFile() throws IOException {
        LineIterator iterator = FileUtils.lineIterator(new File(is.getRefFileName()));
        while (iterator.hasNext()) {
            String line = iterator.nextLine();
            int start = 0;
            int end = line.indexOf(':');
            String refID = line.substring(start, end);
            start = end + 1;
            end = line.indexOf(':', start);
            Integer offset = Integer.parseInt(line.substring(start, end));
            Integer length = Integer.parseInt(line.substring(end + 1));
            offsets.put(refID, new Pair<Integer, Integer>(offset, length));
        }
        LineIterator.closeQuietly(iterator);
    }

    public ReadResult read(String refId, ExtraParam param) throws IOException {
        ReadResult res = cache.get(refId);
        if (res == null) {
            Pair<Integer, Integer> params = offsets.get(refId);
            if (params != null) {
                Integer length = params.getSecond();
                byte[] resBuffer = new byte[length];
                indexBuffer.position(params.getFirst());
                indexBuffer.get(resBuffer, 0, length);
                res = is.processLine(new String(resBuffer), param);
                cache.put(refId, res);
            }
        }
        return res;
    }

    public void close() throws IOException {
        indexChannel.close();
    }
}
