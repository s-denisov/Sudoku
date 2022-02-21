package com.sdenisov.sudoku.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Cell {
    @PrimaryKey(autoGenerate = true) @NonNull public int cellId;
    @NonNull public int index;
    @NonNull public String sudokuName;
    public Integer value;
    @NonNull public boolean playerInput;
}
