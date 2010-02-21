package edu.bgu.ir2009.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import edu.bgu.ir2009.IndexerV2;
import edu.bgu.ir2009.auxiliary.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

public class IndexingDialog extends JDialog implements Observer {
    private JPanel contentPane;
    private JProgressBar readerProgressBar;
    private JProgressBar parsingProgressBar;
    private JProgressBar indexingProgressBar;
    private JButton buttonCancel;
    private JProgressBar savingProgressBar;
    private JProgressBar savingDocsProgressBar;
    private JButton closeButton;
    private JLabel statusBarTextField;
    private IndexerV2 indexer;

    public IndexingDialog() {
        setTitle("Indexing...");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        UpFacade.getInstance().addObserver(this);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        pack();
    }

    private void onCancel() {
        if (buttonCancel.isEnabled()) {
            statusBarTextField.setText("Canceling...");
            buttonCancel.setEnabled(false);
            indexer.stop();
            dispose();
        }
        if (closeButton.isEnabled()) {
            dispose();
        }
    }

    public IndexerV2 getIndexer() {
        return indexer;
    }

    public static void main(String[] args) {
        IndexingDialog dialog = new IndexingDialog();
        dialog.setVisible(true);
    }

    public void update(Observable o, final Object arg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (arg instanceof IndexEvent) {
                    IndexEvent indexEvent = (IndexEvent) arg;
                    indexer = indexEvent.getIndexer();
                    buttonCancel.setEnabled(true);
                } else {
                    if (arg instanceof ReaderEvent) {
                        ReaderEvent readerEvent = (ReaderEvent) arg;
                        readerProgressBar.setValue((int) (((double) readerEvent.getFilesRead()) / readerEvent.getTotalFiles() * 100));
                        readerProgressBar.setString(readerEvent.getFilesRead() + "/" + readerEvent.getTotalFiles());
                    } else {
                        if (arg instanceof ParserEvent) {
                            ParserEvent parserEvent = (ParserEvent) arg;
                            parsingProgressBar.setValue((int) (((double) parserEvent.getParsedDocs()) / parserEvent.getTotalParsedDocs() * 100));
                            parsingProgressBar.setString(parserEvent.getParsedDocs() + "/" + parserEvent.getTotalParsedDocs());
                        } else {
                            if (arg instanceof IndexerEvent) {
                                IndexerEvent indexerEvent = (IndexerEvent) arg;
                                indexingProgressBar.setValue((int) (((double) indexerEvent.getIndexedDocs()) / indexerEvent.getTotalToIndexDocs() * 100));
                                indexingProgressBar.setString(indexerEvent.getIndexedDocs() + "/" + indexerEvent.getTotalToIndexDocs());
                            } else {
                                if (arg instanceof StatusEvent) {
                                    StatusEvent statusEvent = (StatusEvent) arg;
                                    switch (statusEvent.getType()) {
                                        case DONE:
                                            buttonCancel.setEnabled(false);
                                            closeButton.setEnabled(true);
                                        case MSG:
                                            statusBarTextField.setText(statusEvent.getMsg());
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
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
        contentPane = new JPanel();
        contentPane.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:m:grow"));
        final JLabel label1 = new JLabel();
        label1.setText("Reading Progress:");
        CellConstraints cc = new CellConstraints();
        contentPane.add(label1, cc.xy(1, 1));
        readerProgressBar = new JProgressBar();
        readerProgressBar.setString("");
        readerProgressBar.setStringPainted(true);
        contentPane.add(readerProgressBar, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label2 = new JLabel();
        label2.setText("Parsing Progress:");
        contentPane.add(label2, cc.xy(1, 3));
        parsingProgressBar = new JProgressBar();
        parsingProgressBar.setString("");
        parsingProgressBar.setStringPainted(true);
        contentPane.add(parsingProgressBar, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label3 = new JLabel();
        label3.setText("Indexing Progress:");
        contentPane.add(label3, cc.xy(1, 5));
        indexingProgressBar = new JProgressBar();
        indexingProgressBar.setString("");
        indexingProgressBar.setStringPainted(true);
        contentPane.add(indexingProgressBar, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        statusBarTextField = new JLabel();
        statusBarTextField.setText("Indexing...");
        contentPane.add(statusBarTextField, cc.xyw(1, 7, 3));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "top:d:noGrow"));
        contentPane.add(panel1, cc.xyw(1, 9, 3, CellConstraints.DEFAULT, CellConstraints.BOTTOM));
        buttonCancel = new JButton();
        buttonCancel.setEnabled(false);
        buttonCancel.setText("Cancel");
        panel1.add(buttonCancel, cc.xy(3, 1, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        closeButton = new JButton();
        closeButton.setEnabled(false);
        closeButton.setText("Close");
        panel1.add(closeButton, cc.xy(5, 1, CellConstraints.DEFAULT, CellConstraints.BOTTOM));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
