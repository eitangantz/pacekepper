package com.pacekeeper.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pacekeeper.App;
import com.pacekeeper.model.RunningSession;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PrefUtils {
    private static PrefUtils sInstance;
    private static final String PREF_NAME = "com.pacekeeper.utils.PrefUtils";
    private static final String KEY_AUDIO_CUE = "audio cue";
    private static final String KEY_MEASURMENT = "measurement";
    private static final String KEY_AUTO_PAUSE = "auto pause";
    private static final String KEY_RUNNING_SESSIONS = "running sessions";
    private SharedPreferences preferences;
    private static Type runningSessionsType = new TypeToken<ArrayList<RunningSession>>() {
    }.getType();

    @NonNull
    private Gson mGson;

    @NonNull
    public static PrefUtils getInstance() {
        if (sInstance == null) {
            synchronized (PrefUtils.class) {
                if (sInstance == null) {
                    sInstance = new PrefUtils(App.getInstance());
                }
            }
        }

        return sInstance;
    }

    private PrefUtils(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        GsonBuilder builder = new GsonBuilder();

        mGson = builder.create();
    }

    public void setAudioCue(String audioCue) {
        preferences.edit().putString(KEY_AUDIO_CUE, audioCue).apply();
    }

    public String getAudioCue() {
        return preferences.getString(KEY_AUDIO_CUE, "Male voice");
    }

    public void setAutoPause(boolean value) {
        preferences.edit().putBoolean(KEY_AUTO_PAUSE, value).apply();
    }

    public boolean getAutoPause() {
        return preferences.getBoolean(KEY_AUTO_PAUSE, false);
    }

    public void setMeasurement(String text) {
        preferences.edit().putString(KEY_MEASURMENT, text).apply();
    }

    public String getMeasurement() {
        return preferences.getString(KEY_MEASURMENT, "Miles");
    }

    public void saveRunningSession(RunningSession runningSession) {
        List<RunningSession> runningSessions = getRunningSession();
        runningSessions.add(runningSession);
        preferences.edit().putString(KEY_RUNNING_SESSIONS, mGson.toJson(runningSessions)).apply();
    }

    public List<RunningSession> getRunningSession() {
        String sessionsString = preferences.getString(KEY_RUNNING_SESSIONS, null);
        List<RunningSession> runningSessions = mGson.fromJson(sessionsString, runningSessionsType);
        if(runningSessions == null) {
            runningSessions = new ArrayList<>();
        }
        return runningSessions;
    }
}
