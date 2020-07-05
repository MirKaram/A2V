package com.example.artghul_drama;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class activity_video extends AppCompatActivity {

    private VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        String p = getIntent().getExtras().getString("PATH");

        videoView = findViewById(R.id.videoView2);
        videoView.setVideoPath(p);
        videoView.setMediaController(new MediaController(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }
}