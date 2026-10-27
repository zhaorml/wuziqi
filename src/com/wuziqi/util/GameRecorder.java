package com.wuziqi.util;

import com.wuziqi.model.Move;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 对局记录工具类
 * 负责将对局过程保存到文件，以及从文件加载对局记录用于回放
 */
public class GameRecorder {
    /** 记录文件存放目录 */
    private final String recordDir;
    /** 日期格式化 */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    /**
     * 构造对局记录器
     * @param recordDir 记录文件目录
     */
    public GameRecorder(String recordDir) {
        this.recordDir = recordDir;
        File dir = new File(recordDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 保存对局记录到文件
     * @param moves 落子历史
     * @param winner 获胜方（"BLACK"/"WHITE"/"DRAW"）
     * @return 保存的文件路径
     * @throws IOException 保存失败
     */
    public String saveRecord(List<Move> moves, String winner) throws IOException {
        String fileName = "game_" + DATE_FORMAT.format(new Date()) + ".wzg";
        File file = new File(recordDir, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
            // 写入文件头
            writer.write("# 五子棋对局记录");
            writer.newLine();
            writer.write("# 日期: " + new Date());
            writer.newLine();
            writer.write("# 胜者: " + winner);
            writer.newLine();
            writer.write("# 总步数: " + moves.size());
            writer.newLine();
            writer.write("---");
            writer.newLine();

            // 写入每一步
            for (Move move : moves) {
                writer.write(move.toFileString());
                writer.newLine();
            }
        }

        return file.getAbsolutePath();
    }

    /**
     * 从文件加载对局记录
     * @param filePath 文件路径
     * @return 落子历史列表
     * @throws IOException 加载失败
     */
    public List<Move> loadRecord(String filePath) throws IOException {
        List<Move> moves = new ArrayList<>();
        File file = new File(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 跳过注释和分隔符
                if (line.isEmpty() || line.startsWith("#") || line.equals("---")) {
                    continue;
                }
                Move move = Move.fromFileString(line);
                if (move != null) {
                    moves.add(move);
                }
            }
        }

        return moves;
    }

    /**
     * 获取记录目录下所有对局记录文件
     * @return 记录文件列表
     */
    public File[] listRecordFiles() {
        File dir = new File(recordDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".wzg"));
        if (files != null) {
            java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        }
        return files;
    }

    /**
     * 从记录文件中读取胜者信息
     * @param filePath 文件路径
     * @return 胜者字符串
     * @throws IOException 读取失败
     */
    public String getWinnerFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(file, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("# 胜者:")) {
                    return line.substring(line.indexOf(":") + 1).trim();
                }
            }
        }
        return "未知";
    }
}
