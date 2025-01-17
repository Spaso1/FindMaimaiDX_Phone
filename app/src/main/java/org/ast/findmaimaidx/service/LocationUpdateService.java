package org.ast.findmaimaidx.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ast.findmaimaidx.R;
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

public class LocationUpdateService extends Service {

    private static final String TAG = "LocationUpdateService";
    private static final long UPDATE_INTERVAL = 3000; // 30 seconds
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "location_channel";
    private Context context = this;
    private static int id = 0;
    private static String gpsapi;

    private LocationManager locationManager = null;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static LocationListener locationListener = null;
    private Handler handler;
    private Runnable runnable;
    public static List<Place> a = new ArrayList<>();
    public static List<Place> b = new ArrayList<>();
    public static String city;
    private String x;
    private String y;

    @Override
    @SuppressLint("MissingPermission")
    public void onCreate() {
        super.onCreate();
        Log.d("服务", "后台定位服务启动");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("服务", "Location updated");
                Log.d(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                // 输出地址日志
                Log.d(TAG, "Address: Latitude=" + location.getLatitude() + ", Longitude=" + location.getLongitude());
                // 发送广播
                Intent broadcastIntent = new Intent("LOCATION_UPDATE");
                broadcastIntent.putExtra("latitude", location.getLatitude());
                broadcastIntent.putExtra("longitude", location.getLongitude());
                x = String.valueOf(location.getLatitude());
                y = String.valueOf(location.getLongitude());
                Geocoder geocoder = new Geocoder(LocationUpdateService.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String detail = address.getAddressLine(0);
                        city = address.getLocality();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                SharedPreferences settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
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

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "Status changed: " + provider + ", " + status);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d(TAG, "Provider enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d(TAG, "Provider disabled: " + provider);
            }
        };

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                requestSingleUpdate();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        Log.d("服务", "服务已启动");

        // 检查位置服务是否启用
        if (!isLocationEnabled()) {
            Log.e(TAG, "Location services are not enabled");
            // 提示用户启用位置服务
        }

        handler.post(runnable);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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
        stopForeground(true);
    }

    private void requestLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER; // 或 LocationManager.NETWORK_PROVIDER

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 处理权限未授予的情况
            return;
        }

        locationManager.requestLocationUpdates(provider, UPDATE_INTERVAL, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
    }

    private void requestSingleUpdate() {
        String provider = LocationManager.GPS_PROVIDER;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 处理权限未授予的情况
            return;
        }

        locationManager.requestSingleUpdate(provider, locationListener, Looper.getMainLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Update Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Update Service")
                .setContentText("Updating location every 3 seconds")
                .setSmallIcon(R.drawable.ic_launcher) // 替换为您的通知图标资源
                .build();
    }
}
