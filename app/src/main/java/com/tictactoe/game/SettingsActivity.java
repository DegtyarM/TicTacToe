package com.tictactoe.game;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferencesHelper prefsHelper;
    private Switch darkThemeSwitch;
    private Switch soundsSwitch;
    private SeekBar volumeSeekBar;
    private TextView volumeValueText;
    private TextView versionText;
    private SoundManager soundManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefsHelper = new SharedPreferencesHelper(this);
        soundManager = new SoundManager(this, prefsHelper);

        darkThemeSwitch = findViewById(R.id.darkThemeSwitch);
        soundsSwitch = findViewById(R.id.soundsSwitch);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeValueText = findViewById(R.id.volumeValueText);
        versionText = findViewById(R.id.versionText);
        findViewById(R.id.backButton).setOnClickListener(v -> {
            soundManager.playSound(SoundManager.SOUND_SELECT);
            finish();
        });

        darkThemeSwitch.setChecked(prefsHelper.isDarkTheme());
        soundsSwitch.setChecked(prefsHelper.isSoundsEnabled());
        volumeSeekBar.setProgress((int) (prefsHelper.getVolume() * 100));
        updateVolumeText(prefsHelper.getVolume());

        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(getString(R.string.app_version) + ": " + versionName);
        } catch (Exception e) {
            versionText.setText(getString(R.string.app_version) + ": 1.0.0");
        }

        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setDarkTheme(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        soundsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsHelper.setSoundsEnabled(isChecked);
        });

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                prefsHelper.setVolume(volume);
                updateVolumeText(volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void updateVolumeText(float volume) {
        int percentage = (int) (volume * 100);
        volumeValueText.setText(percentage + "%");
    }
}

