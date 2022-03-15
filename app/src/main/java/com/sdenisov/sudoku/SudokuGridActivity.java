package com.sdenisov.sudoku;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SudokuGridActivity extends AppCompatActivity {

    // Declares all class variables
    private int boxRows;
    private int boxColumns;
    private int rows;
    private int difficulty;

    // All constants are declared here for easy modification
    private static final String BACKSPACE_BUTTON_TEXT = "X";
    // Labels used for intent extras. It is important that they are unique within the application and even within
    // Android - to make sure this is the case, I used the package name (com.sdenisov.sudoku) in the label
    private static final String INTENT_IS_GENERATOR_LABEL = "com.sdenisov.sudoku.SudokuGridActivity.isGenerator";

    // Constants used for keys in SharedPreferences
    private static final String DIFFICULTY_KEY = "com.sdenisov.sudoku.SudokuGridActivity.dialogue.difficulty";
    private static final String GENERATOR_GRID_SIZE_KEY =
            "com.sdenisov.sudoku.SudokuGridActivity.dialogue.gridSize.generator";

    private SudokuCellView selectedCell;
    private SudokuData sudokuData;
    private final List<SudokuCellView> cells = new ArrayList<>();
    private SudokuSaver sudokuSaver;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_grid);
        newGame(true);
    }


    // firstGame determines whether this is the first game since the app was opened - used to decide whether to load
    // a sudoku or generate a new one
    private void newGame(boolean firstGame) {
        Intent intent = getIntent(); // Gets the intent that started this activity to get extras from the intent
        // difficulty is zero or less for solver (I'll use -1)
        difficulty = intent.getBooleanExtra(INTENT_IS_GENERATOR_LABEL, true) ? 1 : -1;

        // Note that these two methods can only be called after we find out whether this is a generator or solver
        setUpButtons();
        setUpBottomNavigationView();

        // Gets the shared preferences and assigns them to the sharedPref attribute
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        // The sudokuSaver is instantiated as soon as we know whether this grid is a generator or solver
        sudokuSaver = new SudokuSaver(sharedPref, difficulty > 0);

        // Creates an "options" View based on the dialog_play XML file
        View options = getLayoutInflater().inflate(R.layout.dialog_options, null);

        // If is a solver (difficulty <= 0) then the grid size selected by default is 9x9.
        // If it is a generator then if possible it gets the previous grid size from SharedPreferences and sets it as
        // the default. If not then it sets the default grid size to 9x9.
        // defaultGridSize is the id of the appropriate RadioButton
        int defaultGridSize = difficulty > 0
            ? sharedPref.getInt(GENERATOR_GRID_SIZE_KEY, R.id.size9) : R.id.size9;
        ((RadioButton) options.findViewById(defaultGridSize)).setChecked(true);

        // Sets the previous difficulty as the default difficulty (if this is a solver, this simply wouldn't be visible)
        // If not possible sets it to "Easy".
        int previousDifficulty = sharedPref.getInt(DIFFICULTY_KEY, R.id.difficulty_easy);
        ((RadioButton) options.findViewById(previousDifficulty)).setChecked(true);

        // The option for setting the difficulty in the solver is removed by hiding the view
        if (difficulty <= 0) options.findViewById(R.id.option_difficulty).setVisibility(View.GONE);

        // A sudoku can only be loaded when the app is opened (i.e. firstGame is true) as at other times, the user has
        // requested a new sudoku so wouldn't want for their current one to be loaded
        if (firstGame && sudokuSaver.loadSudoku() != null) { // If a sudoku can be loaded ...
            sudokuData = sudokuSaver.loadSudoku(); // Loads a sudoku
            // Sets boxRows, boxColumns and rows attributes of this class (SudokuGridActivity) based on the attributes
            // of sudokuData
            boxRows = sudokuData.getBoxRows();
            boxColumns = sudokuData.getBoxColumns();
            rows = boxRows * boxColumns;
            createGrid(); // Creates a grid of the correct size
            createDigitButtons(); // Creates the digit buttons at the bottom of the grid, including the "X" button

            for (int i = 0; i < cells.size(); i++) { // Iterates through each SudokuCellView in order
                // If statement means only calls updateCellNotes if the cell has notes, to make the code more efficient
                if (sudokuData.getValue(i).hasNotes()) {
                    // Shows the notes to the user
                    updateCellNotes(cells.get(i), sudokuData.getValue(i).notes, true);
                }
            }
            updateGrid(); // Updates cells in the grid to show values from sudokuData (rather than just notes)
            ProgressBar generatorProgress = findViewById(R.id.generator_progress);
            generatorProgress.setVisibility(View.GONE); // The sudoku has now been loaded so the progress bar is removed
        } // If a sudoku can't be loaded then a dialogue is shown allowing the user to select the size (and the
        // difficulty if this is a generator). Once submitted, the generator generates and shows a sudoku while the
        // solver shows a blank grid (of the correct size)
        else {
            // Dialog is shown when the activity is first started by choosing it from the menu. It is shown in both the
            // generator and solver. To create a grid of a different size, the user can use the BottomNavigationMenu to
            // start another activity then use the dialog to select the new desired size.
            new AlertDialog.Builder(this).setTitle("Options")
                    // sets the view for the dialog - this is positioned between the title and the submit button
                    .setView(options)
                    .setPositiveButton("Submit", (dialog, id) -> newGameDialogOnSubmit((Dialog) dialog, options))
                    // If this is the first game then can't be cancelled as if it is cancelled then the user would end
                    // up without a grid
                    .setCancelable(!firstGame)
                    .show();
        }
    }

    private void newGameDialogOnSubmit(Dialog dialog, View options) {
            // Resets all the variables to make sure the game is truly restarted
            selectedCell = null;
            sudokuData = null;
            cells.clear();

            // Resets the layout so that a new grid can be created
            setContentView(R.layout.activity_sudoku_grid);

            // The layout has been reset so the buttons and the BottomNavigationView have to be set up again
            setUpButtons();
            setUpBottomNavigationView();

            SharedPreferences.Editor editor = sharedPref.edit(); // Gets the editor from sharedPref

            RadioGroup size = options.findViewById(R.id.option_size);

            ProgressBar generatorProgress = findViewById(R.id.generator_progress);
            generatorProgress.setVisibility(View.VISIBLE);

            // Checks which radio box was selected by checking its id
            if (size.getCheckedRadioButtonId() == R.id.size6) {
                // A 6x6 grid has 3 rows of boxes and 2 columns of boxes
                boxRows = 3;
                boxColumns = 2;
            } else if (size.getCheckedRadioButtonId() == R.id.size9) {
                boxRows = 3;
                boxColumns = 3;
            } else {
                boxRows = 4;
                boxColumns = 3;
            }
            if (difficulty > 0) { // This only runs for the sudoku generator
                // If this is a generator sudoku then saves the selected grid size
                editor.putInt(GENERATOR_GRID_SIZE_KEY, size.getCheckedRadioButtonId());

                RadioGroup difficultyOptions = options.findViewById(R.id.option_difficulty);
                // Checks which radio button was selected and sets the difficulty to the corresponding value:
                if (difficultyOptions.getCheckedRadioButtonId() == R.id.difficulty_easy) {
                    difficulty = 1;
                } else if (difficultyOptions.getCheckedRadioButtonId() == R.id.difficulty_medium) {
                    difficulty = 2;
                } else if (difficultyOptions.getCheckedRadioButtonId() == R.id.difficulty_hard) {
                    difficulty = 3;
                } else {
                    difficulty = 4;
                }
                // Saves the selected difficulty by saving the id of the selected radiobutton (as ids are
                // not changed when the app is restarted, even if the phone is switched off).
                editor.putInt(DIFFICULTY_KEY, difficultyOptions.getCheckedRadioButtonId());
            }
            editor.apply(); // Saves the changes to sharedPref

            // It is important that these lines use the correct boxRows and boxColumns values, so these lines
            // are placed after boxRows and boxColumns have been set up
            rows = boxRows * boxColumns;
            sudokuData = new SudokuData(boxRows, boxColumns);
            createGrid();
            createDigitButtons();
            updateGrid();

            // If this is a generator, then the lines below are run so that a sudoku is generated as soon
            // as the user opens the activity
            if (difficulty > 0) {
                // Shows progress bar to the user so that they can see the sudoku is being loaded and the app
                // didn't just freeze
                generatorProgress.setVisibility(View.VISIBLE);
                dialog.dismiss(); // Closes the dialog so that the progress bar is shown
                findViewById(R.id.table_grid).post(() -> { // Makes sure the lines below are executed only
                         // after all the other lines here (i.e. in the lambda in setPositiveButton).
                         // This means that the user will be shown an empty grid with a loading sign while
                         // the sudoku is being generated, allowing them to see that their request is being
                         // processed
                    // Generates the sudoku
                    sudokuData = SudokuGenerator.generate(difficulty, boxRows, boxColumns);
                    updateGrid(); // Fills the grid with the generated sudoku
                    generatorProgress.setVisibility(View.GONE); // Makes the progress bar invisible
                    // Saves the sudoku so that it is loaded again if the app is restarted
                    sudokuSaver.saveSudoku(sudokuData);
                });
            } else {
                // The progress bar is hidden in the solver
                generatorProgress.setVisibility(View.GONE);
            }
            // The sudoku is saved after it is generated so that it is loaded again if the user reopens
            // the app (this is for both the generator or solver)
            sudokuSaver.saveSudoku(sudokuData);
    }

    private void setUpBottomNavigationView() {
        BottomNavigationView navigation = findViewById(R.id.navigation);

        if (difficulty <= 0) {
            // Temporarily removes the onNavigationIteSelectedListener, so that a new activity is not started
            navigation.setOnNavigationItemSelectedListener(null);
            // As this activity is a solver, makes sure the solver item is selected in the BottomNavigationView
            navigation.setSelectedItemId(R.id.action_solve);
        }

        navigation.setOnNavigationItemSelectedListener(item -> {
            Intent intent2 = new Intent(this, SudokuGridActivity.class);
            if (item.getItemId() == R.id.action_play) { // Checks which item is selected by checking the item id
                intent2.putExtra(INTENT_IS_GENERATOR_LABEL, true); // So that a generator is created
            } else if (item.getItemId() == R.id.action_solve) {
                intent2.putExtra(INTENT_IS_GENERATOR_LABEL, false); // So that a solver is created
            }
            startActivity(intent2);
            // The selected item is not highlighted - it starts a new activity so the highlighting wouldn't be visible
            // anyway (and would be out of date if the user returns to this activity e.g. by pressing the back button)
            return false;
        });
    }

    // Makes sure only the necessary buttons are displayed: submit and notes for generator, solve and clear for solver
    private void setUpButtons() {
        Button submitButton = findViewById(R.id.button_submit);
        if (difficulty > 0) {
            submitButton.setText(getText(R.string.submit));
            // Button is no longer shown to the user so it is as if it's not there
            findViewById(R.id.button_clear).setVisibility(View.INVISIBLE);
        } else {
            submitButton.setText(getText(R.string.solve));
            findViewById(R.id.toggle_notes).setVisibility(View.INVISIBLE);
        }
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
            if (((Button) findViewById(R.id.button_submit)).getText().equals("Unsolve") ||
                    difficulty > 0 && cellData.isInitialValue()) {
                // Stops execution here to prevent the value in the selected cell from being modified.
                // In the solver, this happens if the grid is filled in (due to the solve button being clicked), so the
                //   user will have to click "Unsolve" to modify any values
                // In the generator, this happens for initial values
                return;
            }
            Button digit = (Button) view;
            ToggleButton noteMode = findViewById(R.id.toggle_notes);
            if (noteMode.isChecked()) { // If note mode is on
                if (digit.getText() != BACKSPACE_BUTTON_TEXT) { // The backspace button is ignored in note mode
                    // Finds the value chosen by converting the button's text to an integer
                    int valueChosen = Integer.parseInt(String.valueOf(digit.getText()));
                    // Toggles the value of the corresponding note by flipping its boolean value.
                    // Note that the index is valueChosen - 1 as an index of 0 corresponds to note number 1.
                    cellData.notes[valueChosen - 1] = !cellData.notes[valueChosen - 1];
                    cellData.setValue(null); // Removes the value as notes cannot coexist with a value
                    updateCellNotes(selectedCell, cellData.notes, false); // Displays the changes to the user
                }
            } else {
                // Sets text autoscaling - if autoscaling has been removed by updateCellNotes then this undoes that
                // change
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(selectedCell, 1, 400,
                        1, TypedValue.COMPLEX_UNIT_DIP);
                // Checks if the digit is the backspace digit by checking its text
                // Removes all notes as notes cannot exist together with a
                if (digit.getText() == BACKSPACE_BUTTON_TEXT) {
                    selectedCell.setText("");
                    cellData.setValue(null);
                    // This cell is empty so is now allowed to be modified so is no longer initial
                    cellData.setInitialValue(false);
                } else {
                    selectedCell.setText(digit.getText());
                    // Note that the value of the button's text can be converted to an integer, as the only button
                    // where this is not allowed is the backspace button, and we know that this isn't the backspace
                    // button.
                    cellData.setValue(Integer.parseInt(String.valueOf(digit.getText())));
                    // This is a value entered by the player, so is initial for the solver and not initial for the
                    // generator
                    cellData.setInitialValue(difficulty <= 0);
                }
                // Removes all notes as notes cannot coexist with a value
                Arrays.fill(cellData.notes, false);
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
        // Saves the sudoku so that any changes made by the user are automatically saved
        sudokuSaver.saveSudoku(sudokuData);
    }

    private void selectCell(View cell) {
        // Removes border around old selected cell
        SudokuCellView sudokuCellView = (SudokuCellView) cell;
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
        selectedCell = sudokuCellView;
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
                            continue columnLoop;
                        }
                    }
                    cell.setText(""); // If a cell's value is null, its text is removed
                }
            }
        }
    }

    private void updateCellNotes(SudokuCellView cell, boolean[] notes, boolean loadingSudoku) {
        // loadingSudoku is true if this is notes from initially loading the sudoku and is false if these are notes
        // that the user is adding right now
        if (!loadingSudoku) {
            // This line removes the text auto sizing that was set by another line
            // When adding notes after generating a sudoku, this line is required for notes to be displayed correctly
            // so is run. When adding notes after loading a sudoku, notes are still displayed correctly with this line
            // so there's no harm running it.
            // When loading a sudoku, this line results in notes not being displayed (for some reason) but if this line
            // is not run then notes are displayed correctly, so it is not run.
            TextViewCompat.setAutoSizeTextTypeWithDefaults(cell, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
        }
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
        Log.d("project", result.toString());
        cell.setText(result);
        cell.setTextColor(Color.GRAY);
    }

    public void solveSudoku(View view) {
        Button button = (Button) view;
        // A string resource is used for "Solve" and "Unsolve" text, so that the text can be modified easily
        if (button.getText().equals(getText(R.string.submit))) {
            if (sudokuData.findErrors().size() != 0) {
                new AlertDialog.Builder(this).setTitle("Invalid")
                        .setMessage("Your sudoku contains an error").show();
            } else if (sudokuData.containsEmptyCells()) {
                new AlertDialog.Builder(this).setTitle("Invalid")
                        .setMessage("You haven't finished the sudoku").show();
            } else {
                new AlertDialog.Builder(this).setTitle("Congratulations")
                        .setMessage("You have successfully solved the sudoku")
                        .setPositiveButton("New game", (dialog, id) -> newGame(false))
                        .setNegativeButton("Continue", (dialog, id) -> {})
                        .show();
            }
        } else if (button.getText().equals(getText(R.string.solve))) {
            long before = System.nanoTime();
            int difficulty = SudokuSolver.solve(sudokuData, 1); // Modifies sudokuData object to solve sudoku
            // The time taken by the solver is found by recording the system time before and after and finding the
            // difference. It is also divided by a billion to convert from nanoseconds to seconds.
            Log.d("project", String.valueOf((double) (System.nanoTime() - before) / 1_000_000_000));
            Log.d("project", String.valueOf(difficulty));
            if (difficulty != -1) {
                // If a solution exists then the button is set to "Unsolve" to allow the user to easily remove all
                // the filled values
                button.setText(R.string.unsolve);
            } else {
                // If there are no solutions then a dialogue is shown, explaining this to the user.
                new AlertDialog.Builder(this).setTitle("Error")
                        .setMessage("This sudoku has no solutions").show();
                // Note that the button is not set to "Unsolve" as the solver hasn't filled in any values, so the user
                // can modify the input sudoku immediately after closing the dialogue, without having to click "unsolve"
            }
        } else if (button.getText().equals(getText(R.string.unsolve))) {
            SudokuSolver.unsolve(sudokuData); // Removes all values from solving - i.e. values where playerInput is false
            button.setText(R.string.solve);
        }
        updateGrid(); // Updates grid based on new sudokuData object
    }

    // Clears all cells. Can only run in the solver (not in the generator)
    public void clear(View view) {
        for (int row = 0; row < sudokuData.getRows(); row++) {
            for (int column = 0; column < sudokuData.getRows(); column++) {
                SudokuData.SudokuCell cell = sudokuData.getValue(row, column);
                // Empties each cell by setting its value to null
                cell.setValue(null);
                // This cell is empty so is now allowed to be modified so is no longer initial
                cell.setInitialValue(false);
                // Removes all notes from the cell (as notes are added by the solver to aid finding the solution)
                Arrays.fill(cell.notes, false);
            }
        }
        updateGrid();
        // Sets the submit button's text to "solve", as "unsolve" is only used for a filled grid.
        ((Button) findViewById(R.id.button_submit)).setText(getText(R.string.solve));

        // Saves the cleared sudoku so that the user can start fresh if they close the app now then open it again.
        sudokuSaver.saveSudoku(sudokuData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Is called automatically when the activity is started
        getMenuInflater().inflate(R.menu.menu, menu); // Loads the res/menu/menu.xml file
        return super.onCreateOptionsMenu(menu);
    }

    // Automatically called when one of the items from the menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Currently the only item but adding code to check id in case I choose to add more items in the future
        if (item.getItemId() == R.id.new_sudoku) {
            newGame(false); // Starts a new game, showing a dialogue to the user
        }
        return true;
    }

    public void fillInWorldsHardestSudoku() {
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
        cell = sudokuData.getValue(7, 2 + 1);
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
}