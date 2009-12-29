package edu.bgu.ir2009;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 15:08:03
 */
public class ReadFile {
    private final static Logger logger = Logger.getLogger(ReadFile.class);
    private final static UnParsedDocument emptyDoc = new UnParsedDocument();

    private final String dirName;
    private final ExecutorService executorService;
    private final BlockingQueue<UnParsedDocument> docQueue = new LinkedBlockingQueue<UnParsedDocument>();

    public ReadFile(Configuration config) {
        this.dirName = config.getDocumentsDir();
        executorService = Executors.newFixedThreadPool(config.getReaderThreadsCount());
    }

    public void start() throws XMLStreamException, FileNotFoundException {
        executorService.execute(new Runnable() {
            public void run() {
                File file = new File(dirName);
                String[] files = file.list();
                for (int i = 0, filesLength = files.length; i < filesLength; i++) {
                    String fileName = files[i];
                    logger.info(fileName + " is being submitted for reading...");
                    if (i == filesLength - 1) {
                        final Future<?> future = executorService.submit(new ReaderWorker(fileName));
                        executorService.execute(new Runnable() {
                            public void run() {
                                try {
                                    if (future != null) {
                                        future.get(); //wait for last reading to finish
                                    }
                                } catch (InterruptedException e) {
                                    logger.warn(e, e);
                                } catch (ExecutionException e) {
                                    logger.error(e, e);
                                }
                                logger.debug("putting empty doc in queue");
                                putDocument(emptyDoc);
                                executorService.shutdown();
                            }
                        });
                    } else {
                        executorService.execute(new ReaderWorker(fileName));
                    }
                }
            }
        });
    }

    public UnParsedDocument getNextDocument() {
        UnParsedDocument res = null;
        try {
            res = docQueue.take();
            if (res.getDocNo() == null) {
                res = null;
            }
        } catch (InterruptedException e) {
            logger.debug("Interrupted while getting unparsed document from the docsQueue");
        }
        return res;
    }

    private void read(String fileName) throws XMLStreamException, FileNotFoundException {
        logger.info("Starting reading file: " + fileName);
        XMLStreamReader xmlParser = XMLInputFactory.newInstance().createXMLStreamReader(new DocumentInputStream(dirName + "/" + fileName));
        StAXOMBuilder builder = new StAXOMBuilder(xmlParser);
        OMElement omElement = builder.getDocumentElement();
        Iterator docIterator = omElement.getChildElements();
        while (docIterator.hasNext()) {
            UnParsedDocument dr = new UnParsedDocument();
            OMElement docElement = (OMElement) docIterator.next();
            Iterator inDocElIterator = docElement.getChildElements();
            while (inDocElIterator.hasNext()) {
                OMElement inDocEl = (OMElement) inDocElIterator.next();
                String localName = inDocEl.getLocalName().trim();
                if ("DOCNO".equals(localName)) {
                    dr.setDocNo(inDocEl.getText().trim());
                } else {
                    if ("TEXT".equals(localName)) {
                        dr.setText(inDocEl.getText());
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
            putDocument(dr);
        }
        logger.info("Finished reading file: " + fileName);
    }


    private void putDocument(UnParsedDocument dr) {
        try {
            docQueue.put(dr);
            logger.debug("Finished reading doc: " + dr.getDocNo());
        } catch (InterruptedException ignored) {
            logger.debug("Interrupted while putting unparsed document in the docsQueue");
        }
    }

    private class ReaderWorker implements Runnable {
        private final String fileName;

        public ReaderWorker(String fileName) {
            this.fileName = fileName;
        }

        public void run() {
            try {
                read(fileName);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }

    public static void main(String[] args) throws IOException, XMLStreamException {
        BasicConfigurator.configure();
        ReadFile readFile = new ReadFile(new Configuration("project.cfg"));
        readFile.start();
        UnParsedDocument doc;
        while ((doc = readFile.getNextDocument()) != null) {
        }
    }
}
