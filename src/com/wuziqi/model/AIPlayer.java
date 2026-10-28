package com.wuziqi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI玩家类
 * 基于评分表法的五子棋AI算法
 * 对每个空位进行攻防评分，选择最高分位置落子
 */
public class AIPlayer {
    /** AI执棋颜色 */
    private final Piece aiPiece;
    /** 随机数生成器，用于同分情况下随机选择 */
    private final Random random;

    // 棋型评分常量
    /** 连五 */
    private static final int SCORE_FIVE = 100000;
    /** 活四 */
    private static final int SCORE_LIVE_FOUR = 10000;
    /** 冲四 */
    private static final int SCORE_RUSH_FOUR = 1000;
    /** 活三 */
    private static final int SCORE_LIVE_THREE = 1000;
    /** 眠三 */
    private static final int SCORE_SLEEP_THREE = 100;
    /** 活二 */
    private static final int SCORE_LIVE_TWO = 100;
    /** 眠二 */
    private static final int SCORE_SLEEP_TWO = 10;
    /** 活一 */
    private static final int SCORE_LIVE_ONE = 10;

    /** 四个检测方向 */
    private static final int[][] DIRECTIONS = {
        {0, 1},   // 横向
        {1, 0},   // 纵向
        {1, 1},   // 正斜
        {1, -1}   // 反斜
    };

    /**
     * 构造AI玩家
     * @param aiPiece AI执棋颜色
     */
    public AIPlayer(Piece aiPiece) {
        this.aiPiece = aiPiece;
        this.random = new Random();
    }

    /**
     * 根据当前棋盘状态计算AI的最佳落子位置
     * @param board 当前棋盘
     * @return 最佳落子位置 [row, col]，无合法位置返回null
     */
    public int[] getBestMove(Board board) {
        // 如果棋盘为空，下在天元（中心位置）
        if (board.getMoveCount() == 0) {
            return new int[]{Board.BOARD_SIZE / 2, Board.BOARD_SIZE / 2};
        }

        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();

        // 遍历所有空位，计算评分
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                if (board.getPiece(row, col) != Piece.EMPTY) continue;

                // 只考虑已有棋子附近的位置（减少计算量，提高效率）
                if (!hasNeighbor(board, row, col, 2)) continue;

                // 进攻评分：AI自己在该位置落子的得分
                int attackScore = evaluatePosition(board, row, col, aiPiece);
                // 防守评分：对手在该位置落子的得分（需要阻止）
                int defenseScore = evaluatePosition(board, row, col, aiPiece.getOpponent());

                // 综合评分：进攻为主，防守为辅
                // 防守权重略低于进攻，但对手活四/连五必须防守
                int totalScore = attackScore * 2 + defenseScore;

                // 如果对手能形成连五或活四，必须防守
                if (defenseScore >= SCORE_FIVE) {
                    totalScore = SCORE_FIVE + 1;
                } else if (defenseScore >= SCORE_LIVE_FOUR) {
                    totalScore = Math.max(totalScore, SCORE_LIVE_FOUR + 500);
                }

                if (totalScore > bestScore) {
                    bestScore = totalScore;
                    bestMoves.clear();
                    bestMoves.add(new int[]{row, col});
                } else if (totalScore == bestScore) {
                    bestMoves.add(new int[]{row, col});
                }
            }
        }

        if (bestMoves.isEmpty()) {
            // 如果没有候选位置（不应该发生），随机选一个空位
            return findRandomEmpty(board);
        }

        // 同分情况下随机选择，增加游戏变化
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    /**
     * 评估在指定位置落子后形成的棋型得分
     * @param board 棋盘
     * @param row 行
     * @param col 列
     * @param piece 假设落子的棋子颜色
     * @return 评分
     */
    private int evaluatePosition(Board board, int row, int col, Piece piece) {
        int totalScore = 0;

        for (int[] dir : DIRECTIONS) {
            totalScore += evaluateDirection(board, row, col, dir[0], dir[1], piece);
        }

        return totalScore;
    }

    /**
     * 评估单个方向上的棋型
     * 统计连续棋子数和两端是否被堵
     * @param board 棋盘
     * @param row 落子行
     * @param col 落子列
     * @param dRow 方向行增量
     * @param dCol 方向列增量
     * @param piece 棋子颜色
     * @return 该方向评分
     */
    private int evaluateDirection(Board board, int row, int col, int dRow, int dCol, Piece piece) {
        int count = 1; // 包含假设落子的位置
        int block = 0; // 被封堵的端点数
        int empty = 0; // 连续棋子中的空位（跳棋情况）

        // 向正方向统计
        int r = row + dRow;
        int c = col + dCol;
        while (board.isValidPosition(r, c)) {
            if (board.getPiece(r, c) == piece) {
                count++;
            } else if (board.getPiece(r, c) == Piece.EMPTY) {
                empty++;
                if (empty >= 1) break; // 遇到一个空位后停止（简化处理）
            } else {
                block++;
                break;
            }
            r += dRow;
            c += dCol;
        }
        if (!board.isValidPosition(r, c)) block++;

        // 向反方向统计
        r = row - dRow;
        c = col - dCol;
        empty = 0;
        while (board.isValidPosition(r, c)) {
            if (board.getPiece(r, c) == piece) {
                count++;
            } else if (board.getPiece(r, c) == Piece.EMPTY) {
                empty++;
                if (empty >= 1) break;
            } else {
                block++;
                break;
            }
            r -= dRow;
            c -= dCol;
        }
        if (!board.isValidPosition(r, c)) block++;

        // 根据连子数和封堵情况评分
        return scoreByPattern(count, block);
    }

    /**
     * 根据连子数和封堵端数计算棋型分数
     * @param count 连续棋子数
     * @param block 被封堵的端数（0=两端开放，1=一端封堵，2=两端封堵）
     * @return 评分
     */
    private int scoreByPattern(int count, int block) {
        if (count >= 5) return SCORE_FIVE;

        if (block == 2) {
            // 两端都被堵，除非已经连五否则无威胁
            return 0;
        }

        switch (count) {
            case 4:
                return (block == 0) ? SCORE_LIVE_FOUR : SCORE_RUSH_FOUR;
            case 3:
                return (block == 0) ? SCORE_LIVE_THREE : SCORE_SLEEP_THREE;
            case 2:
                return (block == 0) ? SCORE_LIVE_TWO : SCORE_SLEEP_TWO;
            case 1:
                return (block == 0) ? SCORE_LIVE_ONE : 0;
            default:
                return 0;
        }
    }

    /**
     * 检查指定位置周围指定范围内是否有棋子
     * 用于减少AI计算量，只考虑有棋子的附近区域
     * @param board 棋盘
     * @param row 行
     * @param col 列
     * @param range 范围
     * @return 附近是否有棋子
     */
    private boolean hasNeighbor(Board board, int row, int col, int range) {
        for (int dr = -range; dr <= range; dr++) {
            for (int dc = -range; dc <= range; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = row + dr;
                int c = col + dc;
                if (board.isValidPosition(r, c) && board.getPiece(r, c) != Piece.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 随机找一个空位
     * @param board 棋盘
     * @return 空位坐标
     */
    private int[] findRandomEmpty(Board board) {
        List<int[]> empties = new ArrayList<>();
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (board.getPiece(i, j) == Piece.EMPTY) {
                    empties.add(new int[]{i, j});
                }
            }
        }
        if (empties.isEmpty()) return null;
        return empties.get(random.nextInt(empties.size()));
    }
}
