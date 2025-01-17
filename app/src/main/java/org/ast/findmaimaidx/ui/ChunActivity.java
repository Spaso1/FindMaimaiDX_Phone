package org.ast.findmaimaidx.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.location.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.map2d.BasicMapActivity;
import org.ast.findmaimaidx.utill.AddressParser;
import org.ast.findmaimaidx.utill.PlaceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class  ChunActivity extends AppCompatActivity {
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
    private Handler handler = new Handler(Looper.getMainLooper());
    public static final int LOCATION_CODE = 301;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;

    private LocationManager locationManager;
    private RecyclerView recyclerView;
    private PlaceAdapter placeAdapter;
    private String locationProvider = null;
    private TextView addressTextView;
    @Override
    @SuppressLint({"MissingInflatedId", "Range", "UnspecifiedRegisterReceiverFlag", "SetTextI18n"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chun);
        settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
        boolean isFlag = true;
        String userInput = "";

        shoucang = getSharedPreferences("shoucang@", MODE_PRIVATE);
        editor = shoucang.edit();
        try {
            // 获取传递过来的数据
            Intent intent = getIntent();
            userInput = intent.getStringExtra("address");
            if(intent.getStringExtra("address") != null) {
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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        if(settingProperties.getString("image_uri", null) != null) {
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

        FloatingActionButton button2 = findViewById(R.id.fab);
        button2.setOnClickListener(v -> {
            //刷新定位

            // 创建一个AlertDialog.Builder对象
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final CharSequence[] items = {"联系作者","b50", "自动刷新定位", "手动选择定位","地图","切换到舞萌","设置及更多"};
// 设置对话框标题
            builder.setTitle("选择");
// 添加“确定”按钮
            builder.setItems(items, (dialog, item) -> {
                switch (item) {
                    case 0:
// 创建一个Intent对象，指定动作和数据类型
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("message/rfc822"); // 设置数据类型为邮件
// 设置邮件的基本信息
                        String[] recipients = {"astralpath@163.com"}; // 收件人邮箱地址
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, "astralpath@163.com");
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FindMaimaiDX问题提交"); // 邮件主题
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "邮件至 astralpath@163.com"); // 邮件正文

// 检查是否有可以处理此Intent的应用
                        if (emailIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(emailIntent); // 启动邮件发送界面
                        } else {
                            // 如果没有可以处理此Intent的应用，则显示错误消息
                            Toast.makeText(this, "没有可用的邮件客户端", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case 1:
                        Intent intent = new Intent(ChunActivity.this, b50.class);
                        startActivity(intent);
                        break;
                    case 2:
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        try {
                            x = String.valueOf(location.getLongitude());
                            y = String.valueOf(location.getLatitude());

                            Geocoder geocoder = new Geocoder(ChunActivity.this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!addresses.isEmpty()) {
                                Toast.makeText(ChunActivity.this, "定位成功", Toast.LENGTH_SHORT).show();
                                Address address = addresses.get(0);
                                String detail = address.getAddressLine(0);
                                addressTextView.setText(detail);
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
                                                        Intent intent = new Intent(ChunActivity.this, ChunActivity.class);
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
                        Intent intent2 = new Intent(ChunActivity.this, BasicMapActivity.class);
                        intent2.putExtra("x", x);
                        intent2.putExtra("y", y);
                        ArrayList<Place> aL = new ArrayList<>(a);

                        intent2.putParcelableArrayListExtra("place_list_key", aL);
                        startActivity(intent2);
                        break;
                    case 5:
                        Intent intent1 = new Intent(ChunActivity.this, MainLaunch.class);
                        startActivity(intent1);
                        break;
                    case 6:
                        Intent intent3 = new Intent(ChunActivity.this, SettingActivity.class);
                        startActivity(intent3);
                        break;
                }
            }).show();
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
                        String web = "http://mai.godserver.cn:11451/api/chu/v1/search?prompt1=" + city.split("市")[0] + "&status=市";
                        if(!isFlag) {
                            web = "http://mai.godserver.cn:11451/api/chu/v1/search?data_place=" + tagplace;
                        }
                        @SuppressLint("StaticFieldLeak") Request request = new Request.Builder()
                                .url(web)
                                .build();

                        try (Response response = client.newCall(request).execute()) {
                            if (((Response) response).isSuccessful()) {
                                return response.body().string();
                            } else {
                                Toast.makeText(ChunActivity.this, "致命错误,服务器未启动", Toast.LENGTH_SHORT).show();

                                return "Error: " + response.code();
                            }
                        } catch (Exception e) {
                            Toast.makeText(ChunActivity.this, "致命错误,服务器未启动", Toast.LENGTH_SHORT).show();
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
                                }
                            }catch (Exception e) {

                            }
                        }
                        a.clear();
                        TreeMap<Double, Place> treeMap = new TreeMap<>();

                        for (Place p : b) {
                            Log.d("ChunActivity", p.getName() + "" + p.getNum() + "," + p.getNumJ());
                            double distance = DistanceCalculator.calculateDistance(Double.parseDouble(x), Double.parseDouble(y), p.getX(), p.getY());
                            try {
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
                            }catch (Exception e) {
                                e.printStackTrace();
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
                                    Intent intent = new Intent(ChunActivity.this, PageActivity.class);
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
                                    intent.putExtra("type","chun");
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
                        Toast.makeText(ChunActivity.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show();//最终实现处

                    }

                }
            }.execute();
        }catch (Exception e) {
            Toast.makeText(ChunActivity.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT).show();

        }

    }

    private List<Place> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Place>>() {
        }.getType();
        if(jsonString.equals("BedWeb")) {
            Toast.makeText(ChunActivity.this, "网络错误(服务器维护?)", Toast.LENGTH_SHORT);
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
                    Geocoder geocoder = new Geocoder(ChunActivity.this, Locale.getDefault());
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
                        Log.d("Location","定位失败");
                        x = String.valueOf(116.3912757);
                        y = String.valueOf(39.906217);

                        addressTextView.setText("未知定位,默认设置北京市");
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
                addressTextView.setText("未知定位,默认设置北京市");
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
                    Toast.makeText(ChunActivity.this, "定位成功", Toast.LENGTH_SHORT);
                    x = String.valueOf(location.getLongitude());
                    y = String.valueOf(location.getLatitude());
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(ChunActivity.this, Locale.getDefault());
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
                    Toast.makeText(ChunActivity.this, "定位成功", Toast.LENGTH_SHORT);

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
                Toast.makeText(ChunActivity.this, "错误", Toast.LENGTH_SHORT);

            }
        }
        sendGetRequest();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChunActivity.this));

    }
}
