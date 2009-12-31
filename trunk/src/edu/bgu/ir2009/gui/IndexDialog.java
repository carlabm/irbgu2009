package edu.bgu.ir2009.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import edu.bgu.ir2009.DownFacade;
import org.apache.log4j.BasicConfigurator;

import javax.swing.*;
import java.awt.event.*;

public class IndexDialog extends JDialog {
    private JPanel contentPane;
    private JTextField sourceDocumentsDirectoryTextField;
    private JButton button1;
    private JTextField stopWordsFileTextField;
    private JButton button2;
    private JCheckBox useStemmerCheckBox;
    private JButton moreButton;
    private JButton buttonOK;
    private JButton buttonCancel;

    public IndexDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

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
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                int action = chooser.showOpenDialog(contentPane);
                if (action == JFileChooser.APPROVE_OPTION) {
                    sourceDocumentsDirectoryTextField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                int action = chooser.showOpenDialog(contentPane);
                if (action == JFileChooser.APPROVE_OPTION) {
                    stopWordsFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
    }

    private void onOK() {
        IndexingDialog dialog = new IndexingDialog();
        try {
            DownFacade.getInstance().startIndexing(sourceDocumentsDirectoryTextField.getText(), stopWordsFileTextField.getText(), useStemmerCheckBox.isSelected());
        } catch (Exception e) {
            //TODO error dialog
        }
        dispose();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        IndexDialog dialog = new IndexDialog();
        dialog.pack();
        dialog.setVisible(true);
//        System.exit(0);
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
        contentPane.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:max(d;300px):grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:22px:noGrow,top:4dlu:noGrow,center:48px:grow"));
        final JLabel label1 = new JLabel();
        label1.setText("Source Documents Directory:");
        CellConstraints cc = new CellConstraints();
        contentPane.add(label1, cc.xy(1, 1));
        sourceDocumentsDirectoryTextField = new JTextField();
        contentPane.add(sourceDocumentsDirectoryTextField, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        button1 = new JButton();
        button1.setText("...");
        contentPane.add(button1, cc.xy(5, 1));
        final JLabel label2 = new JLabel();
        label2.setText("Stop Words File:");
        contentPane.add(label2, cc.xy(1, 3));
        stopWordsFileTextField = new JTextField();
        contentPane.add(stopWordsFileTextField, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        button2 = new JButton();
        button2.setText("...");
        contentPane.add(button2, cc.xy(5, 3));
        final JLabel label3 = new JLabel();
        label3.setText("Use Stemmer?");
        contentPane.add(label3, cc.xy(1, 5));
        useStemmerCheckBox = new JCheckBox();
        useStemmerCheckBox.setText("");
        contentPane.add(useStemmerCheckBox, cc.xy(3, 5));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:noGrow", "center:d:noGrow"));
        contentPane.add(panel1, cc.xyw(1, 7, 5, CellConstraints.DEFAULT, CellConstraints.BOTTOM));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel1.add(buttonCancel, cc.xy(5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        buttonOK = new JButton();
        buttonOK.setText("Index");
        panel1.add(buttonOK, cc.xy(3, 1));
        moreButton = new JButton();
        moreButton.setText("More...");
        panel1.add(moreButton, cc.xy(1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        label1.setLabelFor(sourceDocumentsDirectoryTextField);
        label2.setLabelFor(stopWordsFileTextField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}