package com.sprinthub.example.mediaplayground.media;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;

import static com.sprinthub.example.mediaplayground.media.NotificationBuilder.NOW_PLAYING_NOTIFICATION;

public class MusicService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;
    private BecomingNoisyReceiver mNoisyReceiver;
    private NotificationManagerCompat mNotificationManager;
    private NotificationBuilder mNotificationBuilder;
//    private MusicSource mMusicSource;
    private MediaSessionConnector mMediaSessionConnector;

    private boolean mIsForegroundService = false;

    private AudioAttributes mAudioAttributes = new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build();

    private ExoPlayer mExoPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        mExoPlayer = ExoPlayerFactory.newSimpleInstance(this,
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        ((SimpleExoPlayer) mExoPlayer).setAudioAttributes(mAudioAttributes, true);

        // Build a PendingIntent that can be used to launch the ui
        Intent sessionIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0);

        // Create a new MediaSession
        mSession = new MediaSessionCompat(this, "MusicService");
        mSession.setSessionActivity(sessionActivityPendingIntent);
        mSession.setActive(true);

        setSessionToken(mSession.getSessionToken());

        mController = new MediaControllerCompat(this, mSession);
        mController.registerCallback(new MediaControllerCallback());

        mNotificationBuilder= new NotificationBuilder(this);
        mNotificationManager = NotificationManagerCompat.from(this);

        // Exoplayer will manage the MediaSession for us
        mMediaSessionConnector = new MediaSessionConnector(mSession);
        // Produces DataSource instances through which media data is loaded
        DefaultDataSourceFactory dataSourceFactory =
                new DefaultDataSourceFactory(this,
                        Util.getUserAgent(this, MEDIA_PLAYGROUND_USER_AGENT), null);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        MediaControllerCallback() {

        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            PlaybackStateCompat currentState = mController.getPlaybackState();
            if (currentState != null) {
                updateNotification(currentState);
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state != null) {
                updateNotification(state);
            }
        }

        private void updateNotification(PlaybackStateCompat state) {
            PlaybackStateCompat updatedState = state;
            if (mController.getMetadata() == null) {
                return;
            }

            // Skip building notification when state is "none"
            Notification notification;
            if (updatedState.getState() != PlaybackStateCompat.STATE_NONE) {
                notification = mNotificationBuilder.buildNotification(mSession.getSessionToken());
            } else {
                notification = null;
            }

            switch (updatedState.getState()) {
                case PlaybackStateCompat.STATE_BUFFERING:
                case PlaybackStateCompat.STATE_PLAYING: {
                    mNoisyReceiver.register();

                    if (!mIsForegroundService) {
                        startService(new Intent(getApplicationContext(), MusicService.class));
                        startForeground(NOW_PLAYING_NOTIFICATION, notification);
                        mIsForegroundService = true;
                    }
                    else if (notification != null) {
                        mNotificationManager.notify(NOW_PLAYING_NOTIFICATION, notification);
                    }
                }

                default: {
                    mNoisyReceiver.unregister();

                    if (mIsForegroundService) {
                        stopForeground(true);
                        mIsForegroundService = false;

                        // If playback has ended, also stop service
                        if (updatedState.getState() == PlaybackStateCompat.STATE_NONE) {
                            stopSelf();
                        }

                        if (notification != null) {
                            mNotificationManager.notify(NOW_PLAYING_NOTIFICATION, notification);
                        } else {
                            removeNowPlayingNotification();
                        }
                    }
                }

            }
        }

    }

    private void removeNowPlayingNotification() {
        stopForeground(true);
    }

    /**
     * Helper class to retrieve the metadata for the Exoplayer MediaSesison Connection
     * extension to call {@link MediaSessionCompat#setMetadata(MediaMetadataCompat)}.
     */

    private class MediaQueueNavigator extends TimelineQueueNavigator {

        private MediaSessionCompat mediaSession;

        private Timeline.Window window = new Timeline.Window();

        public MediaQueueNavigator(MediaSessionCompat mediaSession) {
            super(mediaSession);
            this.mediaSession = mediaSession;
        }

        @Override
        public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
            return (MediaDescriptionCompat) player.getCurrentTimeline().getWindow(windowIndex, window, true).tag;
        }
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        private Context context;
        private MediaSessionCompat.Token sessionToken;

        private IntentFilter noisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        private MediaControllerCompat controller;

        private BecomingNoisyReceiver(Context context, MediaSessionCompat.Token sessionToken) {
            this.context = context;
            this.sessionToken = sessionToken;

            try {
                controller = new MediaControllerCompat(context, this.sessionToken);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }


        private boolean registered = false;

        void register() {
            if (!registered) {
                context.registerReceiver(this, noisyIntentFilter);
                registered = true;
            }
        }

        void unregister() {
            if (registered) {
                context.unregisterReceiver(this);
                registered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.requireNonNull(intent.getAction()).equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                controller.getTransportControls().pause();
            }
        }
    }

    private static final String MEDIA_PLAYGROUND_USER_AGENT = "media_playground.next";
}
