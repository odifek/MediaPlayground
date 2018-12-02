package com.sprinthub.example.mediaplayground.data;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

public class SongSearch {
     //BaseColumns._ID, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Albums.ALBUM,
     //MediaStore.Audio.Media.TITLE, "data1", "data2"

    public long id;
    public String mimeType;
    public String artist;
    public String album;
    public String title;
    public String data1;
    public String data2;

    public SongSearch(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
        data1 = cursor.getString(cursor.getColumnIndexOrThrow("data1"));
        data2 = cursor.getString(cursor.getColumnIndexOrThrow("data2"));
    }
}
