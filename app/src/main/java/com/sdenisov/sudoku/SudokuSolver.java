package com.sdenisov.sudoku;

import java.util.ArrayList;
import java.util.List;

public class SudokuSolver {
    // Works by modifying sudokuData object so doesn't need to return a value.
    public static boolean solve(SudokuData sudokuData) {
        updateNotes(sudokuData, -1);
        return solve2(sudokuData);
    }
    static int i = 0;
    static int j = 1;
    private static boolean solve2(SudokuData sudokuData) {
//            i++;
//            Log.d("project", String.valueOf(i));
//            if (i == j) return true;

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
        if (noteCount((leastNotesCell)) == 0) {
            return false;
        }
        List<Integer> intNotes = notesToInt(leastNotesCell);
        for (int i = 0; i < intNotes.size(); i++) {
            leastNotesCell.setValue(intNotes.get(i));
            if (i > 0) updateNotes(sudokuData, intNotes.get(i-1));
            updateNotes(sudokuData, intNotes.get(i));
            if (solve2(sudokuData)) return true;
        }
        leastNotesCell.setValue(null);
        updateNotes(sudokuData, intNotes.get(intNotes.size() - 1));
//        updateNotes(sudokuData, 0);
//
        if (intNotes.size() >= 2) updateNotes(sudokuData, intNotes.get(intNotes.size() - 2));
        return false;
    }

    // noteToUpdate is the value of the note that should be updated - for example, if it is 1 then only the notes for 1
    // will be updated in cells. This allows improving performance, as notes wouldn't be updated needlessly.
    // If noteToUpdate is 0 or less (I will be passing -1) then all notes will be updated.
    public static void updateNotes(SudokuData sudokuData, int noteToUpdate) {
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                if (cell.getValue() == null) {
                    boolean[] notes = new boolean[sudokuData.getRows()];
                    if (noteToUpdate <= 0) {
                        for (int value = 1; value <= sudokuData.getRows(); value++) {
                            cell.setValue(value);
                            notes[value - 1] = sudokuData.findErrors().size() == 0;
                        }
                    } else {
                        System.arraycopy(cell.notes, 0, notes, 0, notes.length);
                        cell.setValue(noteToUpdate);
                        notes[noteToUpdate - 1] = sudokuData.findErrors().size() == 0;
                    }
                    cell.setValue(null);
                    System.arraycopy(notes, 0, cell.notes, 0, notes.length);
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
        i =0;j++;
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
