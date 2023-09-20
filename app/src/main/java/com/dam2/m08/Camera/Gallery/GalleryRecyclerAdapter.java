package com.dam2.m08.Camera.Gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam2.m08.Objects.AppImage;
import com.dam2.m08.Utils;
import com.example.projecte_maps.R;

import java.util.ArrayList;

public class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryRecyclerAdapter.ViewHolder>{
    private ArrayList<AppImage> imgList;
    private final Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private String id;

        public ViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.imageView);
        }

        public ImageView getImageView() {
            return imageView;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public GalleryRecyclerAdapter(ArrayList<AppImage> dataSet, Context context) {
        imgList = dataSet;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.gallery_icon, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        AppImage img = imgList.get(position);
        if (img != null) {
            ImageView imageView = viewHolder.getImageView();
            imageView.setImageBitmap(getSizedBitmap(img.getThumbnail()));
            imageView.setOnClickListener(l -> {
                Intent i = new Intent(context, GallerySliderActivity.class);
                i.putExtra("id", img.getId());
                context.startActivity(i);
            });
            viewHolder.setId(img.getId());
        }
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }

    private Bitmap getSizedBitmap(Bitmap img) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidth = displayMetrics.widthPixels / displayMetrics.density;
        int columnWidth = (int) Math.floor(screenWidth / (((GalleryActivity)context).largeGrid ? Utils.GRID_SIZE_LARGE : Utils.GRID_SIZE_SMALL));
        float ratio =  (float)img.getWidth() / (float)img.getHeight();
        return Bitmap.createScaledBitmap(img, columnWidth, (int) Math.floor(img.getHeight() * ratio), true);
    }

}
