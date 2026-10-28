package com.wuziqi.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 五子棋游戏客户端
 * 负责与服务器建立连接、发送和接收消息
 * 使用独立线程监听服务器消息
 */
public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    /** 消息接收线程 */
    private Thread receiveThread;
    /** 连接状态 */
    private volatile boolean connected;
    /** 客户端分配到的棋子颜色 */
    private String assignedColor;
    /** 消息监听器 */
    private ClientListener listener;

    /**
     * 客户端消息监听器接口
     */
    public interface ClientListener {
        void onConnected(String color);
        void onDisconnected();
        void onOpponentConnected();
        void onOpponentDisconnected();
        void onMoveReceived(int row, int col);
        void onChatReceived(String sender, String text);
        void onUndoRequest();
        void onUndoAccepted();
        void onUndoRejected();
        void onGameOver(String winner);
        void onError(String message);
    }

    /**
     * 构造游戏客户端
     * @param listener 消息监听器
     */
    public GameClient(ClientListener listener) {
        this.listener = listener;
    }

    /**
     * 连接到服务器
     * @param host 服务器地址
     * @param port 服务器端口
     * @throws IOException 连接失败异常
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        connected = true;

        // 发送连接请求
        sendMessage(new Message(MessageType.CONNECT, "请求连接", "Client"));

        // 启动接收线程
        receiveThread = new Thread(this::receiveLoop, "Client-Receive-Thread");
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    /**
     * 消息接收循环
     */
    private void receiveLoop() {
        try {
            while (connected) {
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    handleMessage((Message) obj);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                System.err.println("接收消息失败: " + e.getMessage());
                handleDisconnect();
            }
        }
    }

    /**
     * 处理接收到的消息
     * @param message 消息
     */
    private void handleMessage(Message message) {
        if (listener == null) return;

        switch (message.getType()) {
            case CONNECT_ACK:
                assignedColor = message.getContent();
                listener.onConnected(assignedColor);
                break;
            case OPPONENT_CONNECTED:
                listener.onOpponentConnected();
                break;
            case OPPONENT_DISCONNECTED:
                listener.onOpponentDisconnected();
                break;
            case MOVE:
                int[] pos = message.parseMove();
                if (pos != null) {
                    listener.onMoveReceived(pos[0], pos[1]);
                }
                break;
            case CHAT:
                listener.onChatReceived(message.getSender(), message.getContent());
                break;
            case UNDO_REQUEST:
                listener.onUndoRequest();
                break;
            case UNDO_ACCEPT:
                listener.onUndoAccepted();
                break;
            case UNDO_REJECT:
                listener.onUndoRejected();
                break;
            case GAME_OVER:
                listener.onGameOver(message.getContent());
                break;
            case ERROR:
                listener.onError(message.getContent());
                break;
            default:
                break;
        }
    }

    /**
     * 发送消息到服务器
     * @param message 消息
     */
    public void sendMessage(Message message) {
        if (!connected || out == null) return;
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
            handleDisconnect();
        }
    }

    /**
     * 发送落子消息
     * @param row 行
     * @param col 列
     */
    public void sendMove(int row, int col) {
        sendMessage(Message.createMoveMessage(row, col, "Player"));
    }

    /**
     * 发送聊天消息
     * @param text 聊天内容
     */
    public void sendChat(String text) {
        sendMessage(Message.createChatMessage(text, "Player"));
    }

    /**
     * 发送悔棋请求
     */
    public void sendUndoRequest() {
        sendMessage(new Message(MessageType.UNDO_REQUEST, "请求悔棋", "Player"));
    }

    /**
     * 发送同意悔棋
     */
    public void sendUndoAccept() {
        sendMessage(new Message(MessageType.UNDO_ACCEPT, "同意悔棋", "Player"));
    }

    /**
     * 发送拒绝悔棋
     */
    public void sendUndoReject() {
        sendMessage(new Message(MessageType.UNDO_REJECT, "拒绝悔棋", "Player"));
    }

    /**
     * 发送游戏结束消息
     * @param winner 获胜方
     */
    public void sendGameOver(String winner) {
        sendMessage(new Message(MessageType.GAME_OVER, winner, "Player"));
    }

    /**
     * 处理断开连接
     */
    private void handleDisconnect() {
        connected = false;
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // 忽略
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getAssignedColor() {
        return assignedColor;
    }
}
