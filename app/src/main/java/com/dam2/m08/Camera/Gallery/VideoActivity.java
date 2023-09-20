package com.dam2.m08.Camera.Gallery;

import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projecte_maps.R;

public class VideoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        try {
            findViewById(R.id.btnBack).setOnClickListener(l -> finish());
            String path = "";
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                path = extras.getString("path");
            }
            VideoView videoView = findViewById(R.id.video);
            videoView.setVideoPath(path);
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            videoView.setOnCompletionListener(l -> finish());
            videoView.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
