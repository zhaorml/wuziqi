package com.wuziqi.ui;

import com.wuziqi.model.Board;
import com.wuziqi.model.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 对局回放窗口
 * 支持逐步回放、自动播放、暂停、快进快退等功能
 */
public class ReplayFrame extends JFrame {
    /** 棋盘面板 */
    private BoardPanel boardPanel;
    /** 棋盘数据 */
    private Board board;
    /** 落子历史 */
    private List<Move> moves;
    /** 当前回放步数 */
    private int currentStep;
    /** 自动播放定时器 */
    private Timer playTimer;
    /** 状态标签 */
    private JLabel statusLabel;
    /** 进度滑块 */
    private JSlider progressSlider;

    /**
     * 构造回放窗口
     * @param moves 落子历史
     * @param winner 胜者
     */
    public ReplayFrame(List<Move> moves, String winner) {
        super("对局回放 - 胜者: " + winner);
        this.moves = moves;
        this.currentStep = 0;
        this.board = new Board();

        setSize(700, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initUI();
        updateBoard();
    }

    /**
     * 初始化界面
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // 棋盘面板
        boardPanel = new BoardPanel();
        boardPanel.setClickEnabled(false);
        boardPanel.setBoard(board);
        add(boardPanel, BorderLayout.CENTER);

        // 控制面板
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // 状态标签
        statusLabel = new JLabel("步数: 0 / " + moves.size(), SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        controlPanel.add(statusLabel, BorderLayout.NORTH);

        // 进度条
        progressSlider = new JSlider(0, moves.size(), 0);
        progressSlider.setMajorTickSpacing(Math.max(1, moves.size() / 10));
        progressSlider.setPaintTicks(true);
        progressSlider.setPaintLabels(true);
        progressSlider.addChangeListener(e -> {
            if (!progressSlider.getValueIsAdjusting()) {
                currentStep = progressSlider.getValue();
                updateBoard();
            }
        });
        controlPanel.add(progressSlider, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));

        JButton firstButton = new JButton("⏮ 开始");
        JButton prevButton = new JButton("◀ 上一步");
        JButton playButton = new JButton("▶ 播放");
        JButton nextButton = new JButton("下一步 ▶");
        JButton lastButton = new JButton("结束 ⏭");

        firstButton.addActionListener(e -> {
            currentStep = 0;
            updateBoard();
        });

        prevButton.addActionListener(e -> {
            if (currentStep > 0) {
                currentStep--;
                updateBoard();
            }
        });

        playButton.addActionListener(e -> {
            if (playTimer != null && playTimer.isRunning()) {
                playTimer.stop();
                playButton.setText("▶ 播放");
            } else {
                startAutoPlay();
                playButton.setText("⏸ 暂停");
            }
        });

        nextButton.addActionListener(e -> {
            if (currentStep < moves.size()) {
                currentStep++;
                updateBoard();
            }
        });

        lastButton.addActionListener(e -> {
            currentStep = moves.size();
            updateBoard();
        });

        buttonPanel.add(firstButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(playButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(lastButton);

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * 开始自动播放
     */
    private void startAutoPlay() {
        if (currentStep >= moves.size()) {
            currentStep = 0;
            updateBoard();
        }

        playTimer = new Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStep < moves.size()) {
                    currentStep++;
                    updateBoard();
                } else {
                    playTimer.stop();
                    // 重置播放按钮文本需要在事件 dispatch 线程
                    SwingUtilities.invokeLater(() -> {
                        for (Component c : ((JPanel) ((JPanel) getContentPane().getComponent(1)).getComponent(2)).getComponents()) {
                            if (c instanceof JButton && ((JButton) c).getText().contains("暂停")) {
                                ((JButton) c).setText("▶ 播放");
                            }
                        }
                    });
                }
            }
        });
        playTimer.start();
    }

    /**
     * 更新棋盘显示
     */
    private void updateBoard() {
        board.loadFromHistory(moves, currentStep);
        boardPanel.setBoard(board);
        boardPanel.repaint();

        statusLabel.setText("步数: " + currentStep + " / " + moves.size());
        progressSlider.setValue(currentStep);

        // 显示当前步信息
        if (currentStep > 0 && currentStep <= moves.size()) {
            Move move = moves.get(currentStep - 1);
            statusLabel.setText("步数: " + currentStep + " / " + moves.size()
                + " | " + move.getPiece() + " → (" + move.getRow() + "," + move.getCol() + ")");
        }
    }

    @Override
    public void dispose() {
        if (playTimer != null) {
            playTimer.stop();
        }
        super.dispose();
    }
}
