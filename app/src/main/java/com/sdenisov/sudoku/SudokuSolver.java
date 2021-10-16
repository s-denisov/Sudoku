package com.sdenisov.sudoku;

public class SudokuSolver {
    // Works by modifying sudokuData object so doesn't need to return a value.
    public static void solve(SudokuData sudokuData) {
        int index = 0; // From left to right, top to bottom
        indexLoop: // A label for the while loop
        while (index < sudokuData.getRows() * sudokuData.getRows()) {
            SudokuData.SudokuCell currentCell = sudokuData.getValue(index / sudokuData.getRows(), // integer division
                    index % sudokuData.getRows());
            if (currentCell.getValue() == null) { // If the current cell is empty then it is set to the first value i.e. 1
                currentCell.setValue(1);
                currentCell.setInitialValue(false);
            }
            while (sudokuData.findErrors().size() != 0) {
                // If a cell hasn't reached maximum value and there are errors then its value is incremented
                if (currentCell.getValue() < sudokuData.getRows()) {
                    currentCell.setValue(currentCell.getValue() + 1);
                } // If a cell has reached maximum value and there are still errors then backtracking occurs
                else {
                    currentCell.setValue(null); // First, the value of the cell is set to a blank
                    while (true) {
                        index--; // Moves back by decrementing index
                        if (index < 0) {
                            return; // There are no possible solutions so the solver stops execution
                        }
                        SudokuData.SudokuCell currentCellMovingBack
                                = sudokuData.getValue(index / sudokuData.getRows(),
                                index % sudokuData.getRows()); // Finds new current cell for moving back
                        // Cell can only be modified if it is not an initial value
                        if (!currentCellMovingBack.isInitialValue()) {
                            if (currentCellMovingBack.getValue() == sudokuData.getRows()) {
                                // If cell is at maximum value, then its value is set to empty
                                currentCellMovingBack.setValue(null);
                            } else {
                                // If the current cell is not at the maximum value then it is incremented and the
                                // algorithm restarts from this cell. Moving back stops
                                currentCellMovingBack.setValue(currentCellMovingBack.getValue() + 1);
                                continue indexLoop; // Goes to the start of the loop with label indexLoop
                            }
                        }
                    }
                }
            }
            index++;
        }
    }

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
