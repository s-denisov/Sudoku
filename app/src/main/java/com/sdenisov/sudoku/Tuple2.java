package com.sdenisov.sudoku;

import java.util.AbstractMap;

public class Tuple2<K, V> extends AbstractMap.SimpleEntry<K, V> {
    public Tuple2(K key, V value) {
        super(key, value);
    }
}
