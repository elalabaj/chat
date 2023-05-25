package client;

import common.MessageListener;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowClient extends JFrame implements MessageListener {
    private JPanel panel;
    private JScrollPane scrollPane;
    private JList list;
    private JTextField textField;
    private JButton buttonSend;
    private JComboBox comboBoxGroups;
    private final List<DefaultListModel<String>> groups;
    private final List<Integer> groupIds;
    private final Map<Integer, Integer> groupOrder;
    private final Client client;

    WindowClient(Client client) {
        this.client = client;

        setContentPane(panel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 500);
        setVisible(true);
        setTitle("Client");

        groups = new ArrayList<>();
        groups.add(new DefaultListModel<>());
        groupIds = new ArrayList<>();
        groupIds.add(0);
        groupOrder = new HashMap<>();
        groupOrder.put(0, 0);

        list.setModel(groups.get(0));
        buttonSend.addActionListener(e -> sendMessage());
        comboBoxGroups.addItemListener(e -> {
            if (e.getID() == ItemEvent.ITEM_STATE_CHANGED) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedIndex = comboBoxGroups.getSelectedIndex();
                    list.setModel(groups.get(selectedIndex));
                }
            }
        });
    }

    public void addGroup(int groupId, String name) {
        groups.add(new DefaultListModel<>());
        groupIds.add(groupId);
        groupOrder.put(groupId, groups.size() - 1);
        comboBoxGroups.addItem(name);
    }

    private void sendMessage() {
        String message = textField.getText();
        int group = comboBoxGroups.getSelectedIndex();
        client.sendMessage(groupIds.get(group), message);
        textField.setText("");
        groups.get(group).addElement(message);
    }

    @Override
    public void onMessageReceived(int group, String message) {
        if (!groupOrder.containsKey(group)) {
            addGroup(group, message);
        } else {
            groups.get(groupOrder.get(group)).addElement(message);
        }
    }
}
