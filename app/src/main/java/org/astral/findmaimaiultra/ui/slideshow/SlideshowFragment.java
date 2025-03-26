package org.astral.findmaimaiultra.ui.slideshow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import org.astral.findmaimaiultra.been.Release;
import org.astral.findmaimaiultra.databinding.FragmentSlideshowBinding;
import org.astral.findmaimaiultra.service.GitHubApiService;
import org.astral.findmaimaiultra.ui.ImagePickerListener;
import org.astral.findmaimaiultra.ui.LinkQQBot;
import org.astral.findmaimaiultra.utill.GitHubApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class SlideshowFragment extends Fragment {
    private SharedPreferences settingProperties;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private TextInputEditText shuiyuEditText;
    private TextInputEditText luoxueEditText;
    private TextInputEditText userId;
    private String x;
    private String y;
    private FragmentSlideshowBinding binding;
    private ImagePickerListener imagePickerListener;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ImagePickerListener) {
            imagePickerListener = (ImagePickerListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ImagePickerListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingProperties = requireActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);

        if (allPermissionsGranted()) {
            // 初始化代码
        } else {
            requestPermissions();
        }
    }

    private void show(String text) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show());
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
        );
    }

    @Override
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
        changeButton.setOnClickListener(v -> imagePickerListener.openFileChooser());
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

    private String getAppVersionName() {
        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
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
                        try {
                            String tagName = release.getTagName();
                            String name = release.getName();
                            String body = release.getBody();
                            String htmlUrl = release.getHtmlUrl();
                            TextView textView = binding.vits;
                            textView.setText(textView.getText() + tagName + "\n" + name + "\n" + body);
                            //Toast.makeText(SettingActivity.this, "Latest Release:\nTag Name: " + tagName + "\nName: " + name + "\nBody: " + body + "\nHTML URL: " + htmlUrl, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.d("SettingActivity", "获取最新版本失败: ", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
