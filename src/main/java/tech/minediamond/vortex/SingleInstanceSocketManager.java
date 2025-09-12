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

package tech.minediamond.vortex;

import com.google.inject.Inject;
import javafx.application.Platform;
import lombok.Setter;
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

@Slf4j
public class SingleInstanceSocketManager {

    private static final int SINGLE_INSTANCE_PORT = 38727;// 端口号
    private static final String FOCUS_COMMAND = "VORTEX::FOCUS_WINDOW";

    private static ServerSocket singleInstanceSocket;
    private static WindowAnimator windowAnimator;
    private static boolean isStageReady = false;

    public static boolean startSocket(){
        try {
            singleInstanceSocket = new ServerSocket(SINGLE_INSTANCE_PORT, 10, InetAddress.getLoopbackAddress());
            log.info("已连接端口");
            startInstanceListener();
            return true;
        } catch (IOException e) {
            log.warn("端口已被占用");
            sendFocusCommandToFirstInstance();
            Platform.exit();
            return false;
        }
    }

    private static void startInstanceListener() {
        Thread listenerThread = new Thread(() -> {
            while (!singleInstanceSocket.isClosed()) {
                try (Socket clientSocket = singleInstanceSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String command = in.readLine();
                    // 收到聚焦命令
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
                    log.error(e.getMessage(), e);
                }
            }
        });

        // 设置为守护线程，这样它不会阻止 JVM 退出
        listenerThread.setDaemon(true);
        listenerThread.setName("Vortex Instance Listener");
        listenerThread.start();
    }

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

    public static void setStageReady(WindowAnimator animator) {
        isStageReady = true;
        windowAnimator = animator;
    }


}
