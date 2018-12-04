package com.sprinthub.example.mediaplayground.data;

import android.support.v4.media.MediaMetadataCompat;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

public class MusicRepository {

    private static MusicRepository instance;

    private Disposable allSongsDisposable;
    private BehaviorSubject<List<MediaMetadataCompat>> songsSubject = BehaviorSubject.create();

    public MusicRepository getInstance() {
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
                    }, error -> Timber.e(error, "MusicProvider: get songs error"));
        }
        return songsSubject.subscribeOn(Schedulers.io());
    }
}
