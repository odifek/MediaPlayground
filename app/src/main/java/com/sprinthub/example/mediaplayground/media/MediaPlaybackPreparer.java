package com.sprinthub.example.mediaplayground.media;

import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.sprinthub.example.mediaplayground.data.MusicRepository;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class MediaPlaybackPreparer implements MediaSessionConnector.PlaybackPreparer {

    private ExoPlayer mExoPlayer;
    private MusicRepository mMusicRepository;
    private DataSource.Factory mDataSourceFactory;

    public MediaPlaybackPreparer(MusicRepository musicRepository, ExoPlayer exoPlayer, DataSource.Factory dataSourceFactory) {
        mExoPlayer = exoPlayer;
        mMusicRepository = musicRepository;
        mDataSourceFactory = dataSourceFactory;
    }

    @Override
    public long getSupportedPrepareActions() {
        return PlaybackStateCompat.ACTION_PREPARE |
                PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PREPARE_FROM_URI |
                PlaybackStateCompat.ACTION_PLAY_FROM_URI |
                PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
    }

    @Override
    public void onPrepare() {

    }

    @Override
    public void onPrepareFromMediaId(String mediaId, Bundle extras) {
        Disposable disposable;
        // Note that the media id should be the unique media id? without hierarchy information?
        Timber.d("About to play mediaId: %s", mediaId);
//        disposable = mMusicRepository.getSongByMediaId(mediaId)
//                .subscribe(metadataCompat -> {
//                    // TODO: 12/5/18 Handle errors here
//                    if (metadataCompat == null) {
//                        Timber.d("Thread: %s", Thread.currentThread().getName());
//                        Timber.w("Content not found: MediaId=%s", mediaId);
//                        // TODO: 12/5/18 Notify caller of the error
//                    } else {
//                        mMusicRepository.getSongsMetadata()
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(mediaMetadataCompats -> {
//                                    ConcatenatingMediaSource mediaSource = getMediaSource(mediaMetadataCompats, mDataSourceFactory);
//                                    int initialWindowIndex = mediaMetadataCompats.indexOf(metadataCompat);
//
//                                    mExoPlayer.prepare(mediaSource);
//                                            mExoPlayer.seekTo(initialWindowIndex, 0);
//                                        });
//                            }
//                        });
        List<MediaMetadataCompat> mediaMetadataCompats = mMusicRepository.getSongList();
        MediaMetadataCompat itemToPlay = mediaMetadataCompats.get(6);
        Timber.d("Thread: %s", Thread.currentThread().getName());
//        Timber.w("Content not found: MediaId=%s", itemToPlay.getDescription().getMediaId());
        ConcatenatingMediaSource mediaSource = getMediaSource(mediaMetadataCompats, mDataSourceFactory);
        int initialWindowIndex = mediaMetadataCompats.indexOf(itemToPlay);

        mExoPlayer.prepare(mediaSource);
        mExoPlayer.seekTo(initialWindowIndex, 0);
    }

    @Override
    public void onPrepareFromSearch(String query, Bundle extras) {

    }

    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {

    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }

//    private List<MediaMetadataCompat> buildPlayList(MediaMetadataCompat item) {
////        item.get
//    }

    private ExtractorMediaSource getMediaSource(MediaMetadataCompat metadata, DataSource.Factory factory) {
        Bundle bundle = metadata.getBundle();
        MediaDescriptionCompat description = metadata.getDescription();
        if (description.getExtras() != null) {
            description.getExtras().putAll(bundle);
        }
        Uri mediaUri = Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
        return new ExtractorMediaSource.Factory(factory)
                .setTag(description)
                .createMediaSource(mediaUri);
    }

    private ConcatenatingMediaSource getMediaSource(List<MediaMetadataCompat> metadatas, DataSource.Factory factory) {
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        for (MediaMetadataCompat metadata :
                metadatas) {
            concatenatingMediaSource.addMediaSource(getMediaSource(metadata, factory));
        }
        return concatenatingMediaSource;
    }
}
