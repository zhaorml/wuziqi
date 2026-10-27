package com.wuziqi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 棋盘类
 * 维护15x15的棋盘状态，提供落子、悔棋、胜负判断等核心功能
 */
public class Board {
    /** 棋盘大小 */
    public static final int BOARD_SIZE = 15;
    /** 获胜所需连子数 */
    public static final int WIN_COUNT = 5;

    /** 棋盘数据，board[row][col] */
    private Piece[][] board;
    /** 落子历史记录，用于悔棋和回放 */
    private List<Move> moveHistory;
    /** 当前轮到哪方落子 */
    private Piece currentPlayer;
    /** 游戏是否结束 */
    private boolean gameOver;
    /** 获胜方 */
    private Piece winner;
    /** 获胜的五子连珠位置，用于高亮显示 */
    private List<int[]> winningLine;

    /**
     * 构造棋盘，初始化空棋盘
     */
    public Board() {
        reset();
    }

    /**
     * 重置棋盘到初始状态
     */
    public void reset() {
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = Piece.EMPTY;
            }
        }
        moveHistory = new ArrayList<>();
        currentPlayer = Piece.BLACK; // 黑棋先行
        gameOver = false;
        winner = null;
        winningLine = null;
    }

    /**
     * 在指定位置落子
     * @param row 行坐标
     * @param col 列坐标
     * @return 是否落子成功
     */
    public boolean makeMove(int row, int col) {
        // 检查坐标合法性
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false;
        }
        // 检查位置是否为空
        if (board[row][col] != Piece.EMPTY) {
            return false;
        }
        // 检查游戏是否已结束
        if (gameOver) {
            return false;
        }

        // 落子
        board[row][col] = currentPlayer;
        Move move = new Move(row, col, currentPlayer, moveHistory.size() + 1);
        moveHistory.add(move);

        // 判断胜负
        if (checkWin(row, col, currentPlayer)) {
            gameOver = true;
            winner = currentPlayer;
        } else if (moveHistory.size() == BOARD_SIZE * BOARD_SIZE) {
            // 棋盘下满，平局
            gameOver = true;
            winner = Piece.EMPTY;
        } else {
            // 切换玩家
            currentPlayer = currentPlayer.getOpponent();
        }

        return true;
    }

    /**
     * 悔棋：撤销最后一步落子
     * @return 是否成功悔棋
     */
    public boolean undoMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        board[lastMove.getRow()][lastMove.getCol()] = Piece.EMPTY;

        // 恢复游戏状态
        gameOver = false;
        winner = null;
        winningLine = null;
        currentPlayer = lastMove.getPiece();

        return true;
    }

    /**
     * 检查指定位置落子后是否形成五子连珠
     * 检测横向、纵向、正斜(\)、反斜(/)四个方向
     * @param row 落子行
     * @param col 落子列
     * @param piece 棋子颜色
     * @return 是否获胜
     */
    public boolean checkWin(int row, int col, Piece piece) {
        // 四个方向：横向、纵向、正斜、反斜
        int[][] directions = {
            {0, 1},   // 横向
            {1, 0},   // 纵向
            {1, 1},   // 正斜 \
            {1, -1}   // 反斜 /
        };

        for (int[] dir : directions) {
            List<int[]> line = new ArrayList<>();
            line.add(new int[]{row, col});

            // 向正方向计数
            int count = 1;
            int r = row + dir[0];
            int c = col + dir[1];
            while (isValidPosition(r, c) && board[r][c] == piece) {
                line.add(new int[]{r, c});
                count++;
                r += dir[0];
                c += dir[1];
            }

            // 向反方向计数
            r = row - dir[0];
            c = col - dir[1];
            while (isValidPosition(r, c) && board[r][c] == piece) {
                line.add(0, new int[]{r, c});
                count++;
                r -= dir[0];
                c -= dir[1];
            }

            if (count >= WIN_COUNT) {
                winningLine = line;
                return true;
            }
        }
        return false;
    }

    /**
     * 检查坐标是否在棋盘范围内
     * @param row 行
     * @param col 列
     * @return 是否合法
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * 获取指定位置的棋子
     * @param row 行
     * @param col 列
     * @return 棋子
     */
    public Piece getPiece(int row, int col) {
        if (!isValidPosition(row, col)) return Piece.EMPTY;
        return board[row][col];
    }

    /**
     * 获取最后一步落子
     * @return 最后一步，无则返回null
     */
    public Move getLastMove() {
        if (moveHistory.isEmpty()) return null;
        return moveHistory.get(moveHistory.size() - 1);
    }

    /**
     * 获取落子历史的副本
     * @return 落子历史列表
     */
    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    /**
     * 获取当前步数
     * @return 步数
     */
    public int getMoveCount() {
        return moveHistory.size();
    }

    public Piece getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Piece player) {
        this.currentPlayer = player;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Piece getWinner() {
        return winner;
    }

    public List<int[]> getWinningLine() {
        return winningLine;
    }

    /**
     * 获取棋盘数据的副本
     * @return 棋盘二维数组
     */
    public Piece[][] getBoardData() {
        Piece[][] copy = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, BOARD_SIZE);
        }
        return copy;
    }

    /**
     * 根据落子历史重建棋盘状态（用于回放）
     * @param moves 落子历史
     * @param stepCount 回放至第几步
     */
    public void loadFromHistory(List<Move> moves, int stepCount) {
        reset();
        for (int i = 0; i < stepCount && i < moves.size(); i++) {
            Move m = moves.get(i);
            board[m.getRow()][m.getCol()] = m.getPiece();
            moveHistory.add(m);
        }
        if (!moves.isEmpty() && stepCount > 0) {
            currentPlayer = moves.get(Math.min(stepCount, moves.size()) - 1).getPiece().getOpponent();
        }
    }
}
