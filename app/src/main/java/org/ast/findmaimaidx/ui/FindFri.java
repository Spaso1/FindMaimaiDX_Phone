package org.ast.findmaimaidx.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.CompassView;
import org.ast.findmaimaidx.been.GpsData;
import org.ast.findmaimaidx.service.LocationUpdateService;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

public class FindFri extends AppCompatActivity implements SensorEventListener {
    private Context context = this;
    private String target_gpsapi;

    private CompassView compassView;
    private SensorManager sensorManager;
    private FusedLocationProviderClient fusedLocationClient;
    private float[] gravityValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] remappedRotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private float targetBearing;
    private double target_Latitude;
    private double target_Longitude;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findfri);

        SharedPreferences settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
        if (settingProperties.contains("gpsapi")) {
            String gpsapi = settingProperties.getString("gpsapi", "");
            TextInputEditText your = findViewById(R.id.your);
            your.setText(gpsapi);
            your.setKeyListener(null);
        } else {
            String originalString = new Random(11111).toString();
            settingProperties.edit().putString("gpsapi", Base64.getEncoder().encodeToString(originalString.getBytes())).apply();
            TextInputEditText your = findViewById(R.id.your);
            your.setText(Base64.getEncoder().encodeToString(originalString.getBytes()));
            your.setKeyListener(null);
        }
        if (settingProperties.contains("targetapi")) {
            target_gpsapi = settingProperties.getString("targetapi", "");
            TextInputEditText target = findViewById(R.id.target);
            target.setText(target_gpsapi);
            Toast.makeText(this, "已获取目标api", Toast.LENGTH_SHORT).show();
        }else {
            target_gpsapi = "123";
        }
        compassView = findViewById(R.id.compassView);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 获取当前位置
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        OkHttpClient client = new OkHttpClient();
                        TextInputEditText target = findViewById(R.id.target);
                        target_gpsapi = Objects.requireNonNull(target.getText()).toString();
                        Toast.makeText(context, "正在获取目标位置key:" + target_gpsapi, Toast.LENGTH_SHORT).show();
                        Request request = new Request.Builder()
                                .url("http://mai.godserver.cn:11451/api/ago/get?key=" + target_gpsapi)
                                .build();

                        // 使用 AsyncTask 发送请求
                        new SendRequestTask(client, request).execute();
                        if (location != null) {
                            // 假设目标经纬度为 (39.9042, 116.4074) 北京
                            double targetLatitude = target_Latitude;
                            double targetLongitude = target_Longitude;
                            targetBearing = location.bearingTo(new Location("") {{
                                setLatitude(targetLatitude);
                                setLongitude(targetLongitude);
                            }});
                            Log.d("目标", "目标方向：" + targetBearing);
                        }
                    }
                });
        MaterialButton find = findViewById(R.id.save_sub);
        find.setOnClickListener(v -> {
            TextInputEditText target = findViewById(R.id.target);
            target_gpsapi = Objects.requireNonNull(target.getText()).toString();
            if (target_gpsapi.equals("")) {
                Toast.makeText(context, "请输入目标api", Toast.LENGTH_SHORT).show();
            } else {
                settingProperties.edit().putString("targetapi", target_gpsapi).apply();
                Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(FindFri.this, FindFri.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private class SendRequestTask extends AsyncTask<Void, Void, String> {
        private OkHttpClient client;
        private Request request;

        public SendRequestTask(OkHttpClient client, Request request) {
            this.client = client;
            this.request = request;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
            } else {
                Gson gson = new Gson();
                GpsData gpsData = gson.fromJson(result, GpsData.class);
                try {
                    target_Latitude = gpsData.getX();
                    target_Longitude = gpsData.getY();
                    Log.d("Scores", result);
                }catch (Exception e) {
                    target_Latitude  = 39.9042;
                    target_Longitude = 116.4074;
                    Toast.makeText(context, "目标经纬度获取失败，默认设置为北京", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册传感器监听器
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // 取消传感器监听器
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, event.values.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticFieldValues, 0, event.values.length);
        }

        boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticFieldValues);
        if (success) {
            // 重新映射坐标系
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, remappedRotationMatrix);

            SensorManager.getOrientation(remappedRotationMatrix, orientation);
            float azimuth = (float) Math.toDegrees(orientation[0]); // 设备相对于正北方的角度

            // 调整目标方向
            float direction = (azimuth+0) - targetBearing; // 设备指向目标的方向

            // 处理方向角的范围
            while (direction < -180) {
                direction += 360;
            }
            while (direction > 180) {
                direction -= 360;
            }

            compassView.setDirection(direction);

//            // 打印日志
//            Log.d("FindFri", "Gravity: " + gravityValues[0] + ", " + gravityValues[1] + ", " + gravityValues[2]);
//            Log.d("FindFri", "Magnetic Field: " + magneticFieldValues[0] + ", " + magneticFieldValues[1] + ", " + magneticFieldValues[2]);
//            Log.d("FindFri", "Azimuth: " + azimuth);
//            Log.d("FindFri", "Target Bearing: " + targetBearing);
//            Log.d("FindFri", "Direction: " + direction);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }
}
