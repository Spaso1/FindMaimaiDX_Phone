package org.ast.findmaimaidx;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import okhttp3.*;
import org.ast.findmaimaidx.been.*;
import org.ast.findmaimaidx.updater.ui.UpdateActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class b50 extends AppCompatActivity {
    public static Context context;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private RelativeLayout mainLayout;
    private SharedPreferences setting ;
    @Override
    @SuppressLint({"MissingInflatedId", "Range", "WrongViewCast", "ClickableViewAccessibility"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b50);
        context = this;
        setting = getSharedPreferences("setting", MODE_PRIVATE);
        mainLayout = findViewById(R.id.main);
        String shuiyu_username = setting.getString("shuiyu_username", null);
        String luoxue_username = setting.getString("luoxue_username", null);
        if(shuiyu_username == null) {
            if(luoxue_username == null) {
                Toast.makeText(b50.this, "请先绑定水鱼账号", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(b50.this, UpdateActivity.class);
                startActivity(intent);
            }
        }else {
            int use_ = setting.getInt("use_", 0);
            if(use_ ==0) {
                sendRawData(shuiyu_username);
            }else {
                Log.d("TAG", "sendRawData: " + luoxue_username);
                new LuoxueOkhttpRequest(luoxue_username).execute();
                new LuoxueUserOkhttpRequest(luoxue_username).execute();
            }
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
            initView(playerData,null,0);
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

    class LuoxueOkhttpRequest extends AsyncTask<Void, Void, String> {
        private String code;

        private final String URL = "https://maimai.lxns.net/api/v0/user/maimai/player/bests";
        public LuoxueOkhttpRequest(String code) {
            this.code = code;
        }
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(URL)
                    .header("X-User-Token",code) // 添加认证头
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Log.d("OkHttpGetRequest", "Response: " + result);
            } else {
                Log.e("OkHttpGetRequest", "Failed to fetch data");
            }
            Gson gson = new Gson();
            Lx_res response = gson.fromJson(result, Lx_res.class);
            initView(null,response,1);
            // 现在你可以访问response对象中的数据
            System.out.println(response.isSuccess());
            System.out.println(response.getData().getStandardTotal());
            // 其他字段的访问类似
        }
    }

    class LuoxueUserOkhttpRequest extends AsyncTask<Void, Void, String> {
        private String code;

        private final String URL = "https://maimai.lxns.net/api/v0/user/maimai/player/";
        public LuoxueUserOkhttpRequest(String code) {
            this.code = code;
        }
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(URL)
                    .header("X-User-Token",code) // 添加认证头
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Log.d("LuoxueUserOkhttpRequest", "Response: " + result);
            } else {
                Log.e("LuoxueUserOkhttpRequest", "Failed to fetch data");
            }
            Gson gson = new Gson();
            Lx_playerInfo response = gson.fromJson(result, Lx_playerInfo.class);
            TextView user_name = findViewById(R.id.user_name);
            user_name.setText(response.getData().getName());
            ImageView user_icon = findViewById(R.id.user_avatar);
            String url = "https://assets2.lxns.net/maimai/icon/" + response.getData().getIcon().getId() + ".png";
            Glide.with(b50.this)
                    .load(url)
                    .into(user_icon);
        }
    }

    class SongHttp extends AsyncTask<Void, Void, String> {
        private final String URL = "https://maimai.lxns.net/api/v0/maimai/song/";
        private int code;
        private int type;
        private int count;
        public SongHttp(int code , int type,int count) {
            this.code = code;
            this.type = type;
            this.count = count;
        }
        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Log.d("asssssssssssss",URL + code);
            Request request = new Request.Builder()
                    .url(URL + code)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        @SuppressLint({"MissingInflatedId", "LocalSuppress", "SetTextI18n"})
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Log.d("LuoxueUserOkhttpRequest", "Response: " + result);
            } else {
                Log.e("LuoxueUserOkhttpRequest", "Failed to fetch data");
            }
            try {
                Gson gson = new Gson();
                Song song = gson.fromJson(result, Song.class);
                Log.d("LuoxueUserOkhttpRequest", "Response: " + song.getTitle());
                AlertDialog.Builder builder = new AlertDialog.Builder(b50.this);
                AlertDialog dialog = builder.create();
                // 获取布局Inflater
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // 使用布局文件创建视图
                View dialogView = inflater.inflate(R.layout.song, null);
                ImageView song_img = dialogView.findViewById(R.id.photo);
                TextView song_name = dialogView.findViewById(R.id.song_name);
                if(type==0) {
                    song_name.setText(song.getTitle() + "~Standard");
                }else {
                    song_name.setText(song.getTitle() + "~DX");
                }
                TextView uid = dialogView.findViewById(R.id.uid);
                uid.setText("ID:" + code+"");
                if(type==0) {
                    Difficulty[] difficulties = song.getDifficulties().get("standard");
                    for (Difficulty difficulty : difficulties) {
                        if(difficulty.getDifficulty() ==2) {
                            TextView red_level_value = dialogView.findViewById(R.id.red_level_value);
                            red_level_value.setText(difficulty.getLevel_value() + "");
                            TextView red_note_designer = dialogView.findViewById(R.id.red_note_designer);
                            red_note_designer.setText(difficulty.getNote_designer());
                        }else
                        if(difficulty.getDifficulty() ==3) {
                            TextView pink_level_value = dialogView.findViewById(R.id.pink_level_value);
                            pink_level_value.setText(difficulty.getLevel_value() + "");
                            TextView pink_note_designer = dialogView.findViewById(R.id.pink_note_designer);
                            pink_note_designer.setText(difficulty.getNote_designer());
                        }else
                        if(difficulty.getDifficulty() ==4) {
                            TextView white_level_value = dialogView.findViewById(R.id.white_level_value);
                            white_level_value.setText(difficulty.getLevel_value() + "");
                            TextView white_note_designer = dialogView.findViewById(R.id.white_note_designer);
                            white_note_designer.setText(difficulty.getNote_designer());
                        }
                    }
                }else {
                    Difficulty[] difficulties = song.getDifficulties().get("dx");
                    for (Difficulty difficulty : difficulties) {
                        if(difficulty.getDifficulty() ==2) {
                            TextView red_level_value = dialogView.findViewById(R.id.red_level_value);
                            red_level_value.setText(difficulty.getLevel_value() + "");
                            TextView red_note_designer = dialogView.findViewById(R.id.red_note_designer);
                            red_note_designer.setText(difficulty.getNote_designer());
                        }else
                        if(difficulty.getDifficulty() ==3) {
                            TextView pink_level_value = dialogView.findViewById(R.id.pink_level_value);
                            pink_level_value.setText(difficulty.getLevel_value() + "");
                            TextView pink_note_designer = dialogView.findViewById(R.id.pink_note_designer);
                            pink_note_designer.setText(difficulty.getNote_designer());
                        }else
                        if(difficulty.getDifficulty() ==4) {
                            TextView white_level_value = dialogView.findViewById(R.id.white_level_value);
                            white_level_value.setText(difficulty.getLevel_value() + "");
                            TextView white_note_designer = dialogView.findViewById(R.id.white_note_designer);
                            white_note_designer.setText(difficulty.getNote_designer());
                        }
                    }
                }
                Glide.with(b50.this)
                        .load("https://assets2.lxns.net/maimai/jacket/" + song.getId() + ".png")
                        .into(song_img);
                dialog.setView(dialogView);
                dialog.show();
            }catch (Exception e) {
                Toast.makeText(b50.this, "慢点点击嘛~", Toast.LENGTH_SHORT).show();
                if(count <3) {
                    new SongHttp(code - 10000,type,count).execute();
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private void initView(PlayerData playerData, Lx_res lx_res, int use) {
        if(use==1) {
            playerData = new PlayerData();
            List<Chart> dx = new ArrayList<>();
            for (int i = 0; i < lx_res.getData().getDx().size(); i++) {
                Chart chart = new Chart();
                chart.setTitle(lx_res.getData().getDx().get(i).getSong_name());
                chart.setLevel(lx_res.getData().getDx().get(i).getLevel());
                chart.setLevelIndex(lx_res.getData().getDx().get(i).getLevel_index());
                chart.setAchievements(lx_res.getData().getDx().get(i).getAchievements());
                chart.setRa((int)(lx_res.getData().getDx().get(i).getDx_rating()));
                chart.setDxScore(lx_res.getData().getDx().get(i).getDx_score());
                chart.setFs(lx_res.getData().getDx().get(i).getFs());
                chart.setFc(lx_res.getData().getDx().get(i).getFc());
                chart.setRate(lx_res.getData().getDx().get(i).getRate());
                int t =lx_res.getData().getDx().get(i).getId();
                if(!lx_res.getData().getDx().get(i).getType().equals("standard")) {
                    chart.setSongId(t + 10000);
                }else {
                    chart.setSongId(t);
                }
                if (lx_res.getData().getDx().get(i).getLevel_index() == 0) {
                    chart.setLevelLabel("Basic");
                }else if (lx_res.getData().getDx().get(i).getLevel_index() == 1) {
                    chart.setLevelLabel("Advanced");
                }else if (lx_res.getData().getDx().get(i).getLevel_index() == 2) {
                    chart.setLevelLabel("Expert");
                }else if (lx_res.getData().getDx().get(i).getLevel_index() == 3) {
                    chart.setLevelLabel("Master");
                }else if (lx_res.getData().getDx().get(i).getLevel_index() ==4) {
                    chart.setLevelLabel("Re:MASTER");
                }
                if(lx_res.getData().getStandard().get(i).getType().equals("standard")) {
                    chart.setType("SD");
                }else{
                    chart.setType("DX");
                }
                dx.add(chart);
            }
            List<Chart> sd = new ArrayList<>();
            for (int i = 0; i < lx_res.getData().getStandard().size(); i++) {
                Chart chart = new Chart();
                chart.setTitle(lx_res.getData().getStandard().get(i).getSong_name());
                chart.setLevel(lx_res.getData().getStandard().get(i).getLevel());
                chart.setRa((int)(lx_res.getData().getStandard().get(i).getDx_rating()));
                chart.setLevelIndex(lx_res.getData().getStandard().get(i).getLevel_index());
                chart.setAchievements(lx_res.getData().getStandard().get(i).getAchievements());
                chart.setDxScore(lx_res.getData().getStandard().get(i).getDx_score());
                chart.setFs(lx_res.getData().getStandard().get(i).getFs());
                chart.setFc(lx_res.getData().getStandard().get(i).getFc());
                chart.setRate(lx_res.getData().getStandard().get(i).getRate());
                int t =lx_res.getData().getStandard().get(i).getId();
                if(!lx_res.getData().getStandard().get(i).getType().equals("standard")) {
                    chart.setSongId(t + 10000);
                }else {
                    chart.setSongId(t);
                }
                if (lx_res.getData().getStandard().get(i).getLevel_index() == 0) {
                    chart.setLevelLabel("Basic");
                }else if (lx_res.getData().getStandard().get(i).getLevel_index() == 1) {
                    chart.setLevelLabel("Advanced");
                }else if (lx_res.getData().getStandard().get(i).getLevel_index() == 2) {
                    chart.setLevelLabel("Expert");
                }else if (lx_res.getData().getStandard().get(i).getLevel_index() == 3) {
                    chart.setLevelLabel("Master");
                }else if (lx_res.getData().getStandard().get(i).getLevel_index() ==4) {
                    chart.setLevelLabel("Re:MASTER");
                }
                if(lx_res.getData().getStandard().get(i).getType().equals("standard")) {
                    chart.setType("SD");
                }else{
                    chart.setType("DX");
                }
                sd.add(chart);
            }
            Charts charts = new Charts();
            charts.setDx(dx);
            charts.setSd(sd);
            playerData.setCharts(charts);
            playerData.setRating(lx_res.getData().getDxTotal()+lx_res.getData().getStandardTotal());
        }
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
                if(use==1) {
                    ra.setText(chart.getLevel() + " -> " + chart.getRa()+"");
                }
                songIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(chart.getType().equals("DX")) {
                            new SongHttp(chart.getSongId() - 10000,1,0).execute();
                        }else {
                            new SongHttp(chart.getSongId() - 10000,1,0).execute();
                        }
                    }
                });
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
                if(use==1) {
                    ra.setText(chart.getLevel() + " -> " + chart.getRa()+"");
                }
                songIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(chart.getType().equals("DX")) {
                            new SongHttp(chart.getSongId() - 10000,1,0).execute();
                        }else {
                            new SongHttp(chart.getSongId(),0,0).execute();
                        }
                    }
                });
                songGrid.addView(songCard);
            }
        }
        TextView textView3 = findViewById(R.id.user_score_details);
        textView3.setText("b35:" + b35 + " + b15:" + b15 + " = " + (b35+b15));
    }
}
