package org.ast.findmaimaidx.ui;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.Market;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.been.PlaceContent;
import org.ast.findmaimaidx.message.ApiResponse;
import org.ast.findmaimaidx.adapter.ReviewAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PageActivity extends AppCompatActivity {
    private double[] tagXY;
    private String tagplace;
    public static  TextView t2 ;
    private String type_code = "mai";
    public static List<Market> marketList = new ArrayList<>();
    public static LinearLayout t31 ;
    public static Context context;
    public static String key = "";
    public static List<TextView> textViews = new ArrayList<>();
    private MaterialButton likeButton;
    private MaterialButton disButton;
    private boolean isLike = false;
    private boolean isDis = false;
    private String meituan = "";
    private String douyin = "";
    private SharedPreferences sp ;
    private SharedPreferences shoucang ;
    private TextView numberPeo;
    private MaterialButton adminIt;
    private OkHttpClient client;
    private Place place;
    public static int id;
    @Override
    @SuppressLint({"MissingInflatedId", "Range", "SetTextI18n", "UnspecifiedRegisterReceiverFlag"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.page);
        client = new OkHttpClient();
        /**
         * 基础内容加载
         */
        String name = getIntent().getStringExtra("name").split(" ")[0];
        int id2 = getIntent().getIntExtra("id", 0);
        String address = getIntent().getStringExtra("address");
        String province = getIntent().getStringExtra("province");
        String city = getIntent().getStringExtra("city");
        String area = getIntent().getStringExtra("area");
        double x = getIntent().getDoubleExtra("x", 0);
        double y = getIntent().getDoubleExtra("y", 0);
        int count = getIntent().getIntExtra("count", 0);
        int good = getIntent().getIntExtra("good", 0);
        int bad = getIntent().getIntExtra("bad", 0);
        int num = getIntent().getIntExtra("num",0);
        int numJ = getIntent().getIntExtra("numJ",0);
        meituan = getIntent().getStringExtra("meituan");
        douyin = getIntent().getStringExtra("douyin");
        numberPeo = findViewById(R.id.numberPeo);
        TextView textView = findViewById(R.id.nameTextView);
        textView.setText(name);
        TextView textView2 = findViewById(R.id.addressTextView);
        textView2.setText(address);
        TextView textView3 = findViewById(R.id.provinceTextView);
        textView3.setText(province);
        TextView textView4 = findViewById(R.id.cityTextView);
        textView4.setText(city);
        TextView textView5 = findViewById(R.id.areaTextView);
        textView5.setText(area);
        TextView t1 = findViewById(R.id.num5);
        adminIt = findViewById(R.id.admin);
        t1.setText("舞萌总机台 " + (num + numJ));
        if(getIntent().hasExtra("type")) {
            String type = getIntent().getStringExtra("type");
            type_code = "chu";
            t1.setText("中二总机台 " + (num + numJ));
        }
        TextView t2 = findViewById(R.id.num6);
        t2.setText("国机 " + num);
        TextView t3 = findViewById(R.id.num7);
        tagXY = new double[]{x,y};
        tagplace = name;
        MaterialButton share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", address );
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(PageActivity.this, "机厅地址信息已经复制!", Toast.LENGTH_SHORT).show();
            }
        });
        MaterialButton bi = findViewById(R.id.bi);
        bi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                openLinkHub(meituan,douyin);
            }
        });

        MaterialButton button = findViewById(R.id.button);
        button.setText("导航");
        id = id2;
        Log.d("id", String.valueOf(id));
        /**
         * 获取附近商超
         */
        place = new Place(id2, name, province, city, area, address, 1, x, y, count, good, bad);
        place.setNumJ(numJ);
        place.setNum(num);
        findnear(place);

        TextView tor2 = findViewById(R.id.textView2);
        tor2.setText("\n附近商超");
        tor2.setTextSize(20.0F);
        t31 = findViewById(R.id.hor);

        sp = getSharedPreferences("like@dis", MODE_PRIVATE);
        shoucang = getSharedPreferences("shoucang@", MODE_PRIVATE);
        getNumberPeo();
        SharedPreferences.Editor editor = sp.edit();
        SharedPreferences.Editor editor2 = shoucang.edit();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮点击时的操作
                tagXY[0] = x;
                tagXY[1] = y;
                tagplace = name;
                showNavigationOptions();
            }
        });
        /**
         * 添加点赞点踩效果
         */
        likeButton = findViewById(R.id.likeButton);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLike) {
                    startShakeAnimation(likeButton);

                }else {
                    if (isDis) {
                        backAnimation(disButton);
                        sendGetRequest(4);
                    }
                    isLike = true;
                    isDis = false;
                    startLikeAnimation(likeButton);
                    editor.remove(name);
                    editor.putString(name,"1");
                    editor.apply();
                    sendGetRequest(1);
                }
            }
        });
        disButton = findViewById(R.id.disButton);
        disButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDis) {
                    startShakeAnimation(disButton);
                }else {
                    if (isLike) {
                        backAnimation(likeButton);
                        sendGetRequest(3);
                    }
                    isDis = true;
                    isLike = false;
                    startLikeAnimation(disButton);
                    editor.remove(name);
                    editor.putString(name,"0");
                    editor.apply();
                    sendGetRequest(2);
                }
            }
        });

        if(sp.contains(name)) {
            if(sp.getString(name,"0").equals("1")) {
                likeButton.setBackgroundColor(Color.parseColor("#FF0000"));
                likeButton.setTextColor(Color.parseColor("#FFFFFF"));
                isLike = true;
            }else {
                disButton.setBackgroundColor(Color.parseColor("#FF0000"));
                disButton.setTextColor(Color.parseColor("#FFFFFF"));
                isDis = true;
            }
        }

        /**
         * 添加收藏
         */
        com.google.android.material.switchmaterial.SwitchMaterial switch1 = findViewById(R.id.switch1);
        if(shoucang.contains(id2 + "")) {
            switch1.setChecked(true);
        }
        switch1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(switch1.isChecked()) {
                    editor2.putString(id2 + "","1");
                }else {
                    editor2.remove(id2 + "");
                }
                editor2.apply();
            }
        });
        /**
         * 添加商超
         */
        MaterialButton addMarket = findViewById(R.id.add);
        addMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// 创建对话框构建器
                // 创建一个线性布局
                LinearLayout layout = new LinearLayout(PageActivity.context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(16, 16, 16, 16);

                // 创建店铺名称输入框
                EditText etShopName = new EditText(PageActivity.context);
                etShopName.setHint("请输入店铺名称");
                layout.addView(etShopName);

                // 创建机厅距离输入框
                EditText etHallDistance = new EditText(PageActivity.context);
                etHallDistance.setHint("请输入机厅距离(米)");
                layout.addView(etHallDistance);

                // 创建对话框构建器
                AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.context);
                // 设置自定义布局
                builder.setView(layout);

                // 添加确定按钮
                builder.setPositiveButton("确定", (dialogInterface, i) -> {
                    String shopName = etShopName.getText().toString();
                    String hallDistance = etHallDistance.getText().toString();
                    Market market = new Market();
                    market.setMarketName(shopName);
                    market.setParentId(id);
                    DecimalFormat decimalFormat = new DecimalFormat("0.#");
                    double distance = Double.parseDouble(decimalFormat.format(Double.parseDouble(hallDistance) / 1000));
                    market.setDistance(distance);
                    market.setType(1);
                    market.setX(place.getX());
                    market.setY(place.getY());
                    new SendMarketRequestTask().execute(market);
                    Log.d("body",market.toString());
                    Toast.makeText(PageActivity.context, "已添加,等待审核", Toast.LENGTH_SHORT).show();
                });

                // 添加取消按钮
                builder.setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());

                // 创建对话框
                AlertDialog dialog = builder.create();

                // 显示对话框
                dialog.show();
            }
        });
        MaterialButton back = findViewById(R.id.updateButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //创建一个可以输入数量的弹窗,在点击确定后执行指定操作
                AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.context);
                LinearLayout layout = new LinearLayout(PageActivity.context);
                layout.setOrientation(LinearLayout.VERTICAL);
                EditText editText = new EditText(PageActivity.context);
                editText.setHint("这是国框数量 目前是" + place.getNum() + "个");
                EditText editTextJ = new EditText(PageActivity.context);
                editTextJ.setHint("这是\uD83D\uDCB3数量 目前是" + place.getNumJ() + "个");
                editTextJ.setText("0");
                layout.addView(editText);
                layout.addView(editTextJ);
                builder.setTitle("输入数量")
                        .setView(layout)
                        .setPositiveButton("确定", (dialog, which) -> {
                            String input = editText.getText().toString();
                            Log.d("输入的数量是：", input);
                            try {
                                int inputNum = Integer.parseInt(input);
                                int inputJ = Integer.parseInt(editTextJ.getText().toString());
                                sendUpdateNum(id,Integer.parseInt(input),inputJ);
                            }catch (Exception e) {
                                Toast.makeText(PageActivity.context, "请输入数字", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

            }
        });
        WebView webView = findViewById(R.id.imageView1);
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
        WebView webView2 = findViewById(R.id.imageView2);
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

        checkAndIntial();
        getContent();

    }
    @SuppressLint("MissingInflatedId")
    private void getContent() {
        MaterialButton button = findViewById(R.id.list);
        button.setOnClickListener(v -> {
            // 创建弹窗
            AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_reviews, null);
            builder.setView(dialogView);

            // 获取弹窗中的视图
            RecyclerView recyclerViewReviews = dialogView.findViewById(R.id.recyclerViewReviews);
            EditText editTextComment = dialogView.findViewById(R.id.editTextComment);
            EditText usrName = dialogView.findViewById(R.id.userName);
            MaterialButton buttonSubmit = dialogView.findViewById(R.id.buttonSubmit);

            // 获取评价数据
            fetchReviewsFromApi(reviews -> {
                // 设置RecyclerView的适配器
                ReviewAdapter adapter = new ReviewAdapter(reviews);
                recyclerViewReviews.setLayoutManager(new LinearLayoutManager(PageActivity.this));
                recyclerViewReviews.setAdapter(adapter);

                // 设置发表评论按钮的点击事件
                buttonSubmit.setOnClickListener(v1 -> {
                    String comment = editTextComment.getText().toString().trim();
                    String userName = usrName.getText().toString().trim();
                    if (!comment.isEmpty()) {
                        PlaceContent newReview = new PlaceContent();
                        newReview.setUser_name(userName); // 可以替换为当前用户的名字
                        newReview.setUser_content(comment);
                        newReview.setPlace_id(id); // 设置关联的place_id
                        newReview.setUsed(true); // 设置是否使用
                        adapter.addReview(newReview);
                        editTextComment.setText("");
                        sendReviewToServer(newReview);
                        // 可以在这里添加发送评论到服务器的逻辑
                    }
                });

                // 显示弹窗
                AlertDialog dialog = builder.create();
                dialog.show();
            });
        });
    }

    private void sendReviewToServer(PlaceContent review) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        String json = gson.toJson(review);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/mai/v1/placeContent")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(PageActivity.this, "发送评论失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(PageActivity.this, "评论发送成功", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(PageActivity.this, "发送评论失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void fetchReviewsFromApi(Consumer<List<PlaceContent>> callback) {
        OkHttpClient client = new OkHttpClient();
        Log.d("TAG", "fetchReviewsFromApi: " + id);
        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/mai/v1/placeContent?id=" + id)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(PageActivity.this, "获取评价失败", Toast.LENGTH_SHORT).show();
                    callback.accept(new ArrayList<>());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<PlaceContent>>() {}.getType();
                    List<PlaceContent> reviews = gson.fromJson(responseData, listType);
                    runOnUiThread(() -> callback.accept(reviews));
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(PageActivity.this, "获取评价失败", Toast.LENGTH_SHORT).show();
                        callback.accept(new ArrayList<>());
                    });
                }
            }
        });
    }


    private void checkAndIntial() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String url = "http://mai.godserver.cn:11451/api/mai/v1/check?androidId=" + androidId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        adminIt.setOnClickListener(v -> {
            // 创建一个可以输入数量的弹窗,在点击确定后执行指定操作
            AlertDialog.Builder builder = new AlertDialog.Builder(PageActivity.this);
            LinearLayout layout = new LinearLayout(PageActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 16, 16);

// 创建一个 ScrollView 并将 LinearLayout 添加到其中
            ScrollView scrollView = new ScrollView(PageActivity.this);
            scrollView.addView(layout);

// 创建店铺名称输入框及其标签
            TextView textNameLabel = new TextView(PageActivity.this);
            textNameLabel.setText("店铺名称:");
            EditText textName = new EditText(PageActivity.this);
            textName.setHint("请输入店铺名称");
            textName.setText(place.getName());
            layout.addView(textNameLabel);
            layout.addView(textName);

// 创建省份输入框及其标签
            TextView textProvinceLabel = new TextView(PageActivity.this);
            textProvinceLabel.setText("省份:");
            EditText textProvince = new EditText(PageActivity.this);
            textProvince.setHint("请输入省份");
            textProvince.setText(place.getProvince());
            layout.addView(textProvinceLabel);
            layout.addView(textProvince);

// 创建城市输入框及其标签
            TextView textCityLabel = new TextView(PageActivity.this);
            textCityLabel.setText("城市:");
            EditText textCity = new EditText(PageActivity.this);
            textCity.setHint("请输入城市");
            textCity.setText(place.getCity());
            layout.addView(textCityLabel);
            layout.addView(textCity);

// 创建地区输入框及其标签
            TextView textAreaLabel = new TextView(PageActivity.this);
            textAreaLabel.setText("地区:");
            EditText textArea = new EditText(PageActivity.this);
            textArea.setHint("请输入地区");
            textArea.setText(place.getArea());
            layout.addView(textAreaLabel);
            layout.addView(textArea);

// 创建地址输入框及其标签
            TextView textAddressLabel = new TextView(PageActivity.this);
            textAddressLabel.setText("地址:");
            EditText textAddress = new EditText(PageActivity.this);
            textAddress.setHint("请输入地址");
            textAddress.setText(place.getAddress());
            layout.addView(textAddressLabel);
            layout.addView(textAddress);

// 创建经度输入框及其标签
            TextView textXLabel = new TextView(PageActivity.this);
            textXLabel.setText("经度:");
            EditText textX = new EditText(PageActivity.this);
            textX.setHint("请输入经度");
            textX.setText(String.valueOf(place.getX()));
            layout.addView(textXLabel);
            layout.addView(textX);

// 创建纬度输入框及其标签
            TextView textYLabel = new TextView(PageActivity.this);
            textYLabel.setText("纬度:");
            EditText textY = new EditText(PageActivity.this);
            textY.setHint("请输入纬度");
            textY.setText(String.valueOf(place.getY()));
            layout.addView(textYLabel);
            layout.addView(textY);

// 创建国机数量输入框及其标签
            TextView textNumLabel = new TextView(PageActivity.this);
            textNumLabel.setText("国机数量:");
            EditText textNum = new EditText(PageActivity.this);
            textNum.setHint("请输入国机数量");
            textNum.setText(String.valueOf(place.getNum()));
            layout.addView(textNumLabel);
            layout.addView(textNum);

// 创建币数量输入框及其标签
            TextView textNumJLabel = new TextView(PageActivity.this);
            textNumJLabel.setText("日机数量:");
            EditText textNumJ = new EditText(PageActivity.this);
            textNumJ.setHint("请输入日机数量");
            textNumJ.setText(String.valueOf(place.getNumJ()));
            layout.addView(textNumJLabel);
            layout.addView(textNumJ);

// 创建是否使用输入框及其标签
            TextView textIsUseLabel = new TextView(PageActivity.this);
            textIsUseLabel.setText("是否使用:");
            EditText textIsUse = new EditText(PageActivity.this);
            textIsUse.setHint("请输入是否使用");
            textIsUse.setText(String.valueOf(place.getIsUse()));
            layout.addView(textIsUseLabel);
            layout.addView(textIsUse);

            builder.setTitle("编辑店铺信息")
                    .setView(scrollView) // 设置 ScrollView 作为对话框的内容视图
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 获取输入框的值并更新 place 对象
                        place.setName(textName.getText().toString());
                        place.setProvince(textProvince.getText().toString());
                        place.setCity(textCity.getText().toString());
                        place.setArea(textArea.getText().toString());
                        place.setAddress(textAddress.getText().toString());
                        place.setX(Double.parseDouble(textX.getText().toString()));
                        place.setY(Double.parseDouble(textY.getText().toString()));
                        place.setNum(Integer.parseInt(textNum.getText().toString()));
                        int num2 = 0;
                        try {
                            num2 = Integer.parseInt(textNumJ.getText().toString());
                        } catch (NumberFormatException e) {
                            throw new RuntimeException(e);
                        }
                        place.setNumJ(num2);
                        place.setIsUse(Integer.parseInt(textIsUse.getText().toString()));
                        // 调用 sendUpdateNum 方法上传更新
                        update(place);
                    })
                    .setNegativeButton("取消", null)
                    .show();

        });


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                adminIt.setVisibility(View.GONE);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        if(!responseData.equals("1")) {
                            adminIt.setVisibility(View.GONE);
                            Toast.makeText(PageActivity.this, "管理员", Toast.LENGTH_LONG).show();
                        }
                        Log.d("TAG", "Response: " + responseData);
                    });
                } else {
                    adminIt.setVisibility(View.GONE);
                }
            }
        });
    }
    public void update(Place place) {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String url = "http://mai.godserver.cn:11451/api/mai/v1/place?androidId=" + androidId;
        String json = new Gson().toJson(place);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(PageActivity.this, "Request failed", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        Log.d("TAG", "Response: " + responseData);
                        Toast.makeText(PageActivity.this, "更改成功", Toast.LENGTH_SHORT).show();
                    });
                }
                runOnUiThread(() -> {
                });
            }
        });
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
        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidamap://route?sourceApplication=appName&slat=&slon=&sname=我的位置&dlat=" + tagXY[1] +"&dlon="+ tagXY[0]+"&dname=" +tagplace + "&dev=0&t=2"));
        PageActivity.this.startActivity(intent);
    }

    private void startBaiduMaps() {
        Toast.makeText(PageActivity.context, "111", Toast.LENGTH_SHORT).show();
        String uri = "baidumap://map/direction?destination=latlng:" + tagXY[0] + "," + tagXY[1] +"&mode=driving&src=appName";
        Intent intent = new Intent("com.baidu.tieba",  android.net.Uri.parse(uri));
        startActivity(intent);
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
    @SuppressLint("StaticFieldLeak")
    private void getNumberPeo() {
        try {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    OkHttpClient client = new OkHttpClient();
                    try {
                        String web = "http://www.godserver.cn:11451/api/mai/v1/placePeo?";
                        // 将JSON对象转换为RequestBody
                        MediaType JSON = MediaType.get("application/json; charset=utf-8");
                        @SuppressLint("StaticFieldLeak") Request request = new Request.Builder()
                                .url(web + "id=" + id)
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
                    try {
                        ApiResponse apiResponse = new Gson().fromJson(result, ApiResponse.class);
                        if (apiResponse.getStatus() == 200) {
                            numberPeo.setText("机厅预计人数:" + apiResponse.getMessage());
                        }
                    }catch (Exception e) {
                        numberPeo.setText("无法预计");
                    }

                }
            }.execute();
        } catch (Exception e) {
            Log.e("OkHttp", "Error: " + e.getMessage());
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void sendGetRequest(int type) {
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                String web = "http://mai.godserver.cn:11451/api/" + type_code + "/v1/place?id=" + id + "&type=" + type;
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
                    Toast.makeText(PageActivity.this, "上传失败!", Toast.LENGTH_SHORT).show();
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if(result.equals("1")) {
                    Toast.makeText(PageActivity.this, "上传成功!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(PageActivity.this, "数据会在第二天刷新~", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void findnear(Place place_centor) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                String web = "http://mai.godserver.cn:11451/api/" + type_code + "/v1/near?id=" + place_centor.getId();
                Log.d("Web", web);
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

            @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
            @Override
            protected void onPostExecute(String result) {
                if (result.contains("[{")) {
                    marketList = parseJsonToPlaceList2(result);
                    Toast.makeText(PageActivity.context, "数据获取成功"+ result, Toast.LENGTH_SHORT);
                    for (Market market : marketList) {
                        Log.d("Market", market.getMarketName());
                    }
                    for (int i = 0; i < marketList.size(); i++) {
                        TextView t = new TextView(PageActivity.context);
                        t.setTextColor(ContextCompat.getColor(PageActivity.context, R.color.textcolorPrimary));

                        double distance = marketList.get(i).getDistance();
                        int type = marketList.get(i).getType();
                        DecimalFormat decimalFormat = new DecimalFormat("0.#");
                        String formattedResult = decimalFormat.format(distance*1000);
                        if(type==1) {
                            t.setTextColor(Color.rgb(255, 182, 193));
                        }else if(type==2) {
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
                            Toast.makeText(PageActivity.context, "即将导航" + marketList.get(finalI).getMarketName(), Toast.LENGTH_SHORT).show();
                            showNavigationOptions();
                        });
                        textViews.add(t);
                        t31.addView(t);
                    }
                }else {
                    TextView ttt =new TextView(PageActivity.context);
                    ttt.setText("暂时关闭");
                    ttt.setTextColor(ContextCompat.getColor(PageActivity.context, R.color.textcolorPrimary));

                    t31.addView(ttt);
                }
            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    private void sendUpdateNum(int id,int num,int numJ) {
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                String web = "http://mai.godserver.cn:11451/api/" + type_code + "/v1/num?id=" + id + "&num=" + num + "&numJ=" + numJ;
                Log.d("Web", numJ + "");
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
                    Toast.makeText(PageActivity.this, "上传失败!", Toast.LENGTH_SHORT).show();
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if(result.equals("1")) {
                    Toast.makeText(PageActivity.this, "上传成功!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(PageActivity.this, "机厅数据更新,重启软件后即可在主界面生效", Toast.LENGTH_SHORT).show();
                    TextView num1 = findViewById(R.id.num5);
                    num1.setText("舞萌机台 "+num);
                    AlertDialog.Builder ne = new AlertDialog.Builder(PageActivity.this);
                    ne.setTitle("重启软件即可生效")
                            .setPositiveButton("重启", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("不重启", null)
                            .show();
                }
            }
        }.execute();
    }
    private static List<Market> parseJsonToPlaceList2(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Market>>() {
        }.getType();
        return gson.fromJson(jsonString, placeListType);
    }

    private void startLikeAnimation(MaterialButton button) {
        // 缩放动画
        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.5f);
        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(button, "scaleX", 1.5f, 1f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(button, "scaleY", 1.5f, 1f);

        // 晃动动画
        ObjectAnimator shake1 = ObjectAnimator.ofFloat(button, "rotation", 0f, -5f);
        ObjectAnimator shake2 = ObjectAnimator.ofFloat(button, "rotation", -5f, 5f);
        ObjectAnimator shake3 = ObjectAnimator.ofFloat(button, "rotation", 5f, -3f);
        ObjectAnimator shake4 = ObjectAnimator.ofFloat(button, "rotation", -3f, 0f);

        // 颜色变化动画
        ObjectAnimator colorAnim = ObjectAnimator.ofInt(button, "backgroundColor", Color.BLACK, Color.RED);
        colorAnim.setEvaluator(new ArgbEvaluator());

        // 组合动画
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleOutX, scaleOutY, shake1, shake2, shake3, shake4);
        animatorSet.play(scaleInX).with(scaleInY).after(shake4);
        animatorSet.play(colorAnim).with(scaleInX);

        animatorSet.setDuration(300);
        animatorSet.start();
    }
    private void startShakeAnimation(MaterialButton button) {
        // 晃动动画
        ObjectAnimator shake1 = ObjectAnimator.ofFloat(button, "rotation", 0f, -5f);
        ObjectAnimator shake2 = ObjectAnimator.ofFloat(button, "rotation", -5f, 5f);
        ObjectAnimator shake3 = ObjectAnimator.ofFloat(button, "rotation", 5f, -3f);
        ObjectAnimator shake4 = ObjectAnimator.ofFloat(button, "rotation", -3f, 0f);

        // 组合动画
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(shake1, shake2, shake3, shake4);

        animatorSet.setDuration(300);
        animatorSet.start();
    }
    private void backAnimation(MaterialButton button) {
        // 颜色变化动画
        ObjectAnimator colorAnim = ObjectAnimator.ofInt(button, "backgroundColor", Color.RED, Color.BLACK);
        colorAnim.setEvaluator(new ArgbEvaluator());

        // 组合动画
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(colorAnim);

        animatorSet.setDuration(300);
        animatorSet.start();
    }

    private class SendMarketRequestTask extends AsyncTask<Market, Void, String> {

        @Override
        protected String doInBackground(Market... markets) {
            Market market = markets[0];

            // 使用 Gson 将 Market 对象转换为 JSON 字符串
            Gson gson = new Gson();
            String json = gson.toJson(market);

            // 创建 OkHttpClient 实例
            OkHttpClient client = new OkHttpClient();

            // 创建请求体
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

            // 创建请求
            Request request = new Request.Builder()
                    .url("http://mai.godserver.cn:11451/api/" + type_code + "/v1/near")
                    .post(body)
                    .build();

            try {
                // 发送请求
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    return "Request failed: " + response.code();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Request failed: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("TAG", "Response: " + result);
        }
    }
    private void openLinkHub(String meituan,String douyin) {
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择链接")

                .setItems(new String[]{"美团:" + meituan, "抖音:" + douyin}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("link", meituan);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(PageActivity.this, "已复制链接，请在美团中粘贴并打开", Toast.LENGTH_SHORT).show();
                        }else {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("link", douyin);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(PageActivity.this, "已复制链接，请在抖音中粘贴并打开", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setPositiveButton("更新链接(null就是不存在)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 创建一个输入框的 Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("编辑链接");

                        // 创建一个 LinearLayout 来包含两个 EditText
                        LinearLayout layout = new LinearLayout(context);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(16, 16, 16, 16);

                        // 创建美团链接的 EditText
                        EditText meituanEditText = new EditText(context);
                        meituanEditText.setHint("美团链接(店铺点击复制链接即可)");
                        meituanEditText.setText(meituan); // 设置默认值
                        layout.addView(meituanEditText);

                        // 创建抖音链接的 EditText
                        EditText douyinEditText = new EditText(context);
                        douyinEditText.setHint("抖音链接(团购点击复制链接即可)");
                        douyinEditText.setText(douyin); // 设置默认值
                        layout.addView(douyinEditText);

                        // 设置对话框的视图
                        builder.setView(layout);

                        // 添加确定按钮
                        builder.setPositiveButton("确定", (dialog, which) -> {
                            // 获取用户输入的美团和抖音链接
                            String newMeituanLink = meituanEditText.getText().toString();
                            String newDouyinLink = douyinEditText.getText().toString();

                            sendUpdateLink(id,newMeituanLink,newDouyinLink);
                            // 调用回调函数
                        });

                        // 添加取消按钮
                        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

                        // 创建并显示对话框
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    private void sendUpdateLink(int id,String meituan,String douyin) {
        this.meituan = meituan;
        this.douyin = douyin;
        RequestBody body = RequestBody.create(meituan, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/" + type_code + "/v1/updateLink?id=" + id + "&meituan=" + meituan + "&douyin=" + douyin)
                .post(body)
                .build();
        Log.d("url",("http://mai.godserver.cn:11451/api/" + type_code + "/v1/updateLink?id=" + id + "&meituan=" + meituan + "&douyin=" + douyin));
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(PageActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("TAG", "Response: " + responseBody);
                    runOnUiThread(() -> {
                        Toast.makeText(PageActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
