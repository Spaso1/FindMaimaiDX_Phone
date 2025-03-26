package org.astral.findmaimaiultra.ui.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.been.Place;
import org.astral.findmaimaiultra.databinding.FragmentGalleryBinding;
import org.astral.findmaimaiultra.map2d.BasicMapActivity;
import org.astral.findmaimaiultra.utill.SharedViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;
    private MapView mapView;
    private BaiduMap baiduMap;
    private SharedViewModel sharedViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);
        SDKInitializer.setAgreePrivacy(requireContext().getApplicationContext(),true);
        SDKInitializer.initialize(requireContext().getApplicationContext());
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        double x = 116.3912757;
        double y = 39.906217;
        try {
            x = Double.parseDouble(Objects.requireNonNull(sharedViewModel.getSharedMap().getValue().get("x")));
            y = Double.parseDouble(Objects.requireNonNull(sharedViewModel.getSharedMap().getValue().get("y")));
        }catch (Exception e) {
            Toast.makeText(getContext(), "经纬度获取失败", Toast.LENGTH_SHORT).show();
        }


        mapView = binding.bmapView;
        mapView.onCreate(getContext(),savedInstanceState);

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

        ArrayList<Place> placeList = Objects.requireNonNull(sharedViewModel.getPlacelist().getValue());
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
        return root;
    }
    private void addMarker(LatLng latLng, String title, String snippet) {
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.sd);
        MarkerOptions markerOptions = new MarkerOptions();
        Bundle bundle = new Bundle();
        bundle.putString("snippet", snippet);
        bundle.putString("title", title);
        markerOptions.position(latLng)
                .title(title)
                .extraInfo(bundle)
                .icon(descriptor);
        baiduMap.addOverlay(markerOptions);
    }

    // 在 showMarkerInfoDialog 方法中获取 snippet
    private void showMarkerInfoDialog(Marker marker) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.marker_info_dialog, null);
        TextView titleTextView = dialogView.findViewById(R.id.titleTextView);
        TextView snippetTextView = dialogView.findViewById(R.id.snippetTextView);
        Bundle extraInfo = marker.getExtraInfo();
        try {
            titleTextView.setText(extraInfo.getString("title"));
            // 获取 snippet
            snippetTextView.setText(extraInfo.getString("snippet"));

            new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setPositiveButton("导航", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            double lx = marker.getPosition().latitude;
                            double ly = marker.getPosition().longitude;
                            Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("baidumap://map/direction?origin=latlng:" + lx + "," + ly + "|name:我的位置&destination=name:" + marker.getTitle() + "&mode=driving&src=yourCompanyName|yourAppName"));
                            getContext().startActivity(intent);
                        }
                    })
                    .setNegativeButton("关闭", null)
                    .show();
        }catch (Exception e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "乌蒙痴位置", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}