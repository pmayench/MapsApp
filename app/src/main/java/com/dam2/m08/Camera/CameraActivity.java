package com.dam2.m08.Camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dam2.m08.AppImageList;
import com.dam2.m08.Camera.Gallery.GalleryActivity;
import com.dam2.m08.CurrentUser;
import com.dam2.m08.Llamadas.AppImageCRUD;
import com.dam2.m08.Messages;
import com.dam2.m08.Objects.AppImage;
import com.dam2.m08.Utils;
import com.example.projecte_maps.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    private TextureView textureView;
    private ImageButton btnTakePhoto;
    private ImageButton btnTakeVideo;
    private ImageButton btnOpenGallery;
    private ImageButton btnBack;

    private HashMap<Integer, Integer> orientations;

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private MediaRecorder mediaRecorder;
    private Size imageSize;
    private Size videoSize;
    private int totalRotation;
    private File file;
    private File folder;
    private final int REQUEST_PERMISSION_CODE = 13;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private AppImageCRUD db;
    private MediaPlayer mediaPlayer;
    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera(width, height); }
        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}
        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) { return false; }
        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) { cameraDevice.close(); }
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("camera_background_thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            showMessage(e.getLocalizedMessage());
        }
    }

    private void openCamera(int width, int height) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = getFirstForwardCamera(manager);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
            totalRotation = getRotation(characteristics, deviceOrientation);
            int rotatedWidth = width;
            int rotatedHeight = height;
            if (totalRotation == 90 || totalRotation == 270) {
                rotatedWidth = height;
                rotatedHeight = width;
            }
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                imageSize = chooseSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                videoSize = chooseSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                manager.openCamera(cameraId, stateCallback, null);
                //createMediaRecorder();
            }
        } catch (CameraAccessException e) {
            showMessage(e.getLocalizedMessage());
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture != null) {
                texture.setDefaultBufferSize(imageSize.getWidth(), imageSize.getHeight());
                Surface surface = new Surface(texture);
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surface);
                final CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if (cameraDevice == null) {
                            return;
                        }
                        cameraCaptureSession = session;
                        updatePreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        showMessage("Error in camera configuration");
                    }
                };
                cameraDevice.createCaptureSession(Collections.singletonList(surface), stateCallback, null);
            }
        } catch (CameraAccessException e) {
            showMessage(e.getLocalizedMessage());
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            showMessage("Error with camera preview");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            showMessage(e.getLocalizedMessage());
        }
    }

    private void createMediaRecorder() throws IOException {
        setFile(false);
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file);
        mediaRecorder.setVideoEncodingBitRate(1000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOrientationHint(totalRotation);
        mediaRecorder.prepare();
    }

    private void takePhoto() {
        if (cameraDevice != null) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                    Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                btnTakePhoto.setEnabled(false);
            }
            if (isStoragePermissionGranted()) {
                try {
                    ImageReader reader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), ImageFormat.JPEG, 1);
                    List<Surface> outputSurfaces = new ArrayList<>(2);
                    outputSurfaces.add(reader.getSurface());
                    outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
                    CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    captureBuilder.addTarget(reader.getSurface());
                    captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();
                    captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientations.get(rotation));
                    setFile(true);
                    ImageReader.OnImageAvailableListener readerListener = r -> {
                        try (Image image = reader.acquireLatestImage()) {
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            save(bytes);
                            runOnUiThread(this::setThumbnail);
                        } catch (IOException e) {
                            showMessage(e.getLocalizedMessage());
                        }
                    };
                    reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                    CameraCaptureSession.CaptureCallback captureCallback  = new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            createCameraPreview();
                        }
                    };
                    CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.capture(captureBuilder.build(), captureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                showMessage(e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            showMessage("Error in camera configuration");
                            createCameraPreview();
                        }
                    };
                    cameraDevice.createCaptureSession(outputSurfaces, stateCallback, mBackgroundHandler);
                    mediaPlayer = MediaPlayer.create(this, R.raw.photo_sound);
                    mediaPlayer.start();
                } catch (CameraAccessException e) {
                    showMessage(e.getLocalizedMessage());
                }
            }
        }
    }

    private void startRecording() {
        try {
            createMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture != null) {
                texture.setDefaultBufferSize(imageSize.getWidth(), imageSize.getHeight());
                Surface previewSurface = new Surface(texture);
                Surface recordSurface = mediaRecorder.getSurface();
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                captureRequestBuilder.addTarget(previewSurface);
                captureRequestBuilder.addTarget(recordSurface);
                CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try {
                            cameraCaptureSession = session;
                            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                            //updatePreview();
                            mediaRecorder.start();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        showMessage("Error in camera configuration");
                    }
                };
                cameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface), stateCallback, null);
                changeVideoButton(false);
                mediaPlayer = MediaPlayer.create(this, R.raw.start_recording);
                mediaPlayer.start();
            }
        } catch (IOException | CameraAccessException e) {
            showMessage(e.getLocalizedMessage());
        }
    }

    private void stopRecording() {
        changeVideoButton(true);
        mediaPlayer = MediaPlayer.create(this, R.raw.stop_recording);
        mediaPlayer.start();
        mediaRecorder.stop();
        mediaRecorder.reset();
        createCameraPreview();
        runOnUiThread(this::setThumbnail);
    }

    private int getRotation(CameraCharacteristics characteristics, Integer deviceOrientation) {
        if (deviceOrientation != null) {
            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            deviceOrientation = orientations.get(deviceOrientation);
            if (deviceOrientation == null) deviceOrientation = 0;
            return (sensorOrientation + deviceOrientation + 360) % 360;
        } else return 0;
    }

    private String getFirstForwardCamera(CameraManager manager) throws CameraAccessException {
        for (String id : manager.getCameraIdList()) {
            if (manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) return id;
        }
        return manager.getCameraIdList()[0];
    }

    private void setFile(boolean img) {
        file = null;
        folder = new File(Utils.FOLDER_NAME);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = img ? "IMG_" + timeStamp + ".jpg" : "REC_" + timeStamp + ".mp4";
        file = new File(getExternalFilesDir(Utils.FOLDER_NAME), "/" + imageFileName);
    }

    private void save(byte[] bytes) throws IOException {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        Utils.getCurrentUserLocation(client, this,
                task -> {
                    if (task.isSuccessful()) {
                        AppImage img = new AppImage(
                                processImage(bytes),
                                task.getResult().getLatitude(),
                                task.getResult().getLongitude(),
                                LocalDateTime.now(),
                                CurrentUser.user.getEmail());
                        db.insert(img, subtask -> {
                            if (subtask.isSuccessful()) {
                                Messages.showMessage(this, "Image saved");
                                AppImageList.imageList.add(img);
                                Utils.orderAppImageList(AppImageList.imageList);
                                setThumbnail();
                            } else {
                                Messages.showMessage(CameraActivity.this, "Error saving image");
                            }
                        });
                    } else {
                        Messages.showMessage(CameraActivity.this, "Error obtaining location");
                    }
                });
    }

    private Bitmap processImage(byte[] bytes) {
        Matrix m = new Matrix();
        m.postRotate(90);
        Bitmap img = Utils.compress(Utils.getBitmap(bytes));
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), m, true);
    }

    private boolean isStoragePermissionGranted() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    private void setThumbnail() {
        db.get(task -> {
            if (task.isSuccessful()) {
                ArrayList<AppImage> imgList = db.collectionToAppImageList(task.getResult());
                if (imgList.size() > 0) {
                    CameraActivity.this.runOnUiThread(() -> btnOpenGallery.setImageBitmap(imgList.get(0).getThumbnail()));
                }
            } else {
                Messages.showMessage(CameraActivity.this, "Error loading images");
            }
        });
    }

    private void changeVideoButton(boolean stop) {
        btnTakeVideo.setImageResource(stop ? R.mipmap.video : R.mipmap.stop_video);
        btnTakeVideo.setOnClickListener(stop ? l -> startRecording() : l -> stopRecording());
        btnTakePhoto.setEnabled(stop);
        btnTakePhoto.setVisibility(stop ? View.VISIBLE : View.INVISIBLE);
        btnOpenGallery.setEnabled(stop);
        btnOpenGallery.setVisibility(stop ? View.VISIBLE : View.INVISIBLE);
    }


    public static Size chooseSize(Size[] choices, int width, int height) {
        for (int i = 0; i < choices.length; i++) {
            Size size = choices[i];
            if (size.getWidth() <= width && size.getHeight() <= height) {
                return size;
            }
        }
        return choices[choices.length-1];
    }

    private void openGallery() {
        startActivity(new Intent(CameraActivity.this, GalleryActivity.class));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_camera);
            textureView = findViewById(R.id.textureView);
            btnTakePhoto = findViewById(R.id.btnTakePhoto);
            btnTakeVideo = findViewById(R.id.btnTakeVideo);
            btnOpenGallery = findViewById(R.id.btnOpenGallery);
            btnBack = findViewById(R.id.btnBack);

            textureView.setSurfaceTextureListener(textureListener);
            btnTakePhoto.setOnClickListener(l -> takePhoto());
            btnTakeVideo.setOnClickListener(l -> startRecording());
            btnOpenGallery.setOnClickListener(l -> openGallery());
            btnBack.setOnClickListener(l -> finish());

            orientations = new HashMap<>();
            orientations.put(Surface.ROTATION_0, 0);
            orientations.put(Surface.ROTATION_90, 90);
            orientations.put(Surface.ROTATION_180, 180);
            orientations.put(Surface.ROTATION_270, 270);

            mediaPlayer = new MediaPlayer();
            db = new AppImageCRUD(CurrentUser.user.getEmail());

            setThumbnail();
        } catch (Exception e) {
            showMessage(e.getLocalizedMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView == null) textureView = findViewById(R.id.textureView);
        if (textureView.isAvailable()) openCamera(textureView.getWidth(), textureView.getHeight());
        else textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showMessage("You must grant necessary permissions.");
                finish();
            }
        }
    }
}

