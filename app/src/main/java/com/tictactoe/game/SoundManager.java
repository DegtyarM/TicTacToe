package com.tictactoe.game;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private SoundPool soundPool;
    private Map<Integer, Integer> soundMap;
    private SharedPreferencesHelper prefsHelper;
    private boolean soundsEnabled;
    private float volume;

    public static final int SOUND_MOVE = 1;
    public static final int SOUND_WIN = 2;
    public static final int SOUND_SELECT = 3;
    public static final int SOUND_LOSE = 4;

    public SoundManager(Context context, SharedPreferencesHelper prefsHelper) {
        this.prefsHelper = prefsHelper;
        this.soundsEnabled = prefsHelper.isSoundsEnabled();
        this.volume = prefsHelper.getVolume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .build();
        } else {
            soundPool = new SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        soundMap = new HashMap<>();
        loadSounds(context);
    }

    private void loadSounds(Context context) {
        soundMap.put(SOUND_MOVE, soundPool.load(context, R.raw.move, 1));
        soundMap.put(SOUND_WIN, soundPool.load(context, R.raw.win, 1));
        soundMap.put(SOUND_SELECT, soundPool.load(context, R.raw.select, 1));
        soundMap.put(SOUND_LOSE, soundPool.load(context, R.raw.lose, 1));
    }

    public void playSound(int soundId) {
        if (soundsEnabled && soundMap.containsKey(soundId)) {
            soundPool.play(soundMap.get(soundId), volume, volume, 1, 0, 1f);
        }
    }

    public void updateSettings() {
        soundsEnabled = prefsHelper.isSoundsEnabled();
        volume = prefsHelper.getVolume();
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}

