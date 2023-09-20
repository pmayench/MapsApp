package com.dam2.m08;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.dam2.m08.Camera.CameraActivity;
import com.dam2.m08.Llamadas.AppImageCRUD;
import com.dam2.m08.Objects.AppImage;
import com.example.projecte_maps.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.projecte_maps.databinding.ActivityMapsBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Button btnLogOut;
    ImageButton btnOpenCamera;
    private final int ACCES_LOCATION_REQUEST_CODE = 10001;
    FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseAuth mAuth;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnLogOut = findViewById(R.id.btnLogout);
        btnLogOut.setEnabled(false);
        btnLogOut.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();

        btnOpenCamera.setOnClickListener(v -> startActivity(new Intent(MapsActivity.this, CameraActivity.class)));
        btnLogOut.setOnClickListener(view ->
        {
            mAuth.signOut();
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Marker
        LatLng mrkP9 = new LatLng(41.398325, 2.203191);
        mMap.addMarker(new MarkerOptions().position(mrkP9).title("Institut Poblenou"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mrkP9));

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            enableUserLocation();
            zoomToUserLocation();
        }
        else
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCES_LOCATION_REQUEST_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCES_LOCATION_REQUEST_CODE);
            }
        }
        fillMap();
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocation()
    {
        mMap.setMyLocationEnabled(true);
    }

    @SuppressLint("MissingPermission")
    private void zoomToUserLocation()
    {
        Utils.getCurrentUserLocation(fusedLocationProviderClient, this,
                task -> {
                    if (task.isSuccessful()) {
                        LatLng latLng = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
                    } else {
                        Messages.showMessage(MapsActivity.this, "Error al obtener la ubicación");
                    }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ACCES_LOCATION_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                enableUserLocation();
                zoomToUserLocation();
            }
        }
    }

    private void fillMap() {
        AppImageCRUD db = new AppImageCRUD(CurrentUser.user.getEmail());
        db.get(task -> {
            if (task.isSuccessful()) {
                AppImageList.imageList = db.collectionToAppImageList(task.getResult());
                if (AppImageList.imageList.size() > 0) {
                    for (AppImage img : AppImageList.imageList) {
                        addMarker(img.getLocation(), img.getThumbnail(), img.getDate().format(Utils.dtf));
                    }
                }
            }
        });
    }

    private void addMarker(LatLng latLng, Bitmap bitmap, String title)
    {
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .title(title));
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillMap();
    }
}