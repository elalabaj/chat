package server;

import common.MessageListener;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WindowServer extends JFrame implements MessageListener {
    private JPanel panel;
    private JList list;
    private JScrollPane scrollPane;
    private JTextField textField;
    private JButton buttonSend;
    private JComboBox comboBoxGroups;
    private JButton buttonNewGroup;
    private List<DefaultListModel<String>> groups;
    private final Server server;

    WindowServer(Server server) {
        this.server = server;

        setContentPane(panel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 500);
        setVisible(true);
        setTitle("Server");

        groups = new ArrayList<>();
        groups.add(new DefaultListModel<>());
        list.setModel(groups.get(0));
        buttonSend.addActionListener(e -> sendMessage());
        buttonNewGroup.addActionListener(e -> newGroup());
        comboBoxGroups.addItemListener(e -> {
            if (e.getID() == ItemEvent.ITEM_STATE_CHANGED) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedIndex = comboBoxGroups.getSelectedIndex();
                    list.setModel(groups.get(selectedIndex));
                }
            }
        });
    }

    private void newGroup() {
        Map<Long, ClientThread> clients = server.getThreads();
        String[] names = clients.values().stream().map(ClientThread::getUsername).toArray(String[]::new);
        Long[] ids = clients.keySet().toArray(Long[]::new);

        DialogNewGroup dialog = new DialogNewGroup(names, (name, results) -> {
            Long[] selectedIds = new Long[results.length];
            for (int i = 0; i < results.length; i++) selectedIds[i] = ids[results[i]];
            server.addGroup(name, selectedIds);

            groups.add(new DefaultListModel<>());
            comboBoxGroups.addItem(name);
        });

        dialog.setSize(300, 300);
        dialog.setVisible(true);
        dialog.setTitle("New group");
    }

    private void sendMessage() {
        String message = textField.getText();
        int group = comboBoxGroups.getSelectedIndex();
        server.sendMessage(group, message);
        textField.setText("");
        groups.get(group).addElement(message);
    }

    @Override
    public void onMessageReceived(int group, String message) {
        groups.get(group).addElement(message);
    }
}
