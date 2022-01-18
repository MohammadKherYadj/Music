package com.example.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    ImageView btn_Play, btn_Next, btn_Previous, btn_Fast_Forward, btn_Fast_Rewind, Song_Image;
    TextView Song_Name, Start, End;
    SeekBar seekBar;

    String S_Name;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySong;
    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        Song_Image = findViewById(R.id.ImageView);

        btn_Previous = findViewById(R.id.btn_Previous);
        btn_Next = findViewById(R.id.btn_Next);
        btn_Play = findViewById(R.id.btn_Play);
        btn_Fast_Forward = findViewById(R.id.btn_Fast_Forward);
        btn_Fast_Rewind = findViewById(R.id.btn_Fast_Rewind);

        Song_Name = findViewById(R.id.text_sn);
        Start = findViewById(R.id.text_sStart);
        End = findViewById(R.id.text_sEnd);

        seekBar = findViewById(R.id.SeekBar);

        btn_Play.setBackgroundResource(R.drawable.ic_pause_icon);




        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mySong = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = intent.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        Start.setSelected(true);

        Uri uri = Uri.parse(mySong.get(position).toString());
        S_Name = mySong.get(position).getName();

        Song_Name.setText(S_Name);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_200), PorterDuff.Mode.SRC_IN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        End.setText(endTime);
        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTine = createTime(mediaPlayer.getCurrentPosition());
                Start.setText(currentTine);
                handler.postDelayed(this, delay);
            }
        }, delay);

        btn_Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    btn_Play.setBackgroundResource(R.drawable.ic_play_icon);
                    mediaPlayer.pause();
                } else {
                    btn_Play.setBackgroundResource(R.drawable.ic_pause_icon);
                    mediaPlayer.start();
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btn_Next.performClick();
            }
        });

        btn_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position + 1) % mySong.size());
                Uri u = Uri.parse(mySong.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                S_Name = mySong.get(position).getName();
                Song_Name.setText(S_Name);
                mediaPlayer.start();
                btn_Play.setBackgroundResource(R.drawable.ic_pause_icon);
                startAnimation(Song_Image);
            }
        });
        btn_Previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position - 1) < 0) ? (mySong.size() - 1) : (position - 1);
                Uri u = Uri.parse(mySong.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                S_Name = mySong.get(position).getName();
                Song_Name.setText(S_Name);
                mediaPlayer.start();
                btn_Play.setBackgroundResource(R.drawable.ic_pause_icon);
                startAnimation(Song_Image);
            }
        });

        btn_Fast_Forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);

                }
            }
        });
        btn_Fast_Rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });
    }

    public void startAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(Song_Image, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        time += min + ":";
        if (sec < 10) {
            time += "0";
        }
        time += sec;
        return time;
    }
}