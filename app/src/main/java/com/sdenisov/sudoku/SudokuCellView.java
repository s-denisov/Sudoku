package com.sdenisov.sudoku;

import android.content.Context;

public class SudokuCellView extends androidx.appcompat.widget.AppCompatTextView {
    public int row;
    public int column;

    public SudokuCellView(Context context, int row, int column) {
        super(context);
        this.row = row;
        this.column = column;
    }
}
