package com.example.cameralessphotography;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends Activity {
    private Button shotBTN, menuBTN, weatherBTN;
    private ImageView photoIV;
    private TextView testTV;
    private ProgressBar pb;
    private WeatherAPICaller weatherAPICaller;
    private HashMap<String, String> weatherInfo;
    private String blob, photo_base64, datetime, uploadStr;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastKnownLocation;
    private Bundle bd;
    private float[] accelerometerValue, gyroscopeValue, megnetometerValue, orientationArray;
    private SensorDataReader sensorDataReader;
    private DatabaseHelper databaseHelper;
    private DataTypeConverter dataTypeConverter;
    private Map<String, String> camera_settings;
    private Map<String, Object> photograph_parameters;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        sensorDataReader.startListening();
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorDataReader.stopListening();
    }
    private void init() {
        requestPermission();

        shotBTN = findViewById(R.id.shotButton);
        menuBTN = findViewById(R.id.menuButton);
        weatherBTN = findViewById(R.id.weatherButton);
        photoIV = findViewById(R.id.photoImageView);
        testTV = findViewById(R.id.testTextView);
        pb = findViewById(R.id.mainProgressBar);

        weatherAPICaller = new WeatherAPICaller();
        sensorDataReader = new SensorDataReader(this);
        databaseHelper = new DatabaseHelper();
        dataTypeConverter = new DataTypeConverter();

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bd = getIntent().getExtras();
        if(bd == null) bd = new Bundle();
        initCameraSettings();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        while(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {}
        };

        boolean provider_exist = false;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1.0f, locationListener);
            provider_exist = true;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1.0f, locationListener);
            provider_exist = true;
        }
        if (!provider_exist)
        {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_SHORT).show();
            return;
        }

        clickListeners();
    }
    private void clickListeners() {
        shotBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pb.getVisibility()==View.INVISIBLE) pb.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            lastKnownLocation = getLocation();
                            if(lastKnownLocation==null)
                            {
                                onUIShotProcess(2);
                                return;
                            }

                            onUIShotProcess(0);
                            //Toast.makeText(MainActivity.this, "拍攝中，請稍後...", Toast.LENGTH_SHORT).show();

                            accelerometerValue = sensorDataReader.getAccelerometerValue();
                            gyroscopeValue = sensorDataReader.getGyroscopeValue();
                            megnetometerValue = sensorDataReader.getMegnetometerValue();

                            float[] rotationMatrix = new float[9];
                            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValue, megnetometerValue);
                            orientationArray = new float[3];
                            SensorManager.getOrientation(rotationMatrix, orientationArray);
                            for(int i = 0; i < 3; ++i)
                            {
                                orientationArray[i] = (float)Math.toDegrees(orientationArray[i]);
                            }

                            datetime = sdf.format(new Date());

                            weatherInfo = weatherAPICaller.getWeatherInfo(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                            if(!setPhotograph_parameters())
                            {
                                onUIShotProcess(2);
                                return;
                            }

                            //photograph_parameters.put("weather information", weatherInfo);
                            //photograph_parameters.put("orientation array", orientationArray);

                            databaseHelper.getPhotoInfoList(photograph_parameters, new DatabaseHelper.OnPhotoInfoListRetrievedListener() {
                                @Override
                                public void onPhotoInfoListRetrieved(List<Map<String, Object>> photoInfoList) {
                                    if(!photoInfoList.isEmpty())
                                    {
                                        Glide.with(MainActivity.this)
                                                .load(photoInfoList.get(0).get("photo_url"))
                                                .into(photoIV);
                                        databaseHelper.uploadShotResult(
                                                dataTypeConverter.getShotResultMap4Firebase(photoInfoList.get(0), photograph_parameters),
                                                new DatabaseHelper.OnUploadCompleteListener() {
                                                    @Override
                                                    public void onComplete(boolean success) {
                                                        if(success) onUIShotProcess(1);
                                                        else onUIShotProcess(4);
                                                    }
                                                });
                                    }
                                    else
                                    {
                                        onUIShotProcess(3);
                                    }

                                }

                                @Override
                                public void onFailure(Exception e) {
                                    onUIShotProcess(2);
                                }
                            });
//                          Thread thread = new Thread(downloadThread);
//                          thread.start();
                        }
                        catch (JSONException|InterruptedException e)
                        {
                            onUIShotProcess(2);
                        }
                    }
                }).start();
            }
        });

        weatherBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastKnownLocation = getLocation();
                if(lastKnownLocation==null) return;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                testTV.setText("查詢中...");
                            }
                        });
                    }
                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            weatherInfo = weatherAPICaller.getWeatherInfo(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        }
                        catch (JSONException|InterruptedException e)
                        {
                            weatherInfo.clear();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!weatherInfo.isEmpty())
                                    testTV.setText(weatherInfo.get("weather"));
                                else
                                    testTV.setText("請稍後再試");
                            }
                        });
                    }
                }).start();
            }
        });

        menuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                if(!bd.isEmpty()) intent.putExtras(bd);
                startActivity(intent);
                finish();
            }
        });
    }
    private void onUIShotProcess(int mode)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (mode)
                {
                    case 0://開始拍照
                        Toast.makeText(MainActivity.this, "拍攝中，請稍後...", Toast.LENGTH_SHORT).show();
                        break;
                    case 1://拍攝完成
                        if(pb.getVisibility()==View.VISIBLE) pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "拍攝完成", Toast.LENGTH_SHORT).show();
                        break;
                    case 2://拍攝失敗
                        if(pb.getVisibility()==View.VISIBLE) pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "拍攝失敗，請稍後再試...", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        if(pb.getVisibility()==View.VISIBLE) pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "找不到條件相符的照片!", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        if(pb.getVisibility()==View.VISIBLE) pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "拍攝完成，但實驗結果上傳失敗。", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

    }
    private boolean setPhotograph_parameters()
    {
        if(weatherInfo.isEmpty()) return false;
        photograph_parameters = dataTypeConverter.getWeatherInfoMap4FirebaseQuery(weatherInfo);
        photograph_parameters.put("datetime", datetime);
        photograph_parameters.put("latitude", lastKnownLocation.getLatitude());
        photograph_parameters.put("longitude", lastKnownLocation.getLongitude());
        photograph_parameters.put("pitch", orientationArray[1]);
        photograph_parameters.put("roll", orientationArray[2]);
        photograph_parameters.put("yaw", orientationArray[0]);
        return true;
    }
    private Location getLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location==null)
        {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    private JSONObject getUploadObjectForMySQL()
    {
        try {

            JSONObject uploadObject = new JSONObject();

            uploadObject.put("weather", weatherInfo.get("weather"));
            uploadObject.put("station_name", weatherInfo.get("StationName"));
            uploadObject.put("latitude", lastKnownLocation.getLatitude());
            uploadObject.put("longitude", lastKnownLocation.getLatitude());
            uploadObject.put("roll", orientationArray[2]);
            uploadObject.put("yaw", orientationArray[0]);
            uploadObject.put("pitch", orientationArray[1]);

            return uploadObject;
        }
        catch (JSONException e)
        {
            return (new JSONObject());
        }
    }

    private void initCameraSettings()
    {
        camera_settings = new HashMap<>();
        if(bd.isEmpty())
        {
            camera_settings.put("orientation", "Auto");
            camera_settings.put("aspect_ratio", "Auto");
            camera_settings.put("exposure", "Auto");
            camera_settings.put("white_balance", "Auto");
            camera_settings.put("focalLen", "Auto");
        }
        else
        {
            camera_settings.put("orientation", bd.getString("orientation"));
            camera_settings.put("aspect_ratio", bd.getString("aspect_ratio"));
            camera_settings.put("exposure", bd.getString("exposure"));
            camera_settings.put("white_balance", bd.getString("white_balance"));
            camera_settings.put("focalLen", bd.getString("focalLen"));
        }
    }

    private void requestPermission()
    {
        boolean[] permissionHasGone = new boolean[7];
        permissionHasGone[0] = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        permissionHasGone[1] = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        permissionHasGone[2] = checkSelfPermission(Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED;
        permissionHasGone[3] = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        permissionHasGone[4] = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        permissionHasGone[5] = checkSelfPermission(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS) == PackageManager.PERMISSION_GRANTED;
        permissionHasGone[6] = checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;


        String[] permissionStr = new String[7];
        permissionStr[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        permissionStr[1] = Manifest.permission.READ_EXTERNAL_STORAGE;
        permissionStr[2] = Manifest.permission.ACCESS_MEDIA_LOCATION;
        permissionStr[3] = Manifest.permission.ACCESS_FINE_LOCATION;
        permissionStr[4] = Manifest.permission.ACCESS_COARSE_LOCATION;
        permissionStr[5] = Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS;
        permissionStr[6] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int numOfPermissions = 0;
            for(int i=0;i<permissionHasGone.length;++i)
                if(!permissionHasGone[i])
                    ++numOfPermissions;
            if(numOfPermissions==0) return;
            String[] permissions = new String[numOfPermissions];

            int index=0;
            for(int i=0;i<permissionHasGone.length;++i)
            {
                if(!permissionHasGone[i])
                {
                    permissions[index] = permissionStr[i];
                    ++index;
                }
            }
            requestPermissions(permissions, 100);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("離開").setMessage("是否要離開?")
                    .setPositiveButton("離開", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finishAffinity();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }).show();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解除位置監聽器的註冊
        locationManager.removeUpdates(locationListener);
    }
}
