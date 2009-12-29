package edu.bgu.ir2009;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 26/12/2009
 * Time: 15:59:15
 */
public class PostingFileUtils {
    private static final Logger logger = Logger.getLogger(PostingFileUtils.class);
    private Configuration config;

    public PostingFileUtils(Configuration config) {
        this.config = config;
    }

    public void saveParsedDocument(ParsedDocument doc) throws IOException {
        File docFile = new File(config.getParsedDocsDir(), doc.getDocNo());
        if (!docFile.exists()) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(docFile));
                writer.write(doc.getByLine());
                writer.newLine();
                writer.write(doc.getCn());
                writer.newLine();
                writer.write(String.valueOf(doc.getDate()));
                writer.newLine();
                writer.write(doc.getIn());
                writer.newLine();
                writer.write(doc.getPage());
                writer.newLine();
                writer.write(doc.getPub());
                writer.newLine();
                writer.write(doc.getTp());
                writer.newLine();
                writer.write(doc.getText());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    public ParsedDocument loadParsedDocument(String docNo) {
        UnParsedDocument unParsedDoc = new UnParsedDocument();
        StringBuilder stringBuilder = new StringBuilder();
        File docFile = new File(config.getParsedDocsDir(), docNo);
        if (docFile.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(docFile));
                unParsedDoc.setByLine(reader.readLine());
                unParsedDoc.setCn(reader.readLine());
                unParsedDoc.setDate(Long.parseLong(reader.readLine()));
                unParsedDoc.setIn(reader.readLine());
                unParsedDoc.setPage(reader.readLine());
                unParsedDoc.setPub(reader.readLine());
                unParsedDoc.setTp(reader.readLine());
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
            } catch (Exception e) {
                logger.error(e, e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                        logger.error(ignored, ignored);
                    }
                }
            }
        }
        unParsedDoc.setText(stringBuilder.toString());
        return new ParsedDocument(unParsedDoc);
    }

    public void saveIndex(Map<String, TermData> index) throws IOException {
        logger.info("Saving index to file: " + config.getIndexFileName());
        File file = new File(config.getIndexFileName());
        if (file.exists()) {
            file.delete();
        }
        FileWriter writer = new FileWriter(file);
        for (TermData td : index.values()) {
            writer.write(td.getSavedString() + "\n");
        }
        writer.close();
        logger.info("Finished saving index...");
    }

    public InMemoryIndex loadInMemoryIndex() throws IOException {
        InMemoryIndex res = new InMemoryIndex(config);
        res.load();
        return res;
    }
}
