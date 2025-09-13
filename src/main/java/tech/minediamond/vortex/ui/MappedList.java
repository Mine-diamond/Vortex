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
                if (c.wasRemoved()) {
                    List<T> removed = new ArrayList<>(mapped.subList(c.getFrom(), c.getFrom() + c.getRemovedSize()));
                    for (int i = 0; i < c.getRemovedSize(); i++) {
                        mapped.remove(c.getFrom());
                    }
                    nextRemove(c.getFrom(), removed);
                }
                if (c.wasAdded()) {
                    List<T> adds = new ArrayList<>();
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        adds.add(mapper.apply(getSource().get(i)));
                    }
                    mapped.addAll(c.getFrom(), adds);
                    nextAdd(c.getFrom(), c.getTo());
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

