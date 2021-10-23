package com.sdenisov.sudoku;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
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
    // Using a constant allows the backspace button text to be modified easily.
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
        cell.setValue(8);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(1, 2);
        cell.setValue(3);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(1, 3);
        cell.setValue(6);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(2, 1);
        cell.setValue(7);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(2, 4);
        cell.setValue(9);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(2, 6);
        cell.setValue(2);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(3, 1);
        cell.setValue(5);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(3, 5);
        cell.setValue(7);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(4, 4);
        cell.setValue(4);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(4, 5);
        cell.setValue(5);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(4, 6);
        cell.setValue(7);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(5, 3);
        cell.setValue(1);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(5, 7);
        cell.setValue(3);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(6, 2);
        cell.setValue(1);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(6, 7);
        cell.setValue(6);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(6, 8);
        cell.setValue(8);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(7, 2);
        cell.setValue(8);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(7, 2+1);
        cell.setValue(5);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(7, 7);
        cell.setValue(1);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(8, 1);
        cell.setValue(9);
        cell.setInitialValue(true);
        cell = sudokuData.getValue(8, 6);
        cell.setValue(4);
        cell.setInitialValue(true);
        updateGrid();
    }

    private void createGrid() {
        TableLayout grid = findViewById(R.id.table_grid);
        // The black color shows through gaps in the children of grid, thus acting as a border.
        grid.setBackgroundColor(Color.BLACK);
        for (int row = 0; row < rows; row++) {
            TableRow tableRow = new TableRow(this);
            List<SudokuCellView> rowCells = new ArrayList<>(); // the current row's cells
            for (int column = 0; column < rows; column++) {
                SudokuCellView cell = new SudokuCellView(this, row, column);
                cell.setOnClickListener(this::selectCell); // When the cell is clicked, it is selected
                cell.setBackgroundColor(Color.WHITE);
                cell.setGravity(Gravity.CENTER); // Centers text horizontally and vertically
                // Text is in monospace so that each character has the same width so that notes have the same
                // position, regardless of whether other notes are present (i.e. regardless whether a digit or space
                // is used for them). I have decided that it is simpler to use monospace for final values as well.
                cell.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
                // Makes the text automatically resize to fill the TextView, working for grids of any size.
                // Maximum size is so large that it is effectively unlimited.
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(cell, 1, 400,
                        1, TypedValue.COMPLEX_UNIT_DIP); // from https://stackoverflow.com/a/52772600
                tableRow.addView(cell);
                rowCells.add(cell);
            }
            grid.addView(tableRow);
            // tableRow has width and height equal to MATCH_PARENT which means that the width and height are made as
            // large as possible. Weight is set to 1 to make sure all rows have the same width and height.
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT, 1));
            for (int column = 0; column < rowCells.size(); column++) {
                // The same params are set for each cell as for each row.
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT, 1);
                // A thickness of 3px is used for a thick margin, while 1px is used for a thin margin. A thin margin
                // is used around cells, while a thick margin is used around boxes.
                params.setMargins(
                        // For cells every boxRows starting from column 0, there is a box boundary to the left of them
                        column % boxRows == 0 ? 3 : 1,
                        // For cells every boxColumns starting from row 0, there is a box boundary on top of them
                        row % boxColumns == 0 ? 3 : 1,
                        // For cells every boxRows starting from column boxRows - 1, (the last column in the first box)
                        // there is a box boundary to the right
                        (column + 1) % boxRows == 0 ? 3 : 1,
                        // For cells every boxColumns starting from row boxColumns - 1, (the last row in the first box)
                        // there is a box boundary beneath them
                        (row + 1) % boxColumns == 0 ? 3 : 1
                );
                TextView cell = rowCells.get(column);
                cell.setLayoutParams(params);
                // This is the reason the parameters are only set after all cells for the row have been added - so that
                // the cell is fully placed with the width and height equal to its final width and height. As a result,
                // the max width and height are set to the current width and height, to prevent resizing, which was
                // found to be a problem during testing.
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
            // Single digit buttons are added to the first (top) container, while two digit buttons are added to the
            // second (bottom) container.
            if (i < 10) digitsContainer.addView(digit);
            else digitsContainer2.addView(digit);
        }
        // The bottom container also contains the backspace button.
        digitsContainer2.addView(createDigitButton(BACKSPACE_BUTTON_TEXT));
    }

    // Creates a digit Button and returns it, so that it can be added to the appropriate container.
    private Button createDigitButton(String text) {
        Button digit = new Button(this);
        digit.setText(text);
        // Button wraps text so is made as small as possible. Weight set to 1 so that all buttons within the same
        // layout have the same size.
        digit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        digit.setOnClickListener(this::setCellValue);
        return digit;
    }

    private void setCellValue(View view) {
        if (selectedCell != null) {
            SudokuData.SudokuCell cellData = sudokuData.getValue(selectedCell.row, selectedCell.column);
            Button digit = (Button) view;
            ToggleButton noteMode = findViewById(R.id.toggle_notes);
            if (noteMode.isChecked()) { // If note mode is on
                if (digit.getText() != BACKSPACE_BUTTON_TEXT) { // The backspace button is ignored in note mode
                    // Finds the value chosen by converting the button's text to an integer
                    int valueChosen = Integer.parseInt(String.valueOf(digit.getText()));
                    // Toggles the value of the corresponding note by flipping its boolean value.
                    // Note that the index is valueChosen - 1 as an index of 0 corresponds to note number 1.
                    cellData.notes[valueChosen - 1] = !cellData.notes[valueChosen - 1];
                    updateCellNotes(selectedCell, cellData.notes); // Displays the changes to the user
                }
            } else {
                // Sets text autoscaling - if autoscaling has been removed by updateCellNotes then this undoes that
                // change
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(selectedCell, 1, 400,
                        1, TypedValue.COMPLEX_UNIT_DIP);
                // Checks if the digit is the backspace digit by checking its text
                if (digit.getText() == BACKSPACE_BUTTON_TEXT) {
                    selectedCell.setText("");
                    cellData.setValue(null);
                    cellData.setInitialValue(false); // This cell is empty so is now allowed to be modified by the solver
                } else {
                    selectedCell.setText(digit.getText());
                    // Note that the value of the button's text can be converted to an integer, as the only button
                    // where this is not allowed is the backspace button, and we know that this isn't the backspace
                    // button.
                    cellData.setValue(Integer.parseInt(String.valueOf(digit.getText())));
                    cellData.setInitialValue(true); // To make sure the value is dark and isn't modified by the solver
                }
            }
        }
        List<Tuple2<Integer, Integer>> errorCoordinates = sudokuData.findErrors();
        for (SudokuCellView cell : cells) {
            // Checks if the list of error coordinates contains the coordinates of this cell ...
            if (errorCoordinates.contains(new Tuple2<>(cell.row, cell.column))) {
                // ... then the cell has an error so its color is set to the error color (red)
                cell.setTextColor(Color.RED);
            } else {
                // If the cell doesn't have an error then its color is set to its old color
                cell.setTextColor(sudokuData.getValue(cell.row, cell.column).getColor());
            }
        }
    }

    private void selectCell(View cell) {
        // Removes border around old selected cell
        if (selectedCell != null) selectedCell.setBackgroundColor(Color.WHITE);
        if (cell == selectedCell) {
            // Clicking a cell already selected unselects it (note that the previous line also runs in this case,
            // so the border is removed).
            selectedCell = null;
            return;
        }
        // https://stackoverflow.com/a/29414975
        // Draws a border around the selected cell
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.WHITE); // white background
        // The border color is the app's primary color, so that it fits thematically with the rest of the app
        border.setStroke(5, ContextCompat.getColor(this, R.color.design_default_color_primary)); // border
        cell.setBackground(border);
        selectedCell = (SudokuCellView) cell;
    }

    // Updates the grid, so that it displays all up-to-date information from SudokuData
    private void updateGrid() {
        for (int row = 0; row < rows; row++) {
            columnLoop:
            for (int column = 0; column < rows; column++) {
                SudokuData.SudokuCell cellData = sudokuData.getValue(row, column);
                SudokuCellView cell = cells.get(row * rows + column);
                if (cellData.getValue() != null) {
                    // row * rows + column is used for index - e.g. if row = 0 and column = 1 then index is rows,
                    // which is correct, as the for loop has just iterated through all columns in the first row,
                    // so the number of cells it has iterated through equals the number of columns which equals rows.

                    // Updates the text and the text color
                    cell.setText(String.valueOf(cellData.getValue()));
                    cell.setTextColor(cellData.getColor());

                    // I will consider updating the notes as well, but it isn't necessarily for now, as currently
                    // whenever any notes are changed, updateCellNotes is called (manually)
                } else {
                    for (boolean note : cellData.notes) { // Checks if there are any notes
                        // If there are notes then moves onto next cell, leaving this cell unchanged
                        if (note) {
                            updateCellNotes(cell, cellData.notes);
                            continue columnLoop;
                        }
                    }
                    cell.setText(""); // If a cell's value is null, its text is removed
                }
            }
        }
    }

    private void updateCellNotes(SudokuCellView cell, boolean[] notes) {
        // Disables text resizing because the notes need to have the same size in all cells, but resizing could
        // result in different sizes.
        TextViewCompat.setAutoSizeTextTypeWithDefaults(cell, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
        // Through trial and error, I found that dividing by boxRows + 1 gives the best results
        // As each cell has the same width, this value should be the same for each cell
        cell.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) cell.getWidth() / (boxRows + 1));

        // Uses a StringBuilder instead of just concatenating to a string, as the StringBuilder has better performance
        StringBuilder result = new StringBuilder("");
        for (int i = 1; i <= notes.length; i++) {
            result.append(notes[i - 1] ? i : " ") // If a note is present, the corresponding number is appended,
                    // otherwise a note is appended which, due to using monospace, has the same width.
                    .append(i == notes.length ? "" : // No space or newline is added after the last character
                            // A newline is usually added every boxRows, so that the grid of notes has the same width
                            // as the box. However, for 2 digit notes, a newline is added every boxRows / 2, as the
                            // notes are twice longer so the newlines are twice more frequent. If a newline is not
                            // added then a space is added instead.
                            i % (i < 10 ? boxRows : boxRows / 2) == 0 ? "\n" : " ");
        }
        cell.setText(result);
    }

    public void solveSudoku(View view) {
        Button button = (Button) view;
        // A string resource is used for "Solve" and "Unsolve" text, so that the text can be modified easily
        if (button.getText().equals(getText(R.string.solve))) {
            long before = System.nanoTime();
            SudokuSolver.unsolve(sudokuData);
            SudokuSolver.solve(sudokuData); // Modifies sudokuData object to solve sudoku
            Log.d("project", String.valueOf((double) (System.nanoTime() - before) / 1_000_000_000));
//            button.setText(R.string.unsolve);
        } else if (button.getText().equals(getText(R.string.unsolve))) {
            SudokuSolver.unsolve(sudokuData); // Removes all values from solving - i.e. values where playerInput is false
            button.setText(R.string.solve);
        }
        updateGrid(); // Updates grid based on new sudokuData object
    }
}