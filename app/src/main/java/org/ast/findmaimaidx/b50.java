package org.ast.findmaimaidx;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.DisplayContext;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import okhttp3.*;
import org.ast.findmaimaidx.been.Chart;
import org.ast.findmaimaidx.been.PlayerData;
import org.ast.findmaimaidx.updater.ui.UpdateActivity;
import org.ast.findmaimaidx.utill.ZoomableViewGroup;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import static android.view.Surface.*;

public class b50 extends AppCompatActivity {
    public static Context context;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private RelativeLayout mainLayout;
    private SharedPreferences mContextSp;
    @Override
    @SuppressLint({"MissingInflatedId", "Range", "WrongViewCast", "ClickableViewAccessibility"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b50);
        context = this;
        mainLayout = findViewById(R.id.main);
        mContextSp = this.getSharedPreferences(
                "updater.data",
                Context.MODE_PRIVATE);
        String username = mContextSp.getString("username", null);
        if(username == null) {
            Toast.makeText(b50.this, "请先绑定水鱼账号", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(b50.this, UpdateActivity.class);
            startActivity(intent);
        }else {
            sendRawData(username);
        }
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> saveScreenshot());
        Button updateButton = findViewById(R.id.updateButton);

        updateButton.setOnClickListener(v -> {
            Intent intent = new Intent(b50.this, UpdateActivity.class);
            startActivity(intent);
        });
        TextView textView = findViewById(R.id.user_score);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(b50.this, b50.class);
            startActivity(intent);
            finish();
        });
    }
    private void sendRawData(String username) {
        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();

        // 原始数据
        String rawData = "{\"username\":\"" + username + "\",\"b50\":true}";

        // 创建 RequestBody
        RequestBody body = RequestBody.create(rawData, MediaType.get("application/json; charset=utf-8"));

        // 创建 Request
        Request request = new Request.Builder()
                .url("https://www.diving-fish.com/api/maimaidxprober/query/player")
                .post(body)
                .build();

        // 使用 AsyncTask 发送请求
        new SendRequestTask(client, request).execute();
        Log.d("TAG", "sendRawData: " + rawData);

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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                return;
            }

            // 使用Gson进行反序列化
            Gson gson = new Gson();
            PlayerData playerData = gson.fromJson(result, PlayerData.class);

            // 打印一些字段以验证
            Log.d("TAG", "Nickname: " + playerData.getNickname());
            Log.d("TAG", "Rating: " + playerData.getRating());

            // 打印第一个DX图表的信息
            if (playerData.getCharts().getDx() != null && !playerData.getCharts().getDx().isEmpty()) {
                Chart firstDxChart = playerData.getCharts().getDx().get(0);
                Log.d("TAG", "First DX Chart Title: " + firstDxChart.getTitle());
                Log.d("TAG", "First DX Chart Achievements: " + firstDxChart.getAchievements());
            }
            for (Chart chart : playerData.getCharts().getDx()) {
                Log.d("TAG", "Chart Title: " + chart.getTitle());
            }
            for (Chart chart : playerData.getCharts().getSd()) {
                Log.d("TAG", "Chart Title: " + chart.getTitle());
            }
            TextView textView = findViewById(R.id.user_name);
            textView.setText(playerData.getNickname());
            TextView textView2 = findViewById(R.id.user_score);
            textView2.setText(playerData.getRating()+"");

            // 更新UI
            int b15 =0 ;
            int b35 = 0;
            for (int x=0;x < 3;x ++) {
                for (int i = x*5; i < (x+1)*5; i++) {
                    GridLayout songGrid = null;
                    if (x == 0) {
                        songGrid = findViewById(R.id.song_grid1);
                    } else if (x == 1) {
                        songGrid = findViewById(R.id.song_grid2);
                    } else if (x == 2) {
                        songGrid = findViewById(R.id.song_grid3);
                    }
                    LayoutInflater inflater = LayoutInflater.from(context);
                    LinearLayout songCard = (LinearLayout) inflater.inflate(R.layout.song_card, null);
                    // 设置卡片的具体内容（例如歌名、得分等）
                    TextView songTitle = songCard.findViewById(R.id.song_title);
                    Chart chart = playerData.getCharts().getDx().get(i);
                    b15 = b15+chart.getRa();
                    LinearLayout master = songCard.findViewById(R.id.lay);

                    if(chart.getLevelLabel().equals("Re:MASTER")) {
                        master.setBackgroundResource(R.drawable.rounded_background1);
                    }else if(chart.getLevelLabel().equals("Master")) {
                        master.setBackgroundResource(R.drawable.rounded_background2);
                    }else if(chart.getLevelLabel().equals("Expert")) {
                        master.setBackgroundResource(R.drawable.rounded_background3);
                    }else if(chart.getLevelLabel().equals("Advanced")) {
                        master.setBackgroundResource(R.drawable.rounded_background4);
                    }else if(chart.getLevelLabel().equals("Basic")) {
                        master.setBackgroundResource(R.drawable.rounded_background5);
                    }
                    String imageUrl = "http://mai.godserver.cn/resource/static/mai/cover/" + chart.getSongId() + ".png";
                    if(chart.getType().equals("SD")) {
                        songTitle.setText("SD-" +chart.getTitle());
                    }else if(chart.getType().equals("DX")) {
                        songTitle.setText("DX-" + chart.getTitle());
                    }

                    ImageView songIcon = songCard.findViewById(R.id.song_cover);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.transform(new RoundedCorners(14));
                    // 加载背景图片和前景图片
                    Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.title2);
                    Bitmap foreground = null;

                    if(chart.getRate().equals("a")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_a);
                    }else if(chart.getRate().equals("s")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_s);
                    }else if(chart.getRate().equals("ss")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_ss);
                    }else if(chart.getRate().equals("sss")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_sss);
                    }else if(chart.getRate().equals("aa")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_aa);
                    }else if(chart.getRate().equals("aaa")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_aaa);
                    }else if(chart.getRate().equals("b")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_b);
                    }else if(chart.getRate().equals("bb")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_bb);
                    }else if(chart.getRate().equals("bbb")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_bbb);
                    }else if(chart.getRate().equals("sp")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_sp);
                    }else if(chart.getRate().equals("c")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_c);
                    }else if(chart.getRate().equals("ssp")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_ssp);
                    }else if(chart.getRate().equals("sssp")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_sssp);
                    }else if(chart.getRate().equals("d")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_d);
                    }

                    // 创建一个足够大的 Bitmap 来容纳背景图片
                    Bitmap result2 = Bitmap.createBitmap(background.getWidth(), background.getHeight(), background.getConfig());

                    // 计算前景图片的缩放比例，使其占据背景图片的一半大小
                    float scale = Math.min((float) background.getWidth() / foreground.getWidth(),
                            (float) background.getHeight() / foreground.getHeight()) * 1.0f;

                    // 使用 Canvas 绘制
                    Canvas canvas = new Canvas(result2);
                    canvas.drawBitmap(background, 0f, 0f, null);

                    // 创建 Matrix 对象
                    Matrix matrix = new Matrix();

                    // 计算前景图片的位置，使其位于背景图片的中间
                    int foregroundX = (background.getWidth() - (int) (foreground.getWidth() * scale)) / 2;
                    int foregroundY = (background.getHeight() - (int) (foreground.getHeight() * scale)) / 2;

                    // 应用缩放和平移
                    matrix.postScale(scale, scale);
                    matrix.postTranslate(foregroundX, foregroundY);

                    // 绘制前景图片
                    canvas.drawBitmap(foreground, matrix, null);


                    ImageView songIcon2 = songCard.findViewById(R.id.rank);
                    songIcon2.setImageBitmap(result2);
                    Glide.with(context)
                            .load(imageUrl)
                            //.apply(requestOptions)
                            .into(songIcon);
                    //ImageView songtype = songCard.findViewById(R.id.song_type);

                    TextView songScore = songCard.findViewById(R.id.song_score);
                    String formattedNumber = String.format("%.4f", chart.getAchievements()).replaceFirst("\\.0*$", "");

                    songScore.setText(formattedNumber +"%");
                    TextView ra = songCard.findViewById(R.id.song_rating);
                    ra.setText(chart.getDs() + " -> " + chart.getRa()+"");
                    songGrid.addView(songCard);
                }
            }
            //上面为新版本系列
            for (int x=0;x < 7;x ++) {
                for (int i = x*5; i < (x+1)*5; i++) {
                    GridLayout songGrid = null;
                    if(x==0) {
                        songGrid = findViewById(R.id.song_grid4);
                    }else if(x==1) {
                        songGrid = findViewById(R.id.song_grid5);
                    }else if(x==2) {
                        songGrid = findViewById(R.id.song_grid6);
                    }else if(x==3) {
                        songGrid = findViewById(R.id.song_grid7);
                    }else if(x==4) {
                        songGrid = findViewById(R.id.song_grid8);
                    }else if(x==5) {
                        songGrid = findViewById(R.id.song_grid9);
                    }else if(x==6) {
                        songGrid = findViewById(R.id.song_grid10);
                    }
                    LayoutInflater inflater = LayoutInflater.from(context);
                    LinearLayout songCard = (LinearLayout) inflater.inflate(R.layout.song_card, null);
                    // 设置卡片的具体内容（例如歌名、得分等）
                    TextView songTitle = songCard.findViewById(R.id.song_title);
                    Chart chart = playerData.getCharts().getSd().get(i);
                    b35 = b35+chart.getRa();
                    LinearLayout master = songCard.findViewById(R.id.lay);
                    if(chart.getLevelLabel().equals("Re:MASTER")) {
                        master.setBackgroundResource(R.drawable.rounded_background1);
                    }else if(chart.getLevelLabel().equals("Master")) {
                        master.setBackgroundResource(R.drawable.rounded_background2);
                    }else if(chart.getLevelLabel().equals("Expert")) {
                        master.setBackgroundResource(R.drawable.rounded_background3);
                    }else if(chart.getLevelLabel().equals("Advanced")) {
                        master.setBackgroundResource(R.drawable.rounded_background4);
                    }else if(chart.getLevelLabel().equals("Basic")) {
                        master.setBackgroundResource(R.drawable.rounded_background5);
                    }
                    if(chart.getType().equals("SD")) {
                        songTitle.setText("SD-" +chart.getTitle());
                    }else if(chart.getType().equals("DX")) {
                        songTitle.setText("DX-" + chart.getTitle());
                    }
                    String imageUrl = "http://mai.godserver.cn/resource/static/mai/cover/" + chart.getSongId() + ".png";
                    ImageView songIcon = songCard.findViewById(R.id.song_cover);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.transform(new RoundedCorners(14));
                    // 加载背景图片和前景图片
                    Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.title2);
                    Bitmap foreground = null;

                    if(chart.getRate().equals("a")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_a);
                    }else if(chart.getRate().equals("s")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_s);
                    }else if(chart.getRate().equals("ss")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_ss);
                    }else if(chart.getRate().equals("sss")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_sss);
                    }else if(chart.getRate().equals("aa")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_aa);
                    }else if(chart.getRate().equals("aaa")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_aaa);
                    }else if(chart.getRate().equals("b")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_b);
                    }else if(chart.getRate().equals("bb")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_bb);
                    }else if(chart.getRate().equals("bbb")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_bbb);
                    }else if(chart.getRate().equals("sp")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_sp);
                    }else if(chart.getRate().equals("c")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_c);
                    }else if(chart.getRate().equals("ssp")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_ssp);
                    }else if(chart.getRate().equals("sssp")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_sssp);
                    }else if(chart.getRate().equals("d")) {
                        foreground = BitmapFactory.decodeResource(getResources(), R.drawable.rank_d);
                    }

                    // 创建一个足够大的 Bitmap 来容纳背景图片
                    Bitmap result2 = Bitmap.createBitmap(background.getWidth(), background.getHeight(), background.getConfig());

                    // 计算前景图片的缩放比例，使其占据背景图片的一半大小
                    float scale = Math.min((float) background.getWidth() / foreground.getWidth(),
                            (float) background.getHeight() / foreground.getHeight()) * 1.0f;

                    // 使用 Canvas 绘制
                    Canvas canvas = new Canvas(result2);
                    canvas.drawBitmap(background, 0f, 0f, null);

                    // 创建 Matrix 对象
                    Matrix matrix = new Matrix();

                    // 计算前景图片的位置，使其位于背景图片的中间
                    int foregroundX = (background.getWidth() - (int) (foreground.getWidth() * scale)) / 2;
                    int foregroundY = (background.getHeight() - (int) (foreground.getHeight() * scale)) / 2;

                    // 应用缩放和平移
                    matrix.postScale(scale, scale);
                    matrix.postTranslate(foregroundX, foregroundY);

                    // 绘制前景图片
                    canvas.drawBitmap(foreground, matrix, null);


                    ImageView songIcon2 = songCard.findViewById(R.id.rank);
                    songIcon2.setImageBitmap(result2);
                    Glide.with(context)
                            .load(imageUrl)
                            //.apply(requestOptions)
                            .into(songIcon);
                    //ImageView songtype = songCard.findViewById(R.id.song_type);

                    TextView songScore = songCard.findViewById(R.id.song_score);
                    String formattedNumber = String.format("%.4f", chart.getAchievements()).replaceFirst("\\.0*$", "");

                    songScore.setText(formattedNumber +"%");
                    TextView ra = songCard.findViewById(R.id.song_rating);
                    ra.setText(chart.getDs() + " -> " + chart.getRa()+"");

                    songGrid.addView(songCard);
                }
            }
            TextView textView3 = findViewById(R.id.user_score_details);
            textView3.setText("b35:" + b35 + " + b15:" + b15 + " = " + (b35+b15));
        }
    }
    private void saveScreenshot() {
        takeScreenshot(mainLayout);
    }

    private void takeScreenshot(View view) {
        // 创建一个与视图大小相同的 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        saveImage(bitmap);
    }

    private void saveImage(Bitmap finalBitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "screenshot_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Screenshots");
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    Toast.makeText(this, "Screenshot saved successfully", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeScreenshot(mainLayout);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
