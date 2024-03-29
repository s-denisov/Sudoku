package com.sdenisov.sudoku;

import java.util.*;

public class SudokuSolver {
    // Works by modifying sudokuData object so doesn't need to return a new SudokuData object.
    // Need to use separate procedures for solve and solveWithRecursion as the former updates all notes while the
    // latter doesn't. This is because solve is called only once so updating all notes takes an acceptable amount of
    // time, while solveWithRecursion is called multiple times, so it doesn't take an acceptable amount of time.
    // Returns the difficulty: 1 (easy), 2 (medium), 3 (hard) or 4 (unlimited), with -1 if no solutions
    public static int solve(SudokuData sudokuData, int noteSelectionMethod) {
        // If noteSelectionMethod is positive, notes are selected in increasing order.
        // If negative then in decreasing order.
        // If zero then in random order.
        updateNotes(sudokuData, -1);
        int[] guesses = solveWithRecursion(sudokuData, noteSelectionMethod);
        sudokuData.clearNotes(); // Removes any notes that were added to help work out the solution
        if (guesses == null) return -1; // No solutions
        if (Math.min(guesses[2], guesses[3]) > 0) {
            // Looking at very many sudokus, all of them had guesses[2] = guesses[3] = 0, even the world's hardest
            // sudoku. Guessing from 3 or more notes gives many combinations, so if guesses[2] > 0 or guesses[3] > 0
            // then this if statement runs and the sudoku is likely very hard so "unlimited" difficulty is returned
            return 4;
        }
        if (guesses[1] == 0) {
            // Then all guesses were from 1 note, which means the sudoku can be solved using only two simple
            // techniques - single candidate ("guessing" from 1 note) and single position. As a result, it has a fairly
            // low difficulty and is thus rated as easy or medium.
            if (guesses[0] < 2) {
                // Single candidate is generally harder to use than single position, as single candidate can require
                // a large number of cells to be checked. As a result, if single candidate is used less, then the
                // sudoku is given the lower difficulty of 1, while if single candidate is used more, then the sudoku
                // is given the higher difficulty of 2.
                return 1;
            }
            return 2;
        }
        // guesses[1] > 0 and guesses[2] = guesses[3] = 0 then we use guesses[1] to determine the difficulty, as the
        // guesses from guesses[0] are simple so have little impact on the difficulty. A lower value for guesses[1]
        // suggests the difficulty is lower so 3 is returned if it is below a certain value. Otherwise, 4 is returned.
        if (guesses[1] < 2) return 3;
        return 4;
    }

    // Returns an array showing the number of times that the solver guessed from a certain number of notes:
    // [1, 2, 3, 4 or more]. Null is returned if the sudoku has no solutions. This is used later to rate the difficulty
    // of the sudoku - sudokus where guesses are needed from a large number of notes are likely harder.
    private static int[] solveWithRecursion(SudokuData sudokuData, int noteSelectionMethod) {
        Tuple2<Set<SudokuData.SudokuCell>, Boolean> simplificationResult = simplifySinglePosition(sudokuData);
        Set<SudokuData.SudokuCell> cellsChanged = simplificationResult.getFirst();
        // If filling in all cells is impossible then the method fails by removing all values from changed cells then
        // returning null.
        if (!simplificationResult.getSecond()) {
            removeAllCellValues(sudokuData, cellsChanged);
            return null;
        }

        // The cell with the least notes. Starting from this cell is likely to result in better performance - e.g. if it
        // has one note, it can be filled immediately, if it has two then there are only two options to consider.
        SudokuData.SudokuCell leastNotesCell = null;
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                // Finds the cell at the corresponding row and column
                SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                // Cells which already have a value cannot be considered to have the least notes
                if (cell.getValue() == null) {
                    if (leastNotesCell == null || notesToInt(cell).size() < notesToInt(leastNotesCell).size()) {
                        // If leastNotesCell is null or cell has fewer notes than leastNotesCell then cell is set
                        // as the new leastNotesCell
                        leastNotesCell = cell;
                    }
                }
            }
        }
        if (leastNotesCell == null) {
            // Then there are no empty cells. If there were errors then the algorithm would've stopped before reaching
            // this point - so there are no errors so the solver has been successful.
            // An array filled with 0s is returned because no guesses were made at this step. This array will be modified
            // by the previous recursive callers, adding to the array so that the array eventually shows the number of
            // guesses for the whole solver.
            return new int[]{0, 0, 0, 0};
        }
        if (notesToInt((leastNotesCell)).size() == 0) {
            removeAllCellValues(sudokuData, cellsChanged);
            // Then there is at least one cell with no value and no notes. So it has no possible values so there is
            // no possible solution with the inputted values so null is returned.
            return null;
        }
        List<Integer> intNotes = notesToInt(leastNotesCell);
        // The algorithm will go through intNotes in order. intNotes are currently ascending, so if noteSelectionMethod
        // is positive then they don't need to be modified.
        // If noteSelectionMethod is zero then intNotes are shuffled.
        // If noteSelectionMethod is negative then intNotes are reversed so that they are in descending order
        if (noteSelectionMethod == 0) {
            Collections.shuffle(intNotes);
        } else if (noteSelectionMethod < 0) {
            Collections.reverse(intNotes);
        }
        // Iterates through the value of each note in leastNotesCell
        for (int i = 0; i < intNotes.size(); i++) {
            leastNotesCell.setValue(intNotes.get(i));
            // Notes are updated for both the previous and the new value of leastNotesCell. The previous value would
            // be added back to appropriate cells while the new value will be removed from appropriate cells.
            if (i > 0) updateCellNotesForRemovingCellValue(sudokuData, intNotes.get(i - 1), leastNotesCell.row,
                    leastNotesCell.column);
            updateCellNotesForAddingCellValue(sudokuData, intNotes.get(i), leastNotesCell.row, leastNotesCell.column);

            // Calls itself recursively. If the call has been successful then it returns a non-null array, so the previous
            // recursive caller also returns non-null until non-null is returned by solve() to the original caller.
            int[] difficultyOfOtherCells = solveWithRecursion(sudokuData, noteSelectionMethod);
            if (difficultyOfOtherCells != null) {
                difficultyOfOtherCells[Math.min(3, intNotes.size() - 1)]++;
                // Incremented so that difficultyOfOtherCells includes the current guess - e.g. if the current guess
                // was from 2 notes then index 1 is incremented. If the current guess is from more than 3 notes then
                // index 2 is still incremented, as if the guess was from 3 notes.
                return difficultyOfOtherCells;
            }
        }
        cellsChanged.add(leastNotesCell); // This is done so that the value is removed from both
        removeAllCellValues(sudokuData, cellsChanged);
        return null; // All values have been tried for the cell and none are successful so null is returned
    }

    // updateNotes updates notes in all cells.
    // noteToUpdate is the value of the note that should be updated - for example, if it is 1 then only the notes for 1
    // will be updated in cells. This allows improving performance, as notes wouldn't be updated needlessly.
    // If noteToUpdate is 0 or less (I will be passing -1) then all notes will be updated.
    public static void updateNotes(SudokuData sudokuData, int noteToUpdate) {
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                // Gets the cell at the correct row and column
                SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                if (cell.getValue() == null) {
                    // This is a new array for notes, which will be copied onto cell.notes
                    if (noteToUpdate <= 0) {
                        // Iterates through each possible note value
                        for (int value = 1; value <= sudokuData.getRows(); value++) {
                            // cell.value is temporarily set to value in order for findErrors() to work properly
                            cell.setValue(value);
                            // If there are no errors then cell.notes is set to true at the corresponding index - this
                            // is a valid value, so it is recorded in a note. Otherwise, cell.notes is set to false.
                            cell.notes[value - 1] = sudokuData.findErrors().size() == 0;
                        }
                    } else {
                        // Works like before but using `noteToUpdate` instead of `value` from the for loop.
                        cell.setValue(noteToUpdate);
                        cell.notes[noteToUpdate - 1] = sudokuData.findErrors().size() == 0;
                    }
                    // In either of the above cases, the procedure finishes by setting the cell value to null
                    cell.setValue(null);
                }
            }
        }
    }

    // Converts the notes array to a list of integers corresponding to the notes' values
    private static List<Integer> notesToInt(SudokuData.SudokuCell cell) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < cell.notes.length; i++) {
            // The value is one more than the index as values start from 1 but indexes start from 0
            if (cell.notes[i]) result.add(i + 1);
        }
        return result;
    }

    // Updates notes as they would need to be updated if the cell at the given row and column was set to the given value.
    // This is done by iterating through each cell in the groups and setting the appropriate note to false - this is
    // because other cells in the groups cannot have the same value as this cell.
    private static void updateCellNotesForAddingCellValue(SudokuData sudokuData, int value, int row, int column) {
        for (SudokuData.SudokuCell cell : sudokuData.findGroups(row, column)) {
            if (cell.getValue() == null) cell.notes[value - 1] = false;
        }
    }

    // Updates notes as they would need to be updated if the cell at the given value was removed from the given row
    // and column
    private static void updateCellNotesForRemovingCellValue(SudokuData sudokuData, int value, int row, int column) {
        outerLoop:
        for (SudokuData.SudokuCell cell : sudokuData.findGroups(row, column)) {
            if (cell.getValue() == null) {
                // Checks each cell in the same group as the cell from the outer for loop. If any cell has
                // the same values as the value requested then this isn't a valid value for the cell from the outer for
                // loop so the corresponding note isn't added.
                for (SudokuData.SudokuCell cell2 : sudokuData.findGroups(cell.row, cell.column)) {
                    if (Objects.equals(value, cell2.getValue())) {
                        // Moves onto the next cell in the outerLoop, thus skipping the last line that adds the note
                        // (also improves performance by avoiding unnecessary iterations of the for loop
                        continue outerLoop;
                    }
                }
                // If the continue statement hasn't been reached then the values is valid so the corresponding
                // note is added.
                cell.notes[value - 1] = true;
            }
        }
    }

    // Removes all values added by the solver (i.e. where initialValue is false), allowing the user to modify the input
    // sudoku
    public static void unsolve(SudokuData sudokuData) {
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                if (!cell.isInitialValue()) { // Only values added as part of the solution are removed
                    cell.setValue(null); // Removes the value by setting it to null
                    Arrays.fill(cell.notes, false); // Removes all notes
                }
            }
        }
    }

    // Whenever a number has only one possible cell within a group, this sets the cell's value to that number
    // This returns a set of cells affected, so that the changes can be easily undone (it also leaves each cell's notes
    // array unmodified, so changes can be undone simple by setting cell.value to null)
    // It also returns (using a tuple) a boolean, which is true if no error was detected, and
    // false if an error has been detected.
    private static Tuple2<Set<SudokuData.SudokuCell>, Boolean> simplifySinglePosition(SudokuData sudokuData) {
        Set<SudokuData.SudokuCell> modified = new HashSet<>();
        for (Set<SudokuData.SudokuCell> group : sudokuData.findAllGroups()) {
            noteLoop:
            for (int note = 1; note <= sudokuData.getRows(); note++) {
                // This exists solely to check if there's been an error due to this value not being possible in any
                // cell in the group. We get such an error if it is not present in any cell as a note or value - we
                // use singleCellWithNote to check if it is present as a note and existsCellWithValue to check if it
                // is present as a value.
                boolean existsCellWithValue = false;
                SudokuData.SudokuCell singleCellWithNote = null;
                for (SudokuData.SudokuCell cell : group) {
                    // Only runs if corresponding note is present. Checks that the cell value is null, as cells without
                    // a value are not considered to have any notes.
                    if (cell.getValue() == null && cell.notes[note - 1]) {
                        if (singleCellWithNote == null) {
                            // No other cell has been found so far so this is set as the single cell with note
                            singleCellWithNote = cell;
                        } else  {
                            // Then there are at least two cells that have this value as a note. Thus, this procedure
                            // can't do anything for this combination of note and group, so it moves onto the next
                            // combination by moving onto the next note.
                            continue noteLoop;
                        }
                    } else if (cell.getValue() != null && cell.getValue() == note) {
                        // If the cell's value equals the note then the cell with value exists in this group for this
                        // note so there will be no error
                        existsCellWithValue = true;
                    }
                }
                if (singleCellWithNote == null && !existsCellWithValue) {
                    // Then the current value is not present as a note or cell value in this group so the group and
                    // thus the grid cannot be filled so an error is returned. The set of modified cells is still
                    // returned so that any changes can be undone.
                    return new Tuple2<>(modified, false);
                }
                // This checks if there is at least one such cell. If there is more than one then this wouldn't be
                // reached as `continue noteLoop` would be called beforehand.
                if (singleCellWithNote != null) {
                    singleCellWithNote.setValue(note); // Sets the value to note
                    modified.add(singleCellWithNote); // Adds to set of modified notes
                    updateCellNotesForAddingCellValue(sudokuData, note, singleCellWithNote.row,
                            singleCellWithNote.column); // Updates notes in the grid
                    // Note that the notes of singleCellWithNote are not emptied so that singleCellWithNote can be
                    // easily reverted to its previous state by setting its value to null, without having to recover
                    // the notes.
                }
            }
        }
        // If this point has been reached then no errors have been detected so the modified set is returned, along with
        // true
        return new Tuple2<>(modified, true);
    }

    // This empties all cells and updates the notes. It is important that all cells are emptied before notes are
    // updated as otherwise, the cells yet to be emptied might influence the new notes.
    private static void removeAllCellValues(SudokuData sudokuData, Set<SudokuData.SudokuCell> cells) {
        // cellsData contains the value, row and column for each cell, which is just enough information to update
        // the notes for the grid
        List<Tuple2<Integer, Tuple2<Integer, Integer>>> cellsData = new ArrayList<>();
        for (SudokuData.SudokuCell cell : cells) {
            int value = cell.getValue();
            cell.setValue(null);
            cellsData.add(new Tuple2<>(value, new Tuple2<>(cell.row, cell.column)));
        }
        // Updates the notes to the grid based on changes for each cell
        for (Tuple2<Integer, Tuple2<Integer, Integer>> cellData : cellsData) {
            updateCellNotesForRemovingCellValue(sudokuData, cellData.getFirst(),
                    cellData.getSecond().getFirst(), cellData.getSecond().getSecond());
        }
    }
}
