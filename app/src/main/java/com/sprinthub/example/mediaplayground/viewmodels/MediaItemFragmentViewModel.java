package com.sprinthub.example.mediaplayground.viewmodels;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.sprinthub.example.mediaplayground.MediaSessionConnection;
import com.sprinthub.example.mediaplayground.R;
import com.sprinthub.example.mediaplayground.models.MediaItemData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MediaItemFragmentViewModel extends ViewModel {

    private String mMediaId;
    private MediaSessionConnection mSessionConnection;

    private MutableLiveData<List<MediaItemData>> mediaItems = new MutableLiveData<>();

    private MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            List<MediaItemData> mediaItemDataList = new ArrayList<>();
            for (MediaBrowserCompat.MediaItem item :
                    children) {
                mediaItemDataList.add(new MediaItemData(
                        item.getMediaId(),
                        Objects.requireNonNull(item.getDescription().getTitle()).toString(),
                        Objects.requireNonNull(item.getDescription().getSubtitle()).toString(),
                        item.getDescription().getIconUri(),
                        item.isBrowsable(),
                        getResourceForMediaId(item.getMediaId())));
            }
            mediaItems.postValue(mediaItemDataList);
        }
    };

    private Observer<PlaybackStateCompat> playbackStateObserver = new Observer<PlaybackStateCompat>() {
        @Override
        public void onChanged(PlaybackStateCompat playbackStateCompat) {
            PlaybackStateCompat playbackState = playbackStateCompat != null
                    ? playbackStateCompat
                    : MediaSessionConnection.EMPTY_PLAYBACK_STATE;
            MediaMetadataCompat metadata = mSessionConnection.nowPlaying.getValue() != null
                    ? mSessionConnection.nowPlaying.getValue()
                    : MediaSessionConnection.NOTHING_PLAYING;
            mediaItems.postValue(updateState(playbackState, metadata));
        }
    };

    private Observer<MediaMetadataCompat> mediaMetadataObserver = new Observer<MediaMetadataCompat>() {
        @Override
        public void onChanged(MediaMetadataCompat metadataCompat) {
            PlaybackStateCompat playbackState = mSessionConnection.playbackState.getValue() != null
                    ? mSessionConnection.playbackState.getValue()
                    : MediaSessionConnection.EMPTY_PLAYBACK_STATE;
            MediaMetadataCompat metadata = metadataCompat != null
                    ? metadataCompat
                    : MediaSessionConnection.NOTHING_PLAYING;
            mediaItems.postValue(updateState(playbackState, metadata));
        }
    };

    public MediaItemFragmentViewModel(String mediaId, MediaSessionConnection mediaSessionConnection) {
        mMediaId = mediaId;
        mSessionConnection = mediaSessionConnection;

        mediaItems.postValue(Collections.emptyList());

        mSessionConnection.subscribe(mMediaId, mSubscriptionCallback);
        mSessionConnection.playbackState.observeForever(playbackStateObserver);
        mSessionConnection.nowPlaying.observeForever(mediaMetadataObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        // Remove the permanent observers from the MediaSessionConnection.
        mSessionConnection.playbackState.removeObserver(playbackStateObserver);
        mSessionConnection.nowPlaying.removeObserver(mediaMetadataObserver);

        // And then, finally, unsubscribe the media ID that was being watched
        mSessionConnection.unsubscribe(mMediaId, mSubscriptionCallback);
    }

    /**
     * Gets a resource that's used to show that a media item is either playing or paused
     * @param mediaId the media id of the selected
     * @return an int drawable resource
     */
    private int getResourceForMediaId(String mediaId) {
        boolean isActive = false;
        if (mSessionConnection.nowPlaying.getValue() != null) {
            isActive = mediaId.equals(mSessionConnection.nowPlaying.getValue().getDescription().getMediaId());
        }
        boolean isPlaying = false;
        if (mSessionConnection.playbackState.getValue() != null) {
            isPlaying = mSessionConnection.playbackState.getValue().getState() == PlaybackStateCompat.STATE_PLAYING;
        }

        if (!isActive) return NO_RES;
        else if (isPlaying) return R.drawable.ic_pause_black_24dp;
        else return R.drawable.ic_play_arrow_black_24dp;
    }

    private List<MediaItemData> updateState(PlaybackStateCompat playbackState, MediaMetadataCompat metadata) {

        int newResId;
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            newResId = R.drawable.ic_pause_black_24dp;
        } else {
            newResId = R.drawable.ic_play_arrow_black_24dp;
        }

        List<MediaItemData> itemDataList = new ArrayList<>();

        int sizeItems = 0;
        if (mediaItems.getValue() != null) {
            sizeItems = mediaItems.getValue().size();
        }

        for (int i = 0; i < sizeItems; i++) {
            MediaItemData itemData = mediaItems.getValue().get(i);
            if (itemData.mediaId.equals(metadata.getDescription().getMediaId())) {
                itemData.playbackRes = newResId;
            } else {
                itemData.playbackRes = NO_RES;
            }
            itemDataList.add(itemData);
        }

        return itemDataList;
    }

    public LiveData<List<MediaItemData>> getMediaItems() {
        return mediaItems;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private String mediaId;
        private MediaSessionConnection connection;
        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MediaItemFragmentViewModel(mediaId, connection);
        }

        public Factory(String mediaId, MediaSessionConnection mediaSessionConnection) {
            this.mediaId = mediaId;
            this.connection = mediaSessionConnection;
        }
    }

    private static final int NO_RES = 0;
}
