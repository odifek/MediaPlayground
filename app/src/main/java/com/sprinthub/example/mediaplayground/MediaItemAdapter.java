package com.sprinthub.example.mediaplayground;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sprinthub.example.mediaplayground.databinding.ItemMediaBinding;
import com.sprinthub.example.mediaplayground.models.MediaItemData;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class MediaItemAdapter extends ListAdapter<MediaItemData, MediaItemAdapter.MediaViewHolder> {

    private ItemClickCallback<MediaItemData> mCallback;

    public MediaItemAdapter(ItemClickCallback<MediaItemData> callback) {
        super(MediaItemData.diffCallback);
        mCallback = callback;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMediaBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_media, parent, false);
        return new MediaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        holder.binding.setMediaItem(getItem(position));
        holder.binding.setCallback(mCallback);
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        ItemMediaBinding binding;
        MediaViewHolder(@NonNull ItemMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
