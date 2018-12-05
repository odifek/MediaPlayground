package com.sprinthub.example.mediaplayground.data;

import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

public class MusicRepository {

    private static MusicRepository instance;

    private Disposable allSongsDisposable;
    private BehaviorSubject<List<MediaMetadataCompat>> songsSubject = BehaviorSubject.create();

    private List<MediaMetadataCompat> staticSongList = new ArrayList<>();

    private Disposable songDisposable;

    public static MusicRepository getInstance() {
        if (instance == null) {

            synchronized (MusicRepository.class) {
                instance = new MusicRepository();
            }
        }

        return instance;
    }

    private MusicRepository() {

    }

    public Observable<List<MediaMetadataCompat>> getSongsMetadata() {

        if (allSongsDisposable == null || allSongsDisposable.isDisposed()) {
            allSongsDisposable = SqlBriteUtils.createObservableList(cursor -> {
                Song song = new Song(cursor);
                return song.getSongMeta();
            }, Song.getQuery())
                    .subscribe(mediaMetadataCompats -> {
                        Timber.d("%s Songs retrieved from MediaStore", mediaMetadataCompats.size());
                        songsSubject.onNext(mediaMetadataCompats);
                        staticSongList.clear();
                        staticSongList.addAll(mediaMetadataCompats);
                    }, error -> Timber.e(error, "MusicProvider: get songs error"));
        }
        return songsSubject.subscribeOn(Schedulers.io());
    }

    public List<MediaMetadataCompat> getSongList() {
        return staticSongList;
    }

    public Single<MediaMetadataCompat> getSongByMediaId(String mediaId) {
        String id = null;
        if (mediaId != null) {
            id = mediaId.substring(0, mediaId.indexOf("_"));
        }
        return SqlBriteUtils.createSingle(cursor -> {
            Song song = new Song(cursor);
            return song.getSongMeta();
        }, Song.getQuery(id));
    }
}
