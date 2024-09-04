package com.example.cameralessphotography;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private List<List<String>> time_period;

    public DatabaseHelper()
    {
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        collectionRef = db.collection("cameraless_photography_db");
        dataTypeConverter = new DataTypeConverter();
    }
    public void uploadShotResult(Map<String, Object> shotResultMap, final OnUploadCompleteListener onUploadCompleteListener)
    {
        db.collection("experiments").add(shotResultMap)
        .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                onUploadCompleteListener.onComplete(task.isSuccessful());
            }
        });
    }
    public void getPhotoInfoList(Map<String, Object> photograph_parameters, final OnPhotoInfoListRetrievedListener onPhotoInfoListRetrievedListener)
    {
        String station_name = (String)photograph_parameters.get("station_name");
        String weather = (String)photograph_parameters.get("weather");
        String datetime = (String)photograph_parameters.get("datetime");

        int timeSlot = dataTypeConverter.getTimeSlot(datetime);
        if(timeSlot == -1)
        {
            onPhotoInfoListRetrievedListener.onFailure(new Exception());
            return;
        }

        double latitude = (double)photograph_parameters.get("latitude");
        double longitude = (double)photograph_parameters.get("longitude");
        float roll = (float)photograph_parameters.get("roll");
        float yaw = (float)photograph_parameters.get("yaw");
        float pitch = (float)photograph_parameters.get("pitch");
        float weigh_roll = (float)0.05;
        float weigh_yaw = (float)0.90;
        float weigh_pitch = (float)0.05;


        List<String> longiRoundList = dataTypeConverter.getLongiRoundList(longitude);

        List<Integer> orientationArray = dataTypeConverter.getOrientationList(pitch);

        Query query = collectionRef.whereEqualTo("station_name", station_name)
                .whereEqualTo("weather", weather)
                .whereIn("longitude_round", longiRoundList)
                .whereGreaterThan("latitude", latitude - 0.0009)
                .whereLessThan("latitude", latitude + 0.0009)
                .whereIn("orientation", orientationArray)
                .limit(50);

                query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Map<String, Object>> photoInfoList = new ArrayList<>();
                        List<Float> errorList = new ArrayList<>();
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots)
                        {
                            String tDateTime = (String)document.getData().get("datetime");
                            if(dataTypeConverter.getTimeSlot(tDateTime) != timeSlot) continue;

                            Double tRoll = (Double)document.getData().get("roll");
                            float error_roll = (float)Math.abs(tRoll - roll);
                            if(error_roll > 180) error_roll = 360 - error_roll;
                            error_roll = error_roll * weigh_roll;

                            Double tYaw = (Double)document.getData().get("yaw");
                            float error_yaw = (float)Math.abs(tYaw - yaw);
                            if(error_yaw > 180) error_yaw = 360 - error_yaw;
                            error_yaw = error_yaw * weigh_yaw;

                            Double tPitch = (Double)document.getData().get("pitch");
                            float error_pitch = (float)Math.abs(tPitch - pitch);
                            error_pitch = error_pitch * weigh_pitch;

                            float error = error_roll + error_yaw + error_pitch;

                            int idx = 0;
                            for(;idx < errorList.size(); ++idx)
                            {
                                if(error < errorList.get(idx)) break;
                            }
                            if(idx > 10) continue;
                            errorList.add(idx, error);
                            photoInfoList.add(idx, document.getData());
                            if(errorList.size() > 10) errorList.remove(errorList.get(10));
                            if(photoInfoList.size() > 10) photoInfoList.remove(photoInfoList.get(10));
                        }
                        onPhotoInfoListRetrievedListener.onPhotoInfoListRetrieved(photoInfoList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onPhotoInfoListRetrievedListener.onFailure(e);
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        onPhotoInfoListRetrievedListener.onFailure(new Exception());
                    }
                });
    }

    public interface OnPhotoInfoListRetrievedListener
    {
        void onPhotoInfoListRetrieved(List<Map<String, Object>> photoInfoList);
        void onFailure(Exception e);
    }

    public interface OnUploadCompleteListener {
        void onComplete(boolean success);
    }
}
