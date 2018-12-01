package com.sprinthub.example.mediaplayground;


import com.sprinthub.example.mediaplayground.data.Song;
import com.sprinthub.example.mediaplayground.databinding.DataBindingAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

/**
 * A {@link androidx.recyclerview.widget.ListAdapter} that is used to bind all the media items (album, artist, song, playlist)
 * etc
 */
public class MusicAdapter extends DataBindingAdapter<Song> {

    private ItemClickCallback<Song> mCallback;

    /**
     * Instantiates the MusicAdapter. You must supply the
     * the appropriate layout to inflate
     * @param diffCallback is the {@link DiffUtil.ItemCallback} which is used to check items are same or not before adding to list
     */
    public MusicAdapter(@NonNull DiffUtil.ItemCallback<Song> diffCallback, ItemClickCallback<Song> clickCallback) {
        super(diffCallback);
        mCallback = clickCallback;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_music;
    }

    /**
     *
     * @param holder is the viewholder
     * @param position the current position of the adapter
     */
    @Override
    public void onBindViewHolder(@NonNull DataBindingViewHolder<Song> holder, int position) {
        holder.bind(getItem(position), mCallback);
    }

    /**
     * Used by the {@link com.sprinthub.example.mediaplayground.databinding.BindableAdapter} to bind the list using data supplied in the layout
     * @param data is supplied from the xml layout
     */
    @Override
    public void setData(List<Song> data) {
        submitList(data);
    }

}