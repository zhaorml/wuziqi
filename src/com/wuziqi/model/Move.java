package com.wuziqi.model;

import java.io.Serializable;

/**
 * 落子记录类
 * 记录一次落子的位置、棋子颜色和序号
 * 实现Serializable以便网络传输和文件保存
 */
public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 行坐标 (0-14) */
    private final int row;
    /** 列坐标 (0-14) */
    private final int col;
    /** 棋子颜色 */
    private final Piece piece;
    /** 落子序号（从1开始） */
    private final int step;

    /**
     * 构造落子记录
     * @param row 行坐标
     * @param col 列坐标
     * @param piece 棋子颜色
     * @param step 落子序号
     */
    public Move(int row, int col, Piece piece, int step) {
        this.row = row;
        this.col = col;
        this.piece = piece;
        this.step = step;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Piece getPiece() {
        return piece;
    }

    public int getStep() {
        return step;
    }

    @Override
    public String toString() {
        return String.format("第%d步: %s(%d,%d)", step, piece == Piece.BLACK ? "黑" : "白", row, col);
    }

    /**
     * 序列化为字符串，用于文件存储
     * 格式: step,row,col,pieceValue
     * @return 序列化字符串
     */
    public String toFileString() {
        return step + "," + row + "," + col + "," + piece.getValue();
    }

    /**
     * 从字符串反序列化落子记录
     * @param line 字符串行
     * @return 落子记录
     */
    public static Move fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) return null;
        int step = Integer.parseInt(parts[0].trim());
        int row = Integer.parseInt(parts[1].trim());
        int col = Integer.parseInt(parts[2].trim());
        Piece piece = Piece.fromValue(Integer.parseInt(parts[3].trim()));
        return new Move(row, col, piece, step);
    }
}
