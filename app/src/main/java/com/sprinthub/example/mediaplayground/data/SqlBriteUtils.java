package com.sprinthub.example.mediaplayground.data;

import android.database.Cursor;

import com.sprinthub.example.mediaplayground.BuildConfig;
import com.sprinthub.example.mediaplayground.MediaApp;
import com.squareup.sqlbrite3.BriteContentResolver;
import com.squareup.sqlbrite3.QueryObservable;
import com.squareup.sqlbrite3.SqlBrite;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public final class SqlBriteUtils {
    private static final String TAG = SqlBriteUtils.class.getSimpleName();

    private SqlBriteUtils() {

    }

    private static BriteContentResolver wrapContentProvider() {
        final SqlBrite sqlBrite = new SqlBrite.Builder().build();
        BriteContentResolver briteContentResolver = sqlBrite.wrapContentProvider(MediaApp.getInstance().getContentResolver(), Schedulers.io());
        briteContentResolver.setLoggingEnabled(BuildConfig.DEBUG);
        return briteContentResolver;
    }

    private static QueryObservable createObservable(@NonNull Query query) {
        Timber.d("MediaStore query request sent!");
        return wrapContentProvider()
                .createQuery(query.uri, query.projection, query.selection, query.args, query.sort, false);
    }

    public static <T> Observable<List<T>> createObservableList(@NonNull Function<Cursor, T> mapper, Query query) {
        return createObservable(query).mapToList(mapper)

                .doOnError(error -> Timber.e(error,"Query failed.\nError: %s\nQuery: %s", error.getMessage(), query.toString()));
    }

    public static <T> Single<List<T>> createSingleList(@NonNull Function<Cursor, T> mapper, Query query) {
        return createObservableList(mapper, query)
                .first(Collections.emptyList());
    }

    public static <T> Observable<T> createObservable(@NonNull Function<Cursor, T> mapper, Query query) {
        return createObservable(query)
                .mapToOne(mapper);
    }
    public static <T> Single<T> createSingle(@NonNull Function<Cursor, T> mapper, Query query) {
        return createObservable(query)
                .mapToOne(mapper)
                .firstOrError();
    }
}
