package com.sdenisov.sudoku;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// This activity is loaded when the app is loaded. Currently, it immediately loads the SudokuGridActivity, but I
// plan to make this a menu instead, which allows the user to select what to do (e.g. whether to generate or solve
// a sudoku and grid size).
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, SudokuGridActivity.class);
        startActivity(intent);
    }
}