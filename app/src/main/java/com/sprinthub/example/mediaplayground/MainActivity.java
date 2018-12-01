package com.sprinthub.example.mediaplayground;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button mPlayButton;
    private Button mPauseButton;
    private ImageView mAlbumArtImageView;
    private SeekBar mSeekBar;

    private StorageReference mStorageReference;

    private String mSampleMusicUri;

    private MediaPlayer mPlayer;
    private int mPlayerPosition;

    private double startTime = 0;
    private double endTime = 0;

    private TextView mCurrentTimeTv;
    private TextView mDurationTv;

    private Handler mHandler = new Handler();

    OutputStream mOutputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayButton = findViewById(R.id.button_play);
        mPauseButton = findViewById(R.id.button_pause);
        mAlbumArtImageView = findViewById(R.id.imageview_album_art);
        mSeekBar = findViewById(R.id.seekbar_media_position);
        mPlayButton.setEnabled(false);
        mPauseButton.setEnabled(false);

        mCurrentTimeTv = findViewById(R.id.textview_current_time);
        mDurationTv = findViewById(R.id.textview_duration);

        mPlayButton.setEnabled(false);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null && !mPlayer.isPlaying()) {
                    mPlayer.start();
                    mPauseButton.setEnabled(true);
                    mPlayButton.setEnabled(false);

                    endTime = mPlayer.getDuration();
                    startTime = mPlayer.getCurrentPosition();

                    // The max in milliseconds
                    mSeekBar.setMax((int) endTime);

                    mCurrentTimeTv.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes((long)startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long)startTime)
                                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)startTime))));

                    mDurationTv.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes((long)endTime),
                            TimeUnit.MILLISECONDS.toSeconds((long)endTime)
                                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)endTime))));

                    mSeekBar.setProgress((int) startTime);
                    mHandler.postDelayed(UpdateSongTime, 100);

                }
            }
        });


        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.pause();
                    mPauseButton.setEnabled(false);
                    mPlayButton.setEnabled(true);
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayer.seekTo(seekBar.getProgress());
            }
        });

        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            FileDescriptor fileDescriptor = pipe[0].getFileDescriptor();
            mPlayer.setDataSource(fileDescriptor);
            mOutputStream = new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }


        mStorageReference = FirebaseStorage.getInstance().getReference("music/bensound64.m4a");
//        mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                mAlbumArtImageView.setImageResource(R.drawable.betterdays);
//                mSampleMusicUri = uri.toString();
//                setupMediaPlayer();
//            }
//        });
        mStorageReference.getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                InputStream inputStream = taskSnapshot.getStream();
                long totalByteCount = taskSnapshot.getTotalByteCount();

                for (int i = 0; i < totalByteCount; i += 10240) {
                    byte[] bytes = new byte[10240];
                    try {
                        int numBytes = inputStream.read(bytes);
                        mOutputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void setupMediaPlayer() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mSampleMusicUri);
            mPlayer.prepare();
            mPlayer.setLooping(true);
            mPlayButton.setEnabled(true);

            mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    Log.i("Media player", "onBufferingUpdate: endtime: " + endTime);
                    Log.i("Media Player", "onBufferingUpdate: " + percent);
                    double secProgress = (float) percent / 100 * endTime;
                    Log.i("Media Player", "onBufferingUpdate: " + secProgress);
                    mSeekBar.setSecondaryProgress((int)secProgress);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
          startTime = mPlayer.getCurrentPosition();
          mCurrentTimeTv.setText(String.format("%02d:%02d",
                  TimeUnit.MILLISECONDS.toMinutes((long)startTime),
                  TimeUnit.MILLISECONDS.toSeconds((long)startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)startTime))));
          mSeekBar.setProgress((int)startTime);
          mHandler.postDelayed(this, 100);
        }
    };
}
