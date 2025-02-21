package org.ast.findmaimaidx.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ast.findmaimaidx.R;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaikaActivity extends AppCompatActivity {
    private MaterialButton enter;
    private TextInputEditText partyName;
    private Context context;
    private OkHttpClient client;
    private String party;
    private TextView partyHouse;
    private TableLayout tableLayout;
    private SharedPreferences sharedPreferences;
    private TextInputEditText name;
    private String paikaname;
    private MaterialButton add;
    private MaterialButton play;
    private MaterialButton leave;
    private MaterialButton card;
    private Handler handler;
    private String cardStyle;
    private String old = "";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.paika);
        enter = findViewById(R.id.enter);
        partyName = findViewById(R.id.party);
        context = getApplicationContext();
        client = new OkHttpClient();
        add = findViewById(R.id.add);
        leave = findViewById(R.id.leave);
        play = findViewById(R.id.play);
        tableLayout = findViewById(R.id.tableLayout);
        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        name = findViewById(R.id.name);
        partyHouse= findViewById(R.id.partyHouse);
        card = findViewById(R.id.card);
        handler = new Handler();
        if(sharedPreferences.getString("paikaname", null) != null) {
            name.setText(sharedPreferences.getString("paikaname", null));
            paikaname = sharedPreferences.getString("paikaname", null);
        }
        enter.setOnClickListener(v -> {
            enterParty();
        });
        cardStyle = sharedPreferences.getString("cardStyle", "maimai PiNK.png");
        if (!cardStyle.contains(".")){
            cardStyle = cardStyle + ".png";
        }
        card.setText(cardStyle.split("\\.")[0]);
        add.setOnClickListener(v -> {
            paikaname = name.getText().toString();
            if(paikaname.equals("")) {
                Toast.makeText(context, "请输入昵称", Toast.LENGTH_SHORT).show();
            } else {
                updateCard();
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "{\"party\":\"" + party + "\",\"name\":\"" + paikaname + "\"}");
                Request request = new Request.Builder()
                        .url("http://mai.godserver.cn:11451/api/mai/v1/party?party=" + party + "&people=" + paikaname)
                        .post(requestBody)
                        .build();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("paikaname", paikaname);
                editor.apply();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        joinParty();
                    }

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    }
                });
            }
        });
        leave.setOnClickListener(v->{
            Request request = new Request.Builder()
                    .url("http://mai.godserver.cn:11451/api/mai/v1/party?party=" + party + "&people=" + paikaname)
                    .delete()
                    .build();
            play.setVisibility(View.GONE);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    joinParty();
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }
            });
        });
        play.setOnClickListener(v->{
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "{\"party\":\"" + party + "\",\"people\":\"" + paikaname + "\"}");
            Request request = new Request.Builder()
                    .url("http://mai.godserver.cn:11451/api/mai/v1/partyPlay?party=" + party)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        joinParty();
                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                }
            });
        });
        card.setOnClickListener(v -> {
            Toast.makeText(context, "目前样式:" + cardStyle, Toast.LENGTH_SHORT).show();

            // 创建 AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请选择卡牌样式");

            // 加载自定义布局
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_card_style, null);
            LinearLayout layout = dialogView.findViewById(R.id.layout_buttons); // 假设 LinearLayout 的 id 是 layout_buttons

            // 添加选项
            String[] styles = {
                    "maimai でらっくす FESTiVAL PLUS",
                    "maimai でらっくす UNiVERSE PLUS",
                    "maimai でらっくす PLUS",
                    "maimai でらっくす FESTiVAL",
                    "maimai でらっくす UNiVERSE",
                    "maimai でらっくす BUDDiES",
                    "maimai でらっくす",
                    "maimai MURASAKi",
                    "maimai PiNK",
                    "maimai ORANGE",
                    "maimai GreeN",
                    "maimai MiLK"
            };

            // 创建 AlertDialog 对象
            AlertDialog dialog = builder.create();

            for (String style : styles) {
                MaterialButton button = new MaterialButton(this);
                button.setText(style);
                button.setPadding(16, 16, 16, 16);
                button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                button.setTextColor(getResources().getColor(R.color.white));
                button.setOnClickListener(view -> {
                    cardStyle = style + ".png";
                    updateCard();
                    Toast.makeText(context, "已选择样式: " + cardStyle, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
                layout.addView(button);
            }

            // 设置 AlertDialog 的内容视图
            dialog.setView(dialogView);

            // 显示 AlertDialog
            dialog.show();
        });

    }
    private void updateCard() {
        RequestBody emptyRequestBody = RequestBody.create("", MediaType.parse("text/plain"));
        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/mai/v1/player?party=" + party + "&people=" + paikaname + "&card=" + cardStyle)
                .post(emptyRequestBody)
                .build();
        card.setText(cardStyle.split("\\.")[0]);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cardStyle", cardStyle);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("TAG", "Response: " + response.body().string());
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
        });
    }
    private void enterParty() {
        LinearLayout joinParty = findViewById(R.id.joinParty);
        joinParty.setVisibility(LinearLayout.VISIBLE);
        LinearLayout enterParty = findViewById(R.id.enterParty);
        enterParty.setVisibility(LinearLayout.GONE);
        TextInputEditText partyName = findViewById(R.id.party);
        party = partyName.getText().toString();
        if (!name.getText().toString().isEmpty()) {
            paikaname = name.getText().toString();
        }
        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                joinParty();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(runnable);
    }
    private void joinParty() {
        getShangJiPeople();
        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/mai/v1/party?party=" + party)
                .build();
        Log.d("MainLaunch", "onResponse: " + request);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = null;
                    try {
                        responseData = response.body().string();
                        if(old.equals(responseData)) {
                            return;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String finalResponseData = responseData;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("paikaname", paikaname);
                    editor.commit();
                    runOnUiThread(() -> {
                        List<String> list = new Gson().fromJson(finalResponseData, new TypeToken<List<String>>() {
                        }.getType());
                        tableLayout.removeAllViews();

                        // 表头行
                        TableRow headerRow = new TableRow(context);
                        addTextViewToRow(headerRow, "排卡顺序", 2);
                        addTextViewToRow(headerRow, "昵称", 5);
                        addTextViewToRow(headerRow, "辅助操作此玩家", 4);
                        tableLayout.addView(headerRow);

                        // 数据行
                        for (int i = 0; i < list.size(); i++) {
                            String name = list.get(i);
                            if (i == 0) {
                                play.setVisibility(name.equals(paikaname) ? View.VISIBLE : View.GONE);
                            }
                            TableRow row = new TableRow(context);

                            // 排卡顺序
                            addTextViewToRow(row, (i + 1) + "", 1);

                            // 昵称（图片 + 文字）
                            LinearLayout nameLayout = new LinearLayout(context);
                            nameLayout.setOrientation(LinearLayout.VERTICAL);
                            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5);
                            nameLayout.setLayoutParams(layoutParams);

                            // 图片
                            ImageView imageView = new ImageView(context);
                            updateUserCard( imageView,name);
                            nameLayout.addView(imageView);

                            // 文字
                            TextView textView = new TextView(context);
                            textView.setText(name);
                            textView.setTextSize(14);
                            textView.setPadding(30, 20, 0, 0);
                            //设置颜色colorPrimary
                            //设计成斜式
                            textView.setTypeface(Typeface.create("serif-italic", Typeface.NORMAL));
                            textView.setTextColor(getResources().getColor(R.color.colorSecondary));
                            nameLayout.addView(textView);

                            row.addView(nameLayout);

                            // 辅助操作按钮
                            addButtonToRow(row, "插队", 1, name);
                            addButtonToRow(row, "上机", 1, name);
                            addButtonToRow(row, "离开", 1, name);

                            tableLayout.addView(row);
                        }
                    });
                    old = finalResponseData;
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MainLaunch", "onFailure: ", e);
            }
        });
    }
    private void updateUserCard(ImageView imageView,String people) {
        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/mai/v1/player?people=" + people + "&party=" + party)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(()->{
                        Glide.with(context)
                                .load("http://cdn.godserver.cn/resource/static/mai/pic/" + responseData) // 图片URL
                                .placeholder(R.drawable.placeholder) // 占位图
                                .into(imageView);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(context, "卡牌展示失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 辅助方法：添加 TextView 到 TableRow 并设置权重
    private void addTextViewToRow(TableRow row, String text, int weight) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.textcolorPrimary));
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        textView.setLayoutParams(params);
        row.addView(textView);
    }

    // 辅助方法：添加 Button 到 TableRow 并设置权重
    private void addButtonToRow(TableRow row, String text, int weight,String username) {
        Button button = new Button(context);
        button.setText(text);
        if(text.equals("插队")) {
            addButton(button,"change",paikaname,username);
        }else if(text.equals("上机")) {
            addButton(button,"play",paikaname,username);
        }else if(text.equals("离开")) {
            addButton(button,"leave",paikaname,username);
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        button.setLayoutParams(params);
        row.addView(button);
    }
    private void addButton(Button button,String data1,String data0,String data) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Request request = null;
                if(data1.equals("change")) {
                    RequestBody body = RequestBody.create(data1, MediaType.get("application/json; charset=utf-8"));
                    request = new Request.Builder()
                            .url("http://mai.godserver.cn:11451/api/mai/v1/party?party=" + party + "&people=" + data0 + "&changeToPeople=" + data)
                            .put(body)
                            .build();
                }else if(data1.equals("play")) {
                    RequestBody body = RequestBody.create(data1, MediaType.get("application/json; charset=utf-8"));
                    request = new Request.Builder()
                            .url("http://mai.godserver.cn:11451/api/mai/v1/partyPlay?party=" + party + "&people=" + data)
                            .delete(body)
                            .build();
                }else if(data1.equals("leave")) {
                    RequestBody body = RequestBody.create(data1, MediaType.get("application/json; charset=utf-8"));
                    request = new Request.Builder()
                            .url("http://mai.godserver.cn:11451/api/mai/v1/party?party=" + party + "&people=" + data)
                            .delete(body)
                            .build();
                }
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Log.d("TAG", "onResponse: " + response.body().string());
                        runOnUiThread(()->{
                            Toast.makeText(PaikaActivity.this, "操作成功", Toast.LENGTH_SHORT);
                        });
                        joinParty();
                    }

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    }
                });
            }
        });
    }
    public void getShangJiPeople() {
        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/mai/v1/partyPlay?party=" + party)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                runOnUiThread(()->{
                    partyHouse.setText("房间号"+ party+"  "+data + "正在上机");
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }
        });
    }
}
