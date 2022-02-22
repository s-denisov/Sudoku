package com.sdenisov.sudoku;

import android.util.Log;

public class SudokuSaver {

    // Constants to make the markers easy to change
    // Private because they do not need to be accessed by other classes as other classes interact with sudokus
    // directly using saveSudoku and loadSudoku without having to worry about the sudoku string representation
    // (thus providing a form of encapsulation)
    private static final char INITIAL_VALUE_MARKER = ':';
    private static final char NON_INITIAL_VALUE_MARKER = '!';
    private static final char NOTE_PRESENT = '+';
    private static final char NOTE_ABSENT = '-';

    // Saves a sudoku using SharedPreferences so that data isn't lost even if the device is turned off.
    // isGenerator is passed so that sudokus used by the Generator and the Solver are saved separately, allowing the
    // user to use the generator and solve a sudoku at the same time.
    public static void saveSudoku(SudokuData sudokuData, boolean isGenerator) {
        StringBuilder sudokuString = new StringBuilder();
        // Adds initial data about boxRows and boxColumns, with newlines (so that can be parsed easily by converting
        // the string to a list, with each line being an element)
        sudokuString.append(sudokuData.getBoxRows()).append("\n").append(sudokuData.getBoxColumns()).append("\n");
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                // Gets the current cell based on the current row and column
                SudokuData.SudokuCell currentCell = sudokuData.getValue(row, column);
                // Adds the appropriate marker
                sudokuString.append(currentCell.isInitialValue() ? INITIAL_VALUE_MARKER : NON_INITIAL_VALUE_MARKER);
                if (currentCell.hasNotes()) {
                    for (boolean note : currentCell.notes) {
                        sudokuString.append(note ? NOTE_PRESENT : NOTE_ABSENT);
                    }
                    // Represents the notes as a string of + and -
                    // e.g. "+--+-----" means the notes are 1 and 4
                } else if (currentCell.getValue() != null) {
                    // If there are notes, adds the value (doesn't add anything if there are no notes and
                    // the value is null)
                    sudokuString.append(currentCell.getValue());
                    // So e.g. ":9" is initialValue is true and has value of 9
                }
            }
        }
        Log.d("project", sudokuString.toString());
    }
}
