package com.wuziqi.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 五子棋游戏服务器
 * 基于Socket和多线程，支持两个客户端联机对战
 * 负责接收客户端连接、转发消息、维护游戏状态
 */
public class GameServer {
    /** 默认端口 */
    public static final int DEFAULT_PORT = 8888;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    /** 玩家1连接处理器 */
    private ClientHandler player1;
    /** 玩家2连接处理器 */
    private ClientHandler player2;
    /** 服务器运行状态 */
    private volatile boolean running;
    /** 服务器状态监听器 */
    private ServerListener listener;

    /**
     * 服务器状态监听器接口
     */
    public interface ServerListener {
        void onServerStarted(int port);
        void onPlayerConnected(int playerNumber);
        void onPlayerDisconnected(int playerNumber);
        void onServerStopped();
        void onMessageReceived(Message message);
    }

    /**
     * 构造游戏服务器
     * @param listener 状态监听器
     */
    public GameServer(ServerListener listener) {
        this.listener = listener;
        this.threadPool = Executors.newFixedThreadPool(4);
    }

    /**
     * 启动服务器
     * @param port 监听端口
     * @throws IOException 启动失败异常
     */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        if (listener != null) {
            listener.onServerStarted(port);
        }

        // 在线程中接受连接
        threadPool.execute(() -> {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    handleNewConnection(socket);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("接受连接失败: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * 处理新的客户端连接
     * @param socket 客户端Socket
     */
    private synchronized void handleNewConnection(Socket socket) {
        try {
            ClientHandler handler = new ClientHandler(socket);

            if (player1 == null) {
                player1 = handler;
                handler.setPlayerNumber(1);
                // 发送连接确认，分配黑棋
                handler.sendMessage(new Message(MessageType.CONNECT_ACK, "BLACK", "Server"));
                if (listener != null) {
                    listener.onPlayerConnected(1);
                }
                threadPool.execute(handler);
            } else if (player2 == null) {
                player2 = handler;
                handler.setPlayerNumber(2);
                // 发送连接确认，分配白棋
                handler.sendMessage(new Message(MessageType.CONNECT_ACK, "WHITE", "Server"));
                // 通知双方对手已连接
                if (player1 != null) {
                    player1.sendMessage(new Message(MessageType.OPPONENT_CONNECTED, "对手已加入游戏", "Server"));
                }
                handler.sendMessage(new Message(MessageType.OPPONENT_CONNECTED, "对手已加入游戏", "Server"));
                if (listener != null) {
                    listener.onPlayerConnected(2);
                }
                threadPool.execute(handler);
            } else {
                // 服务器已满
                handler.sendMessage(new Message(MessageType.ERROR, "服务器已满，最多支持2人对战", "Server"));
                handler.close();
            }
        } catch (IOException e) {
            System.err.println("处理连接失败: " + e.getMessage());
        }
    }

    /**
     * 广播消息给所有已连接玩家
     * @param message 消息
     * @param excludePlayer 排除的玩家（发送者自己）
     */
    private synchronized void broadcast(Message message, ClientHandler excludePlayer) {
        if (player1 != null && player1 != excludePlayer) {
            player1.sendMessage(message);
        }
        if (player2 != null && player2 != excludePlayer) {
            player2.sendMessage(message);
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        running = false;
        if (player1 != null) player1.close();
        if (player2 != null) player2.close();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭服务器失败: " + e.getMessage());
        }
        threadPool.shutdownNow();
        if (listener != null) {
            listener.onServerStopped();
        }
    }

    /**
     * 获取当前连接的玩家数
     * @return 玩家数
     */
    public synchronized int getConnectedPlayers() {
        int count = 0;
        if (player1 != null) count++;
        if (player2 != null) count++;
        return count;
    }

    /**
     * 客户端连接处理器
     * 每个客户端一个线程，负责接收和转发消息
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private int playerNumber;
        private volatile boolean connected;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.connected = true;
        }

        public void setPlayerNumber(int number) {
            this.playerNumber = number;
        }

        public int getPlayerNumber() {
            return playerNumber;
        }

        /**
         * 发送消息给该客户端
         * @param message 消息
         */
        public void sendMessage(Message message) {
            try {
                out.writeObject(message);
                out.flush();
                out.reset();
            } catch (IOException e) {
                System.err.println("发送消息失败: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                while (connected && running) {
                    Object obj = in.readObject();
                    if (obj instanceof Message) {
                        Message message = (Message) obj;
                        if (listener != null) {
                            listener.onMessageReceived(message);
                        }
                        // 转发给对手
                        broadcast(message, this);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                // 客户端断开
            } finally {
                close();
                handleDisconnect();
            }
        }

        /**
         * 处理客户端断开
         */
        private synchronized void handleDisconnect() {
            if (player1 == this) {
                player1 = null;
                if (player2 != null) {
                    player2.sendMessage(new Message(MessageType.OPPONENT_DISCONNECTED, "对手已断开连接", "Server"));
                }
                if (listener != null) listener.onPlayerDisconnected(1);
            } else if (player2 == this) {
                player2 = null;
                if (player1 != null) {
                    player1.sendMessage(new Message(MessageType.OPPONENT_DISCONNECTED, "对手已断开连接", "Server"));
                }
                if (listener != null) listener.onPlayerDisconnected(2);
            }
        }

        /**
         * 关闭连接
         */
        public void close() {
            connected = false;
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                // 忽略关闭异常
            }
        }
    }
}
