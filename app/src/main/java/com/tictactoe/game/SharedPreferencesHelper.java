package com.tictactoe.game;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "TicTacToePrefs";
    private static final String KEY_CUPS = "cups";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_SOUNDS_ENABLED = "sounds_enabled";
    private static final String KEY_VOLUME = "volume";
    private static final String KEY_BOARD_SIZE = "board_size";

    private SharedPreferences prefs;

    public SharedPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getCups() {
        return prefs.getInt(KEY_CUPS, 0);
    }

    public void setCups(int cups) {
        prefs.edit().putInt(KEY_CUPS, cups).apply();
    }

    public void addCups(int amount) {
        int current = getCups();
        setCups(Math.max(0, current + amount));
    }

    public boolean isDarkTheme() {
        return prefs.getBoolean(KEY_DARK_THEME, false);
    }

    public void setDarkTheme(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply();
    }

    public boolean isSoundsEnabled() {
        return prefs.getBoolean(KEY_SOUNDS_ENABLED, true);
    }

    public void setSoundsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUNDS_ENABLED, enabled).apply();
    }

    public float getVolume() {
        return prefs.getFloat(KEY_VOLUME, 0.5f);
    }

    public void setVolume(float volume) {
        prefs.edit().putFloat(KEY_VOLUME, volume).apply();
    }

    public int getBoardSize() {
        return prefs.getInt(KEY_BOARD_SIZE, 3);
    }

    public void setBoardSize(int size) {
        prefs.edit().putInt(KEY_BOARD_SIZE, size).apply();
    }
}

