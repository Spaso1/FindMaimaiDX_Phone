package org.ast.findmaimaidx.map2d;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.Place;

import java.util.ArrayList;

public class BasicMapActivity extends AppCompatActivity {

    private MapView mapView;
    private BaiduMap baiduMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.setAgreePrivacy(getApplicationContext(),true);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.maimaimap);


        Intent intent = getIntent();
        double x = Double.parseDouble(intent.getStringExtra("x"));
        double y = Double.parseDouble(intent.getStringExtra("y"));
        Log.d("BasicMapActivity", "x: " + x + ", y: " + y);
        mapView = findViewById(R.id.bmapView);
        mapView.onCreate(this,savedInstanceState);

        baiduMap = mapView.getMap();

        // 设置地图中心点
        LatLng latLng = new LatLng(y, x); // 北京市经纬度
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, 13)); // 缩放级别调整为

        // 添加独特样式的标记
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo); // 自定义图标资源
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 130, true); // 缩放到 100x100 像素
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("舞萌痴位置")
                .icon(descriptor); // 使用自定义图标
        baiduMap.addOverlay(markerOptions);

        ArrayList<Place> placeList = intent.getParcelableArrayListExtra("place_list_key");
        for (Place place : placeList) {
            addMarker(new LatLng(place.getY(), place.getX()), place.getName(), place.getAddress());
        }

        // 设置标记点击监听器
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showMarkerInfoDialog(marker);
                return true; // 返回 true 表示已处理点击事件
            }
        });
    }

    // 在 addMarker 方法中设置 snippet
    private void addMarker(LatLng latLng, String title, String snippet) {
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.sd2);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .title(title)
                .icon(descriptor);
        baiduMap.addOverlay(markerOptions);
    }

    // 在 showMarkerInfoDialog 方法中获取 snippet
    private void showMarkerInfoDialog(Marker marker) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.marker_info_dialog, null);
        TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
        TextView snippetTextView = dialogView.findViewById(R.id.snippetTextView);

        titleTextView.setText(marker.getTitle());
        // 获取 snippet
        snippetTextView.setText(marker.getTitle());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("导航", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        double lx = marker.getPosition().latitude;
                        double ly = marker.getPosition().longitude;
                        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("baidumap://map/direction?origin=latlng:" + lx + "," + ly + "|name:我的位置&destination=name:" + marker.getTitle() + "&mode=driving&src=yourCompanyName|yourAppName"));
                        BasicMapActivity.this.startActivity(intent);
                    }
                })
                .setNegativeButton("关闭", null)
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
   