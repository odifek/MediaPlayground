package com.sprinthub.example.mediaplayground;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import androidx.lifecycle.MutableLiveData;

public class MediaSessionConnection {

    private ComponentName mServiceComponent;

    public MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();
    public MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();
    public MutableLiveData<MediaMetadataCompat> nowPlaying = new MutableLiveData<>();

    MediaControllerCompat mMediaController;

    private MediaBrowserConnectionCallback mediaBrowserConnectionCallback;

    private MediaBrowserCompat mMediaBrowser;

    private static volatile MediaSessionConnection instance;
    public static MediaSessionConnection getInstance(ComponentName serviceComponent) {
        if (instance == null) {
            synchronized (MediaSessionConnection.class) {
                instance = new MediaSessionConnection(serviceComponent);
            }
        }
        return instance;
    }

    private MediaSessionConnection(ComponentName componentName) {
//        mContext = MediaApp.getInstance();
        mServiceComponent = componentName;

        nowPlaying.postValue(NOTHING_PLAYING);
        playbackState.postValue(EMPTY_PLAYBACK_STATE);
        mIsConnected.postValue(false);

        mediaBrowserConnectionCallback = new MediaBrowserConnectionCallback(MediaApp.getInstance());
        mMediaBrowser = new MediaBrowserCompat(MediaApp.getInstance(),
                mServiceComponent,
                mediaBrowserConnectionCallback,
                null);
        mMediaBrowser.connect();
    }

    public void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mMediaBrowser.subscribe(parentId, callback);
    }

    public void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mMediaBrowser.subscribe(parentId, callback);
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return mMediaController.getTransportControls();
    }

    public String getRootMediaId() {
        return mMediaBrowser.getRoot();
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        private Context context;
        MediaBrowserConnectionCallback(Context context) {
            this.context = context;
        }
        /**
         * Invoked after {@link MediaBrowserCompat#connect()} when the request has successfully completed
         */
        @Override
        public void onConnected() {
            // Get a MediaController for the MediaSession
            try {
                mMediaController = new MediaControllerCompat(context, mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(new MediaControllerCallback());
                mIsConnected.postValue(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        /**
         * Invoked when the client is disconnected from the media browser
         */
        @Override
        public void onConnectionSuspended() {
            mIsConnected.postValue(false);
        }

        /**
         * Invoked when the connection to the media browser failed.
         */
        @Override
        public void onConnectionFailed() {
            mIsConnected.postValue(false);
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state != null) {
                playbackState.postValue(state);
            } else {
                playbackState.postValue(EMPTY_PLAYBACK_STATE);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                nowPlaying.postValue(metadata);
            } else {
                nowPlaying.postValue(NOTHING_PLAYING);
            }
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
            // TODO: 12/5/18 Handle queue changes
        }

        @Override
        public void onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended();
        }
    }


    public static PlaybackStateCompat EMPTY_PLAYBACK_STATE =
            new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
                    .build();
    public static MediaMetadataCompat NOTHING_PLAYING =
            new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
                    .build();
}
