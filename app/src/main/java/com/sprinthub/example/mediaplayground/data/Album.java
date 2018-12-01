package com.sprinthub.example.mediaplayground.data;

import android.database.Cursor;
import android.provider.MediaStore;

public class Album {

    public long id;
    public long albumId;
    public String title;
    public String albumArtUri;
    public String albumArtist;
    public long year;

    public Album(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
        albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ID));
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM));
        albumArtUri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART));
        albumArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ARTIST));
        year = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.LAST_YEAR));
    }
}
