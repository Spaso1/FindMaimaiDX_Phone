package org.ast.findmaimaidx.ui;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.Market;
import org.ast.findmaimaidx.been.Place;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PageActivity extends AppCompatActivity {
    private double[] tagXY;
    private String tagplace;
    public static  TextView t2 ;
    public static List<Market> marketList = new ArrayList<>();
    public static LinearLayout t31 ;
    public static Context context;
    public static String key = "";
    public static List<TextView> textViews = new ArrayList<>();
    private Button likeButton;
    private Button disButton;
    private boolean isLike = false;
    private boolean isDis = false;
    private SharedPreferences sp ;
    private SharedPreferences shoucang ;
    public static int id;
    @Override
    @SuppressLint({"MissingInflatedId", "Range", "SetTextI18n", "UnspecifiedRegisterReceiverFlag"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.item2);
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
        t1.setText("舞萌总机台 " + (num + numJ));
        if(getIntent().hasExtra("type")) {
            String type = getIntent().getStringExtra("type");
            t1.setText("中二总机台 " + (num + numJ));
        }
        TextView t2 = findViewById(R.id.num6);
        t2.setText("国机 " + num);
        TextView t3 = findViewById(R.id.num7);
        t3.setText("\uD83D\uDCB3 " + numJ);
        TextView x2 = findViewById(R.id.x);
        x2.setText("经度 " + String.valueOf(x));
        TextView y2 = findViewById(R.id.y);
        y2.setText("纬度 " + String.valueOf(y));
        tagXY = new double[]{x,y};
        tagplace = name;
        Button button = findViewById(R.id.button);
        button.setText("导航");
        id = id2;
        Log.d("id", String.valueOf(id));
        /**
         * 获取附近商超
         */
        Place place = new Place(id2, name, province, city, area, address, 1, x, y, count, good, bad);
        place.setNumJ(numJ);
        place.setNum(num);
        findnear(place);

        TextView tor2 = findViewById(R.id.textView2);
        tor2.setText("\n附近商超");
        tor2.setTextSize(20.0F);
        t31 = findViewById(R.id.hor);

        sp = getSharedPreferences("like@dis", MODE_PRIVATE);
        shoucang = getSharedPreferences("shoucang@", MODE_PRIVATE);

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
        Switch switch1 = findViewById(R.id.switch1);
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
        Button addMarket = findViewById(R.id.add);
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
        Button back = findViewById(R.id.updateButton);
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
    private void sendGetRequest(int type) {
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                String web = "http://mai.godserver.cn:11451/api/mai/v1/place?id=" + id + "&type=" + type;
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
                String web = "http://mai.godserver.cn:11451/api/mai/v1/near?id=" + place_centor.getId();
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
                String web = "http://mai.godserver.cn:11451/api/mai/v1/num?id=" + id + "&num=" + num + "&numJ=" + numJ;
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

    private void startLikeAnimation(Button button) {
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
    private void startShakeAnimation(Button button) {
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
    private void backAnimation(Button button) {
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
                    .url("http://mai.godserver.cn:11451/api/mai/v1/near")
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
}
