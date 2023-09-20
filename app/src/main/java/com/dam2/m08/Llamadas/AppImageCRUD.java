package com.dam2.m08.Llamadas;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dam2.m08.Objects.AppImage;
import com.dam2.m08.Utils;
import com.example.projecte_maps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AppImageCRUD {
    private final FirebaseFirestore db;
    private final String USERNAME;

    private final String COLLECTION_NAME = "users";
    private final String SUBCOLLECTION_NAME = "photos";

    private static final String FIELD_ID = "id";
    private static final String FIELD_IMAGE = "image";
    private static final String FIELD_LATITUDE = "latitude";
    private static final String FIELD_LONGITUDE = "longitude";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_USER = "user";

    private final DateTimeFormatter dtfTimestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public AppImageCRUD(String username) {
        USERNAME = username;
        db = FirebaseFirestore.getInstance();
    }

    public void insert(AppImage appImage, OnCompleteListener<Void> onComplete) {
        appImage.setId(appImage.getDate().format(dtfTimestamp));
        update(appImage, onComplete);
    }

    public void update(AppImage appImage, OnCompleteListener<Void> onComplete) {
        Map<String, Object> document = new HashMap<>();
        document.put(FIELD_ID, appImage.getId());
        document.put(FIELD_LATITUDE, appImage.getLocation().latitude);
        document.put(FIELD_LONGITUDE, appImage.getLocation().longitude);
        document.put(FIELD_DATE, appImage.getDate().format(Utils.dtf));
        document.put(FIELD_USER, appImage.getUser());
        document.put(FIELD_IMAGE, appImage.getImageString());
        db.collection(COLLECTION_NAME).document(USERNAME).collection(SUBCOLLECTION_NAME).document(appImage.getId()).set(document).addOnCompleteListener(onComplete);
    }

    public void delete(String id, OnCompleteListener<Void> onComplete) {
        db.collection(COLLECTION_NAME).document(USERNAME).collection(SUBCOLLECTION_NAME).document(id).delete().addOnCompleteListener(onComplete);
    }

    public void get(OnCompleteListener<QuerySnapshot> onComplete) {
        db.collection(COLLECTION_NAME).document(USERNAME).collection(SUBCOLLECTION_NAME).get().addOnCompleteListener(onComplete);
    }

    public void get(String id, OnCompleteListener<DocumentSnapshot> onComplete) {
        db.collection(COLLECTION_NAME).document(USERNAME).collection(SUBCOLLECTION_NAME).document(id).get().addOnCompleteListener(onComplete);
    }

    public AppImage documentToAppImage(DocumentSnapshot document) {
        String id = document.getString(FIELD_ID);
        Bitmap image = Utils.getBitmap(document.getString(FIELD_IMAGE));
        //Bitmap thumbnail = Utils.getBitmap(document.getString(FIELD_THUMBNAIL));
        LocalDateTime date = LocalDateTime.parse(document.getString(FIELD_DATE), Utils.dtf);
        Double lat = document.getDouble(FIELD_LATITUDE);
        Double lon = document.getDouble(FIELD_LONGITUDE);
        String username = document.getString(FIELD_USER);
        return new AppImage(id == null ? "" : id,
                            image == null ? BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.foto_error) : image,
                            //thumbnail == null ? Utils.getThumbnail(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.foto_error)) : thumbnail,
                            lat == null ? 0 : lat,
                            lon == null ? 0 : lon,
                            date == null ? LocalDateTime.now() : date,
                            username == null ? "" : username);
    }

    public ArrayList<AppImage> collectionToAppImageList(QuerySnapshot collection) {
        ArrayList<AppImage> imageList = new ArrayList<>();
        for (DocumentSnapshot doc : collection) {
            imageList.add(documentToAppImage(doc));
        }
        return Utils.orderAppImageList(imageList);
    }
}
