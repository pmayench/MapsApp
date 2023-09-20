package com.dam2.m08.Camera.Gallery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam2.m08.AppImageList;
import com.dam2.m08.Camera.AddImageActivity;
import com.dam2.m08.Utils;
import com.example.projecte_maps.R;


public class GalleryActivity extends AppCompatActivity {
    public boolean largeGrid = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        findViewById(R.id.btnBack).setOnClickListener(l -> finish());
        findViewById(R.id.btnAddImage).setOnClickListener(l -> startActivity(new Intent(GalleryActivity.this, AddImageActivity.class)));
        RecyclerView rvGallery = findViewById(R.id.rvGallery);
        rvGallery.setLayoutManager(new GridLayoutManager(this, Utils.GRID_SIZE_SMALL));
        rvGallery.setAdapter(new GalleryRecyclerAdapter(AppImageList.imageList, this));
        ImageButton btnGridSize = findViewById(R.id.btnGridSize);
        btnGridSize.setOnClickListener(l -> {
            largeGrid = !largeGrid;
            rvGallery.setLayoutManager(new GridLayoutManager(this, largeGrid ? Utils.GRID_SIZE_LARGE : Utils.GRID_SIZE_SMALL));
            btnGridSize.setImageResource(largeGrid ? R.mipmap.grid3x3 : R.mipmap.grid2x2);
        });
    }
}
