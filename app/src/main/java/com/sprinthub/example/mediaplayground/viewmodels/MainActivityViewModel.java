package com.sprinthub.example.mediaplayground.viewmodels;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sprinthub.example.mediaplayground.MediaSessionConnection;
import com.sprinthub.example.mediaplayground.models.MediaItemData;
import com.sprinthub.example.mediaplayground.utils.Event;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import timber.log.Timber;

public class MainActivityViewModel extends ViewModel {

    private MediaSessionConnection mSessionConnection;
    public LiveData<String> rootMediaId;

    private MutableLiveData<Event<String>> _navigateToMediaItem = new MutableLiveData<>();

    public MainActivityViewModel(MediaSessionConnection mediaSessionConnection) {
        mSessionConnection = mediaSessionConnection;

        rootMediaId = Transformations.map(mSessionConnection.mIsConnected,
                isConnected -> {
                    if (isConnected) {
                        return mSessionConnection.getRootMediaId();
                    } else {
                        return null;
                    }
                });
    }

    public LiveData<Event<String>> navigateToMediaItem() {
        return _navigateToMediaItem;
    }

    public void mediaItemClicked(MediaItemData clickedItem) {
        if (clickedItem.browsable) {
            browseToItem(clickedItem);
        } else {
            playMedia(clickedItem);
        }
    }

    private void browseToItem(MediaItemData clickedItem) {
        _navigateToMediaItem.setValue(new Event<>(clickedItem.mediaId));
    }

    /**
     * This method takes a [MediaItemData] and does one of the following:
     * - If the item is *not* the active item, then play it directly.
     * - If the item *is* the active item, check whether "pause" is a permitted command. If it is,
     * then pause playback, otherwise send "play" to resume playback.
     */
    private void playMedia(MediaItemData clickedItem) {
        MediaMetadataCompat nowPlaying = mSessionConnection.nowPlaying.getValue();
        MediaControllerCompat.TransportControls transportControls = mSessionConnection.getTransportControls();

        PlaybackStateCompat state = mSessionConnection.playbackState.getValue();
        boolean isPrepared = false;
        boolean isPlaying = false;
        boolean isPlayEnabled = false;
        if (state != null) {
            isPrepared = (state.getState() == PlaybackStateCompat.STATE_BUFFERING) ||
                    (state.getState() == PlaybackStateCompat.STATE_PLAYING) ||
                    (state.getState() == PlaybackStateCompat.STATE_PAUSED);
            isPlaying = (state.getState() == PlaybackStateCompat.STATE_BUFFERING) ||
                    (state.getState() == PlaybackStateCompat.STATE_PLAYING);
            isPlayEnabled = ((state.getActions() & PlaybackStateCompat.ACTION_PLAY) != 0L) ||
                    (((state.getActions() & PlaybackStateCompat.ACTION_PLAY_PAUSE) != 0L) &&
                            (state.getState() == PlaybackStateCompat.STATE_PAUSED));
        }
        if (isPrepared && nowPlaying != null && clickedItem.mediaId.equals(nowPlaying.getDescription().getMediaId())) {
            if (isPlaying) transportControls.pause();
            else if (isPlayEnabled) transportControls.play();
            else {
                Timber.w("Playable item clicked but neither play nor pause are enabled!\n(mediaId=%s)", clickedItem.mediaId);
            }
        } else {
            transportControls.playFromMediaId(clickedItem.mediaId, null);
        }
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private MediaSessionConnection mSessionConnection;

        public Factory(MediaSessionConnection mediaSessionConnection) {
            mSessionConnection = mediaSessionConnection;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainActivityViewModel(mSessionConnection);
        }
    }
}
