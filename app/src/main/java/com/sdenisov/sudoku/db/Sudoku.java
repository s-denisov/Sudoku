package com.sdenisov.sudoku.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Sudoku {
    @PrimaryKey(autoGenerate = true) @NonNull public int sudokuId;
    @NonNull public String name;
    @NonNull public int boxRows;
    @NonNull public int boxColumns;
}
