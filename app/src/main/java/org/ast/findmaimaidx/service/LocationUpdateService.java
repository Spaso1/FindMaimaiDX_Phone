package org.ast.findmaimaidx.service;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.MainLaunch;
import org.ast.findmaimaidx.PageActivity;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.utill.PlaceAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class LocationUpdateService extends Service {

    private static final String TAG = "LocationUpdateService";
    private static final long UPDATE_INTERVAL = 3000; // 30 seconds

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Handler handler;
    private Runnable runnable;
    public static List<Place> a = new ArrayList<>();
    public static List<Place> b = new ArrayList<>();
    public static String city;
    private String x;
    private String y;
    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d("服务","后台定位服务启动");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Log.d("服务", "Location updated");
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                    // 发送广播
                    Intent broadcastIntent = new Intent("LOCATION_UPDATE");
                    broadcastIntent.putExtra("latitude", location.getLatitude());
                    broadcastIntent.putExtra("longitude", location.getLongitude());
                    x = String.valueOf(location.getLatitude());
                    y = String.valueOf(location.getLongitude());
                    sendGetRequest();
                    sendBroadcast(broadcastIntent);
                }
            }
        };

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                requestLocationUpdates();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private void sendGetRequest() {
        try {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    OkHttpClient client = new OkHttpClient();
                    try {
                        String web = "http://www.godserver.cn:11451/search?prompt1=" + city.split("市")[0] + "&status=市";
                        @SuppressLint("StaticFieldLeak") Request request = new Request.Builder()
                                .url(web)
                                .build();

                        try (Response response = client.newCall(request).execute()) {
                            if (((Response) response).isSuccessful()) {
                                return response.body().string();
                            } else {
                                return "Error: " + response.code();
                            }
                        } catch (Exception e) {
                            Log.e("OkHttp", "Error: " + e.getMessage());
                            return "Error: " + e.getMessage();
                        }
                    } catch (Exception e) {
                        return "BedWeb";
                    }
                }

                @Override
                protected void onPostExecute(String result) {
                    a.clear();
                    b.clear();
                    if (!result.equals("BedWeb")) {
                        a = parseJsonToPlaceList(result);
                        // 设置适配器

                        for (Place p : a) {
                            try {
                                if (p.getName().equals("个人位置")) {
                                    city = p.getCity();
                                }
                                if (p.getIsUse() == 1) {
                                    b.add(p);
                                    System.out.println(p.getName());
                                }
                            } catch (Exception e) {

                            }
                        }
                        a.clear();
                        TreeMap<Double, Place> treeMap = new TreeMap<>();

                        for (Place p : b) {
                            double distance = DistanceCalculator.calculateDistance(Double.parseDouble(x), Double.parseDouble(y), p.getX(), p.getY());
                            treeMap.put(distance, p);
                        }
                        for (Double key : treeMap.keySet()) {
                            a.add(treeMap.get(key));
                        }
                        boolean flag2 = true;
                        sendUpdateJiTing(b.get(0));
                    }
                }
            }.execute();
        }catch (Exception e ){
            e.printStackTrace();
        }
    }
    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {
        }.getType();
        if(jsonString.equals("BedWeb")) {
            return null;
        }
        return gson.fromJson(jsonString, placeListType);
    }


    @SuppressLint("StaticFieldLeak")
    private void sendUpdateJiTing(Place place) {
        try {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    OkHttpClient client = new OkHttpClient();
                    try {
                        String web = "http://www.godserver.cn:11451/updatePeo?place=" + place.getId();
                        @SuppressLint("StaticFieldLeak") Request request = new Request.Builder()
                                .url(web)
                                .build();

                        try (Response response = client.newCall(request).execute()) {
                            if (((Response) response).isSuccessful()) {
                                return response.body().string();
                            } else {
                                return "Error: " + response.code();
                            }
                        } catch (Exception e) {
                            Log.e("OkHttp", "Error: " + e.getMessage());
                            return "Error: " + e.getMessage();
                        }
                    } catch (Exception e) {
                        return "BedWeb";
                    }
                }
                @Override
                protected void onPostExecute(String result) {

                }
            }.execute();
        }catch (Exception e ){

        }
    }
}
