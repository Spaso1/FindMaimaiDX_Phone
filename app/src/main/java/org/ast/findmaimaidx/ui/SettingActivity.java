package org.ast.findmaimaidx.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.yalantis.ucrop.UCrop;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.service.GitHubApiService;
import org.ast.findmaimaidx.been.Release;
import org.ast.findmaimaidx.utill.GitHubApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    private SharedPreferences settingProperties;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private TextInputEditText shuiyuEditText;
    private TextInputEditText luoxueEditText;
    private TextInputEditText userId;
    private String x;
    private String y;
    private String sessionId;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        x = getIntent().getStringExtra("x");
        y = getIntent().getStringExtra("y");
        sessionId = getIntent().getStringExtra("sessionId");
        settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
        SwitchMaterial switchMaterial = findViewById(R.id.switchBeta1);
        switchMaterial.setChecked(settingProperties.getBoolean("setting_autobeta1", false));
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = settingProperties.edit();
            if (isChecked) {
                Toast.makeText(this, "已开启实验性功能,可能并不起作用", Toast.LENGTH_SHORT).show();
                editor.putBoolean("setting_autobeta1", true);
            } else {
                editor.putBoolean("setting_autobeta1", false);
            }
            editor.apply();
        });

        shuiyuEditText = findViewById(R.id.shuiyu);
        luoxueEditText = findViewById(R.id.luoxue);
        userId = findViewById(R.id.userId);
        loadSettings();

        MaterialButton saveButton = findViewById(R.id.save_settings_button);
        saveButton.setOnClickListener(v -> {
            saveSettings(switchMaterial.isChecked(), shuiyuEditText.getText().toString(), luoxueEditText.getText().toString(), userId.getText().toString());
        });
        MaterialButton changeButton = findViewById(R.id.changePhoto);
        changeButton.setOnClickListener(v -> openFileChooser());
        TextView uuid = findViewById(R.id.uuid);
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        uuid.setText("Android ID:" + androidId);
        uuid.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建一个ClipData对象
            ClipData clip = ClipData.newPlainText("label", androidId);
            // 将ClipData对象设置到剪贴板中
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Android ID已复制到剪贴板", Toast.LENGTH_SHORT).show();
        });
        TextView vits = findViewById(R.id.vits);
        MaterialButton openAI = findViewById(R.id.openAi);
        openAI.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, AiActivity.class);
            intent.putExtra("x", x);
            intent.putExtra("y", y);
            startActivity(intent);
        });
        MaterialButton clearAi = findViewById(R.id.clearAi);
        clearAi.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("chats", Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
            Toast.makeText(this, "已清除数据", Toast.LENGTH_SHORT).show();
        });

        vits.setText("App version:" + getAppVersionName() + "\nLatest version:");
        getLatestRelease();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    }

    private String getAppVersionName() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SettingActivity", "onActivityResult called with requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
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
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(90);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        // 计算屏幕宽高比
        float[] aspectRatio = getScreenAspectRatio();

        UCrop.of(uri, destinationUri)
                .withAspectRatio(aspectRatio[0], aspectRatio[1]) // 设置裁剪比例为屏幕比例
                .withMaxResultSize(getScreenWidth(), getScreenHeight()) // 设置最大结果尺寸
                .withOptions(options)
                .start(this);
    }

    private void handleCroppedImage(Intent data) {
        Uri croppedUri = UCrop.getOutput(data);
        if (croppedUri != null) {
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), croppedUri);
                if (photo != null) {
                    File croppedFile = new File(getExternalFilesDir(null), "cropped_image.jpg");
                    try (FileOutputStream out = new FileOutputStream(croppedFile)) {
                        photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        SharedPreferences.Editor editor = settingProperties.edit();
                        editor.putString("image_uri", croppedUri.toString());
                        editor.apply();
                        Toast.makeText(this, "图片已保存", Toast.LENGTH_SHORT).show();
                        Log.d("SettingActivity", "图片已保存到: " + croppedFile.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "保存图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("SettingActivity", "保存图片失败: ", e);
                    }
                } else {
                    Toast.makeText(this, "无法获取裁剪后的图片", Toast.LENGTH_SHORT).show();
                    Log.w("SettingActivity", "无法获取裁剪后的图片");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "无法获取裁剪后的图片: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SettingActivity", "无法获取裁剪后的图片: ", e);
            }
        } else {
            Toast.makeText(this, "无法找到裁剪后的图片", Toast.LENGTH_SHORT).show();
            Log.w("SettingActivity", "无法找到裁剪后的图片");
        }
    }

    private void saveSettings(boolean betaEnabled, String shuiyuUsername, String luoxueUsername, String userId) {
        SharedPreferences.Editor editor = settingProperties.edit();
        MaterialRadioButton materialRadioButton = findViewById(R.id.radioButton1);
        MaterialRadioButton org = findViewById(R.id.org);
        int use = 0;
        if (org.isChecked()) {
            use = 0;
        } else if (materialRadioButton.isChecked()) {
            use = 1;
        } else {
            use = 2;
        }
        editor.putInt("use_", use);
        editor.putBoolean("setting_autobeta1", betaEnabled);
        editor.putString("shuiyu_username", shuiyuUsername);
        editor.putString("luoxue_username", luoxueUsername);
        editor.putString("userId", userId);
        editor.apply();
        Toast.makeText(this, "设置已保存,部分设置需要重启软件生效", Toast.LENGTH_SHORT).show();
    }

    private void loadSettings() {
        SwitchMaterial switchMaterial = findViewById(R.id.switchBeta1);
        shuiyuEditText = findViewById(R.id.shuiyu);
        luoxueEditText = findViewById(R.id.luoxue);
        MaterialRadioButton materialRadioButton = findViewById(R.id.radioButton1);
        MaterialRadioButton materialRadioButton2 = findViewById(R.id.radioButton2);
        MaterialRadioButton org = findViewById(R.id.org);
        switchMaterial.setChecked(settingProperties.getBoolean("setting_autobeta1", false));
        shuiyuEditText.setText(settingProperties.getString("shuiyu_username", ""));
        luoxueEditText.setText(settingProperties.getString("luoxue_username", ""));
        userId.setText(settingProperties.getString("userId", ""));

        int use_ = settingProperties.getInt("use_", 1);
        if (use_ == 0) {
            use_ = 1;
            materialRadioButton.setChecked(true);
            SharedPreferences.Editor editorSetting = settingProperties.edit();
            editorSetting.putInt("use_", use_);
            editorSetting.apply();
        } else if (use_ == 1) {
            materialRadioButton.setChecked(true);
        } else if (use_ == 2) {
            materialRadioButton2.setChecked(true);
        }
        SharedPreferences mContextSp = this.getSharedPreferences(
                "updater.data",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editorSetting = settingProperties.edit();
        String username = mContextSp.getString("username", "");
        if (Objects.requireNonNull(shuiyuEditText.getText()).toString().isEmpty()) {
            if (mContextSp.contains("username")) {
                editorSetting.putString("shuiyu_username", username);
                editorSetting.apply();
                shuiyuEditText.setText(username);
            }
        }
    }

    private void openAppSettings() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage("Please go to settings and enable the required permissions.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void getLatestRelease() {
        GitHubApiService service = GitHubApiClient.getClient();
        Call<Release> call = service.getLatestRelease("Spaso1", "FindMaimaiDX_Phone"); // 替换为你的仓库信息

        call.enqueue(new Callback<Release>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Release> call, @NonNull Response<Release> response) {
                if (response.isSuccessful()) {
                    Release release = response.body();
                    if (release != null) {
                        String tagName = release.getTagName();
                        String name = release.getName();
                        String body = release.getBody();
                        String htmlUrl = release.getHtmlUrl();
                        TextView textView = findViewById(R.id.vits);
                        textView.setText(textView.getText() + tagName + "\n" + name + "\n" + body);
                        //Toast.makeText(SettingActivity.this, "Latest Release:\nTag Name: " + tagName + "\nName: " + name + "\nBody: " + body + "\nHTML URL: " + htmlUrl, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingActivity.this, "No release found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SettingActivity.this, "Failed to get release: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {
                Toast.makeText(SettingActivity.this, "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}
