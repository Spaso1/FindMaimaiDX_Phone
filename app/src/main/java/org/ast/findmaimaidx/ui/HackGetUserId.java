// HackGetUserId.java
package org.ast.findmaimaidx.ui;

import static org.ast.findmaimaidx.utill.AESUtil.encrypt;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.application.MyApplication;
import org.ast.findmaimaidx.been.faker.RegionData;
import org.ast.findmaimaidx.been.faker.UserRegion;
import org.ast.findmaimaidx.message.ApiResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HackGetUserId extends AppCompatActivity {
    private String sessionId;
    private static final int REQUEST_IMAGE_PICK = 1;
    private TextInputEditText userId;
    private OkHttpClient client;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack_get_user_id);
        sessionId = getIntent().getStringExtra("sessionId");
        Log.d("TAG", "onCreate: " + sessionId);
        sp = getSharedPreferences("setting", MODE_PRIVATE);
        Button button = findViewById(R.id.button);
        userId = findViewById(R.id.userId);
        userId.setOnClickListener(v -> {
           Toast.makeText(this, "不可更改", Toast.LENGTH_SHORT).show();
        });
        userId.setText(sp.getString("userId", ""));

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        client = new OkHttpClient();

        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                String qrCode = decodeQRCode(bitmap);
                if (qrCode != null) {
                    Toast.makeText(this, "QR Code: " + qrCode, Toast.LENGTH_LONG).show();
                    sendApiRequest(qrCode);
                } else {
                    Toast.makeText(this, "No QR Code found", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String decodeQRCode(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Log.d("TAG", "decodeQRCode: " + reader.decode(binaryBitmap).getText());
            return reader.decode(binaryBitmap).getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void getTicket(String uid) throws Exception {
        String url = "http://mai.godserver.cn:11451/api/hacker/getTicket";
        RequestBody requestBody = RequestBody.create(new Gson().toJson(new ApiResponse(encrypt(uid))), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", sessionId)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->{
                        try {
                            Toast.makeText(HackGetUserId.this, response.body().string(), Toast.LENGTH_SHORT).show();
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

    private void sendApiRequest(String qrCode) throws Exception {
        String url = "http://mai.godserver.cn:11451/api/hacker/getUserId";
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(encrypt(qrCode));
        RequestBody requestBody = RequestBody.create(new Gson().toJson(apiResponse), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", sessionId)
                .post(requestBody)
                .build();
        Log.d("TAG", "sendApiRequest: " + url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(HackGetUserId.this, "Request failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        Toast.makeText(HackGetUserId.this, "Response: " + responseData, Toast.LENGTH_LONG).show();
                        Log.d("TAG", "Response: " + responseData);
                        userId.setText(responseData);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("userId", responseData);
                        editor.commit();
                        Toast.makeText(HackGetUserId.this, "设置已保存,您的UsrId已写入硬盘!", Toast.LENGTH_SHORT).show();
                        try {
                            getUserRegionData(responseData);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(HackGetUserId.this, "Request not successful", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void getUserRegionData(String userId) throws Exception {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(encrypt(userId));
        RequestBody requestBody = RequestBody.create(new Gson().toJson(apiResponse), MediaType.parse("application/json; charset=utf-8"));
        String url = "http://mai.godserver.cn:11451/api/hacker/getUserRegion" ;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", sessionId)
                .post(requestBody)
                .build();
        Log.d("url",url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(HackGetUserId.this, "Request failed", Toast.LENGTH_SHORT).show());
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
                    runOnUiThread(() -> Toast.makeText(HackGetUserId.this, "Request not successful", Toast.LENGTH_SHORT).show());
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
        headerRow.addView(headerRegionId);
        headerRow.addView(headerPlayCount);
        headerRow.addView(headerProvince);
        headerRow.addView(headerCreated);
        tableLayout.addView(headerRow);

        // 添加数据行
        for (UserRegion userRegion : userRegions) {
            TableRow row = new TableRow(this);
            TextView textViewRegionId = new TextView(this);
            textViewRegionId.setText(String.valueOf(userRegion.getRegionId()));
            TextView textViewPlayCount = new TextView(this);
            textViewPlayCount.setText(String.valueOf(userRegion.getPlayCount()));
            TextView textViewProvince = new TextView(this);
            textViewProvince.setText(userRegion.getProvince());
            TextView textViewCreated = new TextView(this);
            textViewCreated.setText(userRegion.getCreated());
            row.addView(textViewRegionId);
            row.addView(textViewPlayCount);
            row.addView(textViewProvince);
            row.addView(textViewCreated);
            tableLayout.addView(row);
        }
    }
}
