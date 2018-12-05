package com.sprinthub.example.mediaplayground;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.media.AudioManager;
import android.os.Bundle;

import com.sprinthub.example.mediaplayground.viewmodels.MainActivityViewModel;

public class MediaActivity extends AppCompatActivity {

    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        // Since this is a music player, the volume controls should adjust the
        // music volume while in the app?
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mViewModel = ViewModelProviders
                .of(this, Injection.provideMainActivityViewModel())
                .get(MainActivityViewModel.class);
        /**
         * Observe changes to the {@link MainActivityViewModel#rootMediaId}. When the app starts,
         * and the UI connects to {@link com.sprinthub.example.mediaplayground.media.MusicService},
         * this will be updated and the app will show the initial list of media items
         */

        mViewModel.rootMediaId.observe(this,
                rootMediaId -> {
                    if (rootMediaId != null) {
                        navigateToMediaItem(rootMediaId);
                    }
                });
        /**
         * Observe {@link MainActivityViewModel#navigateToMediaItem} for {@link com.sprinthub.example.mediaplayground.utils.Event}s indicating
         * the user has requested to browse to a different {@link com.sprinthub.example.mediaplayground.models.MediaItemData}.
         */
        mViewModel.navigateToMediaItem().observe(this, navEvent -> {
            String mediaId = navEvent.getContentIfNotHandled();
            if (mediaId != null) {
                navigateToMediaItem(mediaId);
            }
        });

    }

    private void navigateToMediaItem(String mediaId) {
        MediaItemsFragment fragment = getBrowseFragment(mediaId);
        if (fragment == null) {
            fragment = MediaItemsFragment.newInstance(mediaId);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.media_browse_fragment, fragment, mediaId);

            // If this is not the top level media (root), we add it to the fragment
            // back stack, so that actionbar toggle and Back will work appropriately:
            if (!isRootId(mediaId)) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }
    }

    private boolean isRootId(String mediaId) {
        return mediaId.equals(mViewModel.rootMediaId.getValue());
    }

    private MediaItemsFragment getBrowseFragment(String mediaId) {
        return (MediaItemsFragment) getSupportFragmentManager().findFragmentByTag(mediaId);
    }
}
