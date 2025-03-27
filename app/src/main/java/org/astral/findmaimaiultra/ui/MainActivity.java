package org.astral.findmaimaiultra.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.widget.*;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class MainActivity extends AppCompatActivity implements ImagePickerListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private SharedPreferences settingProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_music, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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
    private void updatePlace() {
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
        layout.addView(textAddressLabel);
        layout.addView(textAddress);

// 创建经度输入框及其标签
        TextView textXLabel = new TextView(this);
        textXLabel.setText("经度:");
        EditText textX = new EditText(this);
        textX.setHint("请输入经度");
        layout.addView(textXLabel);
        layout.addView(textX);

// 创建纬度输入框及其标签
        TextView textYLabel = new TextView(this);
        textYLabel.setText("纬度:");
        EditText textY = new EditText(this);
        textY.setHint("请输入纬度");
        layout.addView(textYLabel);
        layout.addView(textY);

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
                    addPlace(place);
                })
                .setNegativeButton("取消", null)
                .show();

    }
    private void addPlace(Place place) {
        String url = "http://mai.godserver.cn:11451/api/mai/v1/place";
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
                    Toast.makeText(MainActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
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
