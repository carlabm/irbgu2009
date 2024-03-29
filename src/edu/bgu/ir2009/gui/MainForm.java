package edu.bgu.ir2009.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import edu.bgu.ir2009.IndexerV2;
import edu.bgu.ir2009.Searcher;
import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.RankedDocument;
import edu.bgu.ir2009.auxiliary.UnParsedDocument;
import edu.bgu.ir2009.auxiliary.io.DocumentReadStrategy;
import edu.bgu.ir2009.auxiliary.io.IndexReader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.TreeSet;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 29/12/2009
 * Time: 23:53:01
 */
public class MainForm {
    private static final Logger logger = Logger.getLogger(MainForm.class);
    private JPanel root;
    private JTextField searchTextField;
    private JButton GOButton;
    private JTable table1;
    private JLabel searchLabel;
    private JLabel lastSearchLabel;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem indexDirMenuItem;
    private JMenuItem loadConfMenuItem;
    private ResultsTableModel tableModel;
    private final JFrame frame;
    private Searcher searcher;

    public MainForm(JFrame frame) {
        this.frame = frame;
        initMenuBar();
        searchTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        GOButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
        try {
            setConfiguration(new Configuration());
        } catch (IOException ignored) {
        }
    }

    private void doSearch() {
        String searchString = searchTextField.getText().trim();
        TreeSet<RankedDocument> rankedDocumentTreeSet = searcher.search(searchString);
        logger.info("Got results: " + rankedDocumentTreeSet.size());
        try {
            tableModel.setResultDocuments(rankedDocumentTreeSet);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(root, e.getMessage(), "Error Searching", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        JFrame frame = new JFrame("Information Retrieval");
        MainForm form = new MainForm(frame);
        frame.setJMenuBar(form.menuBar);
        frame.setContentPane(form.root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initMenuBar() {
        menuBar = new JMenuBar();

        //Create file menu
        fileMenu = new JMenu("File");

        indexDirMenuItem = new JMenuItem("Index Directory...");
        indexDirMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IndexDialog dialog = new IndexDialog();
                dialog.setLocationRelativeTo(root);
                dialog.setVisible(true);
                IndexerV2 indexer = dialog.getIndexer();
                if (indexer != null) {
                    try {
                        setConfiguration(indexer.getConfig());
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(root, e1.getMessage(), "Error Loading Configuration After Indexing", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(indexDirMenuItem);

        loadConfMenuItem = new JMenuItem("Load Configuration File...");
        loadConfMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoadConfigDialog dialog = new LoadConfigDialog();
                dialog.setLocationRelativeTo(root);
                dialog.setVisible(true);
                String configFileName = dialog.getConfigFileName();
                if (configFileName != null) {
                    try {
                        loadConfigFile(configFileName);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(root, e1.getMessage(), "Error Loading Configuration File", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(loadConfMenuItem);

        menuBar.add(fileMenu);
    }

    private void loadConfigFile(String configFileName) throws IOException {
        Configuration configuration = new Configuration(configFileName);
        setConfiguration(configuration);
    }

    private void setConfiguration(Configuration configuration) throws IOException {
        searcher = new Searcher(configuration);
        tableModel = new ResultsTableModel(configuration);
        table1.setModel(tableModel);
        tableModel.fireTableDataChanged();
        setSearchEnabled(true);
        searchTextField.requestFocus();
        frame.setTitle("Information Retrieval - Configuration File: " + configuration.getConfigFileName());
    }

    private void setSearchEnabled(boolean state) {
        searchTextField.setEnabled(state);
        GOButton.setEnabled(state);
        searchLabel.setEnabled(state);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        root = new JPanel();
        root.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,fill:d:grow"));
        searchLabel = new JLabel();
        searchLabel.setEnabled(false);
        searchLabel.setText("Search:");
        CellConstraints cc = new CellConstraints();
        root.add(searchLabel, cc.xy(1, 1));
        searchTextField = new JTextField();
        searchTextField.setEnabled(false);
        root.add(searchTextField, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        GOButton = new JButton();
        GOButton.setEnabled(false);
        GOButton.setText("GO");
        root.add(GOButton, cc.xy(5, 1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow", "fill:d:grow"));
        root.add(panel1, cc.xyw(1, 7, 5));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.FILL));
        table1 = new JTable();
        scrollPane1.setViewportView(table1);
        final JLabel label1 = new JLabel();
        label1.setText("Last Search:");
        root.add(label1, cc.xy(1, 5));
        final JSeparator separator1 = new JSeparator();
        root.add(separator1, cc.xyw(1, 3, 5, CellConstraints.FILL, CellConstraints.FILL));
        lastSearchLabel = new JLabel();
        lastSearchLabel.setText("");
        root.add(lastSearchLabel, cc.xyw(3, 5, 3));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }

    private class ResultsTableModel extends AbstractTableModel {
        private final Class<?>[] columnClasses = {Integer.class, String.class, String.class};
        private final String[] columnName = {"Rank:", "Document #:", "Headline:"};
        private final IndexReader<UnParsedDocument, Object> docReader;

        private RankedDocument[] returnedDocuments = new RankedDocument[0];
        private UnParsedDocument[] documentsData = new UnParsedDocument[0];

        private ResultsTableModel(Configuration config) throws IOException {
            docReader = new IndexReader<UnParsedDocument, Object>(new DocumentReadStrategy(config));
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnClasses[columnIndex];
        }

        @Override
        public String getColumnName(int column) {
            return columnName[column];
        }

        public void setResultDocuments(TreeSet<RankedDocument> docs) throws IOException {
            returnedDocuments = docs.descendingSet().toArray(new RankedDocument[docs.descendingSet().size()]);
            documentsData = new UnParsedDocument[returnedDocuments.length];
            for (int i = 0; i < documentsData.length; i++) {
                documentsData[i] = docReader.read(returnedDocuments[i].getDocNum(), null);
            }
            fireTableDataChanged();
        }

        public RankedDocument[] getRankedResults() {
            return returnedDocuments;
        }

        public UnParsedDocument[] getDocumentsData() {
            return documentsData;
        }

        public int getRowCount() {
            return returnedDocuments.length;
        }

        public int getColumnCount() {
            return columnClasses.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Object res = null;
            if (rowIndex < returnedDocuments.length && columnIndex < columnClasses.length) {
                switch (columnIndex) {
                    case 0:
                        res = rowIndex;
                        break;
                    case 1:
                        res = returnedDocuments[rowIndex].getDocNum();
                        break;
                    case 2:
                        res = documentsData[rowIndex].getHeadline();
                        break;
                }
            }
            return res;
        }
    }

}
