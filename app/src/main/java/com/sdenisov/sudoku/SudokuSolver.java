package com.sdenisov.sudoku;

import java.util.ArrayList;
import java.util.List;

public class SudokuSolver {
    // Works by modifying sudokuData object so doesn't need to return a value.
    public static boolean solve(SudokuData sudokuData) {
        fillNotes(sudokuData);
        SudokuData.SudokuCell leastNotesCell = null;
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                if (cell.getValue() == null) {
                    if (leastNotesCell == null || noteCount(cell) < noteCount(leastNotesCell)) {
                        leastNotesCell = cell;
                    }
                }
            }
        }
        if (leastNotesCell == null) return true;
        if (noteCount((leastNotesCell)) == 0) return false;
        for (int note : notesToInt(leastNotesCell)) {
            leastNotesCell.setValue(note);
            if (solve(sudokuData)) return true;
        }
        leastNotesCell.setValue(null);
        return false;
    }
/*
    public static void solve(SudokuData sudokuData) {
        int index = 0; // From left to right, top to bottom
        // When a cell has reached maximum value and still has an error then this is set to false
        // to show that backtracking is occurring
        boolean moveForward = true;
        // rows * rows = rows * columns is the total number of cells within the grid
        while (index < sudokuData.getRows() * sudokuData.getRows()) {
            Stack<SudokuData.SudokuCell> filledCells = new Stack<>();
            fillNotes(sudokuData);
            SudokuData.SudokuCell leastNotesCell = sudokuData.getValue(0, 0);
            for (int row = 0; row < sudokuData.getRows(); row++) {
                for (int column = 0; column < sudokuData.getRows(); column++) {
                    SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                    if (noteCount(cell) < noteCount(leastNotesCell)) {
                        leastNotesCell = cell;
                    }
                }
            }


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
    }*/

    public static void fillNotes(SudokuData sudokuData) {
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                if (cell.getValue() == null) {
                    boolean[] notes = new boolean[sudokuData.getRows()];
                    for (int value = 1; value <= sudokuData.getRows(); value++) {
                        cell.setValue(value);
                        notes[value - 1] = sudokuData.findErrors().size() == 0;
                    }
                    System.arraycopy(notes, 0, cell.notes, 0, notes.length);
                    cell.setValue(null);
                }
            }
        }
    }

    private static int noteCount(SudokuData.SudokuCell cell) {
        int count = 0;
        for (boolean note : cell.notes) if (note) count++;
        return count;
    }

    private static List<Integer> notesToInt(SudokuData.SudokuCell cell) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < cell.notes.length; i++) {
            if (cell.notes[i]) result.add(i + 1);
        }
        return result;
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
