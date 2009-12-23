package edu.bgu.ir2009;

import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 16:33:54
 */
public class DocumentReader {
    private final static Logger logger = Logger.getLogger(DocumentReader.class);

    private BlockingQueue<Character> queue = new LinkedBlockingQueue<Character>();
    private boolean finished = false;
    private final Object lock = new Object();

    private String docNo;
    private long date;
    private String byLine;
    private String cn;
    private String in;
    private String tp;
    private String pub;
    private String page;

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getByLine() {
        return byLine;
    }

    public void setByLine(String byLine) {
        this.byLine = byLine;
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

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
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

    public void readText(String characters) throws XMLStreamException {
        char[] chars = characters.toCharArray();
        for (char aChar : chars) {
            try {
                queue.put(aChar);
            } catch (InterruptedException e) {
                logger.warn(e, e);
            }
        }
    }
}
