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

package tech.minediamond.vortex.ui;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MappedList<S, T> extends TransformationList<T, S> {

    /**
     * 用于将源元素 (S) 转换为目标元素 (T) 的函数。
     */
    private final Function<? super S, ? extends T> mapper;

    /**
     * 存储映射后元素的内部缓存列表。
     * 这个列表与源列表保持同步，所有 get() 和 size() 操作都直接委托给它，以实现 O(1) 的访问效率。
     */
    private final List<T> mapped = new ArrayList<>();

    /**
     * 构造一个 MappedList。
     * 注：可以将返回接收到{@code ObservableList<>} 来直接使用转换后的列表
     *
     * @param source 源列表，其变化将被监听。
     * @param mapper 一个函数，用于将类型 S 的元素转换为类型 T 的元素。
     */
    public MappedList(ObservableList<? extends S> source, Function<? super S, ? extends T> mapper) {
        super(source);
        this.mapper = mapper;
        for (S s : source) {
            mapped.add(mapper.apply(s));
        }
    }

    /**
     * 当源列表发生变化时，此方法被回调。
     * 它的核心职责是：
     * 1. 根据源列表的变化类型（如添加、删除、更新、排序）来更新内部的 {@code mapped} 缓存列表。
     * 2. 调用相应的 {@code next...()} 方法，将这些变化通知给 MappedList 自己的监听器。
     *
     * @param c 描述源列表变化的 {@link Change} 对象。
     */
    @Override
    protected void sourceChanged(Change<? extends S> c) {
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) { //处理排序 (Permutation) 变化
                int from = c.getFrom();
                int to = c.getTo();

                // 创建受影响范围的快照。直接在 mapped 上操作会导致元素被覆盖。
                List<T> oldSeg = new ArrayList<>(mapped.subList(from, to));
                // 根据源列表的置换规则，重新排列内部的 mapped 列表。
                for (int i = from; i < to; i++) {
                    int dest = c.getPermutation(i); // 绝对索引
                    T val = oldSeg.get(i - from);
                    mapped.set(dest, val);
                }

                // 构建并通知监听器发生了排序事件。
                int[] perm = new int[to - from];
                for (int i = from; i < to; i++) {
                    perm[i - from] = c.getPermutation(i);
                }
                nextPermutation(from, to, perm);

            } else if (c.wasUpdated()) {// 处理更新 (Update) 变化
                for (int i = c.getFrom(); i < c.getTo(); i++) {
                    mapped.set(i, mapper.apply(getSource().get(i)));
                    nextUpdate(i);
                }

            } else { //处理添加 (Add) / 删除 (Remove) / 替换 (Replace) 变化
                int from = c.getFrom();
                // 预先构造 removed 列表（如果发生了删除操作）。
                // 必须在修改 mapped 列表之前执行，以捕获被删除元素的旧值。
                List<T> removed = null;
                if (c.wasRemoved()) {
                    removed = new ArrayList<>(mapped.subList(from, from + c.getRemovedSize()));
                }

                if (c.wasAdded() && c.wasRemoved()) { // 检查是否为替换操作（同时发生添加和删除）
                    // 映射所有新添加的源元素。
                    List<T> adds = new ArrayList<>(c.getAddedSize());
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        adds.add(mapper.apply(getSource().get(i)));
                    }
                    // 在 mapped 缓存中执行替换操作：先删除旧元素，再插入新元素。
                    mapped.subList(from, from + c.getRemovedSize()).clear();
                    mapped.addAll(from, adds);

                    // 发出一个语义上更精确的 replace 事件。
                    nextReplace(from, from + adds.size(), removed);

                } else { // 处理单纯地删除或添加
                    if (c.wasRemoved()) { // 单纯移除
                        mapped.subList(from, from + c.getRemovedSize()).clear();
                        nextRemove(from, removed);
                    }
                    if (c.wasAdded()) { // 单纯新增
                        List<T> adds = new ArrayList<>(c.getAddedSize());
                        for (int i = c.getFrom(); i < c.getTo(); i++) {
                            adds.add(mapper.apply(getSource().get(i)));
                        }
                        mapped.addAll(from, adds);
                        nextAdd(from, from + adds.size());
                    }
                }
            }
        }
        endChange();
    }

    @Override
    public int getSourceIndex(int index) {
        return index;
    }

    @Override
    public int getViewIndex(int sourceIndex) {
        return sourceIndex;
    }

    @Override
    public T get(int index) {
        return mapped.get(index);
    }

    @Override
    public int size() {
        return mapped.size();
    }
}

