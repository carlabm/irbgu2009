package edu.bgu.ir2009;

import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 16:33:54
 */
public class DocumentReader {
    private final static Logger logger = Logger.getLogger(DocumentReader.class);

    private BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
    private boolean finished = false;
    private final Object lock = new Object();

    private final String docNo;
    private final long date;
    private final String byLine;
    private String cn;
    private String in;
    private String tp;
    private String page;

    public DocumentReader(String docNo, long date, String byLine) {
        this.docNo = docNo;
        this.date = date;
        this.byLine = byLine;
    }

    public String getDocNo() {
        return docNo;
    }

    public long getDate() {
        return date;
    }

    public String getByLine() {
        return byLine;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public int read() {
        int res = -1;
        if (!finished) {
            synchronized (lock) {
                if (!finished) {
                    try {
                        res = queue.take();
                        if (res == -1) {
                            finished = true;
                        }
                    } catch (InterruptedException e) {
                        logger.warn(e, e);
                    }
                }
            }
        }
        return res;
    }

    public void put(int character) {
        try {
            queue.put(character);
        } catch (InterruptedException e) {
            logger.warn(e, e);
        }
    }
}
