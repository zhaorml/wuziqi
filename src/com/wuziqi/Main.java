package com.wuziqi;

import com.wuziqi.ui.MainFrame;

import javax.swing.*;

/**
 * 网络五子棋程序入口
 * 面向对象程序设计课程设计
 *
 * 技术要点：多线程 + Socket编程 + 五子棋算法 + 人机对战算法
 * 功能：15x15棋盘、联机对战、聊天、悔棋、对局回放、人机对战
 *
 * @author 课程设计小组
 * @version 1.0
 */
public class Main {
    /**
     * 程序主入口
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 获取项目根目录（用于记录文件存储）
        String projectPath = System.getProperty("user.dir");

        // 在事件 dispatch 线程中启动GUI（Swing最佳实践）
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 使用默认外观
            }

            MainFrame frame = new MainFrame(projectPath);
            frame.setVisible(true);

            // 启动后显示模式选择对话框
            frame.showStartDialog();
        });
    }
}
