package com.sdenisov.sudoku;

public class SudokuGenerator {
    public static SudokuData generate(int requiredDifficulty, int boxRows, int boxColumns) {
        SudokuData sudoku = new SudokuData(boxRows, boxColumns);
        for (int row = 0; row < sudoku.getRows(); row++) {
            for (int column = 0; column < sudoku.getRows(); column++) {
                // initialValue is set to true for filled cells, so that they are not modified by the solver
                // (used later to make sure there is exactly one solution, and it is at the right difficulty level)
                // At the start, all cells are filled, so initialValue is set to true for all of them.
                sudoku.getValue(row, column).setInitialValue(true);
            }
        }
        // Creates a filled grid representing the solution by running the solver on an empty grid.
        // Random note selection is used (represented by passing 0 as the second argument)
        // to make sure a different grid is generated each time.
        SudokuSolver.solve(sudoku, 0);
        SudokuData filled = sudoku.copy(); // So that the filled grid is saved, even if sudoku is modified
        boolean removeValue = true; // If true, cells are removed, if false then cells are added
        while (true) {
            // This while loop is used to add or remove a random cell, based on the value of removeValue
            while (true) {
                int row = randomInt(1, sudoku.getRows()); // Row selected randomly
                int column = randomInt(1, sudoku.getRows()); // Column selected randomly
                SudokuData.SudokuCell cell = sudoku.getValue(row, column);
                if (removeValue) {
                    if (cell.getValue() != null) {
                        cell.setValue(null);
                        // Initial value is set to true for filled cells, so that they can be modified by the solver
                        cell.setInitialValue(false);
                        break;
                    }
                } else if (cell.getValue() == null) { // If removeValue is false and the cell is currently empty
                    // ... then it is filled with the value in the corresponding cell of the filled grid
                    cell.setValue(filled.getValue(row, column).getValue());
                    // As stated earlier, initial value is set to true for filled cells,
                    // so that they are not modified by the solver
                    cell.setInitialValue(true);
                    break;
                }
            }
            long timeStarted = System.currentTimeMillis();
            int difficulty = SudokuSolver.solve(sudoku, 1);
            SudokuData solution = sudoku.copy();
            SudokuSolver.unsolve(sudoku);
            // Notes are selected in the opposite order from the previous solver, so that if there are two different
            // solutions then both are discovered
            SudokuSolver.solve(sudoku, -1);
            if (difficulty == -1) {
                // If there are no solutions then it suggests that there are too many initial cells, as there are too few
                // options for filling the grid, so removeValue is set to true
                removeValue = true;
            } else if (!solution.allValuesEqual(sudoku)) {
                // If any of the cells are different in the two solutions, then the two solutions are different so
                // the sudoku is invalid. It suggests that there are too few initial cells, as there are too many
                // options for filling the grid, so removeValue is set to false
                removeValue = false;
            } else if (difficulty == requiredDifficulty) {
                // If the else clause is reached then there is exactly one solution, so this is a valid sudoku
                // so if the difficulty is correct then it is returned
                // Removes all non-initial values, so that the player will have to fill them in themselves
                SudokuSolver.unsolve(sudoku);
                return sudoku;
            } else {
                // Having fewer clues usually makes a sudoku more difficult so removeValue is set to true if the
                // difficulty is too low and false if it is too high
                removeValue = difficulty < requiredDifficulty;
            }
        }
    }

    // Helper function to generate a random integer from min to max inclusive
    private static int randomInt(int min, int max) { // Inclusive
        return (int) ((Math.random() * (max - min)) + min);
    }
}
