package org.ast.findmaimaidx.map2d;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import org.ast.findmaimaidx.PageActivity;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.Place;

import java.util.ArrayList;
import java.util.List;

public class BasicMapActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;
    private Choreographer choreographer;
    private Choreographer.FrameCallback frameCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        double x = Double.parseDouble(intent.getStringExtra("x"));
        double y = Double.parseDouble(intent.getStringExtra("y"));
        Log.d("BasicMapActivity", "x: " + x + ", y: " + y);
        setContentView(R.layout.map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState); // 此方法必须重写

        if (aMap == null) {
            aMap = mapView.getMap();
        }

        // 设置地图中心点
        LatLng latLng = new LatLng(y, x); // 北京市经纬度
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13)); // 缩放级别调整为

        // 添加独特样式的标记
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo); // 自定义图标资源
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 130, true); // 缩放到 100x100 像素

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("舞萌痴位置")
                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap)); // 使用自定义图标
        aMap.addMarker(markerOptions);

        ArrayList<Place> placeList = intent.getParcelableArrayListExtra("place_list_key");
        for (Place place : placeList) {
            addMarker(new LatLng(place.getY(), place.getX()), place.getName(), place.getAddress());
        }
        // 设置标记点击监听器
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showMarkerInfoDialog(marker);
                return true; // 返回 true 表示已处理点击事件
            }
        });

        // 初始化 Choreographer
        choreographer = Choreographer.getInstance();
        frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                // 在这里执行刷新操作
                // 例如，更新地图上的标记位置
                choreographer.postFrameCallback(this);
            }
        };
        // 开始刷新
        startRefreshing();
    }

    private void addMarker(LatLng latLng, String title, String snippet) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .title(title)
                .snippet(snippet);
        aMap.addMarker(markerOptions);
    }

    private void showMarkerInfoDialog(Marker marker) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.marker_info_dialog, null);
        TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
        TextView snippetTextView = dialogView.findViewById(R.id.snippetTextView);

        titleTextView.setText(marker.getTitle());
        snippetTextView.setText(marker.getSnippet());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("导航", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        double lx = marker.getPosition().latitude;
                        double ly = marker.getPosition().longitude;
                        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidamap://route?sourceApplication=appName&slat=&slon=&sname=我的位置&dlat=" + lx +"&dlon="+ ly+"&dname=" + marker.getTitle() + "&dev=0&t=2"));
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
        startRefreshing();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        stopRefreshing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        stopRefreshing();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void startRefreshing() {
        choreographer.postFrameCallback(frameCallback);
    }

    private void stopRefreshing() {
        choreographer.removeFrameCallback(frameCallback);
    }
}
