package org.ast.findmaimaidx.ui;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Geocode;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.map2d.BasicMapActivity;
import org.ast.findmaimaidx.service.LocationUpdateService;
import org.ast.findmaimaidx.ui.ChunActivity;
import org.ast.findmaimaidx.ui.PageActivity;
import org.ast.findmaimaidx.ui.SettingActivity;
import org.ast.findmaimaidx.ui.b50;
import org.ast.findmaimaidx.utill.AddressParser;
import org.ast.findmaimaidx.utill.DeviceInfoUtils;
import org.ast.findmaimaidx.utill.PlaceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.*;
import org.jetbrains.annotations.NotNull;

public class MainLaunch extends AppCompatActivity implements CoroutineScope {
    private Handler handler = new Handler(Looper.getMainLooper());
    public static final int LOCATION_CODE = 301;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerView;
    private PlaceAdapter adapter;
    private String locationProvider = null;
    private TextView addressTextView;
    private static final String GITHUB_API_URL = "https://api.github.com/repos/owner/repo/releases/latest";

    private String tot;
    private String x;
    private String y;
    private PlaceAdapter placeAdapter;
    public static String province;
    public static String city;
    public static List<Place> a = new ArrayList<>();
    public static List<Place> b = new ArrayList<>();
    private BroadcastReceiver locationReceiver;

    private boolean flag = true;
    private double tagXY[] = new double[2];
    private String tagplace;
    private boolean isFlag = true;
    private SharedPreferences shoucang;
    private SharedPreferences.Editor editor;
    private SharedPreferences settingProperties;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    @SuppressLint({"MissingInflatedId", "Range", "UnspecifiedRegisterReceiverFlag", "SetTextI18n"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        String userInput = "";
        shoucang = getSharedPreferences("shoucang@", MODE_PRIVATE);
        editor = shoucang.edit();
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
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            extracted();
        }

        addressTextView = findViewById(R.id.textView);
        FloatingActionButton button2 = findViewById(R.id.fab);
        button2.setOnClickListener(v -> {
            // 创建一个AlertDialog.Builder对象
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final CharSequence[] items = {"联系作者", "b50", "自动刷新定位", "手动选择定位", "地图", "切换到中二", "设置及更多"};
            // 设置对话框标题
            builder.setTitle("选择");
            // 添加“确定”按钮
            builder.setItems(items, (dialog, item) -> {
                switch (item) {
                    case 0:
                        //二级弹窗
                        AlertDialog.Builder builder23 = new AlertDialog.Builder(this);
                        builder23.setTitle("选择");
                        builder23.setItems(new String[]{"QQ", "邮箱"}, (dialog2, item2) -> {
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
                        AlertDialog dialog2 = builder23.create();
                        dialog2.show();
                        break;
                    case 1:
                        Intent intent = new Intent(MainLaunch.this, b50.class);
                        startActivity(intent);
                        break;
                    case 2:
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                            return;
                        }
                        getLastLocation();
                        break;
                    case 3:
                        // 创建一个AlertDialog.Builder
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                        final EditText input = new EditText(this);
                        builder2.setTitle("请输入完整地址")
                                .setView(input)
                                .setPositiveButton("确定", (dialog1, which) -> {
                                    // 获取用户输入的数据
                                    String userInput1 = input.getText().toString();
                                    // 在这里处理用户输入的数据
                                    new Thread(() -> {
                                        handler.post(() -> {
                                            Intent intent1 = new Intent(MainLaunch.this, MainLaunch.class);
                                            intent1.putExtra("address", userInput1);
                                            startActivity(intent1);
                                        });
                                    }).start();
                                })
                                .setNegativeButton("取消", (dialog1, which) -> dialog1.cancel());

                        // 创建并显示对话框
                        AlertDialog dialog23 = builder2.create();
                        dialog23.show();
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
                        Intent intent3 = new Intent(MainLaunch.this, SettingActivity.class);
                        startActivity(intent3);
                        break;
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
            registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"));
            Log.d("机厅位置", "启动位置更新服务");
            Toast.makeText(MainLaunch.this, "已启动位置更新服务", Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.recyclerView);
        if (settingProperties.getString("image_uri", null) != null) {
            Uri uri = Uri.parse(settingProperties.getString("image_uri", null));
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                // 创建一个新的Bitmap来存储结果
                Bitmap blurredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

                // 使用Canvas和Paint进行绘制
                Canvas canvas = new Canvas(blurredBitmap);
                Paint paint = new Paint();
                paint.setAlpha(50); // 设置透明度
                // 绘制原始图像到新的Bitmap上
                canvas.drawBitmap(bitmap, 0, 0, paint);

                // 创建BitmapDrawable并设置其边界为原始bitmap的尺寸
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), blurredBitmap);

                // 使用Matrix进行缩放和裁剪
                Matrix matrix = new Matrix();
                float scale = Math.max((float) recyclerView.getWidth() / blurredBitmap.getWidth(),
                        (float) recyclerView.getHeight() / blurredBitmap.getHeight());
                matrix.postScale(scale, scale);
                matrix.postTranslate(-(blurredBitmap.getWidth() * scale - recyclerView.getWidth()) / 2,
                        -(blurredBitmap.getHeight() * scale - recyclerView.getHeight()) / 2);

                bitmapDrawable.setBounds(0, 0, recyclerView.getWidth(), recyclerView.getHeight());
                bitmapDrawable.getPaint().setShader(new BitmapShader(blurredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                bitmapDrawable.getPaint().getShader().setLocalMatrix(matrix);

                // 设置recyclerView的背景
                recyclerView.setBackground(bitmapDrawable);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "图片加载失败,权限出错!", Toast.LENGTH_SHORT).show();
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        x = String.valueOf(location.getLongitude());
                        y = String.valueOf(location.getLatitude());
                        Geocoder geocoder = new Geocoder(MainLaunch.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!addresses.isEmpty()) {
                                Toast.makeText(MainLaunch.this, "定位成功", Toast.LENGTH_SHORT).show();
                                Address address = addresses.get(0);
                                String detail = address.getAddressLine(0);
                                addressTextView.setText(detail);
                                tot = detail;
                                province = address.getAdminArea();
                                city = address.getLocality();
                                extracted();
                            } else {
                                Log.d("Location", "地址解析失败，使用默认值");
                                addressTextView.setText("未知地址");
                                tot = "未知地址";
                                province = "未知省份";
                                city = "未知城市";
                                extracted();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("Location", "GPS定位失败");
                            addressTextView.setText("未知定位,默认设置北京市");
                            tot = "北京市";
                            province = "北京市";
                            city = "北京市";
                            extracted();
                        }
                    } else {
                        Log.d("Location", "定位失败");
                        addressTextView.setText("未知定位,默认设置北京市");
                        tot = "北京市";
                        province = "北京市";
                        city = "北京市";
                        extracted();
                    }
                });
    }

    private void sendGetRequest() {
        executorService.execute(() -> {
            try {
                String web = "http://mai.godserver.cn:11451/api/mai/v1/search?prompt1=" + city.split("市")[0] + "&status=市";
                if (!isFlag) {
                    web = "http://mai.godserver.cn:11451/api/mai/v1/search?data_place=" + tagplace;
                }
                String result = fetchNetworkData(web);
                handler.post(() -> handleResponse(result));
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(MainLaunch.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show());
                Log.e("OkHttp", "Error: " + e.getMessage());
            }
        });
    }

    private String fetchNetworkData(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    private void handleResponse(String result) {
        a.clear();
        b.clear();
        if (!result.equals("BedWeb")) {
            a = parseJsonToPlaceList(result);
            // 设置适配器
            Log.d("MainLaunch", "x:" + x + "y:" + y);
            for (Place p : a) {
                try {
                    if (p.getName().equals("个人位置")) {
                        x = p.getX() + "";
                        y = p.getY() + "";
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
                if (x == null || x.isEmpty()) {
                    Log.d("Location", "定位失败");
                    addressTextView.setText("未知定位,默认设置北京市");
                    tot = "北京市";
                    province = "北京市";
                    city = "北京市";
                    extracted();
                    return;
                }
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
                adapter = new PlaceAdapter(a, place -> {
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
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
                // 设置Toolbar
                androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);// 设置Toolbar标题

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("FindMaimaiDX - " + a.size() + " 店铺" + "\n" + tot);
                }

                for (Place p : a) {
                    if (p.getX() == 0.0) {
                        // Log.i(p.getId() + "", p.getName() + "没有坐标");
                    }
                }
            }
        } else {
            Toast.makeText(MainLaunch.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {
        }.getType();
        if (jsonString.equals("BedWeb")) {
            Toast.makeText(MainLaunch.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show();
            return null;
        }
        return gson.fromJson(jsonString, placeListType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void extracted() {
        Log.i("TAG", "x=" + x + ";y=" + y);
        if (!isFlag) {
            try {
                AddressParser.parseAddress(tot);
            } catch (Exception e) {
                Toast.makeText(MainLaunch.this, "错误", Toast.LENGTH_SHORT).show();
            }
        }
        sendGetRequest();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    @NotNull
    @Override
    public CoroutineContext getCoroutineContext() {
        return null;
    }
}