package com.sprinthub.example.mediaplayground;

import android.app.Application;

import timber.log.Timber;

public class MediaApp extends Application {

    private static MediaApp instance;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        instance = this;

    }

    public static synchronized MediaApp getInstance() {
        return instance;
    }
}
