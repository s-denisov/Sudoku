package com.sdenisov.sudoku;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SudokuData {
    private final SudokuCell[][] values;
    private final int boxRows;
    private final int boxColumns;

    public class SudokuCell {
        private Integer value;
        private boolean initialValue = false;
        public final boolean[] notes = new boolean[getRows()];

        private SudokuCell(Integer value) {
            this.value = value;
        }

        public void setValue(Integer value) {
            if (value == null || 1 <= value && value <= getRows()) {
                this.value = value;
                Arrays.fill(notes, false);
            }
        }

        public Integer getValue() {
            for (boolean note : notes) if (note) return null;
            return value;
        }

        public boolean isInitialValue() {
            return initialValue;
        }

        public void setInitialValue(boolean initialValue) {
            this.initialValue = initialValue;
        }

        public int getColor() {
            return initialValue ? Color.BLACK : Color.GRAY;
        }
    }

    public SudokuData(int boxRows, int boxColumns) {
        int rows = boxRows * boxColumns; // This is the same as the number of columns
        values = new SudokuCell[rows][rows];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < rows; column++) {
                values[row][column] = new SudokuCell(null);
            }
        }
        this.boxRows = boxRows;
        this.boxColumns = boxColumns;
    }

    public SudokuCell getValue(int row, int column) {
        return values[row][column];
    }

    public int getRows() {
        return values.length;
    }

    private int[] checkDuplicates(SudokuCell[] items) {
        for (int i = 0; i < items.length - 1; i++) {
            SudokuCell item = items[i];
            for (int j = i + 1; j < items.length; j++) {
                if (item.getValue() != null && item.getValue().equals(items[j].getValue())) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{};
    }

    public List<Tuple2<Integer, Integer>> findErrors() {
        for (int row = 0; row < getRows(); row++) {
            int[] duplicates = checkDuplicates(values[row]);
            if (duplicates.length != 0) {
                int finalRow = row;
                return Arrays.stream(duplicates).mapToObj(column ->
                        new Tuple2<>(finalRow, column)).collect(Collectors.toList());
            }
        }
        for (int column = 0; column < getRows(); column++) {
            SudokuCell[] currentColumn = new SudokuCell[getRows()];
            for (int row = 0; row < getRows(); row++) {
                currentColumn[row] = values[row][column];
            }
            int[] duplicates = checkDuplicates(currentColumn);
            if (duplicates.length != 0) {
                int finalColumn = column;
                return Arrays.stream(duplicates).mapToObj(row ->
                                new Tuple2<>(row, finalColumn))
                        .collect(Collectors.toList());
            }
        }
        for (int boxLocationRow = 0; boxLocationRow < boxRows; boxLocationRow++) {
            for (int boxLocationColumn = 0; boxLocationColumn < boxColumns; boxLocationColumn++) {
                SudokuCell[] currentBox = new SudokuCell[getRows()];
                for (int row = 0; row < boxColumns; row++) {
                    for (int column = 0; column < boxRows; column++) {
                        currentBox[row * boxRows + column] =
                                values[boxLocationRow * boxColumns + row][boxLocationColumn * boxRows + column];
                    }
                }
                int[] duplicates = checkDuplicates(currentBox);
                if (duplicates.length != 0) {
                    int finalBoxLocationRow = boxLocationRow;
                    int finalBoxLocationColumn = boxLocationColumn;
                    return Arrays.stream(duplicates).mapToObj(x ->
                                    new Tuple2<>(finalBoxLocationRow * boxColumns + x / boxRows,
                                            finalBoxLocationColumn * boxRows + x % boxRows))
                            .collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
