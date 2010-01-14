package edu.bgu.ir2009.auxiliary.io;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 14/01/2010
 * Time: 00:59:59
 */
public class NIOBufferedReader {
    public static void main(String[] args) throws FileNotFoundException {
        RandomAccessFile file = new RandomAccessFile("1/nw_index", "r");
        FileChannel channel = file.getChannel();
//        channel.map(FileChannel.MapMode.READ_ONLY, )
    }
}
