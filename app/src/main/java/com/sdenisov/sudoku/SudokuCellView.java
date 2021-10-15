package com.sdenisov.sudoku;

import android.content.Context;

// This class is equivalent to a TextView, but with the addition of row and column attributes,
// which are useful as they allow quickly finding the corresponding SudokuCell in the SudokuData object.

public class SudokuCellView extends androidx.appcompat.widget.AppCompatTextView {
    public int row;
    public int column;

    public SudokuCellView(Context context, int row, int column) {
        super(context);
        this.row = row;
        this.column = column;
    }
}
