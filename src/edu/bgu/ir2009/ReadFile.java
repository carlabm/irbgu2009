package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.UnParsedDocument;
import edu.bgu.ir2009.auxiliary.UpFacade;
import edu.bgu.ir2009.auxiliary.io.DocumentIndex;
import edu.bgu.ir2009.auxiliary.io.DocumentInputStream;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
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
    private final static UnParsedDocument EMPTY_DOC = new UnParsedDocument();
    private final static Object lock = new Object();

    private final String dirName;
    private final ExecutorService executor;
    private final BlockingQueue<UnParsedDocument> docQueue = new LinkedBlockingQueue<UnParsedDocument>();
    private final DocumentIndex docIndex;
    private boolean isStarted = false;
    private int totalFiles;
    private int filesRead = 0;

    public ReadFile(String docsDir, String srcStopWordsFileName, boolean useStemmer) {
        this(new Configuration(docsDir, srcStopWordsFileName, useStemmer));
    }

    public ReadFile(Configuration config) {
        this.dirName = config.getDocsDir();
        executor = Executors.newFixedThreadPool(config.getReaderThreadsCount());
        docIndex = new DocumentIndex(config);
    }

    public void start() {
        synchronized (this) {
            if (!isStarted) {
                isStarted = true;
            } else {
                throw new IllegalStateException("cannot start same reader twice");
            }
        }
        executor.execute(new Runnable() {
            public void run() {
                File file = new File(dirName);
                String[] files = file.list();
                totalFiles = files.length;
                UpFacade.getInstance().addReaderEvent(0, totalFiles);
                for (int i = 0, filesLength = files.length; !executor.isShutdown() && i < filesLength; i++) {
                    String fileName = files[i];
                    executor.execute(new ReaderWorker(fileName));
                }
                new Thread(new Runnable() {
                    public void run() {
                        executor.shutdown();
                        try {
                            executor.awaitTermination(10, TimeUnit.DAYS);
                            docIndex.close();
                            docQueue.put(EMPTY_DOC);
                            logger.info("Finished reading all file. Total files: " + totalFiles);
                        } catch (Exception e) {
                            logger.warn(e, e);
                        }
                    }
                }).start();
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

    private void read(String fileName) throws XMLStreamException, IOException {
        DocumentInputStream inputStream = null;
        XMLStreamReader xmlParser = null;
        StAXOMBuilder builder = null;
        try {
            inputStream = new DocumentInputStream(dirName + "/" + fileName);
            xmlParser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            builder = new StAXOMBuilder(xmlParser);
            OMElement omElement = builder.getDocumentElement();
            Iterator docIterator = omElement.getChildElements();
            while (docIterator.hasNext()) {
                UnParsedDocument dr = new UnParsedDocument();
                OMElement docElement = (OMElement) docIterator.next();
                Iterator inDocElIterator = docElement.getChildElements();
                while (inDocElIterator.hasNext()) {
                    OMElement inDocEl = (OMElement) inDocElIterator.next();
                    String localName = inDocEl.getLocalName().trim();
                    String relatedText = inDocEl.getText().trim();
                    if ("DOCNO".equals(localName)) {
                        dr.setDocNo(relatedText);
                    } else {
                        if ("TEXT".equals(localName)) {
                            dr.setText(inDocEl.getText());
                        } else {
                            if ("DATE".equals(localName)) {
                                dr.setDate(Long.parseLong(relatedText));
                            } else {
                                if ("BYLINE".equals(localName)) {
                                    dr.setByLine(relatedText);
                                } else {
                                    if ("CN".equals(localName)) {
                                        dr.setCn(relatedText);
                                    } else {
                                        if ("IN".equals(localName)) {
                                            dr.setIn(relatedText);
                                        } else {
                                            if ("TP".equals(localName)) {
                                                dr.setTp(relatedText);
                                            } else {
                                                if ("PUB".equals(localName)) {
                                                    dr.setPub(relatedText);
                                                } else {
                                                    if ("PAGE".equals(localName)) {
                                                        dr.setPage(relatedText);
                                                    } else {
                                                        if ("HEADLINE".equals(localName)) {
                                                            dr.setHeadline(relatedText);
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
                try {
                    docIndex.addDocument(dr);
                    docQueue.put(dr);
                } catch (InterruptedException ignored) {
                    logger.debug("Interrupted while putting unparsed document in the docsQueue");
                }
            }
        } finally {
            if (builder != null) {
                builder.close();
            }
            if (xmlParser != null) {
                xmlParser.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void stop() {
        executor.shutdownNow();
    }


    private class ReaderWorker implements Runnable {
        private final String fileName;

        public ReaderWorker(String fileName) {
            this.fileName = fileName;
        }

        public void run() {
            try {
                read(fileName);
                synchronized (lock) {
                    filesRead++;
                    UpFacade.getInstance().addReaderEvent(filesRead, totalFiles);
                }
            } catch (Exception e) {
                logger.error(e, e);
                try {
                    docQueue.put(EMPTY_DOC);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
