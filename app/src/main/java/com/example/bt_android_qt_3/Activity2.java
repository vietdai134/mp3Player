package com.example.bt_android_qt_3;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Activity2 extends AppCompatActivity {
    Button btnBack;
    TextView namesong, timestart;
    ImageButton btnskipBack, btnskipForward, btnplay;
    SeekBar timeplay;

    boolean isPlaying = false;
    Handler handler = new Handler();
    private String filePath;
    private int currentPlaybackPosition = 0;
    private MediaPlayer mediaPlayer;

    private int currentIndex = 0;
    private ArrayList<String> selectedMusicFiles;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity2);

        btnBack = findViewById(R.id.Back);
        namesong = findViewById(R.id.nameSong);
        btnskipBack = findViewById(R.id.btnSkipBack);
        btnskipForward = findViewById(R.id.btnSkipForward);
        btnplay = findViewById(R.id.btnPlay);
        timeplay = findViewById(R.id.timePlay);
        timestart = findViewById(R.id.timeStart);

        btnskipBack.setImageResource(android.R.drawable.ic_media_previous);
        btnskipForward.setImageResource(android.R.drawable.ic_media_next);
        mediaPlayer = new MediaPlayer();

        Intent intent = getIntent();

        filePath = intent.getStringExtra("filePath");
        selectedMusicFiles = intent.getStringArrayListExtra("selectedMusicFiles");

        if (filePath != null) {
            namesong.setText(new File(filePath).getName());
            setupMediaPlayer(filePath);
            setupSeekBar();
            playMusic(filePath);
        } else if (selectedMusicFiles != null && !selectedMusicFiles.isEmpty()) {
            namesong.setText(new File(selectedMusicFiles.get(currentIndex)).getName());
            setupMediaPlayer(selectedMusicFiles.get(currentIndex));
            setupSeekBar();
            playSelectedMusic();
        } else {
            Toast.makeText(this, "File path is null", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                releaseMediaPlayer();
                finish();
            }
        });

        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    pauseMusic();
                } else {
                    playMusic(filePath != null ? filePath : selectedMusicFiles.get(currentIndex));
                }
                updatePlayPauseButton();
            }
        });

        btnskipForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToNextSong();
            }
        });

        btnskipBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToPreviousSong();
            }
        });

        timeplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    timestart.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startUpdatingSeekBar();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            isPlaying = true;
            btnplay.setImageResource(android.R.drawable.ic_media_pause);
            startUpdatingSeekBar();
        } else {
            isPlaying = false;
            btnplay.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void playSelectedMusic() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(selectedMusicFiles.get(currentIndex));
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            namesong.setText(new File(selectedMusicFiles.get(currentIndex)).getName()); // Update song name
            updatePlayPauseButton();
            startUpdatingSeekBar();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                navigateToNextSong();
            }
        });
    }

    private void navigateToNextSong() {
        if (selectedMusicFiles != null && currentIndex < selectedMusicFiles.size() - 1) {
            currentIndex++;
            playSelectedMusic();
        } else {
            Toast.makeText(this, "No more song available", Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    private void navigateToPreviousSong() {
        if (currentIndex > 0) {
            currentIndex--;
            playSelectedMusic();
        } else {
            Toast.makeText(this, "Already at the first song", Toast.LENGTH_SHORT).show();
        }
    }

    private void playMusic(String filePath) {
        releaseMediaPlayer();
        mediaPlayer = new MediaPlayer();
        try {
            if (filePath != null) {
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.seekTo(currentPlaybackPosition);
                mediaPlayer.start();
                isPlaying = true;
                updatePlayPauseButton();
                startUpdatingSeekBar();
            } else {
                Toast.makeText(this, "File path is null", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing music", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentPlaybackPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            isPlaying = false;
            updatePlayPauseButton();
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void startUpdatingSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    timeplay.setProgress(currentPosition);
                    timestart.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void setupMediaPlayer(String filePath) {
        mediaPlayer = new MediaPlayer();
        try {
            if (!TextUtils.isEmpty(filePath)) {
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        handler.removeCallbacksAndMessages(null);
                        timeplay.setProgress(0);
                    }
                });
            } else {
                Toast.makeText(this, "File path is null or empty", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSeekBar() {
        timeplay.setMax(mediaPlayer.getDuration());
    }

    private void updatePlayPauseButton() {
        if (isPlaying) {
            btnplay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnplay.setImageResource(android.R.drawable.ic_media_play);
        }
    }
}