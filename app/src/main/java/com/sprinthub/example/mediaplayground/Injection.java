package com.sprinthub.example.mediaplayground;

import android.content.ComponentName;
import android.content.Context;

import com.sprinthub.example.mediaplayground.data.MusicRepository;
import com.sprinthub.example.mediaplayground.media.MusicService;
import com.sprinthub.example.mediaplayground.viewmodels.MainActivityViewModel;
import com.sprinthub.example.mediaplayground.viewmodels.MediaItemFragmentViewModel;

public class Injection {

    public static MusicRepository getMusicRepository() {
        return MusicRepository.getInstance();
    }

    public static MediaSessionConnection provideMediaSessionConnection() {
        return MediaSessionConnection.getInstance(new ComponentName(MediaApp.getInstance(), MusicService.class));
    }

    public static MainActivityViewModel.Factory provideMainActivityViewModel() {
        MediaSessionConnection mediaSessionConnection = provideMediaSessionConnection();
        return new MainActivityViewModel.Factory(mediaSessionConnection);
    }

    public static MediaItemFragmentViewModel.Factory provideMediaItemFragmentViewModel(String mediaId) {
        return new MediaItemFragmentViewModel.Factory(mediaId, provideMediaSessionConnection());
    }
}
