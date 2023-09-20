package com.dam2.m08.Camera.Gallery;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.dam2.m08.AppImageList;
import com.dam2.m08.Objects.AppImage;
import com.example.projecte_maps.R;

import java.util.ArrayList;

public class GallerySliderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_slider);
        String id = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = (String) extras.get("id");
        }
        ViewPager viewPager = findViewById(R.id.galleryViewPager);
        GalleryPagerAdapter viewPagerAdapter = new GalleryPagerAdapter(this, AppImageList.imageList);
        viewPager.setAdapter(viewPagerAdapter);
        int position = 0;
        for (int i = 0; i < AppImageList.imageList.size(); i++) {
            if (AppImageList.imageList.get(i).getId().equals(id)) {
                position = i;
                break;
            }
        }
        viewPager.setCurrentItem(position);
        findViewById(R.id.btnBack).setOnClickListener(l -> finish());
    }
}
