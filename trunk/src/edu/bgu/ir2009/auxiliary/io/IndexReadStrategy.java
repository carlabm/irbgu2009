package edu.bgu.ir2009.auxiliary.io;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: Jan 28, 2010
 * Time: 9:59:55 PM
 */
public interface IndexReadStrategy<ReadResult, ExtraParam> {
    String getIndexFileName();

    String getRefFileName();

    ReadResult processLine(String line, ExtraParam param);
}
