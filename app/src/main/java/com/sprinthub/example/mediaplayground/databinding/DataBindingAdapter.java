package com.sprinthub.example.mediaplayground.databinding;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.sprinthub.example.mediaplayground.DataBindingViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public abstract class DataBindingAdapter<T> extends ListAdapter<T, DataBindingViewHolder<T>>
        implements BindableAdapter<List<T>>{
    protected DataBindingAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public DataBindingViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
        return new DataBindingViewHolder<>(binding);
    }

    @Override
    public abstract void onBindViewHolder(@NonNull DataBindingViewHolder<T> holder, int position);
}