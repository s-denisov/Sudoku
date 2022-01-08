package com.sdenisov.sudoku;

import java.util.ArrayList;
import java.util.List;

public class SudokuGenerator {
    public static SudokuData generate(int requiredDifficulty, int boxRows, int boxColumns) {
        outerLoop:
        while (true) {
            int iterations = 0;
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

            // cellsWithValues and cellsWithoutValues contain indexes of cells
            List<Integer> cellsWithValues = new ArrayList<>();
            for (int i = 0; i < sudoku.getRows() * sudoku.getRows(); i++) {
                // Initially, all cells have values so all indexes are added to cellsWithValues
                cellsWithValues.add(i);
            }
            // Initially there are no cells without values, so this list is initially empty
            List<Integer> cellsWithoutValues = new ArrayList<>();

            while (true) {
                iterations++;
                // If there's a large number of iterations then this sudoku has been worked on for a while so the
                // algorithm is likely "stuck" and is therefore likely to benefit from a restart.
                if (iterations > 1000) continue outerLoop; // Goes back to the start of the outer while loop
                // This while loop is used to add or remove a random cell, based on the value of removeValue
                if (removeValue) {
                    // Randomly chooses the index of what item to select from cellsWithValues
                    int indexOfCellIndex = randomInt(0, cellsWithValues.size() - 1);
                    int cellIndex = cellsWithValues.get(indexOfCellIndex);
                    // The value is removed from this cell, so it is now without a value, so it is removed from the
                    // cellsWithValues list but added to cellsWithoutValues
                    cellsWithValues.remove(indexOfCellIndex); // removes item whose index is indexOfCellIndex (its value is
                    // cellIndex)
                    cellsWithoutValues.add(cellIndex);
                    // Gets the cell with that index from `sudoku`, where the indexes start at 0 and go from left to right
                    // then top to bottom
                    SudokuData.SudokuCell cell = sudoku.getValue(cellIndex / sudoku.getRows(),
                            cellIndex % sudoku.getRows());
                    // The value is removed by setting it to null
                    cell.setValue(null);
                    // initialValue is set to false for empty cells so that they can be modified by the solver
                    cell.setInitialValue(false);
                } else {
                    // Randomly chooses the index of what item to select from cellsWithoutValues
                    int indexOfCellIndex = randomInt(0, cellsWithoutValues.size() - 1);
                    int cellIndex = cellsWithoutValues.get(indexOfCellIndex);
                    // The cell is filled with its value from the filled grid, so it is now with a value,
                    // so it is removed from the cellsWithoutValues list and added to the cellsWithValues list.
                    cellsWithoutValues.remove(indexOfCellIndex);
                    cellsWithValues.add(cellIndex);
                    // Gets the cell with that index from `sudoku`, where the indexes start at 0 and go from left to right
                    // then top to bottom
                    SudokuData.SudokuCell cell = sudoku.getValue(cellIndex / sudoku.getRows(),
                            cellIndex % sudoku.getRows());
                    // The cell is set to the value in the corresponding cell from the filled grid
                    cell.setValue(filled.getValue(cellIndex / sudoku.getRows(),
                            cellIndex % sudoku.getRows()).getValue());
                    // initialValue is set to true for filled cells so that they cannot be modified by the solver
                    cell.setInitialValue(true);
                }
                // At first, there is a large number of iterations when only the previous code is ran, and as
                // removeValue is true and is not changed, this means that values keep getting removed until 65% are
                // removed, so in a 9x9 sudoku there are 28 left.
                if (iterations > sudoku.getRows() * sudoku.getRows() * 0.65) {
                    long timeStarted = System.currentTimeMillis();
                    int difficulty = SudokuSolver.solve(sudoku, 1);
                    SudokuData solution = sudoku.copy();
                    SudokuSolver.unsolve(sudoku);
                    // Notes are selected in the opposite order from the previous solver, so that if there are two different
                    // solutions then both are discovered
                    SudokuSolver.solve(sudoku, -1);
                    // Values are removed initially, but this ensures the number of values does not get too high again
                    // due to values being added again - that at most half the cells are filled. This prevents sudokus
                    // from becoming too easy, which is particularly important for an "Easy" requested difficulty (as
                    // then the sudoku could theoretically have only one empty cell, which is ridiculously easy).
                    if (cellsWithValues.size() > sudoku.getRows() * sudoku.getRows() / 2 || difficulty == -1) {
                        // To prevent sudokus from becoming too easy, the number of clues must be at most half the number
                        // of cells - if it is higher, then clues need to be removed so removeValue is set to true.
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
                    // Removes all non-initial values, so that the next iteration of the while loop starts with a partially
                    // empty sudoku, just like this iteration started
                    SudokuSolver.unsolve(sudoku);
                }
            }
        }
    }

    // Helper function to generate a random integer from min to max inclusive
    private static int randomInt(int min, int max) { // Inclusive
        return (int) ((Math.random() * (max - min)) + min);
    }
}
