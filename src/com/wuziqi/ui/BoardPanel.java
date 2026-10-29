package com.wuziqi.ui;

import com.wuziqi.model.Board;
import com.wuziqi.model.Move;
import com.wuziqi.model.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 棋盘绘制面板
 * 负责绘制15x15五子棋棋盘、棋子、最后落子标记和获胜连线
 * 处理鼠标点击落子事件
 */
public class BoardPanel extends JPanel {
    /** 棋盘大小 */
    private static final int BOARD_SIZE = Board.BOARD_SIZE;
    /** 棋盘边距 */
    private static final int MARGIN = 40;
    /** 格子大小 */
    private static final int CELL_SIZE = 36;
    /** 棋子半径 */
    private static final int PIECE_RADIUS = 15;
    /** 棋盘总尺寸 */
    private static final int BOARD_PIXEL_SIZE = MARGIN * 2 + CELL_SIZE * (BOARD_SIZE - 1);

    /** 棋盘数据引用 */
    private Board board;
    /** 落子监听器 */
    private MoveListener moveListener;
    /** 是否允许点击落子 */
    private boolean clickEnabled;
    /** 鼠标悬停位置 */
    private Point hoverPoint;
    /** 棋盘背景色 */
    private Color boardColor = new Color(220, 180, 120);
    /** 网格线颜色 */
    private Color lineColor = new Color(80, 50, 20);

    /**
     * 落子监听器接口
     */
    public interface MoveListener {
        void onMoveClicked(int row, int col);
    }

    /**
     * 构造棋盘面板
     */
    public BoardPanel() {
        this.clickEnabled = true;
        this.hoverPoint = null;
        setPreferredSize(new Dimension(BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE));
        setBackground(boardColor);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!clickEnabled || board == null) return;
                int[] pos = pixelToBoard(e.getX(), e.getY());
                if (pos != null && moveListener != null) {
                    moveListener.onMoveClicked(pos[0], pos[1]);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int[] pos = pixelToBoard(e.getX(), e.getY());
                if (pos != null) {
                    hoverPoint = new Point(pos[0], pos[1]);
                } else {
                    hoverPoint = null;
                }
                repaint();
            }
        });
    }

    /**
     * 设置棋盘数据
     * @param board 棋盘对象
     */
    public void setBoard(Board board) {
        this.board = board;
        repaint();
    }

    /**
     * 设置落子监听器
     * @param listener 监听器
     */
    public void setMoveListener(MoveListener listener) {
        this.moveListener = listener;
    }

    /**
     * 设置是否允许点击落子
     * @param enabled 是否允许
     */
    public void setClickEnabled(boolean enabled) {
        this.clickEnabled = enabled;
    }

    /**
     * 设置棋盘背景色
     * @param color 颜色
     */
    public void setBoardColor(Color color) {
        this.boardColor = color;
        setBackground(color);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制棋盘网格
        drawGrid(g2d);
        // 绘制星位
        drawStarPoints(g2d);
        // 绘制坐标标注
        drawCoordinates(g2d);

        if (board != null) {
            // 绘制棋子
            drawPieces(g2d);
            // 绘制最后落子标记
            drawLastMoveMarker(g2d);
            // 绘制获胜连线
            drawWinningLine(g2d);
        }

        // 绘制鼠标悬停预览
        if (hoverPoint != null && clickEnabled && board != null
                && board.getPiece(hoverPoint.x, hoverPoint.y) == Piece.EMPTY
                && !board.isGameOver()) {
            drawHoverPreview(g2d, hoverPoint.x, hoverPoint.y);
        }
    }

    /**
     * 绘制棋盘网格线
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(lineColor);
        g2d.setStroke(new BasicStroke(1.5f));

        for (int i = 0; i < BOARD_SIZE; i++) {
            // 横线
            int y = MARGIN + i * CELL_SIZE;
            g2d.drawLine(MARGIN, y, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, y);
            // 竖线
            int x = MARGIN + i * CELL_SIZE;
            g2d.drawLine(x, MARGIN, x, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE);
        }

        // 绘制外边框（加粗）
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRect(MARGIN, MARGIN, (BOARD_SIZE - 1) * CELL_SIZE, (BOARD_SIZE - 1) * CELL_SIZE);
    }

    /**
     * 绘制星位（天元和四个角星）
     */
    private void drawStarPoints(Graphics2D g2d) {
        g2d.setColor(lineColor);
        int[][] starPoints = {
            {3, 3}, {3, 11}, {7, 7}, {11, 3}, {11, 11}
        };
        for (int[] sp : starPoints) {
            int x = MARGIN + sp[1] * CELL_SIZE;
            int y = MARGIN + sp[0] * CELL_SIZE;
            g2d.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    /**
     * 绘制坐标标注
     */
    private void drawCoordinates(Graphics2D g2d) {
        g2d.setColor(lineColor);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();

        for (int i = 0; i < BOARD_SIZE; i++) {
            // 列标（A-O）
            String colLabel = String.valueOf((char) ('A' + i));
            int x = MARGIN + i * CELL_SIZE;
            g2d.drawString(colLabel, x - fm.stringWidth(colLabel) / 2, MARGIN - 12);
            g2d.drawString(colLabel, x - fm.stringWidth(colLabel) / 2, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE + 22);

            // 行标（1-15）
            String rowLabel = String.valueOf(BOARD_SIZE - i);
            int y = MARGIN + i * CELL_SIZE + 4;
            g2d.drawString(rowLabel, MARGIN - 25, y);
            g2d.drawString(rowLabel, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE + 12, y);
        }
    }

    /**
     * 绘制所有棋子
     */
    private void drawPieces(Graphics2D g2d) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != Piece.EMPTY) {
                    drawPiece(g2d, row, col, piece);
                }
            }
        }
    }

    /**
     * 绘制单个棋子
     */
    private void drawPiece(Graphics2D g2d, int row, int col, Piece piece) {
        int x = MARGIN + col * CELL_SIZE;
        int y = MARGIN + row * CELL_SIZE;

        // 棋子阴影
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval(x - PIECE_RADIUS + 2, y - PIECE_RADIUS + 2, PIECE_RADIUS * 2, PIECE_RADIUS * 2);

        if (piece == Piece.BLACK) {
            // 黑棋渐变
            GradientPaint blackGradient = new GradientPaint(
                x - PIECE_RADIUS, y - PIECE_RADIUS, new Color(80, 80, 80),
                x + PIECE_RADIUS, y + PIECE_RADIUS, Color.BLACK
            );
            g2d.setPaint(blackGradient);
        } else {
            // 白棋渐变
            GradientPaint whiteGradient = new GradientPaint(
                x - PIECE_RADIUS, y - PIECE_RADIUS, Color.WHITE,
                x + PIECE_RADIUS, y + PIECE_RADIUS, new Color(200, 200, 200)
            );
            g2d.setPaint(whiteGradient);
        }
        g2d.fillOval(x - PIECE_RADIUS, y - PIECE_RADIUS, PIECE_RADIUS * 2, PIECE_RADIUS * 2);

        // 棋子边框
        g2d.setPaint(null);
        g2d.setColor(piece == Piece.BLACK ? Color.BLACK : new Color(150, 150, 150));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawOval(x - PIECE_RADIUS, y - PIECE_RADIUS, PIECE_RADIUS * 2, PIECE_RADIUS * 2);
    }

    /**
     * 绘制最后落子标记（红色小方框）
     */
    private void drawLastMoveMarker(Graphics2D g2d) {
        Move lastMove = board.getLastMove();
        if (lastMove == null) return;

        int x = MARGIN + lastMove.getCol() * CELL_SIZE;
        int y = MARGIN + lastMove.getRow() * CELL_SIZE;

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRect(x - 6, y - 6, 12, 12);
    }

    /**
     * 绘制获胜连线
     */
    private void drawWinningLine(Graphics2D g2d) {
        List<int[]> winningLine = board.getWinningLine();
        if (winningLine == null || winningLine.size() < 2) return;

        int[] first = winningLine.get(0);
        int[] last = winningLine.get(winningLine.size() - 1);

        int x1 = MARGIN + first[1] * CELL_SIZE;
        int y1 = MARGIN + first[0] * CELL_SIZE;
        int x2 = MARGIN + last[1] * CELL_SIZE;
        int y2 = MARGIN + last[0] * CELL_SIZE;

        g2d.setColor(new Color(255, 0, 0, 180));
        g2d.setStroke(new BasicStroke(4f));
        g2d.drawLine(x1, y1, x2, y2);
    }

    /**
     * 绘制鼠标悬停预览（半透明棋子）
     */
    private void drawHoverPreview(Graphics2D g2d, int row, int col) {
        int x = MARGIN + col * CELL_SIZE;
        int y = MARGIN + row * CELL_SIZE;

        Piece current = board.getCurrentPlayer();
        Color previewColor = (current == Piece.BLACK)
            ? new Color(0, 0, 0, 80)
            : new Color(255, 255, 255, 120);

        g2d.setColor(previewColor);
        g2d.fillOval(x - PIECE_RADIUS, y - PIECE_RADIUS, PIECE_RADIUS * 2, PIECE_RADIUS * 2);
    }

    /**
     * 像素坐标转换为棋盘坐标
     * @param px 像素x
     * @param py 像素y
     * @return 棋盘坐标 [row, col]，不在范围内返回null
     */
    private int[] pixelToBoard(int px, int py) {
        int col = Math.round((float) (px - MARGIN) / CELL_SIZE);
        int row = Math.round((float) (py - MARGIN) / CELL_SIZE);

        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return null;
        }

        // 检查点击是否在交叉点附近（容差范围内）
        int expectedX = MARGIN + col * CELL_SIZE;
        int expectedY = MARGIN + row * CELL_SIZE;
        int tolerance = CELL_SIZE / 2;

        if (Math.abs(px - expectedX) > tolerance || Math.abs(py - expectedY) > tolerance) {
            return null;
        }

        return new int[]{row, col};
    }

    /**
     * 清屏动画效果（逐步清除棋子）
     * @param callback 动画完成回调
     */
    public void clearAnimation(Runnable callback) {
        // 简单实现：直接清除后重绘
        if (board != null) {
            board.reset();
        }
        repaint();
        if (callback != null) {
            callback.run();
        }
    }
}
