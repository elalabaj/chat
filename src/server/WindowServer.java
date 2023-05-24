package server;

import common.MessageListener;

import javax.swing.*;

public class WindowServer extends JFrame implements MessageListener {
    private JPanel panel;
    private JList list;
    private JScrollPane scrollPane;
    private JTextField textField;
    private JButton buttonSend;
    private DefaultListModel<String> listModel;
    private final Server server;

    WindowServer(Server server) {
        this.server = server;

        setContentPane(panel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 500);
        setVisible(true);
        setTitle("Server");

        listModel = new DefaultListModel<>();
        list.setModel(listModel);
        buttonSend.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String message = textField.getText();
        server.sendMessage(message);
        textField.setText("");
        listModel.addElement(message);
    }

    @Override
    public void onMessageReceived(String message) {
        listModel.addElement(message);
    }
}
