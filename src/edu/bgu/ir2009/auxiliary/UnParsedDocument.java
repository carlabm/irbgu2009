package edu.bgu.ir2009.auxiliary;

import org.apache.log4j.Logger;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 16:33:54
 */
public class UnParsedDocument {
    private final static Logger logger = Logger.getLogger(UnParsedDocument.class);
    private String docNo;
    private long date;
    private String byLine = "";
    private String cn = "";
    private String in = "";
    private String tp = "";
    private String pub = "";
    private String page = "";

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

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "UnParsedDocument{" +
                " docNo='" + docNo + '\'' +
                ", page='" + page + '\'' +
                ", pub='" + pub + '\'' +
                ", tp='" + tp + '\'' +
                ", in='" + in + '\'' +
                ", cn='" + cn + '\'' +
                ", byLine='" + byLine + '\'' +
                ", date=" + date +
                '}';
    }
}
