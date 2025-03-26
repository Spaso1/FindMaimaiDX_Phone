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
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.yalantis.ucrop.UCrop;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;

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
        return false;
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
