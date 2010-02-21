package edu.bgu.ir2009.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.io.File;

public class LoadConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JButton button1;
    private String confFileName;

    public LoadConfigDialog() {
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
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().endsWith(".irc");
                    }

                    @Override
                    public String getDescription() {
                        return "*.irc";
                    }
                });
                chooser.setMultiSelectionEnabled(false);
                int action = chooser.showOpenDialog(contentPane);
                if (action == JFileChooser.APPROVE_OPTION) {
                    confFileName = chooser.getSelectedFile().getAbsolutePath();
                    textField1.setText(confFileName);
                } else {
                    confFileName = null;
                }
            }
        });
        pack();
    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public String getConfigFileName() {
        return confFileName;
    }

    public static void main(String[] args) {
        LoadConfigDialog dialog = new LoadConfigDialog();
        dialog.setVisible(true);
        System.exit(0);
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
        contentPane.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;250px):grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow,top:4dlu:noGrow,fill:44px:noGrow"));
        final JLabel label1 = new JLabel();
        label1.setText("Configuration File:");
        CellConstraints cc = new CellConstraints();
        contentPane.add(label1, cc.xy(1, 1));
        textField1 = new JTextField();
        contentPane.add(textField1, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,center:d:noGrow", "center:d:grow"));
        contentPane.add(panel1, cc.xyw(1, 3, 5));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel1.add(buttonCancel, cc.xy(5, 1));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel1.add(buttonOK, cc.xy(3, 1));
        button1 = new JButton();
        button1.setText("...");
        contentPane.add(button1, cc.xy(5, 1));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
