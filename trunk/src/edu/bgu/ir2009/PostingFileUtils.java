package edu.bgu.ir2009;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 26/12/2009
 * Time: 15:59:15
 */
public class PostingFileUtils {
    private static final Logger logger = Logger.getLogger(PostingFileUtils.class);
    private File docsDir;

    public PostingFileUtils(Configuration config) {
        docsDir = new File(config.getParsedDocsDir());
    }

    public void saveParsedDocument(ParsedDocument doc) throws IOException {
        File docFile = new File(docsDir, doc.getDocNo());
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
        File docFile = new File(docsDir, docNo);
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
}
