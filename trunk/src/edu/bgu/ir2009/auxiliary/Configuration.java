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
    private static final String INDEX_FILE_NAME = "index";
    private static final String STOP_WORDS_FILE_NAME = "stop_words.txt";
    private static final String CONF_FILE_NAME = "conf.txt";

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

    public Configuration(String docsDir, String srcStopWordsFileName, boolean useStemmer) {
        this(docsDir, srcStopWordsFileName, useStemmer, 1.0, 2.0);
    }

    public Configuration(String docsDir, String srcStopWordsFileName, boolean useStemmer, double lambda, double gamma) {
        this(docsDir, srcStopWordsFileName, useStemmer, lambda, gamma, 2, 2, 2);
    }

    public Configuration(String docsDir, String srcStopWordsFileName, boolean useStemmer, double lambda, double gamma, int readerThreadsCount, int parserThreadsCount, int indexerThreadsCount) {
        File srcDocsDir = new File(docsDir);
        File stopWordsFile = new File(srcStopWordsFileName);
        if (!srcDocsDir.exists() || !stopWordsFile.exists()) {
            throw new RuntimeException("Could not find stop words and/or source documents directory");
        }
        this.docsDir = docsDir;
        this.useStemmer = useStemmer;
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
        File newDir = new File("" + (biggest + 1));
        boolean success = newDir.mkdir();
        if (!success) {
            throw new RuntimeException("Could not create directory for current configuration!");
        }
        String newDirName = newDir.getName();
        savedDocsFileName = newDirName + "/" + SAVED_DOCS_FILE_NAME;
        //noinspection ResultOfMethodCallIgnored
        indexFileName = newDirName + "/" + INDEX_FILE_NAME;
        this.srcStopWordsFileName = newDirName + "/" + STOP_WORDS_FILE_NAME;
        copyStopWordsFile(stopWordsFile, this.srcStopWordsFileName);
        this.readerThreadsCount = readerThreadsCount;
        this.parserThreadsCount = parserThreadsCount;
        this.indexerThreadsCount = indexerThreadsCount;
        this.lambda = lambda;
        this.gamma = gamma;
        saveConfFile(newDirName + "/" + CONF_FILE_NAME);
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
        config.setProperty("indexFileName", indexFileName);
        config.setProperty("useStemmer", String.valueOf(useStemmer));
        config.setProperty("srcStopWordsFileName", srcStopWordsFileName);
        config.setProperty("readerThreadsCount", String.valueOf(readerThreadsCount));
        config.setProperty("parserThreadsCount", String.valueOf(parserThreadsCount));
        config.setProperty("indexerThreadsCount", String.valueOf(indexerThreadsCount));
        config.setProperty("lambda", String.valueOf(lambda));
        config.setProperty("gamma", String.valueOf(gamma));
        try {
            config.store(new FileOutputStream(confFileName), "");
        } catch (IOException e) {
            logger.error("Could not save configurations file");
        }
    }

    public Configuration(String configFileName) {
        boolean exceptionThrown = false;
        try {
            config.load(new FileInputStream(configFileName));
            logger.debug("Loaded configurations file successfully: " + configFileName + "\n" + config);
        } catch (IOException e) {
            logger.warn("Could not load properties file: " + configFileName + "! Using defaults when possible...");
            exceptionThrown = true;
        }
        docsDir = config.getProperty("docsDir", "src_docs");
        savedDocsFileName = config.getProperty("savedDocsFileName", "docs");
        indexFileName = config.getProperty("indexFileName", "index");
        useStemmer = Boolean.parseBoolean(config.getProperty("useStemmer", "true"));
        srcStopWordsFileName = config.getProperty("srcStopWordsFileName", "stop_words.txt");
        readerThreadsCount = Integer.parseInt(config.getProperty("readerThreadsCount", "2"));
        parserThreadsCount = Integer.parseInt(config.getProperty("parserThreadsCount", "2"));
        indexerThreadsCount = Integer.parseInt(config.getProperty("indexerThreadsCount", "2"));
        lambda = Double.parseDouble(config.getProperty("lambda", "1.0"));
        gamma = Double.parseDouble(config.getProperty("gamma", "2.0"));
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
}
