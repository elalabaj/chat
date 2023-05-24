package client;

import server.MessageListener;

import javax.swing.*;

public class WindowClient extends JFrame implements MessageListener {
    private JPanel panel;
    private JScrollPane scrollPane;
    private JList list;
    private JTextField textField;
    private JButton buttonSend;
    private DefaultListModel<String> listModel;
    private final Client client;

    WindowClient(Client client) {
        this.client = client;

        setContentPane(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(300, 500);
        setVisible(true);
        setTitle("Client");

        listModel = new DefaultListModel<>();
        list.setModel(listModel);
        buttonSend.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String message = textField.getText();
        client.sendMessage(message);
        textField.setText("");
        listModel.addElement("Client: " + message);
    }

    @Override
    public void onMessageReceived(String message) {
        listModel.addElement("Server: " + message);
    }
}
