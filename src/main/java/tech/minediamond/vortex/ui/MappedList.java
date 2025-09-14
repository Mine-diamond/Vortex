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

    private final Function<? super S, ? extends T> mapper;
    private final List<T> mapped = new ArrayList<>();

    public MappedList(ObservableList<? extends S> source, Function<? super S, ? extends T> mapper) {
        super(source);
        this.mapper = mapper;
        for (S s : source) {
            mapped.add(mapper.apply(s));
        }
    }

    @Override
    protected void sourceChanged(Change<? extends S> c) {
        beginChange();
        while (c.next()) {
            if (c.wasPermutated()) {
                int from = c.getFrom();
                int to = c.getTo();

                // 先根据 permutation 重排 mapped（基于快照）
                List<T> oldSeg = new ArrayList<>(mapped.subList(from, to));
                for (int i = from; i < to; i++) {
                    int dest = c.getPermutation(i); // 绝对索引
                    T val = oldSeg.get(i - from);
                    mapped.set(dest, val);
                }

                // 再构造并发送 permutation 事件
                int[] perm = new int[to - from];
                for (int i = from; i < to; i++) {
                    perm[i - from] = c.getPermutation(i);
                }
                nextPermutation(from, to, perm);

            } else if (c.wasUpdated()) {
                for (int i = c.getFrom(); i < c.getTo(); i++) {
                    mapped.set(i, mapper.apply(getSource().get(i)));
                    nextUpdate(i);
                }

            } else {
                int from = c.getFrom();
                // 预先构造 removed 列表（如果有）
                List<T> removed = null;
                if (c.wasRemoved()) {
                    removed = new ArrayList<>(mapped.subList(from, from + c.getRemovedSize()));
                }

                if (c.wasAdded() && c.wasRemoved()) {
                    // 替换：先用新增内容覆盖原区间
                    List<T> adds = new ArrayList<>(c.getAddedSize());
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        adds.add(mapper.apply(getSource().get(i)));
                    }
                    // 先清掉原区间，再插入 adds
                    mapped.subList(from, from + c.getRemovedSize()).clear();
                    mapped.addAll(from, adds);

                    // 发出一次 replace 事件（语义正确）
                    nextReplace(from, from + adds.size(), removed);

                } else {
                    if (c.wasRemoved()) {
                        // 单纯移除
                        mapped.subList(from, from + c.getRemovedSize()).clear();
                        nextRemove(from, removed);
                    }
                    if (c.wasAdded()) {
                        // 单纯新增
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

