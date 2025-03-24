package org.ast.findmaimaidx.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.adapter.PlaceAdapter;
import org.ast.findmaimaidx.been.AmapReverseGeocodeResponse;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Geocode;
import org.ast.findmaimaidx.been.Market;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.map2d.BasicMapActivity;
import org.ast.findmaimaidx.service.LocationUpdateService;
import org.ast.findmaimaidx.utill.AddressParser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainLaunch extends AppCompatActivity {
    public static List<Market> marketList = new ArrayList<>();
    public static String province;
    public static String city;
    public static List<Place> a = new ArrayList<>();
    public static List<Place> b = new ArrayList<>();
    public static List<TextView> textViews = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private String sessionId;
    private LocationManager locationManager;
    private RecyclerView recyclerView;
    private TextView addressTextView;
    private boolean isAdmin;
    private String tot;
    private String x;
    private String y;
    private PlaceAdapter adapter;
    private Context context;
    private BroadcastReceiver locationReceiver;
    private boolean flag = true;
    private double tagXY[] = new double[2];
    private String tagplace;
    private boolean isFlag = true;
    private SharedPreferences shoucang;
    private SharedPreferences settingProperties;
    private boolean isPad = false;
    private LinearLayout t31;
    private DrawerLayout drawerLayout;

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

    /**
     * 打开QQ
     *
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

    private static List<Market> parseJsonToPlaceList2(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Market>>() {
        }.getType();
        return gson.fromJson(jsonString, placeListType);
    }

    @Override
    @SuppressLint({"MissingInflatedId", "Range", "UnspecifiedRegisterReceiverFlag", "SetTextI18n"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainlayout);
        context = this;
        //获取屏幕长宽
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        Log.d("MainLaunch", "onCreate: " + width + " " + height);
        isPad = false;
        if (width >= height) {
            isPad = true;
        }
        if (isPad) {
            setContentView(R.layout.activity_mainpadlayout);
        }
        //设置随机数
        String userInput = "";
        shoucang = getSharedPreferences("shoucang@", MODE_PRIVATE);
        try {
            // 获取传递过来的数据
            Intent intent = getIntent();
            userInput = intent.getStringExtra("address");
            if (intent.getStringExtra("address") != null) {
                Log.d("MainLaunch", "userInput: " + userInput);
                tagplace = userInput;
                isFlag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isFlag) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0x123);
        } else {
            extracted();
        }

        addressTextView = findViewById(R.id.textView);
        FloatingActionButton button2 = findViewById(R.id.fab);
        final CharSequence[][] items = {{"联系作者", "b50", "自动刷新定位", "手动选择定位", "地图", "切换到中二", "排卡", "设置及更多"}};

        button2.setOnClickListener(v -> {

            MaterialAlertDialogBuilder menu = new MaterialAlertDialogBuilder(this);
// 设置对话框标题
            menu.setTitle("选择");
            System.out.println(Arrays.toString(items[0]));
            menu.setItems(items[0], (dialog, item) -> {
                switch (item) {
                    case 0:
                        MaterialAlertDialogBuilder share = new MaterialAlertDialogBuilder(this);
                        share.setTitle("选择");
                        share.setItems(new String[]{"QQ", "邮箱"}, (dialog2, item2) -> {
                            switch (item2) {
                                case 0:
                                    //复制qq群号
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clipData = ClipData.newPlainText("QQ群号", "669030069");
                                    clipboard.setPrimaryClip(clipData);
                                    Toast.makeText(this, "QQ群号已复制到剪贴板", Toast.LENGTH_SHORT).show();
                                    MainLaunch.gotoQQ(this);
                                    break;
                                case 1:
                                    // 获取设备信息
                                    String deviceModel = Build.MODEL;
                                    String androidVersion = Build.VERSION.RELEASE;
                                    String appVersion = getAppVersionName();

                                    // 构建邮件正文
                                    String emailBody = "邮件至 astralpath@163.com\n" +
                                            "Issue: " + "\n\n" +
                                            "Log: \n\n" +
                                            "Environment: \n" +
                                            "   设备机型: " + deviceModel + "\n" +
                                            "   Android版本: " + androidVersion + "\n" +
                                            "   应用版本: " + appVersion;

                                    // 创建一个Intent对象，指定动作和数据类型
                                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                    emailIntent.setType("message/rfc822"); // 设置数据类型为邮件
                                    // 设置邮件的基本信息
                                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"astralpath@163.com"});
                                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Issue提交"); // 邮件主题
                                    emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody); // 邮件正文

                                    // 检查是否有可以处理此Intent的应用
                                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(emailIntent); // 启动邮件发送界面
                                    } else {
                                        // 如果没有可以处理此Intent的应用，则显示错误消息
                                        Toast.makeText(this, "没有可用的邮件客户端", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                        });
                        share.show();
                        break;
                    case 1:
                        Intent intent = new Intent(MainLaunch.this, B50.class);
                        intent.putExtra("sessionId", sessionId);
                        startActivity(intent);
                        break;
                    case 2:
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        try {
                            x = String.valueOf(location.getLongitude());
                            y = String.valueOf(location.getLatitude());

                            Geocoder geocoder = new Geocoder(MainLaunch.this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!addresses.isEmpty()) {
                                Toast.makeText(MainLaunch.this, "定位成功", Toast.LENGTH_SHORT).show();
                                Address address = addresses.get(0);
                                String detail = address.getAddressLine(0);
                                addressTextView.setText(" " + detail);
                                tot = detail;
                                province = address.getAdminArea();
                                city = address.getLocality();
                                extracted();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            addressTextView.setText("Error getting address");
                        }
                        break;
                    case 3:
                        // 创建一个AlertDialog.Builder
                        MaterialAlertDialogBuilder builder2 = new MaterialAlertDialogBuilder(this);
                        final EditText input = new EditText(this);
                        builder2.setTitle("请输入完整地址")
                                .setView(input)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 获取用户输入的数据
                                        String userInput = input.getText().toString();
                                        // 在这里处理用户输入的数据
                                        //
                                        //

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(MainLaunch.this, MainLaunch.class);
                                                        intent.putExtra("address", userInput);
                                                        startActivity(intent);
                                                    }
                                                });
                                            }
                                        }).start();
                                        //
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        // 创建并显示对话框
                        builder2.show();
                        break;
                    case 4:
                        Intent intent2 = new Intent(MainLaunch.this, BasicMapActivity.class);
                        intent2.putExtra("x", x);
                        intent2.putExtra("y", y);
                        ArrayList<Place> aL = new ArrayList<>(a);

                        intent2.putParcelableArrayListExtra("place_list_key", aL);
                        startActivity(intent2);
                        break;
                    case 5:
                        Intent intent1 = new Intent(MainLaunch.this, ChunActivity.class);
                        startActivity(intent1);
                        break;
                    case 6:
                        Intent intent4 = new Intent(MainLaunch.this, PaikaActivity.class);
                        startActivity(intent4);
                        break;
                    case 7:
                        Intent intent3 = new Intent(MainLaunch.this, SettingActivity.class);
                        intent3.putExtra("x", x);
                        intent3.putExtra("y", y);
                        intent3.putExtra("sessionId", sessionId);
                        startActivity(intent3);
                        break;

                    case 8:
                        MaterialAlertDialogBuilder builder_addplace = new MaterialAlertDialogBuilder(this);
                        builder_addplace.setTitle("添加机厅");

// Inflate the custom layout
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_add_place, null);
                        builder_addplace.setView(dialogView);

// Add the positive button
                        builder_addplace.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Get the input values
                                EditText editTextName = dialogView.findViewById(R.id.editTextName);
                                EditText editTextProvince = dialogView.findViewById(R.id.editTextProvince);
                                EditText editTextCity = dialogView.findViewById(R.id.editTextCity);
                                EditText editTextArea = dialogView.findViewById(R.id.editTextArea);
                                EditText editTextAddress = dialogView.findViewById(R.id.editTextAddress);
                                EditText num = dialogView.findViewById(R.id.num);
                                EditText numJ = dialogView.findViewById(R.id.numJ);

                                String name = editTextName.getText().toString();
                                String province = editTextProvince.getText().toString();
                                String city = editTextCity.getText().toString();
                                String area = editTextArea.getText().toString();
                                String address = editTextAddress.getText().toString();

                                int num1 = Integer.parseInt(num.getText().toString());
                                int num2 = 0;
                                try {
                                    num2 = Integer.parseInt(numJ.getText().toString());
                                } catch (NumberFormatException e) {
                                    throw new RuntimeException(e);
                                }

                                // Create a new Place object with the input values
                                Place newPlace = new Place(0, name, province, city, area, address, 1, 0.0, 0.0, 0, 0, 0);
                                newPlace.setNum(num1);
                                newPlace.setNumJ(num2);
                                addPlace(newPlace);
                                // Here you can add the newPlace to your data source or perform other actions
                            }
                        });

// Add the negative button
                        builder_addplace.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

// Create and show the AlertDialog
                        builder_addplace.show();
                }
            }).show();
        });

        settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
        /**
         * 设置侦测
         */
        boolean setting_autobeta1 = settingProperties.getBoolean("setting_autobeta1", false);
        if (setting_autobeta1) {
            // 启动位置更新服务
            Intent serviceIntent = new Intent(this, LocationUpdateService.class);
            startService(serviceIntent);

            // 注册广播接收器
            locationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    double latitude = intent.getDoubleExtra("latitude", 0.0);
                    double longitude = intent.getDoubleExtra("longitude", 0.0);
                    Log.d("机厅位置", "位置更新服务");
                }
            };
            try {
                registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"));
            } catch (Exception e) {

            }
            Log.d("机厅位置", "启动位置更新服务");
            Toast.makeText(MainLaunch.this, "已启动位置更新服务", Toast.LENGTH_SHORT).show();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        if (settingProperties.getString("image_uri", null) != null) {
            Uri uri = Uri.parse(settingProperties.getString("image_uri", null));
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                if (bitmap != null) {
                    // 获取RecyclerView的尺寸
                    int recyclerViewWidth = 0;
                    int recyclerViewHeight = 0;
                    if (isPad) {
                        LinearLayout linearLayout = findViewById(R.id.pad);
                        recyclerViewWidth = linearLayout.getWidth();
                        recyclerViewHeight = linearLayout.getHeight();
                    } else {
                        recyclerViewWidth = recyclerView.getWidth();
                        recyclerViewHeight = recyclerView.getHeight();
                    }
                    if (recyclerViewWidth > 0 && recyclerViewHeight > 0) {
                        // 计算缩放比例
                        float scaleWidth = ((float) recyclerViewWidth) / bitmap.getWidth();
                        float scaleHeight = ((float) recyclerViewHeight) / bitmap.getHeight();

                        // 选择较大的缩放比例以保持图片的原始比例
                        float scaleFactor = Math.max(scaleWidth, scaleHeight);

                        // 计算新的宽度和高度
                        int newWidth = (int) (bitmap.getWidth() * scaleFactor);
                        int newHeight = (int) (bitmap.getHeight() * scaleFactor);

                        // 缩放图片
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                        // 计算裁剪区域
                        int x = (scaledBitmap.getWidth() - recyclerViewWidth) / 2;
                        int y = (scaledBitmap.getHeight() - recyclerViewHeight) / 2;

                        // 处理x和y为负数的情况
                        x = Math.max(x, 0);
                        y = Math.max(y, 0);

                        // 裁剪图片
                        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, x, y, recyclerViewWidth, recyclerViewHeight);

                        // 创建一个新的 Bitmap，与裁剪后的 Bitmap 大小相同
                        Bitmap transparentBitmap = Bitmap.createBitmap(croppedBitmap.getWidth(), croppedBitmap.getHeight(), croppedBitmap.getConfig());

                        // 创建一个 Canvas 对象，用于在新的 Bitmap 上绘制
                        Canvas canvas = new Canvas(transparentBitmap);

                        // 创建一个 Paint 对象，并设置透明度
                        Paint paint = new Paint();
                        paint.setAlpha(128); // 设置透明度为 50% (255 * 0.5 = 128)

                        // 将裁剪后的 Bitmap 绘制到新的 Bitmap 上，并应用透明度
                        canvas.drawBitmap(croppedBitmap, 0, 0, paint);

                        // 创建BitmapDrawable并设置其边界为RecyclerView的尺寸
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), transparentBitmap);

                        // 设置recyclerView的背景
                        if (!isPad) {
                            recyclerView.setBackground(bitmapDrawable);
                        } else {
                            LinearLayout linearLayout = findViewById(R.id.pad);
                            linearLayout.setBackground(bitmapDrawable);
                        }
                    } else {
                        // 如果RecyclerView的尺寸未确定，可以使用ViewTreeObserver来监听尺寸变化
                        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                int recyclerViewWidth = 0;
                                int recyclerViewHeight = 0;
                                if (isPad) {
                                    LinearLayout linearLayout = findViewById(R.id.pad);
                                    recyclerViewWidth = linearLayout.getWidth();
                                    recyclerViewHeight = linearLayout.getHeight();
                                } else {
                                    recyclerViewWidth = recyclerView.getWidth();
                                    recyclerViewHeight = recyclerView.getHeight();
                                }

                                // 计算缩放比例
                                float scaleWidth = ((float) recyclerViewWidth) / bitmap.getWidth();
                                float scaleHeight = ((float) recyclerViewHeight) / bitmap.getHeight();

                                // 选择较大的缩放比例以保持图片的原始比例
                                float scaleFactor = Math.max(scaleWidth, scaleHeight);

                                // 计算新的宽度和高度
                                int newWidth = (int) (bitmap.getWidth() * scaleFactor);
                                int newHeight = (int) (bitmap.getHeight() * scaleFactor);

                                // 缩放图片
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                                // 计算裁剪区域
                                int x = (scaledBitmap.getWidth() - recyclerViewWidth) / 2;
                                int y = (scaledBitmap.getHeight() - recyclerViewHeight) / 2;

                                // 处理x和y为负数的情况
                                x = Math.max(x, 0);
                                y = Math.max(y, 0);

                                // 裁剪图片
                                Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, x, y, recyclerViewWidth, recyclerViewHeight);

                                // 创建一个新的 Bitmap，与裁剪后的 Bitmap 大小相同
                                Bitmap transparentBitmap = Bitmap.createBitmap(croppedBitmap.getWidth(), croppedBitmap.getHeight(), croppedBitmap.getConfig());

                                // 创建一个 Canvas 对象，用于在新的 Bitmap 上绘制
                                Canvas canvas = new Canvas(transparentBitmap);

                                // 创建一个 Paint 对象，并设置透明度
                                Paint paint = new Paint();
                                paint.setAlpha(128); // 设置透明度为 50% (255 * 0.5 = 128)

                                // 将裁剪后的 Bitmap 绘制到新的 Bitmap 上，并应用透明度
                                canvas.drawBitmap(croppedBitmap, 0, 0, paint);

                                // 创建BitmapDrawable并设置其边界为RecyclerView的尺寸
                                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), transparentBitmap);

                                // 设置recyclerView的背景
                                if (!isPad) {
                                    recyclerView.setBackground(bitmapDrawable);
                                } else {
                                    LinearLayout linearLayout = findViewById(R.id.pad);
                                    linearLayout.setBackground(bitmapDrawable);
                                }
                            }
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "图片加载失败,权限出错!", Toast.LENGTH_SHORT).show();
            }
        }

        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String url = "http://mai.godserver.cn:11451/api/mai/v1/check?androidId=" + androidId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainLaunch", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String res = response.body().string();
                    if (res.equals("1")) {
                        System.out.println("1");
                        isAdmin = true;
                        items[0] = new CharSequence[]{"联系作者", "b50", "自动刷新定位", "手动选择定位", "地图", "切换到中二", "排卡", "设置及更多", "添加机厅"};
                    }
                    System.out.println(res);
                }
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 创建 ActionBarDrawerToggle 并将其与 DrawerLayout 和 Toolbar 关联
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        toggle.setToolbarNavigationClickListener(v -> {
            Log.i("MainLaunch", "onClick: ");
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        try {
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            // 设置 NavigationView 的点击事件
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Handle the home action
                } else if (id == R.id.nav_gallery) {
                    Intent intent3 = new Intent(MainLaunch.this, SettingActivity.class);
                    intent3.putExtra("x", x);
                    intent3.putExtra("y", y);
                    intent3.putExtra("sessionId", sessionId);
                    startActivity(intent3);
                    // Handle the gallery action
                } else if (id == R.id.nav_slideshow) {
                    Intent intent = new Intent(MainLaunch.this, B50.class);
                    intent.putExtra("sessionId", sessionId);
                    startActivity(intent);
                    // Handle the slideshow action
                } else if (id == R.id.nav_share) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Github", "https://github.com/Spaso1/FindMaimaiDX_Phone");
                    clipboard.setPrimaryClip(clipData);
                    Toast.makeText(this, "GitHub地址已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    // Handle the share action
                } else if (id == R.id.nav_send) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("QQ群号", "669030069");
                    clipboard.setPrimaryClip(clipData);
                    Toast.makeText(this, "QQ群号已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    MainLaunch.gotoQQ(this);
                    // Handle the send action
                } else if (id == R.id.nav_auto) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    }
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    try {
                        x = String.valueOf(location.getLongitude());
                        y = String.valueOf(location.getLatitude());

                        Geocoder geocoder = new Geocoder(MainLaunch.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (!addresses.isEmpty()) {
                            Toast.makeText(MainLaunch.this, "定位成功", Toast.LENGTH_SHORT).show();
                            Address address = addresses.get(0);
                            String detail = address.getAddressLine(0);
                            addressTextView.setText(" " + detail);
                            tot = detail;
                            province = address.getAdminArea();
                            city = address.getLocality();
                            extracted();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        addressTextView.setText("Error getting address");
                    }
                } else if (id == R.id.nav_map) {
                    Intent intent2 = new Intent(MainLaunch.this, BasicMapActivity.class);
                    intent2.putExtra("x", x);
                    intent2.putExtra("y", y);
                    ArrayList<Place> aL = new ArrayList<>(a);

                    intent2.putParcelableArrayListExtra("place_list_key", aL);
                    startActivity(intent2);
                } else if (id == R.id.nav_paika) {
                    Intent intent4 = new Intent(MainLaunch.this, PaikaActivity.class);
                    startActivity(intent4);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
                runOnUiThread(() -> Toast.makeText(MainLaunch.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    if (!result.equals("BedWeb")) {
                        List<Place> places = parseJsonToPlaceList(result);
                        if (places != null) {
                            runOnUiThread(() -> updateUI(places));
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainLaunch.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainLaunch.this, "致命错误,服务器未启动", Toast.LENGTH_SHORT).show());
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
                } else if (isAdmin) {
                    p.setName("已弃用-" + p.getName());
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
            if (isPad) {
                adapter = new PlaceAdapter(a, new PlaceAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Place place) {
                        startPadPage(place);
                    }
                });
            } else {
                adapter = new PlaceAdapter(a, new PlaceAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Place place) {
                        Intent intent = new Intent(MainLaunch.this, PageActivity.class);
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
            }
            recyclerView.setAdapter(adapter);
            // 设置Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar); // 设置Toolbar标题

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("FindMaimaiDX - " + a.size() + " 店铺" + "\n" + tot);
            }

            for (Place p : a) {
                if (p.getX() == 0.0) {
                    // Log.i(p.getId() + "", p.getName() + "没有坐标");
                }
            }
        }
    }

    private void startPadPage(Place place) {

        TextView name = findViewById(R.id.pag2_nameTextView);
        name.setText(place.getName());
        TextView address = findViewById(R.id.pag2_addressTextView);
        address.setText(place.getAddress());
        TextView province = findViewById(R.id.pag2_provinceTextView);
        province.setText(place.getProvince());
        TextView city = findViewById(R.id.pag2_cityTextView);
        city.setText(place.getCity());
        TextView area = findViewById(R.id.pag2_areaTextView);
        area.setText(place.getArea());
        TextView num = findViewById(R.id.pag2_num5);
        num.setText("国机 " + place.getNum());
        if (place.getNumJ() > 0) {
            TextView numJ = findViewById(R.id.pag2_num6);
            numJ.setText("\uD83D\uDCB3" + place.getNumJ());
        } else {
            TextView numJ = findViewById(R.id.pag2_num6);
            numJ.setText("");
        }
        TextView num7 = findViewById(R.id.pag2_num7);
        num7.setText("机台:" + (place.getNum() + place.getNumJ()));
        String address2 = place.getAddress();
        MaterialButton share = findViewById(R.id.pag2_share);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("text", address2);
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(MainLaunch.this, "机厅地址信息已经复制!", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
        shoucang = getSharedPreferences("shoucang@", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = shoucang.edit();
        int id2 = place.getId();
        com.google.android.material.switchmaterial.SwitchMaterial switch1 = findViewById(R.id.pag2_switch1);
        if (shoucang.contains(id2 + "")) {
            switch1.setChecked(true);
        }
        switch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch1.isChecked()) {
                    editor2.putString(id2 + "", "1");
                } else {
                    editor2.remove(id2 + "");
                }
                editor2.apply();
            }
        });

        int good = place.getGood();
        int bad = place.getBad();
        WebView webView = findViewById(R.id.pag2_imageView1);
        String imageUrl = "https://img.shields.io/badge/recommend-" + good + "-green";
        webView.setBackgroundColor(0x00000000); // 设置背景为透明

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 启用JavaScript
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 注入JavaScript来设置网页背景为透明
                view.loadUrl("javascript:(function() { " +
                        "document.body.style.backgroundColor = 'transparent'; " +
                        "})()");
            }
        });
        webView.loadUrl(imageUrl); // 加载网页
        WebView webView2 = findViewById(R.id.pag2_imageView2);
        String imageUrl2 = "https://img.shields.io/badge/oppose-" + bad + "-red";
        webView2.setBackgroundColor(0x00000000); // 设置背景为透明
        webView2.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 注入JavaScript来设置网页背景为透明
                view.loadUrl("javascript:(function() { " +
                        "document.body.style.backgroundColor = 'transparent'; " +
                        "})()");
            }
        });
        webView2.loadUrl(imageUrl2); // 加载网页
        t31 = findViewById(R.id.pag2_hor);
        findnear(place, "mai", t31);
    }

    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {
        }.getType();
        if (jsonString.equals("BedWeb")) {
            Toast.makeText(MainLaunch.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT);
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
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                        Toast.makeText(MainLaunch.this, "定位成功", Toast.LENGTH_SHORT).show();
                        // 调用高德地图 API 进行逆地理编码
                        reverseGeocode(location.getLatitude(), location.getLongitude());
                    }
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    Toast.makeText(getApplicationContext(), "关闭定位", Toast.LENGTH_SHORT).show();
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
                        runOnUiThread(() -> {
                            addressTextView.setText(" " + address);
                            tot = address;
                            this.province = province;
                            this.city = finalCity;
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
            Geocoder geocoder = new Geocoder(MainLaunch.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String detail = address.getAddressLine(0);
                String province = address.getAdminArea();
                String city = address.getLocality();
                // 更新 UI
                runOnUiThread(() -> {
                    addressTextView.setText(" " + detail);
                    tot = detail;
                    this.province = province;
                    this.city = city;
                    extracted();
                });
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
        addressTextView.setText("未知定位,默认设置北京市");
    }

    //手动刷新定位
    private void extracted() {
        //tot = tot.split("\"")[1];
        Log.i("TAG", "x=" + x + ";y=" + y);
        if (x == null || y == null) {
            Toast.makeText(this, "请确认输入地址是否正确", Toast.LENGTH_SHORT).show();
            return;
        }
        //tot = "天津市东丽区民航大学";
        if (!isFlag) {

        } else {
            try {
                AddressParser.parseAddress(tot);
            } catch (Exception e) {
                Toast.makeText(MainLaunch.this, "错误", Toast.LENGTH_SHORT);

            }
        }
        sendGetRequest();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainLaunch.this));

    }

    private String getAppVersionName() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addPlace(Place place) {
        String url = "http://mai.godserver.cn:11451/api/mai/v1/place";
        String body = new Gson().toJson(place, Place.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .addHeader("Cookie", sessionId)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainLaunch.this, "添加失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        Toast.makeText(MainLaunch.this, "添加成功", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(MainLaunch.this, "添加失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void findnear(Place place_centor, String type_code, LinearLayout t31) {
        OkHttpClient client = new OkHttpClient();
        String web = "http://mai.godserver.cn:11451/api/" + type_code + "/v1/near?id=" + place_centor.getId();
        Log.d("Web", web);

        Request request = new Request.Builder()
                .url(web)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("OkHttp", "Error: " + e.getMessage());
                runOnUiThread(() -> {
                    TextView ttt = new TextView(PageActivity.context);
                    ttt.setText("网络请求失败");
                    ttt.setTextColor(ContextCompat.getColor(PageActivity.context, R.color.textcolorPrimary));
                    t31.addView(ttt);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    runOnUiThread(() -> handleResponse(result));
                } else {
                    runOnUiThread(() -> {
                        TextView ttt = new TextView(PageActivity.context);
                        ttt.setText("请求失败，错误码: " + response.code());
                        ttt.setTextColor(ContextCompat.getColor(PageActivity.context, R.color.textcolorPrimary));
                        t31.addView(ttt);
                    });
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void handleResponse(String result) {
        t31.setVisibility(View.VISIBLE);
        if (result.contains("[{")) {
            marketList = parseJsonToPlaceList2(result);
            for (Market market : marketList) {
                Log.d("Market", market.getMarketName());
            }
            for (int i = 0; i < marketList.size(); i++) {
                TextView t = new TextView(context);
                t.setTextColor(ContextCompat.getColor(this, R.color.textcolorPrimary));

                double distance = marketList.get(i).getDistance();
                int type = marketList.get(i).getType();
                DecimalFormat decimalFormat = new DecimalFormat("0.#");
                String formattedResult = decimalFormat.format(distance * 1000);
                if (type == 1) {
                    t.setTextColor(Color.rgb(255, 182, 193));
                } else if (type == 2) {
                    t.setTextColor(Color.rgb(144, 238, 144));
                }
                t.setText(marketList.get(i).getMarketName() + " \n距离机厅:" + distance + "米\n");
                t.setTextSize(15.0F);
                int finalI = i;
                t.isTextSelectable();
                t.isEnabled();
                t.setOnClickListener(v -> {
                    tagXY[0] = marketList.get(finalI).getX();
                    tagXY[1] = marketList.get(finalI).getY();

                    tagplace = marketList.get(finalI).getMarketName().split(" ")[0];
                    //导航
                    Toast.makeText(this, "即将导航" + marketList.get(finalI).getMarketName(), Toast.LENGTH_SHORT).show();
                    showNavigationOptions();
                });
                Log.d("Market2", marketList.get(i).getMarketName());
                textViews.add(t);
                t31.addView(t);
            }
        } else {
            TextView ttt = new TextView(this);
            ttt.setText("暂时关闭");
            ttt.setTextColor(ContextCompat.getColor(this, R.color.textcolorPrimary));
            t31.addView(ttt);
        }
        Log.d("Market3", t31.getHeight() + "---");
    }

    private void showNavigationOptions() {
        final CharSequence[] items = {"Google Maps", "高德地图", "百度地图(暂时不可用)"};

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
        String uri = "google.navigation:q=" + tagXY[0] + "," + tagXY[1];
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private void startAmap() {
        // 高德地图
        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidamap://route?sourceApplication=appName&slat=&slon=&sname=我的位置&dlat=" + tagXY[1] + "&dlon=" + tagXY[0] + "&dname=" + tagplace + "&dev=0&t=2"));
        MainLaunch.this.startActivity(intent);
    }

    private void startBaiduMaps() {
        Toast.makeText(PageActivity.context, "111", Toast.LENGTH_SHORT).show();
        String uri = "baidumap://map/direction?destination=latlng:" + tagXY[0] + "," + tagXY[1] + "&mode=driving&src=appName";
        Intent intent = new Intent("com.baidu.tieba", android.net.Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
