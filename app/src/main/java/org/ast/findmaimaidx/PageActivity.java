package org.ast.findmaimaidx;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Market;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.utill.FindNearMarket;
import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PageActivity extends AppCompatActivity {
    private double[] tagXY;
    private String tagplace;
    public static  TextView t2 ;
    public static List<Market> marketList = new ArrayList<>();
    public static LinearLayout t3 ;
    public static Context context;
    public static String key = "bb0e04ceb735481cf4e461628345f4ec";
    public static List<TextView> textViews = new ArrayList<>();
    private Button likeButton;
    private Button disButton;
    private boolean isLike = false;
    private boolean isDis = false;
    private SharedPreferences sp ;
    public static int id;
    @Override
    @SuppressLint({"MissingInflatedId", "Range"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.item2);
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

        Place place = new Place(id2, name, province, city, area, address, 1, x, y, count, good, bad);
        findnear(place);

        t2 = findViewById(R.id.textView2);
        t2.setText("\n附近商超");
        t2.setTextSize(20.0F);
        t3 = findViewById(R.id.hor);

        sp = getSharedPreferences("like@dis", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
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
        Toast.makeText(this, "正在获取附近信息", Toast.LENGTH_SHORT).show();


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
        WebView webView = findViewById(R.id.imageView1);
        String imageUrl = "https://img.shields.io/badge/like-" + good + "-green";
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
        String imageUrl2 = "https://img.shields.io/badge/dislike-" + bad + "-red";
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
        PageActivity.this.startActivity(intent);

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
    @SuppressLint("StaticFieldLeak")
    private void sendGetRequest(int type) {
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                String web = "http://www.godserver.cn:11451/place?id=" + id + "&type=" + type;
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
                String web = "https://restapi.amap.com/v5/place/around?key=" + key + "&radius=1000&location=" + place_centor.getX() + "," + place_centor.getY() + "&page_size=25&types=060200|060201|060202|060400|060401|060402|060403|060404|060405|060406|060407|060408|060409|060411|060413|060414|060415|";
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

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(String result) {
                if (result.contains("pois")) {
                    String b = result.split("\"pois\":")[1];
                    result = b.split("]")[0] + "]";
                    marketList = parseJsonToPlaceList2(result);
                    for (Market market : marketList) {
                        Log.d("Market", market.getName());
                    }
                    for (int i = 0; i < marketList.size(); i++) {
                        TextView t = new TextView(PageActivity.context);
                        double distance = DistanceCalculator.calculateDistance(Double.parseDouble(marketList.get(i).getLocation().split(",")[0]), Double.parseDouble(marketList.get(i).getLocation().split(",")[1]), place_centor.getX(), place_centor.getY());
                        DecimalFormat decimalFormat = new DecimalFormat("0.#");
                        String formattedResult = decimalFormat.format(distance*1000);

                        t.setText(marketList.get(i).getName() + " \n距离机厅:" + formattedResult + "米\n");
                        t.setTextSize(15.0F);
                        int finalI = i;
                        t.isTextSelectable();
                        t.isEnabled();
                        t.setOnClickListener(v -> {
                            tagXY[0] = Double.parseDouble(marketList.get(finalI).getLocation().split(",")[0]);
                            tagXY[1] = Double.parseDouble(marketList.get(finalI).getLocation().split(",")[1]);

                            tagplace = marketList.get(finalI).getName().split(" ")[0];
                            //导航
                            Toast.makeText(PageActivity.context, "即将导航" + marketList.get(finalI).getName(), Toast.LENGTH_SHORT).show();
                            showNavigationOptions();
                        });
                        textViews.add(t);
                        t3.addView(t);
                    }
                }else {
                    TextView ttt =new TextView(PageActivity.context);
                    ttt.setText("暂时关闭");
                    t3.addView(ttt);
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
}