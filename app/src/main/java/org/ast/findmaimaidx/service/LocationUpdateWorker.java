package org.ast.findmaimaidx.service;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Place;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocationUpdateWorker extends Worker {

    private static final String TAG = "LocationUpdateWorker";
    private static final long UPDATE_INTERVAL = 3000; // 30 seconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static int id = 0;
    private static String gpsapi;

    public LocationUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 处理权限未授予的情况
            return Result.failure();
        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String city = address.getLocality();
                        String x = String.valueOf(location.getLatitude());
                        String y = String.valueOf(location.getLongitude());

                        SharedPreferences settingProperties = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
                        if (settingProperties.contains("gpsapi")) {
                            gpsapi = settingProperties.getString("gpsapi", "");
                        } else {
                            String originalString = new Random(11111111).toString();
                            settingProperties.edit().putString("gpsapi", Base64.getEncoder().encodeToString(originalString.getBytes())).apply();
                            gpsapi = Base64.getEncoder().encodeToString(originalString.getBytes());
                        }
                        gpsapi = gpsapi.substring(0, 12);

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://mai.godserver.cn:11451/api/mai/v1/search?prompt1=" + city.split("市")[0] + "&status=市")
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e(TAG, "Request failed: " + e.getMessage());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    String result = response.body().string();
                                    List<Place> places = parseJsonToPlaceList(result);
                                    double min = 100000;
                                    for (int i = 0; i < places.size(); i++) {
                                        Place p = places.get(i);
                                        double distance = DistanceCalculator.calculateDistance(Double.parseDouble(x), Double.parseDouble(y), p.getY(), p.getX());
                                        if (distance < min) {
                                            if (!(p.getName() == null || p.getName().equals(""))) {
                                                min = distance;
                                                id = i;
                                            }
                                        }
                                    }
                                    Log.d("Scores", places.get(id).getName() + ";Distance:" + min);
                                    if (min < 0.5) {
                                        sendUpdateJiTing(client, id, gpsapi);
                                    }
                                } else {
                                    Log.e(TAG, "Request failed: " + response.code());
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, Looper.getMainLooper());

        return Result.success();
    }

    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {}.getType();
        if (jsonString.equals("BedWeb")) {
            return new ArrayList<>();
        }
        return gson.fromJson(jsonString, placeListType);
    }

    private void sendUpdateJiTing(OkHttpClient client, int id, String gpsapi) {
        String web = "http://www.godserver.cn:11451/api/mai/v1/placePeo?";
        RequestBody body = RequestBody.create("", MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(web + "id=" + id + "&key=" + gpsapi)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "UpdateJiTing error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "UpdateJiTing successful: " + response.body().string());
                } else {
                    Log.e(TAG, "UpdateJiTing failed: " + response.code());
                }
            }
        });
    }
}
