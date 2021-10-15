package com.sdenisov.sudoku;

import java.util.AbstractMap;

// Acts as a form of renaming for AbstractMap.SimpleEntry, whose name is both misleading (as this doesn't have to be
// a key value pair) and long.
public class Tuple2<K, V> extends AbstractMap.SimpleEntry<K, V> {
    public Tuple2(K key, V value) {
        super(key, value);
    }
}
