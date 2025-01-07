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
import android.graphics.Bitmap;
import android.location.*;
import android.location.Address;
import android.os.*;
import android.provider.Settings;
import android.util.Log;

import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Lx_chart;
import org.ast.findmaimaidx.been.Lx_data_scores;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.ui.Scores;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class LocationUpdateService extends Service {

    private static final String TAG = "LocationUpdateService";
    private static final long UPDATE_INTERVAL = 30000; // 30 seconds
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "location_channel";
    private Context context = this;
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
                sendGetRequest();
                sendBroadcast(broadcastIntent);
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
                //requestLocationUpdates();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        //requestLocationUpdates();
        Log.d("服务", "服务已启动");
        // 检查位置服务是否启用
        if (!isLocationEnabled()) {
            Log.e(TAG, "Location services are not enabled");
            // 提示用户启用位置服务
        }
        SharedPreferences settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
        String gpsapi;
        if(settingProperties.contains("gpsapi")) {
            gpsapi = settingProperties.getString("gpsapi", "");
        }else {
            String originalString = new Random(11111).toString();
            settingProperties.edit().putString("gpsapi", Base64.getEncoder().encodeToString(originalString.getBytes())).apply();
            gpsapi = Base64.getEncoder().encodeToString(originalString.getBytes());
        }
        new Thread(()->{
            try {
                while (true) {
                    Thread.sleep(UPDATE_INTERVAL);
                    requestSingleUpdate();

                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    x = String.valueOf(location.getLatitude());
                    y = String.valueOf(location.getLongitude());
                    OkHttpClient client = new OkHttpClient();
                    // 创建 RequestBody
                    // 创建 Request
                    Request request = new Request.Builder()
                            .url("http://www.godserver.cn:11451/api/ago/update?key=" + gpsapi +"&x=" + x + "&y=" + y)
                            .build();

                    // 使用 AsyncTask 发送请求
                    new SendRequestTask(client, request).execute();
                    Geocoder geocoder = new Geocoder(LocationUpdateService.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses.size() > 0) {
                            Address address = addresses.get(0);
                            city = address.getLocality();

                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    sendGetRequest();}
            }catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }).start();
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

    @SuppressLint("StaticFieldLeak")
    private void sendGetRequest() {
        try {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    OkHttpClient client = new OkHttpClient();
                    try {
                        String web = "http://www.godserver.cn:11451/api/mai/v1/search?prompt1=" + city.split("市")[0] + "&status=市";
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
                        e.printStackTrace();
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
                        double distance = DistanceCalculator.calculateDistance(Double.parseDouble(x), Double.parseDouble(y), b.get(0).getX(), b.get(0).getY());
                        if(distance < 0.3) {
                            Log.d("distance",b.get(0).getName() + "可能有人");
                            sendUpdateJiTing(b.get(0));

                        }
                    }
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {
        }.getType();
        if (jsonString.equals("BedWeb")) {
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
                        String web = "http://www.godserver.cn:11451/api/mai/v1/placePeo?";
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", place.getId());
                        jsonObject.put("hashid", getStableUUID(context));

                        // 将JSON对象转换为RequestBody
                        MediaType JSON = MediaType.get("application/json; charset=utf-8");
                        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                        @SuppressLint("StaticFieldLeak") Request request = new Request.Builder()
                                .url(web)
                                .post(body)
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
        } catch (Exception e) {

        }
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
    public static String getStableUUID(Context context) {
        // 获取设备的Android ID
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // 使用Android ID生成UUID
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) androidId.hashCode() << 32));

        return deviceUuid.toString();
    }


    private class SendRequestTask extends AsyncTask<Void, Void, String> {
        private OkHttpClient client;
        private Request request;

        public SendRequestTask(OkHttpClient client, Request request) {
            this.client = client;
            this.request = request;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("Scores", result);
        }
    }
}
