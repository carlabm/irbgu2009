package edu.bgu.ir2009;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 15:08:03
 */
public class ReadFile {
    private final static Logger logger = Logger.getLogger(ReadFile.class);

    private final String dirName;
    private BlockingQueue<DocumentReader> docQueue = new LinkedBlockingQueue<DocumentReader>();
    private boolean finished = false;
    private final Object lock = new Object();

    public ReadFile(String dirName) {
        this.dirName = dirName;
    }

    public void start(String fileName) throws XMLStreamException, FileNotFoundException {
        XMLStreamReader xmlParser = XMLInputFactory.newInstance().createXMLStreamReader(new DocumentInputStream(dirName + "/" + fileName));
        StAXOMBuilder builder = new StAXOMBuilder(xmlParser);
        OMElement omElement = builder.getDocumentElement();
        Iterator docIterator = omElement.getChildElements();
        while (docIterator.hasNext()) {
            DocumentReader dr = new DocumentReader();
            OMElement docElement = (OMElement) docIterator.next();
            Iterator inDocElIterator = docElement.getChildElements();
            while (inDocElIterator.hasNext()) {
                OMElement inDocEl = (OMElement) inDocElIterator.next();
                String localName = inDocEl.getLocalName().trim();
                if ("DOCNO".equals(localName)) {
                    dr.setDocNo(inDocEl.getText().trim());
                    putDocument(dr);
                } else {
                    if ("TEXT".equals(localName)) {
                        dr.readText(inDocEl.getText());
                    } else {
                        if ("DATE".equals(localName)) {
                            dr.setDate(Long.parseLong(inDocEl.getText().trim()));
                        } else {
                            if ("BYLINE".equals(localName)) {
                                dr.setByLine(inDocEl.getText().trim());
                            } else {
                                if ("CN".equals(localName)) {
                                    dr.setCn(inDocEl.getText().trim());
                                } else {
                                    if ("IN".equals(localName)) {
                                        dr.setIn(inDocEl.getText().trim());
                                    } else {
                                        if ("TP".equals(localName)) {
                                            dr.setTp(inDocEl.getText().trim());
                                        } else {
                                            if ("PUB".equals(localName)) {
                                                dr.setPub(inDocEl.getText().trim());
                                            } else {
                                                if ("PAGE".equals(localName)) {
                                                    dr.setPage(inDocEl.getText().trim());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void putDocument(DocumentReader dr) {
        try {
            docQueue.put(dr);
        } catch (InterruptedException e) {
            logger.warn(e, e);
        }
    }

    public DocumentReader getNextDocument() {
        DocumentReader res = null;
        if (!finished) {
            synchronized (lock) {
                if (!finished) {
                    try {
                        res = docQueue.take();
                        if (res.getDocNo() == null) {
                            finished = true;
                            res = null;
                        }
                    } catch (InterruptedException e) {
                        logger.warn(e, e);
                    }
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        ReadFile readFile = new ReadFile("FT933");
        try {
            readFile.start("FT933_1");
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
