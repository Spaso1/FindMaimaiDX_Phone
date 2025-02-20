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
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.application.MyApplication;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Geocode;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.map2d.BasicMapActivity;
import org.ast.findmaimaidx.service.LocationUpdateService;
import org.ast.findmaimaidx.utill.AddressParser;
import org.ast.findmaimaidx.adapter.PlaceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static androidx.core.location.LocationManagerCompat.requestLocationUpdates;

public class MainLaunch extends AppCompatActivity {
    private Handler handler = new Handler(Looper.getMainLooper());
    public static final int LOCATION_CODE = 301;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private String sessionId;
    private LocationManager locationManager;
    private RecyclerView recyclerView;
    private PlaceAdapter placeAdapter;
    private String locationProvider = null;
    private TextView addressTextView;
    private static final String GITHUB_API_URL = "https://api.github.com/repos/owner/repo/releases/latest";
    private boolean isAdmin;
    private String tot;
    private String x;
    private String y;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private PlaceAdapter adapter;

    public static String province;
    public static String city;
    public static List<Place> a = new ArrayList<>();
    public static List<Place> b = new ArrayList<>();
    private BroadcastReceiver locationReceiver;

    private boolean flag = true;
    private double tagXY[] = new double[2];
    private String tagplace;
    private boolean isFlag = true;
    private SharedPreferences shoucang ;
    private SharedPreferences.Editor editor;
    private SharedPreferences settingProperties;

    @Override
    @SuppressLint({"MissingInflatedId", "Range", "UnspecifiedRegisterReceiverFlag", "SetTextI18n"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        //设置随机数
        String userInput = "";
        shoucang = getSharedPreferences("shoucang@", MODE_PRIVATE);
        editor = shoucang.edit();
        try {
            // 获取传递过来的数据
            Intent intent = getIntent();
            userInput = intent.getStringExtra("address");
            if(intent.getStringExtra("address") != null) {
                Log.d("MainLaunch", "userInput: " + userInput);
                tagplace = userInput;
                isFlag = false;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(isFlag) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0x123);
        }else {
            extracted();
        }

        addressTextView = findViewById(R.id.textView);
        FloatingActionButton button2 = findViewById(R.id.fab);
        final CharSequence[][] items = {{"联系作者", "b50", "自动刷新定位", "手动选择定位", "地图", "切换到中二", "排卡","设置及更多"}};

        button2.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
// 设置对话框标题
            builder.setTitle("选择");
            System.out.println(Arrays.toString(items[0]));
            builder.setItems(items[0], (dialog, item) -> {
                switch (item) {
                    case 0:
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
                        AlertDialog dialog23 = builder23.create();
                        dialog23.show();
                        break;
                    case 1:
                        Intent intent = new Intent(MainLaunch.this, B50.class);
                        intent.putExtra("sessionId",sessionId);
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
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
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
                        AlertDialog dialog2 = builder2.create();
                        dialog2.show();
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
                        AlertDialog.Builder builder_addplace = new AlertDialog.Builder(this);
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
                        AlertDialog alertDialog = builder_addplace.create();
                        alertDialog.show();


                }
            }).show();
        });

        settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
        /**
         * 设置侦测
         */
        boolean setting_autobeta1 = settingProperties.getBoolean("setting_autobeta1",false);
        if(setting_autobeta1) {
            // 启动位置更新服务
            Intent serviceIntent = new Intent(this, LocationUpdateService.class);
            startService(serviceIntent);

            // 注册广播接收器
            locationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    double latitude = intent.getDoubleExtra("latitude", 0.0);
                    double longitude = intent.getDoubleExtra("longitude", 0.0);
                    Log.d("机厅位置","位置更新服务");
                }
            };
            registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"));
            Log.d("机厅位置","启动位置更新服务");
            Toast.makeText(MainLaunch.this, "已启动位置更新服务", Toast.LENGTH_SHORT).show();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        if (settingProperties.getString("image_uri", null) != null) {
            Uri uri = Uri.parse(settingProperties.getString("image_uri", null));
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                if (bitmap != null) {
                    // 获取RecyclerView的尺寸
                    int recyclerViewWidth = recyclerView.getWidth();
                    int recyclerViewHeight = recyclerView.getHeight();

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
                        recyclerView.setBackground(bitmapDrawable);
                    } else {
                        // 如果RecyclerView的尺寸未确定，可以使用ViewTreeObserver来监听尺寸变化
                        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                int recyclerViewWidth = recyclerView.getWidth();
                                int recyclerViewHeight = recyclerView.getHeight();

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
                                recyclerView.setBackground(bitmapDrawable);
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
                    if(res.equals("1")) {
                        System.out.println("1");
                        isAdmin = true;
                        items[0] = new CharSequence[]{"联系作者", "b50", "自动刷新定位", "手动选择定位", "地图", "切换到中二", "排卡","设置及更多","添加机厅"};
                    }
                    System.out.println(res);
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void sendGetRequest() {
        try {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    OkHttpClient client = new OkHttpClient();
                    try {
                        String web = "http://mai.godserver.cn:11451/api/mai/v1/search?prompt1=" + city.split("市")[0] + "&status=市";
                        if(!isFlag) {
                            web = "http://mai.godserver.cn:11451/api/mai/v1/search?data_place=" + tagplace;
                        }
                        @SuppressLint("StaticFieldLeak") Request request = new Request.Builder()
                                .url(web)
                                .build();

                        try (Response response = client.newCall(request).execute()) {
                            if (((Response) response).isSuccessful()) {
                                List<String> cookies = response.headers("Set-Cookie");
                                for (String cookie : cookies) {
                                    if (cookie.startsWith("JSESSIONID=")) {
                                        sessionId = cookie.split(";")[0];
                                        break;
                                    }
                                }
                                return response.body().string();
                            } else {
                                Toast.makeText(MainLaunch.this, "致命错误,服务器未启动", Toast.LENGTH_SHORT).show();

                                return "Error: " + response.code();
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainLaunch.this, "致命错误,服务器未启动", Toast.LENGTH_SHORT).show();
                            Log.e("OkHttp", "Error: " + e.getMessage());
                            return "Error: " + e.getMessage();
                        }
                    }catch (Exception e) {
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
                                if(p.getName().equals("个人位置")) {
                                    x = p.getX() + "";
                                    y = p.getY() + "";
                                    tot = p.getAddress();
                                    city = p.getCity();
                                    province = p.getProvince();
                                }
                                if (p.getIsUse() == 1) {
                                    b.add(p);
                                }else if(isAdmin) {
                                    p.setName("已弃用-"+p.getName());
                                    b.add(p);
                                }
                            }catch (Exception e) {

                            }
                        }
                        a.clear();
                        TreeMap<Double, Place> treeMap = new TreeMap<>();

                        for (Place p : b) {
                            double distance = DistanceCalculator.calculateDistance(Double.parseDouble(x), Double.parseDouble(y), p.getX(), p.getY());

                            if(shoucang.contains(p.getId() + "")) {
                                p.setName(p.getName() + " 收藏" + " 距离您" + String.format(Locale.CHINA, "%.2f", distance) + "km");
                                treeMap.put(distance - 1000, p);

                            }else {
                                p.setName(p.getName() + " 距离您" + String.format(Locale.CHINA, "%.2f", distance) + "km");
                                treeMap.put(distance, p);
                            }
                            if(p.getNumJ()>0) {
                                p.setName(p.getName() + "\uD83D\uDCB3");
                            }
                        }
                        for (Double key : treeMap.keySet()) {
                            a.add(treeMap.get(key));
                        }
                        boolean flag2 = true;
                        if(flag2) {
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
                                    intent.putExtra("count",place.getCount());
                                    intent.putExtra("bad",place.getBad());
                                    intent.putExtra("good",place.getGood());
                                    intent.putExtra("num",place.getNum());
                                    intent.putExtra("numJ",place.getNumJ());
                                    intent.putExtra("meituan",place.getMeituan_link());
                                    intent.putExtra("douyin",place.getDouyin_link());
                                    startActivity(intent);
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
                    }else {
                        Toast.makeText(MainLaunch.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show();//最终实现处
                    }

                }
            }.execute();
        }catch (Exception e) {
            Toast.makeText(MainLaunch.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show();

        }

    }

    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {
        }.getType();
        if(jsonString.equals("BedWeb")) {
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
            //创建locationManger对象
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //获取最新的定位信息
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            try {
                x = String.valueOf(lastKnownLocation.getLongitude());
                y = String.valueOf(lastKnownLocation.getLatitude());
                if (lastKnownLocation != null) {
                    Geocoder geocoder = new Geocoder(MainLaunch.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
                        if (addresses.size() > 0) {
                            Address address = addresses.get(0);
                            String detail = address.getAddressLine(0);
                            addressTextView.setText(" "+detail);
                            tot = detail;
                            province = address.getAdminArea();
                            city = address.getLocality();
                            extracted();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("Location","定位失败");
                        x = String.valueOf(116.3912757);
                        y = String.valueOf(39.906217);

                        addressTextView.setText(" 未知定位,默认设置北京市");
                        tot = "北京市";
                        province ="北京市";
                        city = "北京市";
                        extracted();
                    }
                }
            }catch (Exception e) {
                Log.d("Location","定位失败");
                x = String.valueOf(39.906217);
                y = String.valueOf(116.3912757);
                addressTextView.setText(" 未知定位,默认设置北京市");
                tot = "北京市";
                province ="北京市";
                city = "北京市";
                extracted();
            }


        }
        //每隔三秒获取一次GPS信息
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 12000, 16f, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d("Location", "onLocationChanged");
                if(flag) {
                    Toast.makeText(MainLaunch.this, "定位成功", Toast.LENGTH_SHORT);
                    x = String.valueOf(location.getLongitude());
                    y = String.valueOf(location.getLatitude());
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainLaunch.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);
                                String detail = address.getAddressLine(0);
                                addressTextView.setText(" "+detail);
                                tot = detail;
                                province = address.getAdminArea();
                                city = address.getLocality();
                                extracted();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("Location","GPS定位失败");
                            x = String.valueOf(39.906217);
                            y = String.valueOf(116.3912757);
                            addressTextView.setText("未知定位,默认设置北京市");
                            tot = "北京市";
                            province ="北京市";
                            city = "北京市";
                            extracted();
                        }
                    }
                    Toast.makeText(MainLaunch.this, "定位成功", Toast.LENGTH_SHORT);

                }

            }
        });
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
                Toast.makeText(MainLaunch.this, "错误", Toast.LENGTH_SHORT);

            }
        }
        sendGetRequest();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainLaunch.this));

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
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
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
                }else {
                    Toast.makeText(MainLaunch.this, "添加失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
