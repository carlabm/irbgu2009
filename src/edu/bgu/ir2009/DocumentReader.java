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

    private String text;

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

    public char read() {
        char res = '\0';
        if (!finished) {
            synchronized (lock) {
                if (!finished) {
                    try {
                        res = queue.take();
                        if (res == '\0') {
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

    public void setText(String text) {
        setText(text, true);
    }

    public void setText(String text, boolean disassembleText) {
        if (this.text != null) {
            throw new IllegalStateException("Cannot set text for a reader twice");
        }
        this.text = text;
        if (disassembleText) {
            new Thread(new TextDisassemblerWorker(docNo, text)).start();
        }
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "DocumentReader{" +
                "page='" + page + '\'' +
                ", pub='" + pub + '\'' +
                ", tp='" + tp + '\'' +
                ", in='" + in + '\'' +
                ", cn='" + cn + '\'' +
                ", byLine='" + byLine + '\'' +
                ", date=" + date +
                ", docNo='" + docNo + '\'' +
                '}';
    }

    private class TextDisassemblerWorker implements Runnable {
        private String docNum;
        private final String text;

        public TextDisassemblerWorker(String docNum, String text) {
            this.docNum = docNum;
            this.text = text;
        }

        public void run() {
            char[] chars = text.toCharArray();
            try {
                for (char aChar : chars) {
                    if (aChar != '\0') {
                        queue.put(aChar);
                    }
                }
                queue.put('\0');
                logger.info("Finished disassembling " + docNum + "...");
            } catch (InterruptedException e) {
                logger.warn(e, e);
            }
        }
    }
}
