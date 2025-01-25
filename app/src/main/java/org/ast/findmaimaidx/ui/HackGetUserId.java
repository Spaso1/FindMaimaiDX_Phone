package org.ast.findmaimaidx.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.R;
import java.io.IOException;

public class HackGetUserId extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;
    private ImageView imageView;
    private OkHttpClient client;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack_get_user_id);
        sp = getSharedPreferences("setting",MODE_PRIVATE);
        Button button = findViewById(R.id.button);
        TextInputEditText userId = findViewById(R.id.userId);

        userId.setText(sp.getString("userId",""));

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        client = new OkHttpClient();

        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                imageView.setImageBitmap(bitmap);
                String qrCode = decodeQRCode(bitmap);
                if (qrCode != null) {
                    Toast.makeText(this, "QR Code: " + qrCode, Toast.LENGTH_LONG).show();
                    sendApiRequest(qrCode);
                } else {
                    Toast.makeText(this, "No QR Code found", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
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

    private void sendApiRequest(String qrCode) {
        String url = "http://mai.godserver.cn:11451/api/getUserId?qrCode=" + qrCode;
        Request request = new Request.Builder()
                .url(url)
                .build();

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
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(HackGetUserId.this, "Request not successful", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
