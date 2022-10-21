# Sudoku

An app designed to generate sudokus of a given difficulty, letting the user solve them within the app.
Supports multiple grid sizes and also has a separate sudoku solver functionality.

## Generator

When the app is first opened, the following menu is shown for generating a new sudoku:


<img src="screenshots/generator-menu.png" width="200">


The following are examples of sudokus of the different sizes:


<p float="left">
    <img src="screenshots/6x6.png" width="200">
    <img src="screenshots/9x9.png" width="200">
    <img src="screenshots/12x12.png" width="200">
</p>


User experience features are also provided in the form of notes (pencil marks) and colouring any errors made:

<img src="screenshots/errors-and-notes.png" width="200">

Finishing a sudoku:

<img src="screenshots/finished.png" width="200">

Submitting the finished sudoku:

<img src="screenshots/finished-submitted.png" width="200">

Pressing "new game" results in the same menu being shown as in the first screenshot. The user can also request a
new sudoku at any time before finishing by using the kebab menu:

<img src="screenshots/kebab.png" width="200">

## Solver

The sudoku solver provides a similar menu for creating a grid, but without the difficulty option:

<img src="screenshots/solver-menu.png" width="200">

The following is an example of entering a sudoku into the solver:

<img src="screenshots/unsolved-sudoku.png" width="200">

Pressing "solve":

<img src="screenshots/solved-sudoku.png" width="200">

Care is also taken to ensure that when there are no solutions, this is quickly
detected, and reported to the user:

<img src="screenshots/no-solutions.png" width="200">
