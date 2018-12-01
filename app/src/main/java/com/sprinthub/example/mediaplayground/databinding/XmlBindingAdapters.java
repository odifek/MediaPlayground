package com.sprinthub.example.mediaplayground.databinding;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class XmlBindingAdapters {
    @BindingAdapter("android:data")
    public static <T> void setRecyclerViewProperties(RecyclerView recyclerView, T oldData, T newData) {
        if (recyclerView.getAdapter() instanceof BindableAdapter<?>) {
            if (oldData == newData) {
                return;
            }
            ((BindableAdapter<T>) recyclerView.getAdapter()).setData(newData);
        }
    }

}
