package com.sprinthub.example.mediaplayground.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.List;

public class Song {

    public long id;
    public long duration;
    public String title;
    public String album;
    public long albumId;
    public int year;
    public String artist;
    public long artistId;
    public String mediaUri;
    public long dateAdded;
    public long bookmark;
    public long track;

    public String mediaId;

    public String albumArtUri;
    public List<String> genres;
    public String mimeType;
    public String data1;
    public String data2;
    public Song(Cursor cursor, String search) {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
        data1 = cursor.getString(cursor.getColumnIndexOrThrow("data1"));
        data2 = cursor.getString(cursor.getColumnIndexOrThrow("data2"));
    }

    public Song(Cursor cursor) {
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE));
        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM));
        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));
        id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID));
        duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));
        albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID));
        year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR));
        artistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID));
        dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_ADDED));
        bookmark = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.BOOKMARK));
        track = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK));
        mediaUri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        // Eg 1234_even_i
        mediaId = id + "_" + title.toLowerCase().replace(" ", "_");
    }

    @SuppressLint("WrongConstant")
    public MediaMetadataCompat getSongMeta() {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri);
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
        builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, track);
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId);
        builder.putLong("MEDIA_ITEM_FLAG", MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
        return builder.build();
    }

    public static Query getQuery() {
        return new Query.Builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(MediaStore.Audio.Media.IS_MUSIC + "=1")
                .args(null)
                .sort(MediaStore.Audio.Media.TITLE)
                .build();
    }

    // Query for getting a single item
    public static Query getQuery(String id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.Media._ID + "=?";
        return new Query.Builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(selection)
                .args(new String[]{id})
                .sort(MediaStore.Audio.Media.TITLE)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (duration != song.duration) return false;
        if (albumId != song.albumId) return false;
        if (year != song.year) return false;
        if (artistId != song.artistId) return false;
        if (bookmark != song.bookmark) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        if (album != null ? !album.equals(song.album) : song.album != null) return false;
        return artist != null ? artist.equals(song.artist) : song.artist == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (int) (albumId ^ (albumId >>> 32));
        result = 31 * result + year;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (int) (artistId ^ (artistId >>> 32));
        return result;
    }
}
