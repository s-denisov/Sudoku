package com.sdenisov.sudoku;

public class SudokuSolver {
    // Works by modifying sudokuData object so doesn't need to return a value.

    public static void solve(SudokuData sudokuData) {
        int index = 0; // From left to right, top to bottom
        // When a cell has reached maximum value and still has an error then this is set to false
        // to show that backtracking is occurring
        boolean moveForward = true;
        // rows * rows = rows * columns is the total number of cells within the grid
        while (index < sudokuData.getRows() * sudokuData.getRows()) {
            SudokuData.SudokuCell currentCell = sudokuData.getValue(index / sudokuData.getRows(), // integer division
                    index % sudokuData.getRows()); // Finds the cell at the current index
            if (currentCell.isInitialValue()) {
                // During backtracking, the algorithm moves backwards past initial value cells. Otherwise, it moves
                // forwards past them.
                index = index + (moveForward ? 1 : -1);
            } else {
                if (currentCell.getValue() == null) { // If the current cell is empty then it is set to the first value i.e. 1
                    currentCell.setValue(1);
                }
                if (sudokuData.findErrors().size() == 0 && moveForward) {
                    // This isn't done during backtracking because a value must be changed during backtracking, so we
                    // can't just move forward without changing a value.
                    index++;
                } else if (currentCell.getValue() < sudokuData.getRows()) {
                    // If the value can be incremented then it is incremented
                    currentCell.setValue(currentCell.getValue() + 1);
                    moveForward = true; // A value has changed so backtracking no longer occurs
                } else {
                    currentCell.setValue(null); // Sets cell to empty
                    moveForward = false; // Backtracking occurs, with a previous value having to change
                    index--;
                }
            }
        }
    }

    // Removes all values added by the solver (i.e. where initialValue is false), allowing the user to modify the input sudoku
    public static void unsolve(SudokuData sudokuData) {
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                if (!cell.isInitialValue()) {
                    cell.setValue(null);
                }
            }
        }
    }
}
