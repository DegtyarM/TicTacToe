package com.tictactoe.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainMenuActivity extends AppCompatActivity {
    private SharedPreferencesHelper prefsHelper;
    private TextView cupsTextView;
    private RadioGroup boardSizeGroup;
    private int selectedBoardSize = 3;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefsHelper = new SharedPreferencesHelper(this);
        soundManager = new SoundManager(this, prefsHelper);

        if (prefsHelper.isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        setContentView(R.layout.activity_main_menu);
        
        cupsTextView = findViewById(R.id.cupsTextView);
        boardSizeGroup = findViewById(R.id.boardSizeGroup);
        
        Button pvpButton = findViewById(R.id.pvpButton);
        Button pveButton = findViewById(R.id.pveButton);
        Button settingsButton = findViewById(R.id.settingsButton);

        selectedBoardSize = prefsHelper.getBoardSize();
        switch (selectedBoardSize) {
            case 3:
                ((RadioButton) findViewById(R.id.size3x3)).setChecked(true);
                break;
            case 4:
                ((RadioButton) findViewById(R.id.size4x4)).setChecked(true);
                break;
            case 5:
                ((RadioButton) findViewById(R.id.size5x5)).setChecked(true);
                break;
        }
        
        boardSizeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            soundManager.playSound(SoundManager.SOUND_SELECT);
            if (checkedId == R.id.size3x3) {
                selectedBoardSize = 3;
            } else if (checkedId == R.id.size4x4) {
                selectedBoardSize = 4;
            } else if (checkedId == R.id.size5x5) {
                selectedBoardSize = 5;
            }
            prefsHelper.setBoardSize(selectedBoardSize);
        });
        
        pvpButton.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.SOUND_SELECT);
            Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
            intent.putExtra("mode", "pvp");
            intent.putExtra("boardSize", selectedBoardSize);
            startActivity(intent);
        });
        
        pveButton.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.SOUND_SELECT);
            Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
            intent.putExtra("mode", "pve");
            intent.putExtra("boardSize", selectedBoardSize);
            startActivity(intent);
        });
        
        settingsButton.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.SOUND_SELECT);
            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        updateCupsDisplay();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateCupsDisplay();

        if (prefsHelper.isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    private void updateCupsDisplay() {
        int cups = prefsHelper.getCups();
        cupsTextView.setText(getString(R.string.cups, cups));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release();
        }
    }
}

