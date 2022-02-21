package com.sdenisov.sudoku.db;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface SudokuDao {
    @Query("SELECT * FROM Sudoku ORDER BY sudokuId DESC")
    Sudoku getSudokus();
}
