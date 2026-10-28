package com.wuziqi.net;

/**
 * 网络消息类型枚举
 * 定义客户端与服务器之间通信的所有消息类型
 */
public enum MessageType {
    /** 客户端请求连接 */
    CONNECT,
    /** 服务器确认连接，分配棋子颜色 */
    CONNECT_ACK,
    /** 对手已连接 */
    OPPONENT_CONNECTED,
    /** 对手断开连接 */
    OPPONENT_DISCONNECTED,
    /** 落子消息 */
    MOVE,
    /** 悔棋请求 */
    UNDO_REQUEST,
    /** 同意悔棋 */
    UNDO_ACCEPT,
    /** 拒绝悔棋 */
    UNDO_REJECT,
    /** 聊天消息 */
    CHAT,
    /** 游戏结束 */
    GAME_OVER,
    /** 重新开始请求 */
    RESTART_REQUEST,
    /** 同意重新开始 */
    RESTART_ACCEPT,
    /** 心跳检测 */
    HEARTBEAT,
    /** 错误消息 */
    ERROR
}
