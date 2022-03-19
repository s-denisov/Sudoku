package com.sdenisov.sudoku;

import android.graphics.Color;

import java.util.*;
import java.util.stream.Collectors;

public class SudokuData {
    // 2D array containing information about each cell. Each inner array represents a row
    private final SudokuCell[][] values;
    private final int boxRows; // Number of rows of boxes in the grid
    private final int boxColumns; // Number of columns of boxes in the grid

    public class SudokuCell {
        public int row;
        public int column;

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

        // Sets a value if input is valid. If the input is invalid then it is ignored.
        public void setValue(Integer value) {
            if (value == null || 1 <= value && value <= getRows()) {
                this.value = value;
            }
        }

        public Integer getValue() {
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

        // Finds color like getColor, but for cells with an error
        // The rgb color is light red from https://www.color-name.com/light-red.color
        public int getErrorColor() {
            return initialValue ? Color.RED : Color.rgb(255, 127, 127);
        }

        public boolean hasNotes() {
            for (boolean note : notes) {
                if (note) return true; // If note is true for any element of notes then has a note so return true ...
            }
            return false; // ... otherwise returns false
        }

        // Creates a copy of this SudokuCell object, so that the copy can be modified without modifying this object.
        // This method is used within the copy() method of the SudokuData class
        public SudokuCell copy() {
            SudokuCell result = new SudokuCell(value);
            // Copies each of the attributes
            result.row = row;
            result.column = column;
            result.initialValue = initialValue;
            // Notes are copied using System.arraycopy
            System.arraycopy(notes, 0, result.notes, 0, notes.length);
            return result;
        }
    }

    public SudokuData(int boxRows, int boxColumns) {
        int rows = boxRows * boxColumns; // This is the same as the number of columns
        // values is set to a new 2D array and is populated by empty cells.
        values = new SudokuCell[rows][rows];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < rows; column++) {
                SudokuCell cell = new SudokuCell(null);
                cell.row = row;
                cell.column = column;
                values[row][column] = cell;
            }
        }
        this.boxRows = boxRows;
        this.boxColumns = boxColumns;
    }

    // The user is allowed access to any cell by requesting the row and column and can then modify the cell using
    // the SudokuCell methods (there are restrictions, but they are located within the SudokuCell class) - as the
    // returned SudokuCell variable is a reference, any changes made will also occur within this class.
    // However, the user is not allowed access to the values 2D array, to prevent them from modifying the structure of
    // the array, such as changing its length.
    public SudokuCell getValue(int row, int column) {
        return values[row][column];
    }

    // Gets the current value based on a single index (going from left to right then top to bottom)
    public SudokuCell getValue(int index) {
        return getValue(index / getRows(), index % getRows());
    }

    public int getRows() {
        return values.length;
    }

    public int getBoxRows() {
        return boxRows;
    }

    public int getBoxColumns() {
        return boxColumns;
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
    public List<Tuple2<Integer, Integer>> findErrors() {
        // The list of all the errors. It is returned at the end.
        List<Tuple2<Integer, Integer>> result = new ArrayList<>();

        for (int row = 0; row < getRows(); row++) {
            int[] duplicates = checkDuplicates(values[row]);

            // Detects row duplicates
            if (duplicates.length != 0) {
                // Creating another variable that is equal to the current value of row but doesn't change, as values
                // used in lambda expressions are not allowed to change.
                int finalRow = row;
                // The mapToObj method applies the inputted function to each item in the array, creating a new list
                // out of the result (note that .collect is used to specify the output should be a list).
                result.addAll(Arrays.stream(duplicates).mapToObj(column ->
                        new Tuple2<>(finalRow, column)).collect(Collectors.toList()));
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
                result.addAll(Arrays.stream(duplicates).mapToObj(row ->
                                new Tuple2<>(row, finalColumn)).collect(Collectors.toList()));
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
                    result.addAll(Arrays.stream(duplicates).mapToObj(x ->
                                    // To find the row, we add the number of previous rows from other boxes to the
                                    // row in this
                                    // box, which is found using x DIV boxRows as the index goes from left to right,
                                    // top to bottom. As x and boxRows are both integers, / carries out integer
                                    // division.
                                    // The same logic works for boxColumn, with x MOD boxRows used instead of x DIV
                                    // boxRows.
                                    new Tuple2<>(finalBoxLocationRow * boxColumns + x / boxRows,
                                            finalBoxLocationColumn * boxRows + x % boxRows))
                            .collect(Collectors.toList()));
                }
            }
        }
        return result;
    }

    // A group is a row, column or box - this function doesn't distinguish them. This function takes a row or column and
    // finds all cells which share a group with the cell at that row or column, excluding that cell itself.
    // A set is used because we do not care about the order of the result.
    public Set<SudokuCell> findGroups(int cellRow, int cellColumn) {
        Set<SudokuCell> result = new HashSet<>(); // A HashSet is an implementation of the Set interface
        for (int row = 0; row < getRows(); row++) {
            for (int column = 0; column < getRows(); column++) {
                // To make sure this isn't the same cell as the input cell
                if (row != cellRow || column != cellColumn) {
                    // First condition - same row, second - same column, third - same box. If any of these conditions
                    // are true, the cell is added to the result.
                    // The row of the box is found by dividing row by boxColumns (i.e. the number of rows in a box) and
                    // ignoring the remainder. The column of the box is found by dividing column by boxRows (i.e. the
                    // number of columns in a box). If the row and column of the box are equal then the cells are in
                    // the same box.
                    if (row == cellRow || column == cellColumn ||
                            (row / boxColumns == cellRow / boxColumns && column / boxRows == cellColumn / boxRows)) {
                        result.add(getValue(row, column));
                    }
                }
            }
        }
        return result;
    }

    // This returns a set of groups, with each group being a set of cells
    public Set<Set<SudokuCell>> findAllGroups() {
        Set<Set<SudokuCell>> result = new HashSet<>();
        // Iterates through each row and adds the row as a group
        for (int row = 0; row < getRows(); row++) {
            Set<SudokuCell> currentRow = new HashSet<>();
            // The row is populated by iterating through each column and adding the appropriate cell from `values`
            for (int column = 0; column < getRows(); column++) {
                currentRow.add(values[row][column]);
            }
            result.add(currentRow);
        }
        // Iterates through each column and adds the column as a group
        for (int column = 0; column < getRows(); column++) {
            Set<SudokuCell> currentColumn = new HashSet<>();
            // The column is populated by iterating through each column and adding the appropriate cell from `values`
            for (int row = 0; row < getRows(); row++) {
                currentColumn.add(values[row][column]);
            }
            result.add(currentColumn);
        }
        // Iterates through each box and adds the box as a group. This is done by iterating through each boxRow and  boxColumn
        for (int boxLocationRow = 0; boxLocationRow < boxRows; boxLocationRow++) {
            for (int boxLocationColumn = 0; boxLocationColumn < boxColumns; boxLocationColumn++) {
                // currentBox contains all cells within the box
                Set<SudokuCell> currentBox = new HashSet<>();
                // The number of columns of boxes is the number of rows within a box and vice versa
                for (int row = 0; row < boxColumns; row++) { // This is the row within the box
                    for (int column = 0; column < boxRows; column++) {
                        // boxLocationRow * boxColumn is the number of rows in the boxes above this box.
                        // So the total row of the cell is boxLocationRow * boxColumn + row. The corresponding logic works for columns
                        currentBox.add(values[boxLocationRow * boxColumns + row][boxLocationColumn * boxRows + column]);
                    }
                }
                result.add(currentBox);
            }
        }
        return result;
    }

    // Returns a list of indexes whose values are different
    public List<Integer> findDifferingIndexes(SudokuData otherSudoku) {
        if (boxRows != otherSudoku.boxRows || boxColumns != otherSudoku.boxColumns) {
            // Returns null rather than an empty list so that it can be distinguished from the case where all values are
            // equal
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (int row = 0; row < getRows(); row++) {
            for (int column = 0; column < getRows(); column++) {
                if (!Objects.equals(values[row][column].getValue(), otherSudoku.values[row][column].getValue())) {
                    // If the two values are not equal then converts the row-column combination to the index of the cell
                    // and adds to the list
                    result.add(row * getRows() + column);
                }
            }
        }
        return result;
    }

    public boolean containsEmptyCells() {
        for (int row = 0; row < getRows(); row++) {
            for (int column = 0; column < getRows(); column++) {
                if (values[row][column].getValue() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    // Removes all notes from all cells
    public void clearNotes() {
        for (int row = 0; row < getRows(); row++) {
            for (int column = 0; column < getRows(); column++) {
                // Sets all values of the notes array for this cell to false.
                Arrays.fill(values[row][column].notes, false);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("-\n");
        for (SudokuCell[] row : values) {
            for (SudokuCell value : row) {
                result.append(value.value == null ? "---" :
                        value.isInitialValue() ? " " + value.value + " " : "(" + value.value + ")").append(" ");
            }
            result.append("\n");
        }
        return result.toString();
    }

    // Creates a copy of this SudokuData object, so that the copy can be modified without modifying this object
    public SudokuData copy() {
        // boxRows and boxColumns are copied by passing them to the constructor
        SudokuData result = new SudokuData(boxRows, boxColumns);
        // Cells are copied by iterating through each row and column then copying the cell
        for (int row = 0; row < getRows(); row++) {
            for (int column = 0; column < getRows(); column++) {
                result.values[row][column] = values[row][column].copy();
            }
        }
        return result;
    }
}
