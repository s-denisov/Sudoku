package com.sdenisov.sudoku;

import android.util.Log;

public class SudokuSolver {
    // Works by modifying sudokuData object so doesn't need to return a value.
    public static void solve(SudokuData sudokuData) {
        int index = 0; // From left to right, top to bottom
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
                    boolean moveBack = true;
                    while (moveBack) {
                        index--; // Moves back by decrementing index
                        Log.d("project", String.valueOf(index));
                        if (index < 0) {
                            return; // There are no possible solutions so the solver stops execution
                        }
                        SudokuData.SudokuCell currentCellMovingBack
                                = sudokuData.getValue(index / sudokuData.getRows(),
                                index % sudokuData.getRows()); // Finds new current cell for moving back
                        if (currentCellMovingBack.getValue() == sudokuData.getRows()) {
                            currentCellMovingBack.setValue(null); // Cell value set to empty
                        } else if (!currentCellMovingBack.isInitialValue()) {
                            // If the current cell is below the maximum value and is not an initial value then it can
                            // be incremented so moving back stops.
                            moveBack = false;
                        }
                    }
                }
            }
            index++;
            Log.d("project", String.valueOf(index));
        }
    }
}
