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

package tech.minediamond.vortex.service;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.fxmisc.richtext.CodeArea;
import java.util.function.IntFunction;

public class DynamicLineNumberFactory implements IntFunction<Node> {

    private final CodeArea codeArea;

    // 构造函数，需要传入 CodeArea 实例
    @Inject
    public DynamicLineNumberFactory(@Assisted CodeArea codeArea) {
        this.codeArea = codeArea;
    }


    @Override
    public Node apply(int lineIndex) {
        // 1. 计算总行数，并确定需要的位数
        int totalLines = codeArea.getParagraphs().size();
        // 至少保证两位数的宽度，避免在 1-9 行时太窄
        int maxDigits = Math.max(2, String.valueOf(totalLines).length());

        // 2. 创建动态格式化字符串
        String formatSpec = "%" + maxDigits + "d";
        String lineNumberText = String.format(formatSpec, lineIndex + 1);

        // 3. 创建 Label
        Label lineNumberLabel = new Label(String.valueOf(lineIndex + 1));
        lineNumberLabel.getStyleClass().add("line-number-label"); // 应用 CSS
        lineNumberLabel.setAlignment(Pos.TOP_RIGHT);
        lineNumberLabel.setPadding(new Insets(0, 5, 0, 5));

        // 4. (可选但推荐) 为 Label 设置一个最小宽度，确保在行数很少时也有一个不错的外观
        // 这里可以根据字体大小估算一个值
        lineNumberLabel.setMinWidth(maxDigits * 8 + 10); // 估算值，根据你的字体和Padding调整

        return lineNumberLabel;
    }
}
