package com.sprinthub.example.mediaplayground;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import com.sprinthub.example.mediaplayground.data.Song;
import com.squareup.sqlbrite3.BriteContentResolver;
import com.squareup.sqlbrite3.QueryObservable;
import com.squareup.sqlbrite3.SqlBrite;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MusicSearchViewModel extends AndroidViewModel {

    private static final String TAG = MusicSearchViewModel.class.getSimpleName();
    private Context mContext;
    public MusicSearchViewModel(@NonNull Application application) {
        super(application);
        mContext = application.getApplicationContext();
        initializeSqlBrite();
        configureAutoSearch();
    }

    private MutableLiveData<List<Song>> songSearchResults = new MutableLiveData<>();

    private final PublishSubject<String> mSubject = PublishSubject.create();
    private Disposable mDisposable;

    private void configureAutoSearch() {
        mDisposable =
                mSubject.debounce(300, TimeUnit.MILLISECONDS)
                        .filter(s -> !s.isEmpty())
                        .map(String::trim)
                        .distinctUntilChanged()
                        .switchMap(this::searchMediaStore)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::displayResults);
    }

    private BriteContentResolver mResolver;
    private void initializeSqlBrite() {
        // Use sqlbrite to query the content provider
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        mResolver = sqlBrite
                .wrapContentProvider(mContext.getContentResolver(), Schedulers.io());

    }

    private ObservableSource<List<Song>> searchMediaStore(String query) {
        String[] ccols = new String[] {
                BaseColumns._ID, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Media.TITLE, "data1", "data2"
        };

        Uri search = Uri.parse("content://media/external/audio/search/fancy/" + Uri.encode(query));
        QueryObservable queryObservable =
                mResolver.createQuery(search,
                        null,
                        null,
                        null,
                        null,
                        false);
        return queryObservable.mapToList(cursor -> new Song(cursor, "search"));
    }

    private void displayResults(List<Song> songs) {
        songSearchResults.setValue(songs);
    }

    public void onSearchInputStateChanged(String query) {
        mSubject.onNext(query);
    }
    public void onSubmitSearchQuery(String query) {
        mSubject.onComplete();
    }
    public LiveData<List<Song>> getSongSearchResults() {
        return songSearchResults;
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
