package com.sprinthub.example.mediaplayground;

import android.app.Application;
import android.net.Uri;
import android.provider.MediaStore;

import com.sprinthub.example.mediaplayground.data.Song;
import com.squareup.sqlbrite3.BriteContentResolver;
import com.squareup.sqlbrite3.QueryObservable;
import com.squareup.sqlbrite3.SqlBrite;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MusicViewModel extends AndroidViewModel {

    private MutableLiveData<List<Song>> songsLiveData = new MutableLiveData<>();

    private static final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final Uri ALBUM_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    private static final Uri ARTIST_URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
    private static final Uri GENRE_URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

    private Disposable mDisposable;

    public MusicViewModel(@NonNull Application application) {
        super(application);

        String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + ">0";
        String sortByDateAdded = MediaStore.Audio.AudioColumns.DATE_ADDED + " DESC";

        // Use sqlbrite to query the content provider
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        BriteContentResolver resolver = sqlBrite
                .wrapContentProvider(application.getContentResolver(), Schedulers.io());
        resolver.setLoggingEnabled(true);
        QueryObservable queryObservable =
                resolver.createQuery(AUDIO_URI,
                        null,
                        selection,
                        null,
                        sortByDateAdded,
                        false);
        mDisposable = queryObservable.mapToList(Song::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setSongs);
    }

    public LiveData<List<Song>> getSongs() {
        return songsLiveData;
    }

    private void setSongs(List<Song> songs) {
        this.songsLiveData.setValue(songs);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
