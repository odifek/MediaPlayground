package com.sprinthub.example.mediaplayground;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import com.sprinthub.example.mediaplayground.data.Song;
import com.sprinthub.example.mediaplayground.databinding.ActivityMusicSearchBinding;

import java.util.Objects;

public class MusicSearchActivity extends AppCompatActivity {

    private MusicAdapter mAdapter;
    private ActivityMusicSearchBinding mBinding;
    private MusicSearchViewModel mViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_music_search);

        mViewModel = ViewModelProviders.of(this).get(MusicSearchViewModel.class);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);

        mAdapter = new MusicAdapter(new DiffUtil.ItemCallback<Song>() {
            @Override
            public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
                return oldItem.equals(newItem);
            }
        }, item -> Toast.makeText(this, "Song clicked: " + item.title, Toast.LENGTH_SHORT).show());
        mBinding.recyclerviewSearchMusicList.setAdapter(mAdapter);

        setSupportActionBar(mBinding.toolbarActivitySearch);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);


        mBinding.searchviewActivitySearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.onSubmitSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mViewModel.onSearchInputStateChanged(newText);
                return true;
            }
        });
    }
}
