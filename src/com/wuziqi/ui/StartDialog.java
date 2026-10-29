package com.wuziqi.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 启动对话框
 * 提供游戏模式选择：人机对战、创建服务器（联机）、连接服务器（联机）、对局回放
 */
public class StartDialog extends JDialog {
    /** 选择结果 */
    private StartResult result;
    /** 是否确认选择 */
    private boolean confirmed = false;

    // 输入组件
    private JTextField serverHostField;
    private JTextField serverPortField;
    private JComboBox<String> aiColorCombo;
    private JComboBox<String> aiLevelCombo;

    /**
     * 启动结果类
     */
    public static class StartResult {
        public enum Mode { HUMAN_VS_AI, CREATE_SERVER, JOIN_SERVER, REPLAY }

        public Mode mode;
        public String serverHost;
        public int serverPort;
        public String playerColor; // 人机模式下玩家颜色
        public String aiLevel;     // AI难度

        public StartResult(Mode mode) {
            this.mode = mode;
        }
    }

    /**
     * 构造启动对话框
     * @param parent 父窗口
     */
    public StartDialog(JFrame parent) {
        super(parent, "网络五子棋 - 选择模式", true);
        setSize(420, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
    }

    /**
     * 初始化界面
     */
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 标题
        JLabel titleLabel = new JLabel("网络五子棋");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("请选择游戏模式");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // 模式按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 0, 8));

        JButton aiButton = createModeButton("人机对战", "与电脑AI进行五子棋对战");
        aiButton.addActionListener(e -> showAIDialog());

        JButton serverButton = createModeButton("创建房间（服务器）", "创建服务器，等待对手加入");
        serverButton.addActionListener(e -> showServerDialog());

        JButton clientButton = createModeButton("加入房间（客户端）", "连接到已创建的服务器房间");
        clientButton.addActionListener(e -> showClientDialog());

        JButton replayButton = createModeButton("对局回放", "查看历史对局记录");
        replayButton.addActionListener(e -> {
            result = new StartResult(StartResult.Mode.REPLAY);
            confirmed = true;
            dispose();
        });

        buttonPanel.add(aiButton);
        buttonPanel.add(serverButton);
        buttonPanel.add(clientButton);
        buttonPanel.add(replayButton);

        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // 底部信息
        JLabel infoLabel = new JLabel("推荐人数：2-3人 | 技术：多线程+Socket+AI算法");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(infoLabel);

        add(mainPanel);
    }

    /**
     * 创建模式按钮
     */
    private JButton createModeButton(String text, String tooltip) {
        JButton button = new JButton("<html><b>" + text + "</b><br><small>" + tooltip + "</small></html>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(360, 55));
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return button;
    }

    /**
     * 显示人机对战设置对话框
     */
    private void showAIDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("玩家执棋:"));
        aiColorCombo = new JComboBox<>(new String[]{"黑棋（先手）", "白棋（后手）"});
        panel.add(aiColorCombo);

        panel.add(new JLabel("AI难度:"));
        aiLevelCombo = new JComboBox<>(new String[]{"简单", "普通", "困难"});
        aiLevelCombo.setSelectedIndex(1);
        panel.add(aiLevelCombo);

        panel.add(new JLabel("提示:"));
        JLabel hintLabel = new JLabel("黑棋先行，点击棋盘交叉点落子");
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(hintLabel);

        int choice = JOptionPane.showConfirmDialog(this, panel, "人机对战设置",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice == JOptionPane.OK_OPTION) {
            result = new StartResult(StartResult.Mode.HUMAN_VS_AI);
            result.playerColor = (aiColorCombo.getSelectedIndex() == 0) ? "BLACK" : "WHITE";
            result.aiLevel = (String) aiLevelCombo.getSelectedItem();
            confirmed = true;
            dispose();
        }
    }

    /**
     * 显示创建服务器对话框
     */
    private void showServerDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("监听端口:"));
        serverPortField = new JTextField("8888");
        panel.add(serverPortField);

        panel.add(new JLabel("提示:"));
        JLabel hintLabel = new JLabel("等待对手连接，你将执黑棋先行");
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(hintLabel);

        int choice = JOptionPane.showConfirmDialog(this, panel, "创建服务器",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice == JOptionPane.OK_OPTION) {
            try {
                int port = Integer.parseInt(serverPortField.getText().trim());
                result = new StartResult(StartResult.Mode.CREATE_SERVER);
                result.serverPort = port;
                confirmed = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的端口号", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 显示连接服务器对话框
     */
    private void showClientDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("服务器地址:"));
        serverHostField = new JTextField("127.0.0.1");
        panel.add(serverHostField);

        panel.add(new JLabel("服务器端口:"));
        serverPortField = new JTextField("8888");
        panel.add(serverPortField);

        panel.add(new JLabel("提示:"));
        JLabel hintLabel = new JLabel("连接成功后你将执白棋");
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(hintLabel);

        int choice = JOptionPane.showConfirmDialog(this, panel, "连接服务器",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice == JOptionPane.OK_OPTION) {
            try {
                String host = serverHostField.getText().trim();
                int port = Integer.parseInt(serverPortField.getText().trim());
                result = new StartResult(StartResult.Mode.JOIN_SERVER);
                result.serverHost = host;
                result.serverPort = port;
                confirmed = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的端口号", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 显示对话框并等待用户选择
     * @return 选择结果，取消返回null
     */
    public StartResult showDialog() {
        setVisible(true);
        return confirmed ? result : null;
    }
}
