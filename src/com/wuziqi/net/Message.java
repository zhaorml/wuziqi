package com.wuziqi.net;

import java.io.Serializable;

/**
 * 网络消息类
 * 客户端与服务器之间传输的消息封装
 * 实现Serializable以便对象流传输
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 消息类型 */
    private final MessageType type;
    /** 消息内容（JSON格式或纯文本） */
    private final String content;
    /** 发送方标识 */
    private final String sender;

    /**
     * 构造消息
     * @param type 消息类型
     * @param content 消息内容
     * @param sender 发送方
     */
    public Message(MessageType type, String content, String sender) {
        this.type = type;
        this.content = content;
        this.sender = sender;
    }

    /**
     * 构造消息（无发送方）
     * @param type 消息类型
     * @param content 消息内容
     */
    public Message(MessageType type, String content) {
        this(type, content, null);
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    /**
     * 解析落子消息内容为坐标
     * 格式: "row,col"
     * @return 坐标数组 [row, col]
     */
    public int[] parseMove() {
        if (content == null) return null;
        String[] parts = content.split(",");
        if (parts.length != 2) return null;
        try {
            return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim())
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 创建落子消息
     * @param row 行
     * @param col 列
     * @param sender 发送方
     * @return 消息对象
     */
    public static Message createMoveMessage(int row, int col, String sender) {
        return new Message(MessageType.MOVE, row + "," + col, sender);
    }

    /**
     * 创建聊天消息
     * @param text 聊天内容
     * @param sender 发送方
     * @return 消息对象
     */
    public static Message createChatMessage(String text, String sender) {
        return new Message(MessageType.CHAT, text, sender);
    }

    @Override
    public String toString() {
        return "Message{" + "type=" + type + ", content='" + content + '\'' + ", sender='" + sender + '\'' + '}';
    }
}
