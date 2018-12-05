package com.sprinthub.example.mediaplayground.media;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sprinthub.example.mediaplayground.R;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;

/**
 * Helper class to encapsulate code for building notifications
 */
public class NotificationBuilder {

    private static final String NOW_PLAYING_CHANNEL = "com.sprinthub.example.mediaplayground.media.NOW_PLAYING";
    static final int NOW_PLAYING_NOTIFICATION = 0xb449;

    private Context mContext;
    private NotificationManager platformNotificationManager;

    private NotificationCompat.Action skipToPreviousAction;

    private NotificationCompat.Action playAction;
    private NotificationCompat.Action pauseAction;
    private NotificationCompat.Action skipToNextAction;
    private PendingIntent stopPendingIntent;
    

    public NotificationBuilder(Context context) {
        mContext = context;
        
        platformNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        skipToPreviousAction =
                new NotificationCompat.Action(R.drawable.exo_controls_previous,
                        "Skip to previous track",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(mContext, ACTION_SKIP_TO_PREVIOUS));
        playAction = new NotificationCompat.Action(R.drawable.exo_controls_play,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(mContext, ACTION_PLAY));
        pauseAction = new NotificationCompat.Action(R.drawable.exo_controls_pause,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(mContext, ACTION_PAUSE));
        skipToNextAction = new NotificationCompat.Action(R.drawable.exo_controls_next,
                "Skip to next track",
                MediaButtonReceiver.buildMediaButtonPendingIntent(mContext, ACTION_SKIP_TO_NEXT));
        stopPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(mContext, ACTION_STOP);

    }
    
    public Notification buildNotification(MediaSessionCompat.Token token) {
        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel();
        }

        MediaControllerCompat controller;
        try {
            controller = new MediaControllerCompat(mContext, token);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
        MediaDescriptionCompat description = controller.getMetadata().getDescription();
        PlaybackStateCompat playbackState = controller.getPlaybackState();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOW_PLAYING_CHANNEL);

        // Only add actions for skip back, play/pause, skip forward, based on what is enabled
        int playPauseIndex = 0;
        if ((playbackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) !=  0L) {
            builder.addAction(skipToPreviousAction);
            ++playPauseIndex;
        }
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING ||
            playbackState.getState() == PlaybackStateCompat.STATE_BUFFERING) {
            builder.addAction(pauseAction);
        } else if (((playbackState.getActions() & PlaybackStateCompat.ACTION_PLAY) !=  0L) ||
                ((playbackState.getActions() & PlaybackStateCompat.ACTION_PLAY_PAUSE) !=  0L) &&
                        (playbackState.getState() == PlaybackStateCompat.STATE_PAUSED)) {
            builder.addAction(playAction);
        }

        if ((playbackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) !=  0L) {
            builder.addAction(skipToNextAction);
        }

        MediaStyle mediaStyle = new MediaStyle()
                .setCancelButtonIntent(stopPendingIntent)
                .setMediaSession(token)
                .setShowActionsInCompactView(playPauseIndex)
                .setShowCancelButton(true);

        return builder.setContentIntent(controller.getSessionActivity())
                .setContentText(description.getSubtitle())
                .setContentTitle(description.getTitle())
                .setDeleteIntent(stopPendingIntent)
                .setLargeIcon(description.getIconBitmap())
                .setSmallIcon(R.drawable.ic_audiotrack_dark)
                .setStyle(mediaStyle)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }

    private boolean shouldCreateNowPlayingChannel() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean nowPlayingChannelExists() {
        return platformNotificationManager.getNotificationChannel(NOW_PLAYING_CHANNEL) != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNowPlayingChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(NOW_PLAYING_CHANNEL,
                mContext.getString(R.string.notification_now_playing),
                NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setDescription("Shows what music is currently playing in MediaPlayground");
    }
}
