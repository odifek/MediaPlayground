package com.sprinthub.example.mediaplayground.models;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

/**
 * Data class to encapsulate properties of a {@link android.support.v4.media.MediaBrowserCompat.MediaItem}
 * <p>
 * If an item is {@link android.support.v4.media.MediaBrowserCompat.MediaItem#FLAG_BROWSABLE} it means that it has
 * a list of child media items that can be retrieved by passing the mediaId to {@link android.support.v4.media.MediaBrowserCompat#subscribe}.
 * <p>
 * Objects of this class are built from {@link android.support.v4.media.MediaBrowserCompat.MediaItem}s in
 * {@link com.sprinthub.example.mediaplayground.viewmodels.MediaItemFragmentViewModel#subscriptionCallback}.
 */
public class MediaItemData {

    public String mediaId;
    public String title;
    public String subtitle;
    public Uri albumArtUri;
    public boolean browsable;
    public int playbackRes;

    public MediaItemData(String mediaId, String title, String subtitle, Uri albumArtUri, boolean browsable, int playbackRes) {
        this.mediaId = mediaId;
        this.title = title;
        this.subtitle = subtitle;
        this.albumArtUri = albumArtUri;
        this.browsable = browsable;
        this.playbackRes = playbackRes;
    }

    public static final DiffUtil.ItemCallback<MediaItemData> diffCallback =
            new DiffUtil.ItemCallback<MediaItemData>() {
                int PLAYBACK_RES_CHANGED = 1;
                @Override
                public boolean areItemsTheSame(@NonNull MediaItemData oldItem, @NonNull MediaItemData newItem) {
                    return oldItem.mediaId != null && oldItem.mediaId.equals(newItem.mediaId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull MediaItemData oldItem, @NonNull MediaItemData newItem) {
                    return oldItem.mediaId != null && oldItem.mediaId.equals(newItem.mediaId) && oldItem.playbackRes == newItem.playbackRes;
                }

                @Nullable
                @Override
                public Object getChangePayload(@NonNull MediaItemData oldItem, @NonNull MediaItemData newItem) {
                    if (oldItem.playbackRes != newItem.playbackRes) {
                        return PLAYBACK_RES_CHANGED;
                    } else {
                        return null;
                    }
                }
            };
}
