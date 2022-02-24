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

    // Loads the sudoku from SharedPreferences and returns it, allowing the last used sudoku to be recovered
    // (both for the solver and generator)
    public static SudokuData loadSudoku(boolean isGenerator) {
        String sudokuString = "3\n3\n:4!-+-+-----!1:8:9!:2!:5!:9:3:6:1!!!:4:8!!!:5!!!!!!:5!!!:9:4!:2:1:6!!:3:8!!!!!:5!!:1!!!:5:7!!:9:4!!:1!:8!!:5:3!!:9!!:1:6:8!!:2";
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
}
