package org.astral.findmaimaiultra.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.widget.*;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.yalantis.ucrop.UCrop;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.been.Place;
import org.astral.findmaimaiultra.databinding.ActivityMainBinding;
import org.astral.findmaimaiultra.ui.home.HomeFragment;
import org.astral.findmaimaiultra.utill.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static org.astral.findmaimaiultra.ui.home.HomeFragment.x;
import static org.astral.findmaimaiultra.ui.home.HomeFragment.y;

public class MainActivity extends AppCompatActivity implements ImagePickerListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private SharedPreferences settingProperties;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("setting", MODE_PRIVATE);
        String selectedTheme = preferences.getString("selected_theme", "Theme.FindMaimaiUltra");
        // Check if the system is in night mode
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

// If the theme is gray and night mode is active, switch to white theme
        if ("Theme.FindMaimaiUltra.Gray".equals(selectedTheme) && isNightMode) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("selected_theme", "Theme.FindMaimaiUltra.White");
            editor.apply();
            recreate(); // Recreate the activity to apply the new theme
        }else if ("Theme.FindMaimaiUltra.White".equals(selectedTheme) && !isNightMode) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("selected_theme", "Theme.FindMaimaiUltra.Gray");
            editor.apply();
            recreate(); // Recreate the activity to apply the new theme
        }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            //使用根对象
            Toast.makeText(this, "NFC 不可用", Toast.LENGTH_LONG).show();
            //Snackbar.make(binding.getRoot() , "NFC 不可用", Snackbar.LENGTH_LONG).show();
            //finish();
            //return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        handleNfcIntent(getIntent());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_music,R.id.nav_pixiv, R.id.nav_slideshow,R.id.nav_earth)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("111111111", "onNewIntent: " + intent.getAction());
        handleNfcIntent(intent);
        super.onNewIntent(intent);
    }

    private void handleNfcIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                // Process the messages
                List<String> nfcData = processNdefMessages(msgs);
                for (String data : nfcData) {
                    if(data.contains("paika")) {
                        Intent intent2 = new Intent(this, PaikaActivity.class);
                        intent2.putExtra("data", data);
                        startActivity(intent2);
                    }
                    // 在这里处理NFC数据
                }
            }
        }
    }

    private List<String> processNdefMessages(NdefMessage[] msgs) {
        List<String> nfcData = new ArrayList<>();
        if (msgs == null || msgs.length == 0) return nfcData;

        for (NdefMessage msg : msgs) {
            NdefRecord[] records = msg.getRecords();
            for (NdefRecord record : records) {
                String recordData = parseNdefRecord(record);
                if (recordData != null) {
                    nfcData.add(recordData);
                }
            }
        }
        return nfcData;
    }

    private String parseNdefRecord(NdefRecord record) {
        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
            return parseUri(record);
        } else if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
            return parseText(record);
        } else {
            // 处理其他类型的记录
            return new String(record.getPayload(), StandardCharsets.UTF_8);
        }
    }

    private String parseUri(NdefRecord record) {
        byte[] uriField = record.getPayload();
        String prefix = ((char) (uriField[0] & 0x0F)) + "";
        byte[] fullUri = new byte[uriField.length - 1];
        System.arraycopy(uriField, 1, fullUri, 0, uriField.length - 1);
        return prefix + new String(fullUri, StandardCharsets.UTF_8);
    }

    private String parseText(NdefRecord record) {
        byte[] payload = record.getPayload();
        if (payload.length < 2) {
            Log.w("NfcBroadcastReceiver", "Invalid payload length for text record");
            return null;
        }

        // 第一个字节的高4位表示字符编码（0表示UTF-8，1表示UTF-16）
        String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0077;

        if (languageCodeLength > payload.length - 1) {
            Log.w("NfcBroadcastReceiver", "Invalid language code length");
            return null;
        }

        // 解析语言代码（通常不需要，除非你有特殊需求）
        String languageCode = new String(payload, 1, languageCodeLength, StandardCharsets.US_ASCII);

        // 解析文本数据
        int textStartIndex = 1 + languageCodeLength;
        int textLength = payload.length - textStartIndex;

        if (textLength <= 0) {
            Log.w("NfcBroadcastReceiver", "No text data found");
            return null;
        }

        try {
            return new String(payload, textStartIndex, textLength, Charset.forName(textEncoding));
        } catch (Exception e) {
            Log.w("NfcBroadcastReceiver", "Unsupported charset: " + textEncoding, e);
            return null;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //点击效果
        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(item -> {
            //切换到设置页面
            Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.nav_slideshow);
            return true;
        });
        menu.findItem(R.id.action_paika).setOnMenuItemClickListener(item -> {
            Intent paika = new Intent(this, PaikaActivity.class);
            startActivity(paika);
            return true;
        });
        menu.findItem(R.id.action_update).setOnMenuItemClickListener(item -> {
            Intent update = new Intent(this, UpdateActivity.class);
            startActivity(update);
            return true;
        });
        menu.findItem(R.id.action_updatePlace).setOnMenuItemClickListener(item -> {
            updatePlace();
            return true;
        });


        return false;
    }
    private LatLng selectedLatLng = null;

    private void updatePlace() {
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);
        SDKInitializer.initialize(getApplicationContext());

        Dialog mapDialog = new Dialog(this);
        mapDialog.setContentView(R.layout.dialog_map); // Use a custom layout for the map dialog
        mapDialog.setTitle("选择位置");

        // Get the MapView from the dialog layout
        MapView mapView = mapDialog.findViewById(R.id.mapView);
        BaiduMap baiduMap = mapView.getMap();

        // Set initial map status (e.g., center on Beijing)
        LatLng initialLatLng = new LatLng(Double.parseDouble(y), Double.parseDouble(x)); // Beijing coordinates
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(initialLatLng, 13));
        Snackbar.make(mapView, "请点击地图选择机厅位置", Snackbar.LENGTH_LONG).show();
        // Add a marker when the user clicks on the map
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                baiduMap.clear(); // Clear previous markers
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 130, true);
                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title("机厅位置")
                        .icon(descriptor);
                baiduMap.addOverlay(markerOptions);

                // Save the selected location
                selectedLatLng = latLng;

                Snackbar.make(mapView, "已选择位置: " + latLng.latitude + ", " + latLng.longitude, Snackbar.LENGTH_LONG)
                        .setAction("确定", v -> {
                            // Optional: Handle the action if needed
                            updatePlace2("",latLng.longitude, latLng.latitude);

                            mapDialog.dismiss();
                        }).show();
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                baiduMap.clear(); // Clear previous markers
                LatLng latLng = mapPoi.getPosition();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 130, true);
                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title("机厅位置")
                        .icon(descriptor);
                baiduMap.addOverlay(markerOptions);

                // Save the selected location
                selectedLatLng = latLng;

                Snackbar.make(mapView, "已选择位置: " + latLng.latitude + ", " + latLng.longitude, Snackbar.LENGTH_LONG)
                        .setAction("确定", v -> {
                            // Optional: Handle the action if needed
                            updatePlace2(mapPoi.getName(),latLng.longitude, latLng.latitude);

                            mapDialog.dismiss();
                        }).show();
            }
        });

        // Add a confirm button to the dialog
        Button confirmButton = mapDialog.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                // Handle the selected location (e.g., save it or update the UI)
                Toast.makeText(this, "选定位置: " + selectedLatLng.latitude + ", " + selectedLatLng.longitude, Toast.LENGTH_SHORT).show();
                mapDialog.dismiss();
            } else {
                Toast.makeText(this, "请先选择一个位置", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        mapDialog.show();
    }
    private void updatePlace2(String a,Double x,Double y) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

// 创建一个 ScrollView 并将 LinearLayout 添加到其中
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

// 创建店铺名称输入框及其标签
        TextView textNameLabel = new TextView(this);
        textNameLabel.setText("店铺名称:");
        EditText textName = new EditText(this);
        textName.setHint("请输入店铺名称");
        layout.addView(textNameLabel);
        layout.addView(textName);

// 创建省份输入框及其标签
        TextView textProvinceLabel = new TextView(this);
        textProvinceLabel.setText("省份:");
        EditText textProvince = new EditText(this);
        textProvince.setHint("请输入省份");
        layout.addView(textProvinceLabel);
        layout.addView(textProvince);

// 创建城市输入框及其标签
        TextView textCityLabel = new TextView(this);
        textCityLabel.setText("城市:");
        EditText textCity = new EditText(this);
        textCity.setHint("请输入城市");
        layout.addView(textCityLabel);
        layout.addView(textCity);

// 创建地区输入框及其标签
        TextView textAreaLabel = new TextView(this);
        textAreaLabel.setText("地区:");
        EditText textArea = new EditText(this);
        textArea.setHint("请输入地区");
        layout.addView(textAreaLabel);
        layout.addView(textArea);

// 创建地址输入框及其标签
        TextView textAddressLabel = new TextView(this);
        textAddressLabel.setText("地址:");
        EditText textAddress = new EditText(this);
        textAddress.setHint("请输入地址");
        textAddress.setText(a);
        layout.addView(textAddressLabel);
        layout.addView(textAddress);
// 创建国机数量输入框及其标签
        TextView textNumLabel = new TextView(this);
        textNumLabel.setText("国机数量:");
        EditText textNum = new EditText(this);
        textNum.setHint("请输入国机数量");
        textNum.setText("1");
        layout.addView(textNumLabel);
        layout.addView(textNum);

// 创建币数量输入框及其标签
        TextView textNumJLabel = new TextView(this);
        textNumJLabel.setText("日机数量:");
        EditText textNumJ = new EditText(this);
        textNumJ.setHint("请输入日机数量");
        textNumJ.setText(String.valueOf(0));
        layout.addView(textNumJLabel);
        layout.addView(textNumJ);

// 创建是否使用输入框及其标签
        TextView textIsUseLabel = new TextView(this);
        textIsUseLabel.setText("是否使用:");
        EditText textIsUse = new EditText(this);
        textIsUse.setHint("请输入是否使用");
        textIsUse.setText(String.valueOf(1));
        layout.addView(textIsUseLabel);
        layout.addView(textIsUse);
        Place place = new Place();
        builder.setTitle("编辑店铺信息")
                .setView(scrollView) // 设置 ScrollView 作为对话框的内容视图
                .setPositiveButton("确定", (dialog, which) -> {
                    // 获取输入框的值并更新 place 对象
                    place.setName(textName.getText().toString());
                    place.setProvince(textProvince.getText().toString());
                    place.setCity(textCity.getText().toString());
                    place.setArea(textArea.getText().toString());
                    //保留6位小数Double
                    place.setX(Double.parseDouble(String.format("%.6f", x)));
                    place.setY(Double.parseDouble(String.format("%.6f", y)));
                    place.setAddress(textAddress.getText().toString());
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

                    Log.d("SettingActivity", "更新店铺信息: " + place.toString());
                    addPlace(place);
                })
                .setNegativeButton("取消", null)
                .show();

    }
    private void addPlace(Place place) {
        String url = "https://mais.godserver.cn/api/mai/v1/place";
        String body = new Gson().toJson(place,Place.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    });
                }else {
                    runOnUiThread(() -> {
                        Log.e("SettingActivity", "添加失败: " + response.message());
                        Toast.makeText(MainActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SettingActivity", "onActivityResult called with requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            startCropActivity(uri);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            handleCroppedImage(data);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Log.e("SettingActivity", "Cropping failed: ", cropError);
            Toast.makeText(this, "裁剪失败: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Log.w("SettingActivity", "Unexpected result from image picker or cropper");
            Toast.makeText(this, "裁剪操作未成功", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCropActivity(Uri uri) {
        Uri destinationUri = Uri.fromFile(new File(this.getCacheDir(), "cropped_image.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(90);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        // 计算屏幕宽高比
        float[] aspectRatio = getScreenAspectRatio();

        UCrop uCrop = UCrop.of(uri, destinationUri)
                .withAspectRatio(aspectRatio[0], aspectRatio[1]) // 设置裁剪比例为屏幕比例
                .withMaxResultSize(getScreenWidth(), getScreenHeight()) // 设置最大结果尺寸
                .withOptions(options);

        // 启动 UCrop 并使用 cropImageLauncher 处理结果
        uCrop.start(this);
    }

    private void show(String text) {
        if (this != null) {
            this.runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
        }
    }

    private void handleCroppedImage(Intent data) {
        Uri croppedUri = UCrop.getOutput(data);
        if (croppedUri != null) {
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedUri);
                if (photo != null) {
                    File croppedFile = new File(this.getExternalFilesDir(null), "cropped_image.jpg");
                    try (FileOutputStream out = new FileOutputStream(croppedFile)) {
                        photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        SharedPreferences.Editor editor = settingProperties.edit();
                        editor.putString("image_uri", croppedUri.toString());
                        editor.apply();
                        show("成功");
                        Log.d("SettingActivity", "图片已保存到: " + croppedFile.getAbsolutePath());

                        File backgroundFile =FileUtils.getBackground(this, "background.jpg");
                        try (FileOutputStream out2 = new FileOutputStream(backgroundFile)) {
                            photo.compress(Bitmap.CompressFormat.JPEG, 90, out2);
                            Log.d("SettingActivity", "背景图片已保存到: " + backgroundFile.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        show("失败");
                        Log.e("SettingActivity", "保存图片失败: ", e);
                    }
                } else {
                    show("无法获取裁剪后的图片");
                    Log.w("SettingActivity", "无法获取裁剪后的图片");
                }
            } catch (Exception e) {
                e.printStackTrace();
                show("无法获取裁剪后的图片");
                Log.e("SettingActivity", "无法获取裁剪后的图片: ", e);
            }
        } else {
            show("无法找到裁剪后的图片");
            Log.w("SettingActivity", "无法找到裁剪后的图片");
        }
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private int getScreenHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private float[] getScreenAspectRatio() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float width = displayMetrics.widthPixels;
        float height = displayMetrics.heightPixels;
        return new float[]{width, height};
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }
}
