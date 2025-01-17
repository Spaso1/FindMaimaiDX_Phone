package org.ast.findmaimaidx.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import okhttp3.*;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.lx.Lx_chart;
import org.ast.findmaimaidx.been.lx.Lx_data_scores;

import java.io.IOException;

public class Scores extends AppCompatActivity {
    private Context context;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scores);
        context = this;

        SharedPreferences setting = getSharedPreferences("setting", Context.MODE_PRIVATE);
        OkHttpClient client = new OkHttpClient();
        // 创建 RequestBody
        // 创建 Request
        Request request = new Request.Builder()
                .url("https://maimai.lxns.net/api/v0/user/maimai/player/scores")
                .header("X-User-Token",setting.getString("luoxue_username", ""))
                .build();

        // 使用 AsyncTask 发送请求
        new SendRequestTask(client, request).execute();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
                return;
            }
            Log.d("Scores", result);
            LinearLayout container = findViewById(R.id.container);
            Gson gson = new Gson();
            Lx_data_scores data = gson.fromJson(result, Lx_data_scores.class);
            int cardMargin = dpToPx(16); // 设置卡片之间的间距为 16dp
            // 在循环中创建每个 MaterialCardView
            for (int i = 0; i < data.getData().length; i++) {
                Lx_chart lx_chart = data.getData()[i];

                // 创建 MaterialCardView 并设置属性
                MaterialCardView materialCardView = new MaterialCardView(context);
                materialCardView.setCardBackgroundColor(R.color.black);
                materialCardView.setRadius(dpToPx(16)); // 设置圆角半径
                materialCardView.setCardElevation(3); // 设置阴影效果
                materialCardView.setMaxCardElevation(3); // 设置最大阴影效果
                materialCardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

                if(!lx_chart.getType().equals("standard")) {
                    lx_chart.setId(lx_chart.getId() + 10000);
                }
                String imageUrl = "http://mai.godserver.cn/resource/static/mai/cover/" + lx_chart.getId() + ".png";

                // 创建 ImageView 并设置背景图
                ImageView imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // 设置缩放类型为 centerCrop

                Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                return true;
                            }
                        })
                        .into(imageView);

                // 设置 ImageView 的布局参数
                LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(200)); // 设置高度为 200dp，可以根据需要调整
                imageView.setLayoutParams(imageLayoutParams);

                // 创建内部的 LinearLayout
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // 将 ImageView 添加到 LinearLayout
                linearLayout.addView(imageView);

                // 创建 RelativeLayout
                RelativeLayout relativeLayout = new RelativeLayout(context);
                relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // 创建上方的 TextView 并设置属性
                TextView topTextView = new TextView(context);
                topTextView.setText(lx_chart.getSong_name());
                topTextView.setTextColor(getResources().getColor(android.R.color.black));
                topTextView.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
                RelativeLayout.LayoutParams topTextViewParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                topTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                topTextView.setLayoutParams(topTextViewParams);

                relativeLayout.addView(topTextView);

                // 创建下方的 TextView 并设置属性
                TextView bottomTextView = new TextView(context);
                bottomTextView.setText(lx_chart.getLevel() + "->" + lx_chart.getAchievements());
                bottomTextView.setTextColor(getResources().getColor(android.R.color.black));
                bottomTextView.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
                bottomTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // 将 RelativeLayout 和下方的 TextView 添加到 LinearLayout
                linearLayout.addView(relativeLayout);
                linearLayout.addView(bottomTextView);

                // 将 LinearLayout 添加到 MaterialCardView
                materialCardView.addView(linearLayout);
                container.addView(materialCardView);
                Log.d("Lx_chart", lx_chart.getSong_name());
            }
        }
    }
}
