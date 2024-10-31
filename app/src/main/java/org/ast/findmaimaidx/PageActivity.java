package org.ast.findmaimaidx;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.ast.findmaimaidx.been.Market;
import org.ast.findmaimaidx.been.Place;
import org.ast.findmaimaidx.utill.FindNearMarket;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.ast.findmaimaidx.utill.FindNearMarket.findnear;

public class PageActivity extends AppCompatActivity {
    private double[] tagXY;
    private String tagplace;
    public static List<Market> marketList = new ArrayList<>();
    @Override
    @SuppressLint({"MissingInflatedId", "Range"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item2);
        String name = getIntent().getStringExtra("name").split(" ")[0];
        String address = getIntent().getStringExtra("address");
        String province = getIntent().getStringExtra("province");
        String city = getIntent().getStringExtra("city");
        String area = getIntent().getStringExtra("area");
        double x = getIntent().getDoubleExtra("x", 0);
        double y = getIntent().getDoubleExtra("y", 0);
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
        Place place = new Place(1, name, province, city, area, address, 1, x, y);
        //findnear(place);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行按钮点击时的操作
                showNavigationOptions();
            }
        });
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
}
