package org.astral.findmaimaiultra.ui.slideshow;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.yalantis.ucrop.UCrop;

import org.astral.findmaimaiultra.been.Release;
import org.astral.findmaimaiultra.databinding.FragmentSlideshowBinding;
import org.astral.findmaimaiultra.service.GitHubApiService;
import org.astral.findmaimaiultra.ui.LinkQQBot;
import org.astral.findmaimaiultra.utill.GitHubApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SlideshowFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private SharedPreferences settingProperties;
    private TextInputEditText shuiyuEditText;
    private TextInputEditText luoxueEditText;
    private TextInputEditText userId;
    private String x;
    private String y;
    private FragmentSlideshowBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingProperties = requireActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
    }

    private void show(String text) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show());
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SwitchMaterial switchMaterial = binding.switchBeta1;
        switchMaterial.setChecked(settingProperties.getBoolean("setting_autobeta1", false));
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = settingProperties.edit();
            if (isChecked) {
                show("已开启实验性功能,可能并不起作用");
                editor.putBoolean("setting_autobeta1", true);
            } else {
                editor.putBoolean("setting_autobeta1", false);
            }
            editor.apply();
        });

        shuiyuEditText = binding.shuiyu;
        luoxueEditText = binding.luoxue;
        userId = binding.qqbot;

        MaterialButton openQQBot = binding.openQQbot;
        openQQBot.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getContext(), LinkQQBot.class);
                startActivity(intent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        loadSettings();

        MaterialButton saveButton = binding.saveSettingsButton;
        saveButton.setOnClickListener(v -> {
            saveSettings(switchMaterial.isChecked(), shuiyuEditText.getText().toString(), luoxueEditText.getText().toString(), userId.getText().toString());
        });
        MaterialButton changeButton = binding.changePhoto;
        changeButton.setOnClickListener(v -> openFileChooser());
        TextView uuid = binding.uuid;
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        uuid.setText("Android ID:" + androidId);
        uuid.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建一个ClipData对象
            ClipData clip = ClipData.newPlainText("label", androidId);
            // 将ClipData对象设置到剪贴板中
            clipboard.setPrimaryClip(clip);
            show("已复制Android ID到剪切板");
        });
        TextView vits = binding.vits;
        vits.setText("App version:" + getAppVersionName() + "\nLatest version:");
        getLatestRelease();
        WebView webView = binding.develop;
        webView.setBackgroundColor(0x00000000);
        webView.getSettings().setJavaScriptEnabled(true);
        String url = "http://wekan.godserver.cn/b/eu5nNL7GzF9SLYc6i/findmaimaidx";
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:(function() { " +
                        "document.body.style.backgroundColor = 'transparent'; " +
                        "})()");
            }
        });
        webView.loadUrl(url); // 加载网页
        return root;
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    }

    private String getAppVersionName() {
        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        } else {
            Log.w("SettingActivity", "Unexpected result from image picker or cropper");
        }
    }

    private void startCropActivity(Uri uri) {
        Uri destinationUri = Uri.fromFile(new File(requireActivity().getCacheDir(), "cropped_image.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(90);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        // 计算屏幕宽高比
        float[] aspectRatio = getScreenAspectRatio();

        UCrop.of(uri, destinationUri)
                .withAspectRatio(aspectRatio[0], aspectRatio[1]) // 设置裁剪比例为屏幕比例
                .withMaxResultSize(getScreenWidth(), getScreenHeight()) // 设置最大结果尺寸
                .withOptions(options)
                .start(getActivity());
    }

    private void handleCroppedImage(Intent data) {
        Uri croppedUri = UCrop.getOutput(data);
        if (croppedUri != null) {
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), croppedUri);
                if (photo != null) {
                    File croppedFile = new File(getContext().getExternalFilesDir(null), "cropped_image.jpg");
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

    private void saveSettings(boolean betaEnabled, String shuiyuUsername, String luoxueUsername, String userId) {
        SharedPreferences.Editor editor = settingProperties.edit();
        MaterialRadioButton materialRadioButton = binding.radioButton1;
        MaterialRadioButton org = binding.org;
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
        show("设置已保存");
    }

    private void loadSettings() {
        SwitchMaterial switchMaterial = binding.switchBeta1;
        shuiyuEditText = binding.shuiyu;
        luoxueEditText = binding.luoxue;
        MaterialRadioButton materialRadioButton = binding.radioButton1;
        MaterialRadioButton materialRadioButton2 = binding.radioButton2;
        MaterialRadioButton org = binding.org;
        switchMaterial.setChecked(settingProperties.getBoolean("setting_autobeta1", false));
        shuiyuEditText.setText(settingProperties.getString("shuiyu_username", ""));
        luoxueEditText.setText(settingProperties.getString("luoxue_username", ""));
        userId.setText(settingProperties.getString("userId", ""));

        int use_ = settingProperties.getInt("use_", 1);
        if (use_ == 0) {
            use_ = 0;
            org.setChecked(true);
            SharedPreferences.Editor editorSetting = settingProperties.edit();
            editorSetting.putInt("use_", use_);
            editorSetting.apply();
        } else if (use_ == 1) {
            materialRadioButton.setChecked(true);
        } else if (use_ == 2) {
            materialRadioButton2.setChecked(true);
        }
        SharedPreferences mContextSp = this.getContext().getSharedPreferences(
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
                        if (isAdded()) {
                            TextView textView = binding.vits;
                            textView.setText(textView.getText() + tagName + "\n" + name + "\n" + body);
                        }
                        //Toast.makeText(SettingActivity.this, "Latest Release:\nTag Name: " + tagName + "\nName: " + name + "\nBody: " + body + "\nHTML URL: " + htmlUrl, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {
            }
        });
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private int getScreenHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private float[] getScreenAspectRatio() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float width = displayMetrics.widthPixels;
        float height = displayMetrics.heightPixels;
        return new float[]{width, height};
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}