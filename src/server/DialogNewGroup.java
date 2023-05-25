package server;

import javax.swing.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

public class DialogNewGroup extends JDialog {
    private final BiConsumer<String, int[]> resultsConsumer;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList listUsers;
    private JTextField textFieldName;

    public DialogNewGroup(String[] users, BiConsumer<String, int[]> resultsConsumer) {
        this.resultsConsumer = resultsConsumer;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        listUsers.setListData(users);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (Objects.equals(textFieldName.getText(), "")) {
            JOptionPane.showMessageDialog(this, "Enter the name");
        } else if (listUsers.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "Select users");
        } else {
            resultsConsumer.accept(textFieldName.getText(), listUsers.getSelectedIndices());
            dispose();
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
