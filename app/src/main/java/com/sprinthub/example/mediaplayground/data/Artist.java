package com.sprinthub.example.mediaplayground.data;

import android.database.Cursor;
import android.provider.MediaStore;

public class Artist {

    public String title;
    public long id;
    public int numTracks;
    public int numAlbums;

    public Artist(Cursor cursor) {
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.ARTIST));
        id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));
        numAlbums = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS));
        numTracks = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS));

    }
}
