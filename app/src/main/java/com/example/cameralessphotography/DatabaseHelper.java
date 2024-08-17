package com.example.cameralessphotography;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private CollectionReference collectionRef;

    private DataTypeConverter dataTypeConverter;

    public DatabaseHelper()
    {
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        collectionRef = db.collection("cameraless_photography_db");
        dataTypeConverter = new DataTypeConverter();
    }

    public void getPhotoUrlList(Map<String, Object> photograph_parameters, final OnPhotoUrlListRetrievedListener onPhotoUrlListRetrievedListener)
    {
        final List<String> photoUrlList = new ArrayList<>();
        String station_name = (String)photograph_parameters.get("station_name");
        String weather = (String)photograph_parameters.get("weather");
        double latitude = (double)photograph_parameters.get("latitude");
        double longitude = (double)photograph_parameters.get("longitude");
        float roll = (float)photograph_parameters.get("roll");
        float yaw = (float)photograph_parameters.get("yaw");
        float pitch = (float)photograph_parameters.get("pitch");

        List<String> longiRoundList = new ArrayList<>();
        for (double longi : Arrays.asList(longitude-0.001, longitude, longitude+0.001))
            longiRoundList.add(String.valueOf(Math.round(longi*1000)*0.001));

        List<Integer> orientationArray;
        if(pitch<-45||pitch>45)
        {
            orientationArray = Arrays.asList(5, 6, 7, 8);
        }
        else
        {
            orientationArray = Arrays.asList(1, 2, 3, 4);
        }

        Query query = collectionRef.whereEqualTo("station_name", station_name)
                .whereEqualTo("weather", weather)
                .whereIn("longitude_round", longiRoundList)
//                .whereGreaterThanOrEqualTo("roll", roll - 90)
//                .whereLessThanOrEqualTo("roll", roll + 90)
//                .whereGreaterThanOrEqualTo("yaw", yaw - 90)
//                .whereLessThanOrEqualTo("yaw", yaw + 90)
//                .whereGreaterThanOrEqualTo("pitch", pitch - 90)
//                .whereLessThanOrEqualTo("pitch", pitch + 90)
                .whereGreaterThan("latitude", latitude - 0.0009)
                .whereLessThan("latitude", latitude + 0.0009)
                .whereIn("orientation", orientationArray);
//                .whereGreaterThan("longitude", longitude - 0.0009)
//                .whereLessThan("longitude", longitude + 0.0009)
//                .limit(10);

                query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> photoUrlList = new ArrayList<>();
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots)
                        {
                            Double tYaw = (Double)document.getData().get("yaw");
                            tYaw += 180;
                            float ceilYaw = (yaw + 225) % 360;
                            float floorYaw = (yaw + 135) % 360;


                            boolean rule_fit = false;

                            if(ceilYaw<floorYaw)
                            {
                                if(tYaw <= ceilYaw || tYaw >= floorYaw) rule_fit=true;
                            }
                            else
                            {
                                if(tYaw <= ceilYaw && tYaw >= floorYaw) rule_fit=true;
                            }

                            if(!rule_fit) continue;

                            Double tRoll = (Double)document.getData().get("roll") + 180;
                            float ceilRoll = (roll + 225) % 360;
                            float floorRoll = (roll + 135) % 360;

                            if(ceilRoll<floorRoll)
                            {
                                if(tRoll > ceilRoll && tRoll < floorRoll) rule_fit=false;
                            }
                            else
                            {
                                if(tRoll > ceilRoll || tRoll < floorRoll) rule_fit=false;
                            }


                            if(rule_fit) photoUrlList.add((String)document.getData().get("photo_url"));
                        }
                        onPhotoUrlListRetrievedListener.onPhotoUrlListRetrieved(photoUrlList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onPhotoUrlListRetrievedListener.onFailure(e);
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        onPhotoUrlListRetrievedListener.onFailure(new Exception());
                    }
                });
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        task.getException();
//                        if(task.isSuccessful())
//                        {
//                            List<String> photoUrlList = new ArrayList<>();
//                            for (QueryDocumentSnapshot document : task.getResult())
//                            {
//                                photoUrlList.add((String)document.getData().get("photo_url"));
//                            }
//                            onPhotoUrlListRetrievedListener.onPhotoUrlListRetrieved(photoUrlList);
//                        }
//                        else
//                        {
//                            Exception exception = task.getException();
//                            if (exception != null) {
//                                onPhotoUrlListRetrievedListener.onFailure(exception);
//                            } else {
//                                onPhotoUrlListRetrievedListener.onFailure(new Exception("Unknown error occurred."));
//                            }
//                            //onPhotoUrlListRetrievedListener.onFailure(task.getException());
//                        }
//                    }
//                });
    }

    public interface OnPhotoUrlListRetrievedListener
    {
        void onPhotoUrlListRetrieved(List<String> photoUrlList);
        void onFailure(Exception e);
    }
}
