package com.sdenisov.sudoku;

import android.content.SharedPreferences;

public class SudokuSaver {

    // Constants to make the markers easy to change
    // Private because they do not need to be accessed by other classes as other classes interact with sudokus
    // directly using saveSudoku and loadSudoku without having to worry about the sudoku string representation
    // (thus providing a form of encapsulation)
    private static final String GENERATOR_SUDOKU_STRING_KEY = "com.sdenisov.sudoku.SudokuSaver.sudokuString.generator";
    private static final String SOLVER_SUDOKU_STRING_KEY = "com.sdenisov.sudoku.SudokuSaver.sudokuString.solver";
    private static final char INITIAL_VALUE_MARKER = ':';
    private static final char NON_INITIAL_VALUE_MARKER = '!';
    private static final char NOTE_PRESENT = '+';
    private static final char NOTE_ABSENT = '-';

    // SharedPreferences and isGenerator are the same for the same SudokuGridActivity and each SudokuSaver belongs to a
    // single SudokuGridActivity so sharedPref and isGenerator are passed to the constructor (instead of having to pass
    // them to saveSudoku and loadSudoku each time).
    private final SharedPreferences sharedPref;
    private final boolean isGenerator;

    public SudokuSaver(SharedPreferences sharedPref, boolean isGenerator) {
        this.sharedPref = sharedPref;
        this.isGenerator = isGenerator;
    }

    // Saves a sudoku using SharedPreferences so that data isn't lost even if the device is turned off.
    // isGenerator is passed so that sudokus used by the Generator and the Solver are saved separately, allowing the
    // user to use the generator and solve a sudoku at the same time.
    public void saveSudoku(SudokuData sudokuData) {
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

        // Uses the sharedPref editor to save the sudokuString using the appropriate key (a separate key is used for
        // the generator and solver, so both a generator and solver can be saved at the same time but only one sudoku
        // of each type can be saved at the same time)
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(isGenerator ? GENERATOR_SUDOKU_STRING_KEY : SOLVER_SUDOKU_STRING_KEY, sudokuString.toString());
        editor.apply();
    }

    // Loads the sudoku from SharedPreferences and returns it, allowing the last used sudoku to be recovered
    // (both for the solver and generator)
    public SudokuData loadSudoku() {
        // Retrieves the sudokuString from SharedPreferences based on isGenerator
        // Returns null if there is no such string, allowing the caller to take the appropriate action
        // (this means a new sudoku will be generated instead of loading the previous one).
        String sudokuString = sharedPref.getString(isGenerator ? GENERATOR_SUDOKU_STRING_KEY : SOLVER_SUDOKU_STRING_KEY,
                null);
        if (sudokuString == null) return null;
        // Splits the string into an array of its lines - the first line is boxRows, the next boxColumns and
        // the next has information about the cells
        String[] stringLines = sudokuString.split("\n");
        // Creates the SudokuData object by converting the first two lines from strings to integers
        SudokuData sudokuData = new SudokuData(Integer.parseInt(stringLines[0]), Integer.parseInt(stringLines[1]));
        sudokuString = stringLines[2]; // Only the last line is now needed, so the last line is assigned to sudokuString
        int cellIndex = 0; // The index of the next cell which will modified
        int notesIndex = 0; // The index of the note within the current cell (ignored if there are no notes)
        // The cell currently being modified based on information about it in the string
        SudokuData.SudokuCell currentCell = null;
        for (int charIndex = 0; charIndex < sudokuString.length(); charIndex++) {
            char currentChar = sudokuString.charAt(charIndex);
            // Signifies a new cell starting
            // Note that if two markers are adjacent then the program will move onto the next cell without modifying
            // the value of the current cell so the value is (correctly) kept as null
            if (currentChar == INITIAL_VALUE_MARKER || currentChar == NON_INITIAL_VALUE_MARKER) {
                currentCell = sudokuData.getValue(cellIndex);
                // If the initial value marker is used then initialValue is set to true, otherwise the non-initial
                // value marker is used so initialValue is set to false
                currentCell.setInitialValue(currentChar == INITIAL_VALUE_MARKER);
                notesIndex = 0; // Resets the notesIndex as the notesIndex is specific to the cell
                // Increments the index so that the next marker will result in currentCell becoming the next cell
                cellIndex++;
            } else {
                // If the sudoku string is formatted correctly then the first character is : or ! so  currentCell will
                // be immediately set to a cell - so currentCell should not be null if the string,
                // if formatted correctly. This assertion is used to remove IDE warnings about null pointer exceptions.
                assert currentCell != null;
                if (currentChar == NOTE_PRESENT || currentChar == NOTE_ABSENT) {
                    // If the note present sign is used then the note at the current index is set to true, otherwise
                    // it is set to false
                    currentCell.notes[notesIndex] = currentChar == NOTE_PRESENT;
                    notesIndex++; // So that the next iteration moves onto the next note
                } else if (currentCell.getValue() == null) {
                    // The cell has no value so its value is set to the current character.
                    currentCell.setValue(Integer.parseInt(Character.toString(currentChar)));
                } else {
                    // Then the current cell has a value - this is used for 12x12 sudokus
                    // The new value, as a string is the old value concatenated to the current character
                    String newValue = Integer.toString(currentCell.getValue()) + currentChar;
                    // Converts the new value to an integer and sets the current cell to this value
                    currentCell.setValue(Integer.parseInt(newValue));
                }
            }
        }
        return sudokuData;
    }

    // Removes data about the sudoku (for generator or solver as appropriate), allowing the user to start a new sudoku
    public void removeSudoku() {
        sharedPref.edit() // Uses the sharedPref editor
                .remove(isGenerator ? GENERATOR_SUDOKU_STRING_KEY : SOLVER_SUDOKU_STRING_KEY)
                .apply(); // Applies changes
    }
}
