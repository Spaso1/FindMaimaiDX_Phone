package org.astral.findmaimaiultra.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.adapter.PlaceAdapter;
import org.astral.findmaimaiultra.been.*;
import org.astral.findmaimaiultra.databinding.FragmentHomeBinding;
import org.astral.findmaimaiultra.ui.MainActivity;
import org.astral.findmaimaiultra.ui.PageActivity;
import org.astral.findmaimaiultra.utill.AddressParser;
import org.astral.findmaimaiultra.utill.SharedViewModel;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.*;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private LocationManager locationManager;
    private String tot;
    private String x;
    private String y;
    private PlaceAdapter adapter;
    public static List<Market> marketList = new ArrayList<>();
    private Context context;
    public static String province;
    public static String city;
    public static List<Place> a = new ArrayList<>();
    public static List<Place> b = new ArrayList<>();
    private BroadcastReceiver locationReceiver;
    public static List<TextView> textViews = new ArrayList<>();
    private boolean flag = true;
    private double tagXY[] = new double[2];
    private String tagplace;
    private boolean isFlag = true;
    private SharedPreferences shoucang;
    private SharedPreferences settingProperties;
    private FragmentHomeBinding binding;
    private SharedViewModel sharedViewModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取 SharedPreferences 实例
        context = getContext();
        if (context != null) {
            shoucang = context.getSharedPreferences("shoucang_prefs", Context.MODE_PRIVATE);
            settingProperties = context.getSharedPreferences("setting_prefs", Context.MODE_PRIVATE);
        }
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.recyclerView;

        // 初始化 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 初始化数据
        List<Place> placeList = new ArrayList<>();
        recyclerView.setAdapter(adapter);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0x123);

        // 示例：读取 SharedPreferences 中的数据
        if (shoucang != null) {
            String savedData = shoucang.getString("key_name", "default_value");
            // 使用 savedData
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 示例：写入 SharedPreferences 数据
    private void saveDataToSharedPreferences(String key, String value) {
        if (shoucang != null) {
            SharedPreferences.Editor editor = shoucang.edit();
            editor.putString(key, value);
            editor.apply(); // 或者 editor.commit();
        }
    }
    private void showNetworkErrorToast(String text) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show());
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void sendGetRequest() {
        OkHttpClient client = new OkHttpClient();

        String web = "http://mai.godserver.cn:11451/api/mai/v1/search?prompt1=" + city.split("市")[0] + "&status=市";
        if (!isFlag) {
            web = "http://mai.godserver.cn:11451/api/mai/v1/search?data_place=" + tagplace;
        }

        Request request = new Request.Builder()
                .url(web)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("OkHttp", "Error: " + e.getMessage());
                showNetworkErrorToast("网络错误");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    if (!result.equals("BedWeb")) {
                        List<Place> places = parseJsonToPlaceList(result);
                        if (places != null) {
                            updateUI(places);
                        }
                    } else {
                        showNetworkErrorToast("网络错误(服务器维护)");
                    }
                } else {
                    showNetworkErrorToast( "致命错误,服务器未启动");
                }
            }
        });
    }

    private void updateUI(List<Place> places) {
        a.clear();
        b.clear();

        for (Place p : places) {
            try {
                if (p.getName().equals("个人位置")) {
                    x = String.valueOf(p.getX());
                    y = String.valueOf(p.getY());
                    tot = p.getAddress();
                    city = p.getCity();
                    province = p.getProvince();
                }
                if (p.getIsUse() == 1) {
                    b.add(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        a.clear();
        TreeMap<Double, Place> treeMap = new TreeMap<>();

        for (Place p : b) {
            double distance = DistanceCalculator.calculateDistance(Double.parseDouble(x), Double.parseDouble(y), p.getX(), p.getY());

            if (shoucang.contains(p.getId() + "")) {
                p.setName(p.getName() + " 收藏" + " 距离您" + String.format(Locale.CHINA, "%.2f", distance) + "km");
                treeMap.put(distance - 1000, p);
            } else {
                p.setName(p.getName() + " 距离您" + String.format(Locale.CHINA, "%.2f", distance) + "km");
                treeMap.put(distance, p);
            }
            if (p.getNumJ() > 0) {
                p.setName(p.getName() + "\uD83D\uDCB3");
            }
        }

        for (Double key : treeMap.keySet()) {
            a.add(treeMap.get(key));
        }

        boolean flag2 = true;
        if (flag2) {
            adapter = new PlaceAdapter(a, new PlaceAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Place place) {
                    Intent intent = new Intent(context, PageActivity.class);
                    intent.putExtra("id", place.getId());
                    intent.putExtra("name", place.getName());
                    intent.putExtra("address", place.getAddress());
                    intent.putExtra("province", place.getProvince());
                    intent.putExtra("city", place.getCity());
                    intent.putExtra("area", place.getArea());
                    intent.putExtra("x", place.getX());
                    intent.putExtra("y", place.getY());
                    intent.putExtra("count", place.getCount());
                    intent.putExtra("bad", place.getBad());
                    intent.putExtra("good", place.getGood());
                    intent.putExtra("num", place.getNum());
                    intent.putExtra("numJ", place.getNumJ());
                    intent.putExtra("meituan", place.getMeituan_link());
                    intent.putExtra("douyin", place.getDouyin_link());
                    startActivity(intent);
                }
            });
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setAdapter(adapter);
                    // 设置 Toolbar 标题
                    String navHomeLabel = getString(R.string.menu_home);
                    Toolbar toolbar = ((MainActivity) requireActivity()).findViewById(R.id.toolbar);
                    if(!(toolbar.getTitle().equals("歌曲成绩") || toolbar.getTitle().equals("地图")|| toolbar.getTitle().equals("设置"))) {
                        toolbar.setTitle("FindMaimaiDX - " + a.size() + " 店铺" + "\n" + tot);
                    }

                    // 更新 SharedViewModel 中的 Map
                    sharedViewModel.setPlacelist(new ArrayList<>(a));
                    // 通知适配器数据已更改
                    adapter.notifyDataSetChanged();
                }
            });
            // 设置Toolbar

            for (Place p : a) {
                if (p.getX() == 0.0) {
                    // Log.i(p.getId() + "", p.getName() + "没有坐标");
                }
            }
        }
    }

    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {
        }.getType();
        if(jsonString.equals("BedWeb")) {
            Toast.makeText(context, "网络错误(服务器维护?)", Toast.LENGTH_SHORT);
            return null;
        }
        return gson.fromJson(jsonString, placeListType);
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x123 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 创建 LocationManager 对象
            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            // 获取最新的定位信息
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                // 调用高德地图 API 进行逆地理编码
                reverseGeocode(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            } else {
                Log.d("Location", "无法获取最新定位信息");
                setDefaultLocation(); // 设置默认位置
            }
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 12000, 16f, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.d("Location", "onLocationChanged");
                    if (flag) {
                        // 调用高德地图 API 进行逆地理编码
                        reverseGeocode(location.getLatitude(), location.getLongitude());
                    }
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    Toast.makeText(requireActivity().getApplicationContext(), "关闭定位", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.d("Location", "GPS定位失败");
            setDefaultLocation(); // 设置默认位置
        }
    }

    // 调用高德地图 API 进行逆地理编码
    private void reverseGeocode(double latitude, double longitude) {
        new Thread(() -> {
            try {
                // 构建请求 URL
                x = String.valueOf(longitude);
                y = String.valueOf(latitude);
                String url = "https://restapi.amap.com/v3/geocode/regeo?key=234cad2e2f0706e54c92591647a363c3&location=" + longitude + "," + latitude;
                Log.d("Location", url);
                // 发起网络请求
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // 使用 Gson 解析 JSON
                    Gson gson = new Gson();
                    Log.d("Location", responseData);
                    AmapReverseGeocodeResponse geocodeResponse = gson.fromJson(responseData, AmapReverseGeocodeResponse.class);
                    if (geocodeResponse.getStatus().equals("1")) { // 状态码 "1" 表示成功
                        AmapReverseGeocodeResponse.Regeocode regeocode = geocodeResponse.getRegeocode();
                        AmapReverseGeocodeResponse.AddressComponent addressComponent = regeocode.getAddressComponent();
                        // 解析地址信息
                        String address = regeocode.getFormattedAddress();
                        String province = addressComponent.getProvince();
                        String city;
                        try {
                            city = addressComponent.getCity().get(0).replace("市", "");
                        } catch (Exception e) {
                            city = addressComponent.getProvince().replace("市", "");
                        }
                        // 更新 UI
                        String finalCity = city;
                        getActivity().runOnUiThread(() -> {
                            tot = address;
                            HomeFragment.province = province;
                            HomeFragment.city = finalCity;
                            sharedViewModel.addToMap("tot", tot);
                            sharedViewModel.addToMap("x", x);
                            sharedViewModel.addToMap("y", y);
                            extracted();
                        });
                    } else {
                        Log.d("Location", "高德地图 API 调用失败，尝试使用 Android 自带 Geocoder");
                        fallbackToGeocoder(latitude, longitude); // 调用备用方案
                    }
                } else {
                    Log.d("Location", "高德地图 API 调用失败，尝试使用 Android 自带 Geocoder");
                    fallbackToGeocoder(latitude, longitude); // 调用备用方案
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Location", "高德地图 API 调用失败，尝试使用 Android 自带 Geocoder");
                fallbackToGeocoder(latitude, longitude); // 调用备用方案
            }
        }).start();
    }

    // 备用方案：使用 Android 自带的 Geocoder 进行逆地理编码
    private void fallbackToGeocoder(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String detail = address.getAddressLine(0);
                String province = address.getAdminArea();
                String city = address.getLocality();
                // 更新 UI
                try {
                    requireActivity().runOnUiThread(() -> {
                        tot = detail;
                        this.province = province;
                        this.city = city;
                        extracted();
                    });
                }catch (Exception e) {

                }

            } else {
                Log.d("Location", "Android 自带 Geocoder 获取地址失败");
                setDefaultLocation(); // 设置默认位置
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Location", "Android 自带 Geocoder 获取地址失败");
            setDefaultLocation(); // 设置默认位置
        }
    }

    // 设置默认位置
    private void setDefaultLocation() {
        x = String.valueOf(116.3912757);
        y = String.valueOf(39.906217);
    }

    //手动刷新定位
    private void extracted() {
        //tot = tot.split("\"")[1];
        Log.i("TAG", "x=" + x + ";y=" + y);
        //tot = "天津市东丽区民航大学";
        if(!isFlag) {

        }else {
            try {
                AddressParser.parseAddress(tot);
            } catch (Exception e) {
                Toast.makeText(context, "错误", Toast.LENGTH_SHORT);

            }
        }
        sendGetRequest();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }
    public static List<Geocode> parseJsonToGeocodeList(String jsonString) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonString, JsonArray.class);
        List<Geocode> Geocodes = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Geocode geocode = new Geocode();
            // 获取 marketName
            String formatted_address = jsonObject.get("formatted_address").getAsString();
            geocode.setFormatted_address(formatted_address);
            geocode.setProvince(jsonObject.get("province").getAsString());
            geocode.setCity(jsonObject.get("city").getAsString());
            geocode.setDistrict(jsonObject.get("district").getAsString());
            geocode.setCountry(jsonObject.get("country").getAsString());
            geocode.setLevel(jsonObject.get("level").getAsString());
            geocode.setCitycode(jsonObject.get("citycode").getAsString());
            // 获取 x, y
            String location = jsonObject.get("location").getAsString();
            String[] coordinates = location.split(",");
            geocode.setLocation(location);
            Geocodes.add(geocode);
        }
        return Geocodes;
    }
    private String getAppVersionName() {
        try {
            PackageInfo packageInfo = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 打开QQ
     * @param context
     */
    public static void gotoQQ(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.SplashActivity"));
            if (!(context instanceof Activity)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "未安装QQ", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPlace(Place place) {
        String url = "http://mai.godserver.cn:11451/api/mai/v1/place";
        String body = new Gson().toJson(place,Place.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, "添加失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();
                    });
                }else {
                    Toast.makeText(context, "添加失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showNavigationOptions() {
        final CharSequence[] items = {"Google Maps", "高德地图", "百度地图(暂时不可用)"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        String uri = "google.navigation:q=" + tagXY[0] + "," + tagXY[1];
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private void startAmap() {
        // 高德地图
        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidamap://route?sourceApplication=appName&slat=&slon=&sname=我的位置&dlat=" + tagXY[1] +"&dlon="+ tagXY[0]+"&dname=" +tagplace + "&dev=0&t=2"));
        this.startActivity(intent);
    }

    private void startBaiduMaps() {
        Toast.makeText(PageActivity.context, "111", Toast.LENGTH_SHORT).show();
        String uri = "baidumap://map/direction?destination=latlng:" + tagXY[0] + "," + tagXY[1] +"&mode=driving&src=appName";
        Intent intent = new Intent("com.baidu.tieba",  android.net.Uri.parse(uri));
        startActivity(intent);
    }
    private static List<Market> parseJsonToPlaceList2(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Market>>() {
        }.getType();
        return gson.fromJson(jsonString, placeListType);
    }
}
