package com.wuziqi.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 聊天面板
 * 提供聊天消息显示和发送功能
 */
public class ChatPanel extends JPanel {
    /** 聊天消息显示区域 */
    private JTextArea chatArea;
    /** 消息输入框 */
    private JTextField inputField;
    /** 发送按钮 */
    private JButton sendButton;
    /** 消息发送监听器 */
    private ChatSendListener sendListener;

    /**
     * 消息发送监听器接口
     */
    public interface ChatSendListener {
        void onSendMessage(String text);
    }

    /**
     * 构造聊天面板
     */
    public ChatPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("聊天"));

        // 消息显示区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        chatArea.setBackground(new Color(250, 250, 250));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // 输入区域
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sendButton = new JButton("发送");
        sendButton.setPreferredSize(new Dimension(70, 30));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // 事件监听
        ActionListener sendAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        };
        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);
    }

    /**
     * 设置消息发送监听器
     * @param listener 监听器
     */
    public void setChatSendListener(ChatSendListener listener) {
        this.sendListener = listener;
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        if (sendListener != null) {
            sendListener.onSendMessage(text);
        }
        inputField.setText("");
    }

    /**
     * 显示一条聊天消息
     * @param sender 发送者
     * @param text 消息内容
     */
    public void appendMessage(String sender, String text) {
        SwingUtilities.invokeLater(() -> {
            String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            chatArea.append("[" + time + "] " + sender + ": " + text + "\n");
            // 滚动到底部
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    /**
     * 显示系统消息
     * @param text 系统消息
     */
    public void appendSystemMessage(String text) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("【系统】" + text + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    /**
     * 清空聊天记录
     */
    public void clearChat() {
        chatArea.setText("");
    }

    /**
     * 设置输入是否可用
     * @param enabled 是否可用
     */
    public void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }
}
