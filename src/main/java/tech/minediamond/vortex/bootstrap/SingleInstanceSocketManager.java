/*
 * Vortex
 * Copyright (C) 2025 Mine-diamond
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package tech.minediamond.vortex.bootstrap;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.service.WindowAnimator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 单例应用程序管理器
 * <p>
 * 通过在本地回环地址上监听特定端口，确保同一时间只有一个应用程序实例在运行。
 * <p>
 * 工作流程：
 * 1. 应用程序启动时，调用 {@link #startSocket()} 尝试绑定端口。
 * 2. 如果绑定成功，则表明这是第一个实例，它将开始监听后续启动尝试。
 * 3. 如果绑定失败（端口被占用），则表明已有实例在运行。当前实例会向第一个实例发送一个“聚焦”命令，然后自行退出。
 * 4. 第一个实例接收到“聚焦”命令后，会将自己的主窗口带到前台。
 */
@Slf4j
public class SingleInstanceSocketManager {

    private static final int SINGLE_INSTANCE_PORT = 38727;// 端口号
    private static final String FOCUS_COMMAND = "VORTEX::FOCUS_WINDOW";//在实例间通信的命令，用于请求已存在的实例将窗口置于前台。

    private static ServerSocket singleInstanceSocket;
    private static WindowAnimator windowAnimator;
    private static boolean isStageReady = false;

    /**
     * 启动单例服务套接字。
     * <p>
     * 尝试绑定指定端口。如果成功，则作为主实例运行并开始监听。
     * 如果失败，则向已存在的主实例发送聚焦命令，然后退出当前应用。
     *
     * @return 如果作为主实例启动成功，返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean startSocket() {
        try {
            singleInstanceSocket = new ServerSocket(SINGLE_INSTANCE_PORT, 10, InetAddress.getLoopbackAddress());
            log.info("单例服务启动成功，开始监听端口: {}", SINGLE_INSTANCE_PORT);
            startInstanceListener();
            return true;
        } catch (IOException e) {
            log.warn("端口 {} 已被占用，判定为重复启动。将尝试唤醒已存在的实例。", SINGLE_INSTANCE_PORT);
            sendFocusCommandToFirstInstance();
            Platform.exit();
            return false;
        }
    }

    /**
     * 启动一个后台线程，用于监听来自其他实例的连接请求。
     */
    private static void startInstanceListener() {
        Thread listenerThread = new Thread(() -> {
            while (!singleInstanceSocket.isClosed()) {
                try (Socket clientSocket = singleInstanceSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String command = in.readLine();
                    log.info("接收到来自新实例的命令: {}", command);
                    // 如果接收到的是聚焦命令
                    if (FOCUS_COMMAND.equals(command)) {
                        Platform.runLater(() -> {
                            log.info("已接收命令，将窗口带入前台。");
                            if (isStageReady) {
                                windowAnimator.showMainWindow();
                            }
                        });
                    }
                } catch (IOException e) {
                    // ServerSocket 关闭时会抛出异常，这是正常的退出方式
                    if (singleInstanceSocket.isClosed()) {
                        log.info("实例监听器线程关闭");
                        break;
                    }
                    log.error("实例监听器出现异常， {}",e.getMessage(), e);
                }
            }
        });

        // 设置为守护线程，这样它不会阻止 JVM 退出
        listenerThread.setDaemon(true);
        listenerThread.setName("Vortex Instance Listener");
        listenerThread.start();
    }

    /**
     * 当检测到已有实例运行时，作为客户端连接到该实例并发送聚焦命令。
     */
    private static void sendFocusCommandToFirstInstance() {
        try (Socket clientSocket = new Socket(InetAddress.getLoopbackAddress(), SINGLE_INSTANCE_PORT);
             OutputStream out = clientSocket.getOutputStream()) {

            out.write((FOCUS_COMMAND + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            log.info("聚焦指令发送成功。即将退出。");

        } catch (IOException e) {
            log.error("发送聚焦指令失败");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 注册窗口动画控制器，并标记UI已准备就绪。
     * <p>
     * 当主窗口（Stage）完全初始化后，应调用此方法。
     *
     * @param animator 用于控制窗口显示/隐藏的动画器实例。
     */
    public static void setStageReady(WindowAnimator animator) {
        isStageReady = true;
        windowAnimator = animator;
    }

    /**
     * 在应用程序关闭时，关闭单例服务套接字，释放端口。
     *
     * @throws IOException 如果关闭套接字时发生 I/O 错误。
     */
    public static void closeSocket() throws IOException {
        if (singleInstanceSocket != null && !singleInstanceSocket.isClosed()) {
            singleInstanceSocket.close();
            log.info("连接已关闭");
        }
    }
}
