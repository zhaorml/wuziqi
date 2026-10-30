package com.wuziqi.ui;

import com.wuziqi.model.AIPlayer;
import com.wuziqi.model.Board;
import com.wuziqi.model.Piece;
import com.wuziqi.net.GameClient;
import com.wuziqi.net.GameServer;
import com.wuziqi.util.GameRecorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * 五子棋主窗口
 * 整合棋盘、聊天、菜单、状态栏，支持人机对战和联机对战两种模式
 */
public class MainFrame extends JFrame {
    // UI组件
    private BoardPanel boardPanel;
    private ChatPanel chatPanel;
    private JLabel statusLabel;
    private JLabel turnLabel;

    // 游戏核心
    private Board board;
    private AIPlayer aiPlayer;
    private GameRecorder recorder;

    // 网络相关
    private GameServer gameServer;
    private GameClient gameClient;

    // 游戏状态
    private enum GameMode { NONE, HUMAN_VS_AI, ONLINE_SERVER, ONLINE_CLIENT }
    private GameMode gameMode = GameMode.NONE;
    private Piece playerColor; // 玩家自己的棋子颜色
    private boolean isPlayerTurn; // 是否轮到玩家
    private boolean undoRequested = false;

    // 项目路径
    private final String projectPath;

    /**
     * 构造主窗口
     * @param projectPath 项目根路径
     */
    public MainFrame(String projectPath) {
        super("网络五子棋");
        this.projectPath = projectPath;
        this.board = new Board();
        this.recorder = new GameRecorder(projectPath + File.separator + "records");

        setSize(900, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initMenuBar();
        initUI();
        updateStatus();
    }

    /**
     * 初始化菜单栏
     */
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 游戏菜单
        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGameItem = new JMenuItem("新游戏");
        JMenuItem restartItem = new JMenuItem("重新开始");
        JMenuItem saveItem = new JMenuItem("保存对局");
        JMenuItem replayItem = new JMenuItem("对局回放");
        JMenuItem exitItem = new JMenuItem("退出");

        newGameItem.addActionListener(e -> showStartDialog());
        restartItem.addActionListener(e -> restartGame());
        saveItem.addActionListener(e -> saveGame());
        replayItem.addActionListener(e -> showReplay());
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.add(restartItem);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(replayItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // 操作菜单
        JMenu actionMenu = new JMenu("操作");
        JMenuItem undoItem = new JMenuItem("悔棋");
        undoItem.addActionListener(e -> requestUndo());
        actionMenu.add(undoItem);

        // 设置菜单
        JMenu settingMenu = new JMenu("设置");
        JMenu bgMenu = new JMenu("更换棋盘背景");
        String[] bgNames = {"原木色", "深木色", "竹青色", "米白色"};
        Color[] bgColors = {
            new Color(220, 180, 120),
            new Color(180, 130, 80),
            new Color(200, 210, 170),
            new Color(240, 235, 220)
        };
        for (int i = 0; i < bgNames.length; i++) {
            final Color color = bgColors[i];
            JMenuItem item = new JMenuItem(bgNames[i]);
            item.addActionListener(e -> boardPanel.setBoardColor(color));
            bgMenu.add(item);
        }
        settingMenu.add(bgMenu);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(actionMenu);
        menuBar.add(settingMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    /**
     * 初始化主界面
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // 顶部状态栏
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 5, 15));

        turnLabel = new JLabel("请选择游戏模式开始", SwingConstants.CENTER);
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(turnLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // 中间区域：棋盘 + 聊天
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // 棋盘面板（带边框）
        JPanel boardContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        boardContainer.setBorder(BorderFactory.createTitledBorder("棋盘"));
        boardPanel = new BoardPanel();
        boardPanel.setBoard(board);
        boardPanel.setMoveListener(this::handleBoardClick);
        boardContainer.add(boardPanel);
        splitPane.setLeftComponent(boardContainer);

        // 右侧面板：聊天 + 操作按钮
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        chatPanel = new ChatPanel();
        chatPanel.setChatSendListener(this::handleChatSend);
        chatPanel.setInputEnabled(false);
        rightPanel.add(chatPanel, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JButton undoButton = new JButton("悔棋");
        JButton restartButton = new JButton("重新开始");
        JButton saveButton = new JButton("保存对局");
        JButton disconnectButton = new JButton("断开连接");

        undoButton.addActionListener(e -> requestUndo());
        restartButton.addActionListener(e -> restartGame());
        saveButton.addActionListener(e -> saveGame());
        disconnectButton.addActionListener(e -> disconnect());

        buttonPanel.add(undoButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(disconnectButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);

        // 底部状态栏
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(3, 15, 5, 15));
        statusLabel = new JLabel("就绪 | 模式: 未开始");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(Color.DARK_GRAY);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 显示启动对话框
     */
    public void showStartDialog() {
        StartDialog dialog = new StartDialog(this);
        StartDialog.StartResult result = dialog.showDialog();

        if (result == null) return;

        // 清理之前的状态
        cleanup();

        switch (result.mode) {
            case HUMAN_VS_AI:
                startHumanVsAI(result.playerColor, result.aiLevel);
                break;
            case CREATE_SERVER:
                startCreateServer(result.serverPort);
                break;
            case JOIN_SERVER:
                startJoinServer(result.serverHost, result.serverPort);
                break;
            case REPLAY:
                showReplay();
                break;
        }
    }

    /**
     * 开始人机对战
     */
    private void startHumanVsAI(String color, String level) {
        gameMode = GameMode.HUMAN_VS_AI;
        playerColor = color.equals("BLACK") ? Piece.BLACK : Piece.WHITE;
        aiPlayer = new AIPlayer(playerColor.getOpponent());
        board.reset();
        boardPanel.setBoard(board);
        boardPanel.setClickEnabled(true);
        chatPanel.setInputEnabled(false);
        isPlayerTurn = (playerColor == Piece.BLACK);

        chatPanel.appendSystemMessage("人机对战开始！你执" + (playerColor == Piece.BLACK ? "黑" : "白") + "棋，AI难度: " + level);

        // 如果玩家是白棋，AI先下
        if (!isPlayerTurn) {
            aiMove();
        }
        updateStatus();
    }

    /**
     * 创建服务器（联机对战）
     */
    private void startCreateServer(int port) {
        gameMode = GameMode.ONLINE_SERVER;
        playerColor = Piece.BLACK; // 服务器方执黑
        board.reset();
        boardPanel.setBoard(board);
        boardPanel.setClickEnabled(false);
        chatPanel.setInputEnabled(false);
        isPlayerTurn = false;

        gameServer = new GameServer(new GameServer.ServerListener() {
            @Override
            public void onServerStarted(int port) {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendSystemMessage("服务器已启动，端口: " + port + "，等待对手连接...");
                    statusLabel.setText("服务器运行中 | 等待对手连接");
                });
            }

            @Override
            public void onPlayerConnected(int playerNumber) {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendSystemMessage("玩家" + playerNumber + "已连接");
                    if (playerNumber == 2) {
                        isPlayerTurn = true;
                        boardPanel.setClickEnabled(true);
                        chatPanel.setInputEnabled(true);
                        chatPanel.appendSystemMessage("对手已加入！你执黑棋先行");
                    }
                });
            }

            @Override
            public void onPlayerDisconnected(int playerNumber) {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendSystemMessage("玩家" + playerNumber + "已断开连接");
                    boardPanel.setClickEnabled(false);
                });
            }

            @Override
            public void onServerStopped() {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("服务器已停止");
                });
            }

            @Override
            public void onMessageReceived(com.wuziqi.net.Message message) {
                // 服务器端消息在客户端回调中处理
            }
        });

        try {
            gameServer.start(port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "启动服务器失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            gameMode = GameMode.NONE;
        }
        updateStatus();
    }

    /**
     * 连接服务器（联机对战）
     */
    private void startJoinServer(String host, int port) {
        gameMode = GameMode.ONLINE_CLIENT;
        board.reset();
        boardPanel.setBoard(board);
        boardPanel.setClickEnabled(false);
        chatPanel.setInputEnabled(false);
        isPlayerTurn = false;

        gameClient = new GameClient(new GameClient.ClientListener() {
            @Override
            public void onConnected(String color) {
                SwingUtilities.invokeLater(() -> {
                    playerColor = color.equals("BLACK") ? Piece.BLACK : Piece.WHITE;
                    chatPanel.appendSystemMessage("已连接到服务器！你执" + (playerColor == Piece.BLACK ? "黑" : "白") + "棋");
                    statusLabel.setText("已连接 | " + host + ":" + port);
                });
            }

            @Override
            public void onDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendSystemMessage("与服务器断开连接");
                    boardPanel.setClickEnabled(false);
                    chatPanel.setInputEnabled(false);
                });
            }

            @Override
            public void onOpponentConnected() {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendSystemMessage("对手已加入游戏！");
                    if (playerColor == Piece.BLACK) {
                        isPlayerTurn = true;
                        boardPanel.setClickEnabled(true);
                    }
                    chatPanel.setInputEnabled(true);
                });
            }

            @Override
            public void onOpponentDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendSystemMessage("对手已断开连接");
                    boardPanel.setClickEnabled(false);
                });
            }

            @Override
            public void onMoveReceived(int row, int col) {
                SwingUtilities.invokeLater(() -> {
                    opponentMove(row, col);
                });
            }

            @Override
            public void onChatReceived(String sender, String text) {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendMessage("对手", text);
                });
            }

            @Override
            public void onUndoRequest() {
                SwingUtilities.invokeLater(() -> {
                    handleUndoRequest();
                });
            }

            @Override
            public void onUndoAccepted() {
                SwingUtilities.invokeLater(() -> {
                    performUndo();
                    chatPanel.appendSystemMessage("对手同意悔棋");
                });
            }

            @Override
            public void onUndoRejected() {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendSystemMessage("对手拒绝了悔棋请求");
                });
            }

            @Override
            public void onGameOver(String winner) {
                SwingUtilities.invokeLater(() -> {
                    chatPanel.appendSystemMessage("游戏结束，胜者: " + winner);
                });
            }

            @Override
            public void onError(String message) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(MainFrame.this, message, "错误", JOptionPane.ERROR_MESSAGE);
                });
            }
        });

        try {
            gameClient.connect(host, port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接服务器失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            gameMode = GameMode.NONE;
        }
        updateStatus();
    }

    /**
     * 处理棋盘点击落子
     */
    private void handleBoardClick(int row, int col) {
        if (!isPlayerTurn || board.isGameOver()) return;
        if (board.getPiece(row, col) != Piece.EMPTY) return;

        // 玩家落子
        boolean success = board.makeMove(row, col);
        if (!success) return;

        boardPanel.repaint();
        updateStatus();

        // 检查游戏是否结束
        if (board.isGameOver()) {
            handleGameOver();
            return;
        }

        // 根据模式处理
        if (gameMode == GameMode.HUMAN_VS_AI) {
            isPlayerTurn = false;
            // AI在独立线程中思考，避免界面卡顿
            new Thread(this::aiMove, "AI-Thread").start();
        } else if (gameMode == GameMode.ONLINE_SERVER || gameMode == GameMode.ONLINE_CLIENT) {
            // 发送落子给对手
            if (gameClient != null) {
                gameClient.sendMove(row, col);
            }
            isPlayerTurn = false;
        }
    }

    /**
     * AI落子
     */
    private void aiMove() {
        try {
            // 模拟思考时间
            Thread.sleep(300);
        } catch (InterruptedException e) {
            return;
        }

        int[] move = aiPlayer.getBestMove(board);
        if (move != null) {
            SwingUtilities.invokeLater(() -> {
                board.makeMove(move[0], move[1]);
                boardPanel.repaint();
                updateStatus();
                isPlayerTurn = true;

                if (board.isGameOver()) {
                    handleGameOver();
                }
            });
        }
    }

    /**
     * 对手落子（联机模式）
     */
    private void opponentMove(int row, int col) {
        boolean success = board.makeMove(row, col);
        if (success) {
            boardPanel.repaint();
            updateStatus();
            isPlayerTurn = true;
            boardPanel.setClickEnabled(true);

            if (board.isGameOver()) {
                handleGameOver();
            }
        }
    }

    /**
     * 处理游戏结束
     */
    private void handleGameOver() {
        boardPanel.setClickEnabled(false);
        Piece winner = board.getWinner();
        String winnerText;

        if (winner == Piece.EMPTY) {
            winnerText = "平局！";
        } else if (winner == playerColor) {
            winnerText = "恭喜你获胜！";
        } else {
            winnerText = (gameMode == GameMode.HUMAN_VS_AI ? "AI" : "对手") + "获胜！";
        }

        turnLabel.setText(winnerText);
        chatPanel.appendSystemMessage("游戏结束 - " + winnerText);

        // 自动保存对局
        try {
            String winnerStr = (winner == Piece.EMPTY) ? "DRAW" : winner.name();
            String path = recorder.saveRecord(board.getMoveHistory(), winnerStr);
            chatPanel.appendSystemMessage("对局已自动保存至: " + path);
        } catch (IOException e) {
            chatPanel.appendSystemMessage("保存对局失败: " + e.getMessage());
        }

        JOptionPane.showMessageDialog(this, winnerText, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 处理聊天发送
     */
    private void handleChatSend(String text) {
        if (gameClient != null && gameClient.isConnected()) {
            gameClient.sendChat(text);
            chatPanel.appendMessage("我", text);
        } else if (gameMode == GameMode.HUMAN_VS_AI) {
            chatPanel.appendMessage("我", text);
            // AI简单回复
            String[] replies = {"嗯", "好的", "哈哈", "这步不错", "让我想想", "继续吧"};
            chatPanel.appendMessage("AI", replies[(int) (Math.random() * replies.length)]);
        }
    }

    /**
     * 请求悔棋
     */
    private void requestUndo() {
        if (board.getMoveCount() == 0) {
            JOptionPane.showMessageDialog(this, "还没有落子，无法悔棋", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (gameMode == GameMode.HUMAN_VS_AI) {
            // 人机模式直接悔棋（撤销玩家和AI各一步）
            if (board.getMoveCount() >= 2) {
                board.undoMove(); // 撤销AI
                board.undoMove(); // 撤销玩家
            } else {
                board.undoMove();
            }
            boardPanel.repaint();
            updateStatus();
            isPlayerTurn = true;
            chatPanel.appendSystemMessage("已悔棋");
        } else if (gameMode == GameMode.ONLINE_SERVER || gameMode == GameMode.ONLINE_CLIENT) {
            if (undoRequested) {
                JOptionPane.showMessageDialog(this, "已发送悔棋请求，等待对手回应", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (gameClient != null) {
                gameClient.sendUndoRequest();
                undoRequested = true;
                chatPanel.appendSystemMessage("已发送悔棋请求，等待对手同意...");
            }
        }
    }

    /**
     * 处理对手的悔棋请求
     */
    private void handleUndoRequest() {
        int choice = JOptionPane.showConfirmDialog(this,
            "对手请求悔棋，是否同意？", "悔棋请求",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            if (gameClient != null) {
                gameClient.sendUndoAccept();
            }
            performUndo();
            chatPanel.appendSystemMessage("已同意对手悔棋");
        } else {
            if (gameClient != null) {
                gameClient.sendUndoReject();
            }
        }
    }

    /**
     * 执行悔棋操作
     */
    private void performUndo() {
        if (board.getMoveCount() >= 2) {
            board.undoMove(); // 撤销对手
            board.undoMove(); // 撤销自己
        } else if (board.getMoveCount() == 1) {
            board.undoMove();
        }
        boardPanel.repaint();
        updateStatus();
        isPlayerTurn = true;
        boardPanel.setClickEnabled(true);
        undoRequested = false;
    }

    /**
     * 重新开始游戏
     */
    private void restartGame() {
        if (gameMode == GameMode.NONE) {
            showStartDialog();
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "确定要重新开始吗？当前对局将不会保存。", "确认",
            JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        board.reset();
        boardPanel.repaint();
        undoRequested = false;

        if (gameMode == GameMode.HUMAN_VS_AI) {
            isPlayerTurn = (playerColor == Piece.BLACK);
            boardPanel.setClickEnabled(isPlayerTurn);
            if (!isPlayerTurn) {
                new Thread(this::aiMove, "AI-Thread").start();
            }
        } else {
            isPlayerTurn = (playerColor == Piece.BLACK);
            boardPanel.setClickEnabled(isPlayerTurn);
        }

        chatPanel.appendSystemMessage("游戏已重新开始");
        updateStatus();
    }

    /**
     * 保存对局
     */
    private void saveGame() {
        if (board.getMoveCount() == 0) {
            JOptionPane.showMessageDialog(this, "没有对局记录可保存", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String winner = board.isGameOver()
                ? (board.getWinner() == Piece.EMPTY ? "DRAW" : board.getWinner().name())
                : "未结束";
            String path = recorder.saveRecord(board.getMoveHistory(), winner);
            JOptionPane.showMessageDialog(this, "对局已保存至:\n" + path, "保存成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 显示对局回放
     */
    private void showReplay() {
        File[] files = recorder.listRecordFiles();
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "没有找到对局记录文件", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 显示文件选择列表
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                String winner = recorder.getWinnerFromFile(files[i].getAbsolutePath());
                fileNames[i] = files[i].getName() + " (胜者: " + winner + ")";
            } catch (IOException e) {
                fileNames[i] = files[i].getName();
            }
        }

        String selected = (String) JOptionPane.showInputDialog(this,
            "选择要回放的对局:", "对局回放",
            JOptionPane.PLAIN_MESSAGE, null, fileNames, fileNames[0]);

        if (selected == null) return;

        int index = -1;
        for (int i = 0; i < fileNames.length; i++) {
            if (fileNames[i].equals(selected)) {
                index = i;
                break;
            }
        }

        if (index < 0) return;

        try {
            java.util.List<com.wuziqi.model.Move> moves = recorder.loadRecord(files[index].getAbsolutePath());
            String winner = recorder.getWinnerFromFile(files[index].getAbsolutePath());
            ReplayFrame replayFrame = new ReplayFrame(moves, winner);
            replayFrame.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "加载对局失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 断开连接
     */
    private void disconnect() {
        if (gameClient != null) {
            gameClient.disconnect();
            gameClient = null;
        }
        if (gameServer != null) {
            gameServer.stop();
            gameServer = null;
        }
        gameMode = GameMode.NONE;
        boardPanel.setClickEnabled(false);
        chatPanel.setInputEnabled(false);
        chatPanel.appendSystemMessage("已断开连接");
        updateStatus();
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        if (gameClient != null) {
            gameClient.disconnect();
            gameClient = null;
        }
        if (gameServer != null) {
            gameServer.stop();
            gameServer = null;
        }
        aiPlayer = null;
        undoRequested = false;
    }

    /**
     * 更新状态显示
     */
    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            String modeText;
            switch (gameMode) {
                case HUMAN_VS_AI: modeText = "人机对战"; break;
                case ONLINE_SERVER: modeText = "联机对战(服务器)"; break;
                case ONLINE_CLIENT: modeText = "联机对战(客户端)"; break;
                default: modeText = "未开始";
            }

            String turnText;
            if (board.isGameOver()) {
                Piece winner = board.getWinner();
                turnText = (winner == Piece.EMPTY) ? "平局"
                    : (winner == playerColor ? "你获胜了！" : (gameMode == GameMode.HUMAN_VS_AI ? "AI获胜" : "对手获胜"));
            } else if (gameMode == GameMode.NONE) {
                turnText = "请选择游戏模式开始";
            } else {
                Piece current = board.getCurrentPlayer();
                String colorText = (current == Piece.BLACK) ? "黑棋" : "白棋";
                String who = (current == playerColor) ? "你" : (gameMode == GameMode.HUMAN_VS_AI ? "AI" : "对手");
                turnText = colorText + "回合 - " + who + "落子";
            }

            turnLabel.setText(turnText);
            statusLabel.setText("步数: " + board.getMoveCount() + " | 模式: " + modeText
                + " | 你执" + (playerColor == null ? "?" : (playerColor == Piece.BLACK ? "黑" : "白")) + "棋");
        });
    }

    /**
     * 显示关于对话框
     */
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "网络五子棋 v1.0\n\n"
            + "技术要点:\n"
            + "- 多线程 (AI思考、网络通信)\n"
            + "- Socket编程 (TCP联机对战)\n"
            + "- 五子棋算法 (四方向胜负判断)\n"
            + "- 人机对战算法 (评分表法AI)\n\n"
            + "功能:\n"
            + "- 15x15标准棋盘\n"
            + "- 人机对战 / 联机对战\n"
            + "- 实时聊天\n"
            + "- 悔棋功能\n"
            + "- 对局记录与回放\n"
            + "- 多种棋盘背景\n\n"
            + "面向对象程序设计课程设计",
            "关于", JOptionPane.INFORMATION_MESSAGE);
    }
}
