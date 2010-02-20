package edu.bgu.ir2009.auxiliary;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 25/12/2009
 * Time: 16:01:15
 */
public class Configuration {
    private static final Logger logger = Logger.getLogger(Configuration.class);
    private static final String SAVED_DOCS_FILE_NAME = "saved_docs";
    private static final String SAVED_DOCS_REF_FILE_NAME = "saved_docs_ref";
    private static final String INDEX_FILE_NAME = "index";
    private static final String INDEX_REF_FILE_NAME = "index_ref";
    private static final String NEXT_WORD_INDEX_FILE_NAME = "nw_index";
    private static final String NEXT_WORD_INDEX_REF_FILE_NAME = "nw_index_ref";
    private static final String STOP_WORDS_FILE_NAME = "stop_words.txt";
    private static final String CONF_FILE_NAME = "conf.txt";
    private static final String POSTINGS_FILE_NAME = "postings";

    private final Properties config = new Properties();
    private final String docsDir;
    private final String savedDocsFileName;
    private final String indexFileName;
    private final Boolean useStemmer;
    private final String srcStopWordsFileName;
    private final int readerThreadsCount;
    private final int parserThreadsCount;
    private final int indexerThreadsCount;
    private final double lambda;
    private final double gamma;
    private final int DMax;
    private final String indexReferenceFileName;
    private final int inMemoryIndexCacheSize;
    private final int inMemoryDocsCacheSize;
    private final String savedDocsRefFileName;
    private final String nextWordIndexFileName;
    private final String nextWordRefIndexFileName;
    private final String workingDir;
    private final String postingsFileName;
    private String configFileName;
    private Integer docsCount;

    public Configuration() {
        this(getBiggestDirNum() + "/" + CONF_FILE_NAME);
    }

    public Configuration(String docsDir, String srcStopWordsFileName, boolean useStemmer) {
        this(docsDir, srcStopWordsFileName, useStemmer, 45, 1.0, 2.0);
    }

    public Configuration(String docsDir, String srcStopWordsFileName, boolean useStemmer, int DMax, double lambda, double gamma) {
        this(docsDir, srcStopWordsFileName, useStemmer, DMax, lambda, gamma, 2, 2, 2);
    }

    public Configuration(String docsDir, String srcStopWordsFileName, boolean useStemmer, int DMax, double lambda, double gamma, int readerThreadsCount, int parserThreadsCount, int indexerThreadsCount) {
        File srcDocsDir = new File(docsDir);
        File stopWordsFile = new File(srcStopWordsFileName);
        if (!srcDocsDir.exists() || !stopWordsFile.exists()) {
            throw new RuntimeException("Could not find stop words and/or source documents directory");
        }
        this.docsDir = docsDir;
        this.useStemmer = useStemmer;
        File newDir = new File("" + (getBiggestDirNum() + 1));
        boolean success = newDir.mkdir();
        if (!success) {
            throw new RuntimeException("Could not create directory for current configuration!");
        }
        workingDir = newDir.getName();
        savedDocsFileName = workingDir + "/" + SAVED_DOCS_FILE_NAME;
        savedDocsRefFileName = workingDir + "/" + SAVED_DOCS_REF_FILE_NAME;
        //noinspection ResultOfMethodCallIgnored
        indexFileName = workingDir + "/" + INDEX_FILE_NAME;
        indexReferenceFileName = workingDir + "/" + INDEX_REF_FILE_NAME;
        nextWordIndexFileName = workingDir + "/" + NEXT_WORD_INDEX_FILE_NAME;
        nextWordRefIndexFileName = workingDir + "/" + NEXT_WORD_INDEX_REF_FILE_NAME;
        this.srcStopWordsFileName = workingDir + "/" + STOP_WORDS_FILE_NAME;
        postingsFileName = workingDir + "/" + POSTINGS_FILE_NAME;
        copyStopWordsFile(stopWordsFile, this.srcStopWordsFileName);
        this.readerThreadsCount = readerThreadsCount;
        this.parserThreadsCount = parserThreadsCount;
        this.indexerThreadsCount = indexerThreadsCount;
        this.lambda = lambda;
        this.gamma = gamma;
        this.DMax = DMax;
        inMemoryIndexCacheSize = 200;
        inMemoryDocsCacheSize = 100;
        saveConfFile(workingDir + "/" + CONF_FILE_NAME);
    }

    private void copyStopWordsFile(File stopWordsFile, String srcStopWordsFileName) {
        try {
            FileUtils.copyFile(stopWordsFile, new File(srcStopWordsFileName));
        } catch (IOException e) {
            logger.error(e, e);
        }
    }

    private void saveConfFile(String confFileName) {
        config.setProperty("docsDir", docsDir);
        config.setProperty("savedDocsFileName", savedDocsFileName);
        config.setProperty("savedDocsRefFileName", savedDocsRefFileName);
        config.setProperty("indexFileName", indexFileName);
        config.setProperty("indexRefFileName", indexReferenceFileName);
        config.setProperty("nextWordIndexFileName", nextWordIndexFileName);
        config.setProperty("nextWordRefIndexFileName", nextWordRefIndexFileName);
        config.setProperty("postingsFileName", postingsFileName);
        config.setProperty("useStemmer", String.valueOf(useStemmer));
        config.setProperty("srcStopWordsFileName", srcStopWordsFileName);
        config.setProperty("readerThreadsCount", String.valueOf(readerThreadsCount));
        config.setProperty("parserThreadsCount", String.valueOf(parserThreadsCount));
        config.setProperty("indexerThreadsCount", String.valueOf(indexerThreadsCount));
        config.setProperty("DMax", String.valueOf(DMax));
        config.setProperty("lambda", String.valueOf(lambda));
        config.setProperty("gamma", String.valueOf(gamma));
        config.setProperty("inMemoryIndexCacheSize", String.valueOf(inMemoryIndexCacheSize));
        config.setProperty("inMemoryDocsCacheSize", String.valueOf(inMemoryDocsCacheSize));
        config.setProperty("workingDir", workingDir);
        try {
            config.store(new FileOutputStream(confFileName), "");
            configFileName = confFileName;
        } catch (IOException e) {
            logger.error("Could not save configurations file");
        }
    }

    public Configuration(String configFileName) {
        this.configFileName = configFileName;
        boolean exceptionThrown = false;
        try {
            config.load(new FileInputStream(configFileName));
            logger.debug("Loaded configurations file successfully: " + configFileName + "\n" + config);
        } catch (IOException e) {
            logger.warn("Could not load properties file: " + configFileName + "! Using defaults when possible...");
            exceptionThrown = true;
        }
        workingDir = config.getProperty("workingDir", ".");
        docsDir = config.getProperty("docsDir", "docs");
        savedDocsFileName = config.getProperty("savedDocsFileName", workingDir + "/" + SAVED_DOCS_FILE_NAME);
        savedDocsRefFileName = config.getProperty("savedDocsRefFileName", workingDir + "/" + SAVED_DOCS_REF_FILE_NAME);
        indexFileName = config.getProperty("indexFileName", workingDir + "/" + INDEX_FILE_NAME);
        indexReferenceFileName = config.getProperty("indexRefFileName", workingDir + "/" + INDEX_REF_FILE_NAME);
        nextWordIndexFileName = config.getProperty("nextWordIndexFileName", workingDir + "/" + NEXT_WORD_INDEX_FILE_NAME);
        nextWordRefIndexFileName = config.getProperty("nextWordRefIndexFileName", workingDir + "/" + NEXT_WORD_INDEX_REF_FILE_NAME);
        postingsFileName = config.getProperty("postingsFileName", workingDir + "/" + POSTINGS_FILE_NAME);
        useStemmer = Boolean.parseBoolean(config.getProperty("useStemmer", "true"));
        srcStopWordsFileName = config.getProperty("srcStopWordsFileName", workingDir + "/" + STOP_WORDS_FILE_NAME);
        readerThreadsCount = Integer.parseInt(config.getProperty("readerThreadsCount", "2"));
        parserThreadsCount = Integer.parseInt(config.getProperty("parserThreadsCount", "2"));
        indexerThreadsCount = Integer.parseInt(config.getProperty("indexerThreadsCount", "2"));
        lambda = Double.parseDouble(config.getProperty("lambda", "1.0"));
        gamma = Double.parseDouble(config.getProperty("gamma", "2.0"));
        DMax = Integer.parseInt(config.getProperty("DMax", "10"));
        inMemoryIndexCacheSize = Integer.parseInt(config.getProperty("inMemoryIndexCacheSize", "200"));
        inMemoryDocsCacheSize = Integer.parseInt(config.getProperty("inMemoryDocsCacheSize", "100"));
        docsCount = Integer.parseInt(config.getProperty("docsCount", "0"));
        if (docsCount == 0) {
            docsCount = null;
        }
        if (exceptionThrown) {
            saveConfFile(configFileName);
        }
    }

    public String getDocsDir() {
        return docsDir;
    }

    public int getIndexerThreadsCount() {
        return indexerThreadsCount;
    }

    public String getIndexFileName() {
        return indexFileName;
    }

    public int getParserThreadsCount() {
        return parserThreadsCount;
    }

    public int getReaderThreadsCount() {
        return readerThreadsCount;
    }

    public String getSavedDocsFileName() {
        return savedDocsFileName;
    }

    public String getSrcStopWordsFileName() {
        return srcStopWordsFileName;
    }

    public Boolean useStemmer() {
        return useStemmer;
    }

    public double getLambda() {
        return lambda;
    }

    public double getGamma() {
        return gamma;
    }

    public int getDMax() {
        return DMax;
    }

    public String getIndexReferenceFileName() {
        return indexReferenceFileName;
    }

    public int getInMemoryIndexCacheSize() {
        return inMemoryIndexCacheSize;
    }

    public int getInMemoryDocsCacheSize() {
        return inMemoryDocsCacheSize;
    }

    private static int getBiggestDirNum() {
        File currentDir = new File(".");
        File[] numberNamedDirs = currentDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                boolean accept = file.isDirectory();
                if (accept) {
                    String name = file.getName();
                    try {
                        Integer.parseInt(name);
                    } catch (NumberFormatException e) {
                        accept = false;
                    }
                }
                return accept;
            }
        });
        int biggest = 0;
        for (File dir : numberNamedDirs) {
            int dirNum = Integer.parseInt(dir.getName());
            if (dirNum > biggest) {
                biggest = dirNum;
            }
        }
        return biggest;
    }

    public String getSavedDocsRefFileName() {
        return savedDocsRefFileName;
    }

    public String getNextWordIndexFileName() {
        return nextWordIndexFileName;
    }

    public String getNextWordRefIndexFileName() {
        return nextWordRefIndexFileName;
    }

    public String getWorkingDir() {
        return workingDir;
    }


    public String getPostingsFileName() {
        return postingsFileName;
    }

    public void setTotalDocuments(int docsCount) {
        this.docsCount = docsCount;
        config.setProperty("docsCount", String.valueOf(docsCount));
        try {
            config.store(new FileOutputStream(configFileName), "");
        } catch (IOException e) {
            logger.error("Could not save configurations file");
        }
    }

    public Integer getDocumentsCount() {
        return docsCount;
    }
}
