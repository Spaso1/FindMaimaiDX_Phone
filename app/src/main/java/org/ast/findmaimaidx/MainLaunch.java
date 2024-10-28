package org.ast.findmaimaidx;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.utill.AddressParser;
import org.ast.findmaimaidx.utill.PlaceAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static androidx.core.location.LocationManagerCompat.requestLocationUpdates;

public class MainLaunch extends AppCompatActivity {
    public static final int LOCATION_CODE = 301;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;

    private LocationManager locationManager;
    private RecyclerView recyclerView;
    private PlaceAdapter placeAdapter;
    private String locationProvider = null;
    private TextView addressTextView;

    private String tot;
    private String x;
    private String y;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private PlaceAdapter adapter;
    public static String province;
    public static String city;
    List<Place> a = new ArrayList<>();
    List<Place> b = new ArrayList<>();
    private boolean flag = true;
    private double tagXY[] = new double[2];
    private String tagplace;
    @Override
    @SuppressLint({"MissingInflatedId", "Range"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 检查网络权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // 请求网络权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    REQUEST_CODE_PERMISSIONS);
        }
        addressTextView = findViewById(R.id.textView);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0x123);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        


    }

    @SuppressLint("StaticFieldLeak")
    private void sendGetRequest() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                String web = "http://www.godserver.cn:11451/search?prompt1=" + city.split("市")[0] + "&status=市";
                System.out.println(web);
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
            }

            @Override
            protected void onPostExecute(String result) {
                a = parseJsonToPlaceList(result);
                // 设置适配器

                for (Place p : a) {
                    if (p.getIsUse() == 1) {
                        b.add(p);
                    }
                }
                a.clear();
                TreeMap<Double, Place> treeMap = new TreeMap<>();

                for (Place p : b) {
                    double distance = DistanceCalculator.calculateDistance(Double.parseDouble(x), Double.parseDouble(y), p.getX(), p.getY());
                    p.setName(p.getName() + " 距离您" + String.format(Locale.CHINA, "%.2f", distance) + "km");
                    treeMap.put(distance, p);
                }
                for (Double key : treeMap.keySet()) {
                    a.add(treeMap.get(key));
                }
                adapter = new PlaceAdapter(a, new PlaceAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Place place) {
                        tagXY = new double[]{place.getX(), place.getY()};
                        Toast.makeText(MainLaunch.this, "Clicked: " + place.getName(), Toast.LENGTH_SHORT).show();
                        tagplace = place.getName();
                        showNavigationOptions();

                    }
                });
                recyclerView.setAdapter(adapter);
                // 设置Toolbar
                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);// 设置Toolbar标题

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("FindMaimaiDX - " + a.size() + " 店铺" + "\n" + tot);
                }

                for (Place p : a) {
                    if (p.getX() == 0.0) {
                        //Log.i(p.getId() + "", p.getName() + "没有坐标");
                    }
                }
            }
        }.execute();
    }

    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {
        }.getType();
        return gson.fromJson(jsonString, placeListType);
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x123 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //创建locationManger对象
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //获取最新的定位信息
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            x = String.valueOf(lastKnownLocation.getLongitude());
            y = String.valueOf(lastKnownLocation.getLatitude());
            if (lastKnownLocation != null) {
                Geocoder geocoder = new Geocoder(MainLaunch.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String detail = address.getAddressLine(0);
                        addressTextView.setText(detail);
                        tot = detail;
                        province = address.getAdminArea();
                        city = address.getLocality();
                        extracted();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    addressTextView.setText("Error getting address");
                }
            }
        }
            //每隔三秒获取一次GPS信息
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 8f, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    if(flag) {
                        x = String.valueOf(location.getLongitude());
                        y = String.valueOf(location.getLatitude());
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(MainLaunch.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses.size() > 0) {
                                    Address address = addresses.get(0);
                                    String detail = address.getAddressLine(0);
                                    addressTextView.setText(detail);
                                    tot = detail;
                                    province = address.getAdminArea();
                                    city = address.getLocality();
                                    extracted();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                addressTextView.setText("Error getting address");
                            }
                        }
                        flag = false;
                    }

                }
            });
        }

    private void extracted() {
        //tot = tot.split("\"")[1];
        Log.i("TAG", "x=" + x + ";y=" + y);
        //tot = "天津市东丽区民航大学";
        try {
            AddressParser.parseAddress(tot);

        } catch (Exception e) {
            Toast.makeText(MainLaunch.this, "错误", Toast.LENGTH_SHORT);

        }
        System.out.println(tot);
        System.out.println(city);
        sendGetRequest();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainLaunch.this));
    }

    private void showNavigationOptions() {
        final CharSequence[] items = {"Google Maps", "高德地图", "百度地图"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择导航应用")
                .setItems(items, (dialog, item) -> {
                    switch (item) {
                        case 0:
                            startGoogleMaps();
                            break;
                        case 1:
                            startAmap();
                            break;
                        case 2:
                            startBaiduMaps();
                            break;
                    }
                })
                .show();
    }

    private void startGoogleMaps() {
        String uri = "google.navigation:q=" + tagXY[0] + "," + tagXY[1]; // 北京市经纬度
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private void startAmap() {
        // 高德地图
        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidamap://route?sourceApplication=appName&slat=&slon=&sname=我的位置&dlat=" + tagXY[1] +"&dlon="+ tagXY[0]+"&dname=" +tagplace + "&dev=0&t=2"));
        MainLaunch.this.startActivity(intent);

    }

    private void startBaiduMaps() {
        String uri = "baidumap://map/direction?destination=" + tagXY[0] + "," + tagXY[1] +"&mode=driving&src=appName";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.baidu.BaiduMap");

    }

    public static boolean isPackageInstalled(String packageName) {
        return new File("\\Android\\data\\" + packageName).exists();
    }

    private void showInstallAppDialog(String appName) {
        new AlertDialog.Builder(this)
                .setTitle("应用未安装")
                .setMessage(appName + "尚未安装，是否前往应用商店下载？")
                .setPositiveButton("确定", (dialog, which) -> {
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getAppPackageName(appName)));
                    startActivity(marketIntent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private String getAppPackageName(String appName) {
        switch (appName) {
            case "Google Maps":
                return "com.google.android.apps.maps";
            case "高德地图":
                return "com.autonavi.minimap";
            case "百度地图":
                return "com.baidu.BaiduMap";
            default:
                return "";
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
