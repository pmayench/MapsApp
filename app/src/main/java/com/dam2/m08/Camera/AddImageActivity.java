package com.dam2.m08.Camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dam2.m08.AppImageList;
import com.dam2.m08.CurrentUser;
import com.dam2.m08.Llamadas.AppImageCRUD;
import com.dam2.m08.Messages;
import com.dam2.m08.Objects.AppImage;
import com.dam2.m08.Utils;
import com.example.projecte_maps.R;

import java.io.IOException;
import java.time.LocalDateTime;

public class AddImageActivity extends AppCompatActivity {
    Bitmap img = null;
    Button btnAdd;
    ImageButton btnChooseFile;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                        if (uri != null) {
                            try {
                                Bitmap originalImg = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                                float ratio = (float)originalImg.getWidth() / (float)originalImg.getHeight();
                                float width = 500;
                                float height;
                                if (width < originalImg.getWidth()) {
                                    height = originalImg.getHeight() - ((originalImg.getHeight() - width) / ratio);
                                } else if (width > originalImg.getWidth()) {
                                    height = originalImg.getHeight() + (width - originalImg.getHeight()) / ratio;
                                } else {
                                    width = originalImg.getWidth();
                                    height = originalImg.getHeight();
                                }
                                img = Utils.compress(
                                        Bitmap.createScaledBitmap(originalImg, (int) Math.floor(width), (int) Math.floor(height), true)
                                );
                                btnChooseFile.setImageBitmap(img);
                                btnAdd.setEnabled(true);
                            } catch (IOException e) {
                                Messages.showMessage(this, "Error loading image");
                            }
                        } else {
                            Messages.showMessage(this, "No media selected");
                        }
                });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);
        findViewById(R.id.btnBack).setOnClickListener(l -> finish());
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnChooseFile.setOnClickListener(v -> showFileChooser());
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setEnabled(false);
        EditText txtLat = findViewById(R.id.txtLat);
        EditText txtLon = findViewById(R.id.txtLon);
        AppImageCRUD db = new AppImageCRUD(CurrentUser.user.getEmail());
        btnAdd.setOnClickListener(v -> {
            if (txtLat.getText() != null && txtLon.getText() != null &&
                !txtLat.getText().toString().isEmpty() && !txtLon.getText().toString().isEmpty() &&
                img != null) {
                double lat = Double.parseDouble(txtLat.getText().toString());
                double lon = Double.parseDouble(txtLon.getText().toString());
                if (lat >= -85 && lat <= 85 &&
                    lon >= -180 && lon <= 180) {
                    AppImage appImage = new AppImage(img,
                            lat,
                            lon,
                            LocalDateTime.now(),
                            CurrentUser.user.getEmail()
                    );
                    AppImageList.imageList.add(appImage);
                    db.insert(appImage,
                            task -> {
                                if (task.isSuccessful()) Messages.showMessage(this, "Image saved");
                                else Messages.showMessage(this, task.getException().getLocalizedMessage());
                            }
                    );
                } else {
                    Messages.showMessage(this, "Latitude or longitude off bounds");
                }
            } else {
                Messages.showMessage(this, "Yoy must specify latitude and longitude");
            }
        });
    }

    private void showFileChooser() {
        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = (ActivityResultContracts.PickVisualMedia.VisualMediaType) ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(mediaType)
                .build());
    }
}
