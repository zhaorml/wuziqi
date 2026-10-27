package com.wuziqi.model;

/**
 * 棋子类型枚举
 * 表示棋盘上每个交叉点的状态
 */
public enum Piece {
    /** 空位 */
    EMPTY(0),
    /** 黑棋 */
    BLACK(1),
    /** 白棋 */
    WHITE(2);

    private final int value;

    Piece(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * 获取对手的棋子颜色
     * @return 对手棋子
     */
    public Piece getOpponent() {
        if (this == BLACK) return WHITE;
        if (this == WHITE) return BLACK;
        return EMPTY;
    }

    /**
     * 根据整数值获取棋子类型
     * @param value 整数值
     * @return 棋子类型
     */
    public static Piece fromValue(int value) {
        for (Piece p : values()) {
            if (p.value == value) return p;
        }
        return EMPTY;
    }
}
