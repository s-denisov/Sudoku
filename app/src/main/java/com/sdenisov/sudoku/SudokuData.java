package com.sdenisov.sudoku;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SudokuData {
    // 2D array containing information about each cell. Each inner array represents a row
    private final SudokuCell[][] values;
    private final int boxRows; // Number of rows of boxes in the grid
    private final int boxColumns; // Number of columns of boxes in the grid

    public class SudokuCell {
        private Integer value; // May be null, which represents empty cell.
        // Initial values are values included in the sudoku initially (as part of the problem)
        // while non-initial values are values added later (as part of the solution)
        private boolean initialValue = false;
        // true means present, false means not present. Notes are in order so index 0 corresponds to note 1.
        // It is impossible to set this to an invalid state, so this is public.
        // It is final to make sure the length isn't changed (by reassigning a new array to the variable)
        public final boolean[] notes = new boolean[getRows()];

        private SudokuCell(Integer value) {
            this.value = value;
        }

        // Sets a value if input is valid. Additionally, if a value is set then all notes are removed.
        // If the input is invalid then it is ignored.
        public void setValue(Integer value) {
            if (value == null || 1 <= value && value <= getRows()) {
                this.value = value;
                Arrays.fill(notes, false);
            }
        }

        // If there are any notes, then this returns null so that the object behaves as if there is no value set.
        // Thus setting any notes effectively removes the value.
        public Integer getValue() {
            for (boolean note : notes) if (note) return null;
            return value;
        }

        // The getter and setter for initialValue currently don't do anything special,
        // but can be modified later if necessary (e.g. to add validation)

        public boolean isInitialValue() {
            return initialValue;
        }

        public void setInitialValue(boolean initialValue) {
            this.initialValue = initialValue;
        }

        // Finds color based on initialValue. Using a function here allows color to be modified easily.
        public int getColor() {
            return initialValue ? Color.BLACK : Color.GRAY;
        }
    }

    public SudokuData(int boxRows, int boxColumns) {
        int rows = boxRows * boxColumns; // This is the same as the number of columns
        // values is set to a new 2D array and is populated by empty cells.
        values = new SudokuCell[rows][rows];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < rows; column++) {
                values[row][column] = new SudokuCell(null);
            }
        }
        this.boxRows = boxRows;
        this.boxColumns = boxColumns;
    }

    // The user is allowed access to any cell by requesting the row and column and can then modify the cell using
    // the SudokuCell methods (there are restrictions, but they are located within the SudokuCell class) - as the
    // cell is passed by reference, any changes made will also occur within this class.
    // However, the user is not allowed access to the values 2D array, to prevent them from modifying the structure of
    // the array, such as changing its length.
    public SudokuCell getValue(int row, int column) {
        return values[row][column];
    }

    public int getRows() {
        return values.length;
    }

    // Returns an array of indexes - if there are duplicates, then it contains the indexes of the two duplicates.
    // If there are no duplicates then it is an empty array.
    private int[] checkDuplicates(SudokuCell[] items) {
        // The two for loops iterate through all possible pairs of indexes
        for (int i = 0; i < items.length - 1; i++) {
            SudokuCell item = items[i];
            // j starts from i + 1 rather than 0 because combinations containing indexes less than i + 1 have
            // already been checked
            for (int j = i + 1; j < items.length; j++) {
                // Need to check that item.value is not null because two empty cells are not considered duplicates
                if (item.getValue() != null && item.getValue().equals(items[j].getValue())) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{};
    }

    // Returns a list of coordinates with errors, with coordinates being a tuple in the form (row, column).
    // Currently, this list contains either two coordinates or is empty.
    public List<Tuple2<Integer, Integer>> findErrors() {
        for (int row = 0; row < getRows(); row++) {
            int[] duplicates = checkDuplicates(values[row]);

            // Detects row duplicates
            if (duplicates.length != 0) {
                // Creating another variable that is equal to the current value of row but doesn't change, as values
                // used in lambda expressions are not allowed to change.
                int finalRow = row;
                // The mapToObj method applies the inputted function to each item in the array, creating a new list
                // out of the result (note that .collect is used to specify the output should be a list).
                return Arrays.stream(duplicates).mapToObj(column ->
                        new Tuple2<>(finalRow, column)).collect(Collectors.toList());
                // The row is the same for the two items but the column is equals the index in the array for the
                // current row, which is equal to one of the values in the array result of checkDuplicates
            }
        }

        // Detects column duplicates
        for (int column = 0; column < getRows(); column++) {
            SudokuCell[] currentColumn = new SudokuCell[getRows()];
            // The currentColumn is created by iterating through each row and adding the item from the correct column
            for (int row = 0; row < getRows(); row++) {
                currentColumn[row] = values[row][column];
            }
            // After the currentColumn array is created, this works like the code that checks for row duplicates
            int[] duplicates = checkDuplicates(currentColumn);
            if (duplicates.length != 0) {
                int finalColumn = column;
                return Arrays.stream(duplicates).mapToObj(row ->
                                new Tuple2<>(row, finalColumn))
                        .collect(Collectors.toList());
            }
        }

        // Detects box duplicates
        // boxLocationRow is the row of the box within the grid
        for (int boxLocationRow = 0; boxLocationRow < boxRows; boxLocationRow++) {
            for (int boxLocationColumn = 0; boxLocationColumn < boxColumns; boxLocationColumn++) {
                // currentBox contains all cells within the box
                SudokuCell[] currentBox = new SudokuCell[getRows()];
                // The number of columns of boxes is the number of rows within a box and vice versa
                for (int row = 0; row < boxColumns; row++) { // This is the row within the box
                    for (int column = 0; column < boxRows; column++) {
                        // row * boxRows + column is the index - adding a column means index increases by one, while
                        // adding a row means index increases by boxRows (as index increases by the number of columns
                        // within the box).
                        currentBox[row * boxRows + column] =
                                values[boxLocationRow * boxColumns + row][boxLocationColumn * boxRows + column];
                        // boxLocationRow * boxColumn is the number of rows in the boxes above this box.
                        // So the total row of the cell is boxLocationRow * boxColumn + row
                        // The corresponding logic works for columns
                    }
                }
                int[] duplicates = checkDuplicates(currentBox);
                if (duplicates.length != 0) {
                    int finalBoxLocationRow = boxLocationRow;
                    int finalBoxLocationColumn = boxLocationColumn;
                    return Arrays.stream(duplicates).mapToObj(x ->
                                    // To find the row, we add the number of previous rows from other boxes to the
                                    // row in this
                                    // box, which is found using x DIV boxRows as the index goes from left to right,
                                    // top to bottom. As x and boxRows are both integers, / carries out integer
                                    // division.
                                    // The same logic works for boxColumn, with x MOD boxRows used instead of x DIV
                                    // boxRows.
                                    new Tuple2<>(finalBoxLocationRow * boxColumns + x / boxRows,
                                            finalBoxLocationColumn * boxRows + x % boxRows))
                            .collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
