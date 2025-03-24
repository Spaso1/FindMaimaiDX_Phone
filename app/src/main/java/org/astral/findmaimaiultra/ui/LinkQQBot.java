// HackGetUserId.java
package org.astral.findmaimaiultra.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.been.faker.RegionData;
import org.astral.findmaimaiultra.been.faker.UserRegion;

import java.io.IOException;
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
                Toast.makeText(this, "请输入基于QQ机器人获取的Key", Toast.LENGTH_SHORT).show();
                return;
            }
            if (safecode.getText().toString().equals("")) {
                Toast.makeText(this, "请输入您的安全码", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                sendApiRequest(key.getText().toString(), safecode.getText().toString());
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

    private void sendApiRequest(String key,String safecode) throws Exception {
        String url = "http://mai.godserver.cn:11451/api/qq/safeCoding?result=" + key + "&safecode=" + safecode;

        Request request = new Request.Builder()
                .url(url)
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
                    runOnUiThread(() -> {
                        Toast.makeText(LinkQQBot.this, "Response: " + responseData, Toast.LENGTH_LONG).show();
                        Log.d("TAG", "Response: " + responseData);
                        userId.setText(responseData);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("userId", responseData);
                        editor.apply();
                        Toast.makeText(LinkQQBot.this, "设置已保存,您的UsrId已写入硬盘!", Toast.LENGTH_SHORT).show();
                        try {
                            getUserRegionData(responseData);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LinkQQBot.this, "Request not successful", Toast.LENGTH_SHORT).show());
                }
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
