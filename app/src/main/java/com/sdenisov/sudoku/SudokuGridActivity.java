package com.sdenisov.sudoku;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import java.util.ArrayList;
import java.util.List;

public class SudokuGridActivity extends AppCompatActivity {

    private static final int boxRows = 3;
    private static final int boxColumns = 3;
    private static final int rows = boxRows * boxColumns;
    private static final String BACKSPACE_BUTTON_TEXT = "X";

    private SudokuCellView selectedCell;
    private final SudokuData sudokuData = new SudokuData(boxRows, boxColumns);
    private final List<SudokuCellView> cells = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_grid);
        createGrid();
        createDigitButtons();

        SudokuData.SudokuCell cell = sudokuData.getValue(0, 0);
        cell.setValue(1);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(2, 5);
        cell.setValue(2);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(4, 2);
        cell.setValue(6);
        cell.setInitialValue(false);
        updateGrid();
//        updateCellNotes(cells.get(0), new boolean[]{true, true, true, true, false, false, true, true, true});
    }

    private void createGrid() {
        TableLayout grid = findViewById(R.id.table_grid);
        grid.setBackgroundColor(Color.BLACK);
        for (int row = 0; row < rows; row++) {
            TableRow tableRow = new TableRow(this);
            ArrayList<SudokuCellView> rowCells = new ArrayList<>();
            for (int column = 0; column < rows; column++) {
                SudokuCellView cell = new SudokuCellView(this, row, column);
                cell.setOnClickListener(this::selectCell); // When the cell is clicked, it is selected
                cell.setBackgroundColor(Color.WHITE);
                cell.setGravity(Gravity.CENTER); // Centers text horizontally and vertically
                // Makes the text automatically resize to fill the TextView, working for grids of any size
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(cell, 1, 400,
                        1, TypedValue.COMPLEX_UNIT_DIP); // from https://stackoverflow.com/a/52772600
                tableRow.addView(cell);
                rowCells.add(cell);
            }
            grid.addView(tableRow);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT, 1));
            for (int column = 0; column < rowCells.size(); column++) {
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT, 1);
                params.setMargins(column % boxRows == 0 ? 3 : 1, row % boxColumns == 0 ? 3 : 1,
                        (column+1) % boxRows == 0 ? 3 : 1, (row + 1) % boxColumns == 0 ? 3 : 1);
                TextView cell = rowCells.get(column);
                cell.setLayoutParams(params);
                cell.setMaxWidth(cell.getWidth());
                cell.setMaxHeight(cell.getHeight());
            }
            cells.addAll(rowCells);
        }
    }

    private void createDigitButtons() {
        LinearLayout digitsContainer = findViewById(R.id.layout_digits);
        LinearLayout digitsContainer2 = findViewById(R.id.layout_digits2);
        for (int i = 1; i <= rows; i++) {
            Button digit = createDigitButton(String.valueOf(i));
            if (i < 10) digitsContainer.addView(digit);
            else digitsContainer2.addView(digit);
        }
        digitsContainer2.addView(createDigitButton(BACKSPACE_BUTTON_TEXT));
    }

    private Button createDigitButton(String text) {
        Button digit = new Button(this);
        digit.setText(text);
        digit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        digit.setOnClickListener(this::setCellValue);
        return digit;
    }

    private void setCellValue(View view) {
        if (selectedCell != null) {
            SudokuData.SudokuCell cellData = sudokuData.getValue(selectedCell.row, selectedCell.column);
            Button digit = (Button) view;
            ToggleButton toggleNotes = findViewById(R.id.toggle_notes);
            if (toggleNotes.isChecked()) {
                if (digit.getText() != BACKSPACE_BUTTON_TEXT) {
                    int valueChosen = Integer.parseInt(String.valueOf(digit.getText()));
                    cellData.notes[valueChosen - 1] = !cellData.notes[valueChosen - 1];
                    updateCellNotes(selectedCell, cellData.notes);
                }
            } else {
                if (digit.getText() == BACKSPACE_BUTTON_TEXT) {
                    selectedCell.setText("");
                    cellData.setValue(null);
                } else {
                    selectedCell.setText(digit.getText());
                    cellData.setValue(Integer.parseInt(String.valueOf(digit.getText())));
                }
            }
        }

        List<Tuple2<Integer, Integer>> errorCoordinates = sudokuData.findErrors();
        for (SudokuCellView cell : cells) {
            if (errorCoordinates.contains(new Tuple2<>(cell.row, cell.column))) {
                cell.setTextColor(Color.RED);
            } else {
                cell.setTextColor(sudokuData.getValue(cell.row, cell.column).getColor());
            }
        }
    }

    private void selectCell(View cell) {
        // Removes border around old selected cell
        if (selectedCell != null) selectedCell.setBackgroundColor(Color.WHITE);
        if (cell == selectedCell) {
            selectedCell = null;
            return;
        }
        // https://stackoverflow.com/a/29414975
        // Creates a border around the cell
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.WHITE); // white background
        border.setStroke(5, ContextCompat.getColor(this, R.color.design_default_color_primary)); // border
        cell.setBackground(border);
        selectedCell = (SudokuCellView) cell;
    }

    private void updateGrid() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < rows; column++) {
                SudokuData.SudokuCell cellData = sudokuData.getValue(row, column);
                if (cellData.getValue() != null) {
                    cells.get(row * rows + column).setText(String.valueOf(cellData.getValue()));
                    cells.get(row * rows + column).setTextColor(cellData.getColor());
                }
            }
        }
    }

    private void updateCellNotes(SudokuCellView cell, boolean[] notes) {
        StringBuilder result = new StringBuilder(" ");
        for (int i = 1; i <= notes.length; i++) {
            result.append(notes[i - 1] ? i : " ").append(i != notes.length && i % boxRows == 0 ? "\n " : " ");
        }
        cell.setText(result);
//        cell.setGravity(Gravity.START);
//        cell.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
    }
}