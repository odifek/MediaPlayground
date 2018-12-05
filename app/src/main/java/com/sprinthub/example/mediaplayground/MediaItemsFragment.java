package com.sprinthub.example.mediaplayground;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sprinthub.example.mediaplayground.databinding.FragmentMediaItemsBinding;
import com.sprinthub.example.mediaplayground.viewmodels.MainActivityViewModel;
import com.sprinthub.example.mediaplayground.viewmodels.MediaItemFragmentViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class MediaItemsFragment extends Fragment {

    private static final String ARG_MEDIA_ID = "com.sprinthub.com.example.mediaplayground.MediaItemsFragment.MEDIA_ID";

    private String mMediaId;
    private MainActivityViewModel mMainActivityViewModel;
    private MediaItemFragmentViewModel mMediaItemFragmentViewModel;

    private MediaItemAdapter mListAdapter = new MediaItemAdapter(item -> {
        mMainActivityViewModel.mediaItemClicked(item);
    });

    public MediaItemsFragment() {

    }

    public static MediaItemsFragment newInstance(String mediaId) {
        MediaItemsFragment fragment = new MediaItemsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEDIA_ID, mediaId);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentMediaItemsBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_media_items, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() == null) {
            return;
        }
        if (getArguments() == null) {
            return;
        }
        mMediaId = getArguments().getString(ARG_MEDIA_ID);

        mMainActivityViewModel = ViewModelProviders
                .of(getActivity(), Injection.provideMainActivityViewModel())
                .get(MainActivityViewModel.class);
        mMediaItemFragmentViewModel = ViewModelProviders
                .of(this, Injection.provideMediaItemFragmentViewModel(mMediaId))
                .get(MediaItemFragmentViewModel.class);

        mMediaItemFragmentViewModel.getMediaItems().observe(this,
                mediaItemData -> mListAdapter.submitList(mediaItemData));

        mBinding.recyclerviewSongItems.setAdapter(mListAdapter);
    }
}
