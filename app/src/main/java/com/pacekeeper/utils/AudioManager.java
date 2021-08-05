package com.pacekeeper.utils;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

public class AudioManager {
    private static AudioManager instance;
    private MediaPlayer mediaPlayer;

    private AudioManager() {
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void playTrack(Context context, int track) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        } else {
            mediaPlayer = MediaPlayer.create(context, track);
        }
        try {
            mediaPlayer.start();
        } catch (IllegalStateException ignored) {
        }
    }

    public void stopPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }
}
