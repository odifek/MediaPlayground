package com.sprinthub.example.mediaplayground;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A generic {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} which can be used to bind any media item
 * to given layout
 * @param <T> is a child of MediaItem
 */
public class DataBindingViewHolder<T> extends RecyclerView.ViewHolder {
    private ViewDataBinding mBinding;
    public DataBindingViewHolder(@NonNull ViewDataBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    /**
     * Binds the item to the item layout
     * @param item is the item object we want to bind to view
     * @param callback receives the item that is being clicked
     */
    public void bind(T item, ItemClickCallback callback) {
        mBinding.setVariable(BR.callback, callback);
        mBinding.setVariable(BR.item, item);
        mBinding.executePendingBindings();
    }
}