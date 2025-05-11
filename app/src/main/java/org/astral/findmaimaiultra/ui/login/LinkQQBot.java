// HackGetUserId.java
package org.astral.findmaimaiultra.ui.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.been.faker.RegionData;
import org.astral.findmaimaiultra.been.faker.UserData;
import org.astral.findmaimaiultra.been.faker.UserRegion;
import org.astral.findmaimaiultra.ui.MainActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LinkQQBot extends AppCompatActivity {
    private static Context context;
    private static final int REQUEST_IMAGE_PICK = 1;
    private TextInputEditText userId;
    private OkHttpClient client;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack_get_user_id);
        context = this;
        sp = getSharedPreferences("setting", MODE_PRIVATE);
        userId = findViewById(R.id.userId);
        userId.setOnClickListener(v -> {
           Toast.makeText(this, "不可更改", Toast.LENGTH_SHORT).show();
        });
        userId.setText(sp.getString("userId", ""));
        if(sp.contains("userId")) {
            TextInputLayout userBox = findViewById(R.id.userBox);
            userBox.setVisibility(View.VISIBLE);
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        TextInputEditText key = findViewById(R.id.key);
        TextInputEditText safecode = findViewById(R.id.safecode);

        client = new OkHttpClient();

        MaterialButton bangding = findViewById(R.id.bangding);
        bangding.setOnClickListener(v -> {
            if (key.getText().toString().equals("")) {
                Toast.makeText(this, "请输入邮箱", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                sendApiRequest(key.getText().toString(), safecode.getText().toString(),1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        MaterialButton getTicket = findViewById(R.id.getTicket);
        getTicket.setOnClickListener(v -> {
            try {
                getTicket(userId.getText().toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        // 如果已经保存了userId，则直接获取数据
        if (!userId.getText().toString().equals("")) {
            try {
                getUserRegionData(userId.getText().toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getTicket(String uid) throws Exception {
        String url = "http://mai.godserver.cn:11451/api/qq/wmcfajuan?qq=" + uid + "&num=6";
        Log.d("TAG", "getTicket: " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->{
                        try {
                            Toast.makeText(LinkQQBot.this, response.body().string(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
        });
    }

    private void getUserInfo() {
        String url = "https://www.godserver.cn/cen/user/info";
        SharedPreferences sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        String X_Session_ID = sharedPreferences.getString("sessionId", "");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Session-ID", X_Session_ID)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    User user = new Gson().fromJson(json, User.class);
                    if (user.getMai_userName().equals("")) {
                        runOnUiThread(() ->{
                            Snackbar.make(LinkQQBot.this.findViewById(android.R.id.content), "账号未绑定QQ机器人!请绑定(网站上也可以绑定)", Snackbar.LENGTH_LONG)
                                    .setAction("绑定", v -> {
                                        bindUser();
                                    })
                                    .show();
                        });
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userId",user.getQqId());
                    editor.putString("userName",user.getMai_userName());
                    editor.putString("paikaname",user.getMai_userName());

                    editor.putString("https://mais.godserver.cn", user.getMai_userName());
                    editor.putInt("iconId",Integer.parseInt(user.getMai_avatarId()));
                    editor.apply();
                    try {
                        getUserRegionData(user.getQqId());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
    }
    private void bindUser() {

    }
    private void sendApiRequest(String key,String safecode,int code) throws Exception {
        String url = "https://www.godserver.cn/cen/user/login";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(key);
        loginRequest.setCodeOrPassword(safecode);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(loginRequest)))
                .build();
        Log.d("TAG", "sendApiRequest: " + url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(LinkQQBot.this, "Request failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    SharedPreferences sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    runOnUiThread(() -> {
                        Message message = new Gson().fromJson(responseData, Message.class);
                        if ("login".equals(message.getType())) {
                            if (message.getCode() == 200) {
                                // 登录成功
                                editor.remove("sessionId");
                                editor.putString("sessionId", message.getSessionId());
                                editor.apply();
                                Log.d("TAG","成功!");
                                Snackbar.make(LinkQQBot.this.findViewById(android.R.id.content), "登录成功!", Snackbar.LENGTH_LONG)
                                        .show();
                            } else {
                                // 登录失败
                            }
                        } else if ("reg".equals(message.getType())) {
                            // 注册流程：显示二维码
                            editor.remove("sessionId");
                            editor.putString("sessionId", message.getSessionId());
                            editor.apply();
                            Log.d("TAG","注册成功!");
                            // Base64 解码
                            String decodedKey = Arrays.toString(java.util.Base64.getDecoder().decode(message.getContent()));


                            // 构建 TOTP URI
                            String issuer = "ReisaPage - " + message.getContentType();
                            String totpUri = String.format("otpauth://totp/%s?secret=%s&issuer=%s",
                                    Uri.encode(issuer),
                                    Uri.encode(decodedKey),
                                    Uri.encode(issuer));
                            showQRCodeDialog(totpUri);
                        }
                        getUserInfo();


                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LinkQQBot.this, "Request not successful", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void showQRCodeDialog(String totpUri) {
        try {
            // 生成二维码图片
            Bitmap qrCodeBitmap = encodeAsQRCode(totpUri, 500, 500);

            // 创建 ImageView 并设置图片
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(qrCodeBitmap);

            // 构建 AlertDialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("请使用支持2FA的软件扫描二维码绑定账号,注意!这是你的唯一密码凭证!")
                    .setView(imageView)
                    .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (WriterException e) {
            Toast.makeText(this, "生成二维码失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap encodeAsQRCode(String contents, int width, int height) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, null);
        } catch (IllegalArgumentException iae) {
            return null;
        }

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private void getUserData(String userId) throws Exception {
        String url = "http://mai.godserver.cn:11451/api/qq/userData?qq=" + userId ;

        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    UserData userData = new Gson().fromJson(json, UserData.class);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("https://mais.godserver.cn", userData.getUserName());
                    editor.putInt("iconId",userData.getIconId());
                    editor.putString("rating", userData.getPlayerRating() + "");
                    editor.apply();
                    Log.d("TAG", "onResponse: " + userData.getUserName());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("TAG", "onFailure: " + e.getMessage());
            }
        });
    }

    private void getUserRegionData(String userId) throws Exception {
        String url = "http://mai.godserver.cn:11451/api/qq/region2?qq=" + userId ;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.d("url",url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(LinkQQBot.this, "Request failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        Log.d("TAG", "Response: " + responseData);
                        Gson gson = new Gson();
                        RegionData regionData = gson.fromJson(responseData, RegionData.class);
                        sortUserRegions(regionData.getUserRegionList());
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LinkQQBot.this, "Request not successful", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void sortUserRegions(List<UserRegion> userRegions) {
        Collections.sort(userRegions, new Comparator<UserRegion>() {
            @Override
            public int compare(UserRegion o1, UserRegion o2) {
                return Integer.compare(o2.getPlayCount(), o1.getPlayCount());
            }
        });
        // 处理排序后的数据，例如显示在表格中
        displaySortedUserRegions(userRegions);
    }

    private void displaySortedUserRegions(List<UserRegion> userRegions) {
        // 假设你有一个TableLayout来显示数据
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        tableLayout.removeAllViews();

        // 添加表头
        TableRow headerRow = new TableRow(this);
        TextView headerRegionId = new TextView(this);
        headerRegionId.setText("地区 ID");
        TextView headerPlayCount = new TextView(this);
        headerPlayCount.setText("PC次数");
        TextView headerProvince = new TextView(this);
        headerProvince.setText("省份");
        TextView headerCreated = new TextView(this);
        headerCreated.setText("版本初次日期");
        headerCreated.setTextColor(ContextCompat.getColor(LinkQQBot.context, R.color.primary));
        headerRegionId.setTextColor(ContextCompat.getColor(LinkQQBot.context, R.color.primary));
        headerPlayCount.setTextColor(ContextCompat.getColor(LinkQQBot.context, R.color.primary));
        headerProvince.setTextColor(ContextCompat.getColor(LinkQQBot.context, R.color.primary));
        headerRow.addView(headerRegionId);
        headerRow.addView(headerPlayCount);
        headerRow.addView(headerProvince);
        headerRow.addView(headerCreated);
        tableLayout.addView(headerRow);

        // 添加数据行
        for (UserRegion userRegion : userRegions) {
            TableRow row = new TableRow(this);
            TextView textViewRegionId = new TextView(this);
            textViewRegionId.setTextColor(ContextCompat.getColor(LinkQQBot.context, R.color.primary));
            textViewRegionId.setText(String.valueOf(userRegion.getRegionId()));
            TextView textViewPlayCount = new TextView(this);
            textViewPlayCount.setTextColor(ContextCompat.getColor(LinkQQBot.context, R.color.primary));
            textViewPlayCount.setText(String.valueOf(userRegion.getPlayCount()));
            TextView textViewProvince = new TextView(this);
            textViewProvince.setTextColor(ContextCompat.getColor(LinkQQBot.context, R.color.primary));
            textViewProvince.setText(userRegion.getProvince());
            TextView textViewCreated = new TextView(this);
            textViewCreated.setText(userRegion.getCreated());
            textViewCreated.setTextColor(ContextCompat.getColor(LinkQQBot.context, R.color.primary));
            row.addView(textViewRegionId);
            row.addView(textViewPlayCount);
            row.addView(textViewProvince);
            row.addView(textViewCreated);
            tableLayout.addView(row);
        }
    }
}
