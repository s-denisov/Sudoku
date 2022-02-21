package com.sdenisov.sudoku;

import android.util.Log;

public class SudokuSaver {

    // Constants to make the markers easy to change
    // Private because they do not need to be accessed by other classes as other classes interact with sudokus
    // directly using saveSudoku and loadSudoku without having to worry about the sudoku string representation
    // (thus providing a form of encapsulation)
    private static final char INITIAL_VALUE_MARKER = ':';
    private static final char NON_INITIAL_VALUE_MARKER = '!';

    public static void saveSudoku(SudokuData sudokuData, boolean isGenerator) {
        StringBuilder sudokuString = new StringBuilder();
        sudokuString.append(sudokuData.getBoxRows()).append("\n").append(sudokuData.getBoxColumns()).append("\n");
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                // Gets the current cell based on the current row and column
                SudokuData.SudokuCell currentCell = sudokuData.getValue(row, column);
                // Adds the appropriate marker
                sudokuString.append(currentCell.isInitialValue() ? INITIAL_VALUE_MARKER : NON_INITIAL_VALUE_MARKER);
                // Adds the value
                sudokuString.append(currentCell.getValue());
                // So e.g. ":9" is initialValue is true and has value of 9
            }
        }
        Log.d("project", sudokuString.toString());
    }
}
