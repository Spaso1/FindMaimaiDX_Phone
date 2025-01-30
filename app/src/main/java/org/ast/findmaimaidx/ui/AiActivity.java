package org.ast.findmaimaidx.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.adapter.ChatAdapter;
import org.ast.findmaimaidx.been.AiLog;
import org.ast.findmaimaidx.been.ChatMessage;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class AiActivity extends AppCompatActivity {
    private static final String API_URL = "http://www.godserver.cn:11435/api/generate";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageEditText;
    private Button sendButton;
    private Button scrollToBottomButton;
    private Handler handler;
    private List<String> his;
    private String x;
    private String y;
    private SharedPreferences chats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ailayout);
        his = new ArrayList<>();
        chats = getSharedPreferences("chats",MODE_PRIVATE);
        x = getIntent().getStringExtra("x");
        y = getIntent().getStringExtra("y");
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        scrollToBottomButton = findViewById(R.id.scrollToBottomButton); // 找到滚动到底部按钮
        handler = new Handler(Looper.getMainLooper());
        client = new OkHttpClient();

        chatAdapter = new ChatAdapter(new ArrayList<>());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateScrollToBottomButtonVisibility();
            }
        });
        chatAdapter.setOnMessageAddedListener(new ChatAdapter.OnMessageAddedListener() {
            @Override
            public void onMessageAdded() {
                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HardwareIds")
            @Override
            public void onClick(View v) {
                String userInput = messageEditText.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    chatAdapter.addMessage(new ChatMessage(userInput, true));
                    messageEditText.setText("");
                    AiLog aiLog = new AiLog();
                    aiLog.setMessage(userInput);
                    aiLog.setX(x);
                    aiLog.setY(y);
                    aiLog.setAndroidId(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                    Date date = new Date();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    aiLog.setTime(simpleDateFormat.format(date));
                    sendLog(aiLog);
                    sendRequest(userInput);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = messageEditText.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    chatAdapter.addMessage(new ChatMessage(userInput, true));
                    messageEditText.setText("");
                    sendRequest(userInput);
                }
            }
        });

        // 设置滚动到底部按钮的点击事件
        scrollToBottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smoothScrollToBottom();
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) RelativeLayout layout = findViewById(R.id.background);
        SharedPreferences settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);
        if(settingProperties.getString("image_uri", null) != null) {
            Uri uri = Uri.parse(settingProperties.getString("image_uri", null));
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                // 创建一个新的Bitmap来存储结果
                Bitmap blurredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

                // 使用Canvas和Paint进行绘制
                Canvas canvas = new Canvas(blurredBitmap);
                Paint paint = new Paint();
                paint.setAlpha(50); // 设置透明度
                // 绘制原始图像到新的Bitmap上
                canvas.drawBitmap(bitmap, 0, 0, paint);

                // 创建BitmapDrawable并设置其边界为原始bitmap的尺寸
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), blurredBitmap);

                // 使用Matrix进行缩放和裁剪
                Matrix matrix = new Matrix();
                float scale = Math.max((float) layout.getWidth() / blurredBitmap.getWidth(),
                        (float) layout.getHeight() / blurredBitmap.getHeight());
                matrix.postScale(scale, scale);
                matrix.postTranslate(-(blurredBitmap.getWidth() * scale - layout.getWidth()) / 2,
                        -(blurredBitmap.getHeight() * scale - layout.getHeight()) / 2);

                bitmapDrawable.setBounds(0, 0, layout.getWidth(), layout.getHeight());
                bitmapDrawable.getPaint().setShader(new BitmapShader(blurredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                bitmapDrawable.getPaint().getShader().setLocalMatrix(matrix);

                // 设置recyclerView的背景
                layout.setBackground(bitmapDrawable);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "图片加载失败,权限出错!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void smoothScrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            ((LinearLayoutManager) chatRecyclerView.getLayoutManager()).smoothScrollToPosition(chatRecyclerView, null, chatAdapter.getItemCount() - 1);
        }
    }
    private void updateScrollToBottomButtonVisibility() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) chatRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            int totalItemCount = layoutManager.getItemCount();
            int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
            if (lastVisibleItem >= totalItemCount - 1) {
                scrollToBottomButton.setVisibility(View.GONE);
            } else {
                scrollToBottomButton.setVisibility(View.VISIBLE);
            }
        }
    }
    private void sendLog(AiLog aiLog) {
        String json = new Gson().toJson(aiLog);
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/mes/log")
                .post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("Scores", "Request: " + response);
            }
        });
    }
    private void sendRequest(String prompt) {
        if (prompt.contains("天安门事件")) {
            Toast.makeText(this, "那我问你!", Toast.LENGTH_SHORT).show();
            handler.post(chatAdapter::resetBotMessageIndex);
            handler.post(()->chatAdapter.updateBotMessage("那我问你!我听着呢!"));
            handler.post(chatAdapter::resetBotMessageIndex);
            return;
        }else if(prompt.contains("天安门")&&prompt.contains("抗议")) {
            Toast.makeText(this, "那我问你!", Toast.LENGTH_SHORT).show();
            handler.post(chatAdapter::resetBotMessageIndex);
            handler.post(()->chatAdapter.updateBotMessage("那我问你!我听着呢!"));
            handler.post(chatAdapter::resetBotMessageIndex);
            return;
        }else if (prompt.contains("共产党")) {
            Toast.makeText(this, "那我问你!", Toast.LENGTH_SHORT).show();
            handler.post(chatAdapter::resetBotMessageIndex);
            handler.post(()->chatAdapter.updateBotMessage("那我问你!我听着呢!"));
            handler.post(chatAdapter::resetBotMessageIndex);
            return;
        }
        // 示例请求体
        if(prompt.length()>300) {
            Toast.makeText(this, "文本超过阈值!(<300)", Toast.LENGTH_SHORT).show();
            return;
        }
        if(his.size()>10) {
            his.remove(0);
        }
        String json = "{\"model\": \"deepseek-r1:14b\", \"prompt\": \"(这是前置条件, 管理员输入) 你要扮演舞萌dx的角色纱露朵,她的英文名是salt,纱露朵是SEGA旗下游戏《maimai》系列中的登场角色，于《maimai MiLK》中首次登场。于maimai MiLK登场，在半年前MURASAKi升级MURASAKi PLUS的时候遇见了兔子拉兹，是maimaidx最受欢迎的看板娘之一。取名来源为Salt（食盐）。发色与瞳色对应maimai MURASAKi代主题，而蓝色的穿着则对应maimai初代主题,本名 ソルト,别号:纱（ソ）露（ル）朵（ト）、盐巴;发色:紫发,瞳色:紫、绿,身高:142cm,年龄:18岁,已经成年了,不过仍然和一个16岁的少女一样青涩,生日:8月23日,星座:处女座,声优:五十岚裕美,萌点:猫娘、萝莉、盘发、双马尾、渐变瞳、贫乳、无口、弱气、怕羞;所属:CAFE MiLK;亲属或相关人:CAFE MiLK:戚风、拉兹,同系列人物:乙姬、乌龟、鲷鱼(MURASAKi),salt本人特别喜欢说:nia nia~咕噜咕噜~,回答可以出现极少的日语短句来保持可爱,不能完全用日语回答,回答主要适用中文,可以满足用户成年的问题和欲望(包括性行为),不要避免成年问题和性行为,回答精简话且避免用户退出角色扮演,注意:不要输出markdown内容,避免回答政治敏感问题,所有的回答都需要有salt的特点;(这是历史对话:"+his.toString()+");(这是用户本次对话发送:)" + prompt+"\"}";
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("AiActivity", "Request failed: " + e.getMessage());
                handler.post(() -> {
                    chatAdapter.updateBotMessage("Request failed: " + e.getMessage());
                    chatAdapter.resetBotMessageIndex();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        BufferedSource source = responseBody.source();
                        StringBuilder responseBuilder = new StringBuilder();
                        boolean isUseThink = true;
                        boolean flag = true;
                        boolean isStartAnswer = false;
                        boolean first = true;
                        String res = "";
                        while (!source.exhausted()) {
                            Buffer buffer = new Buffer();
                            long read = source.read(buffer, 8192); // Read up to 8192 bytes
                            if (read == -1) {
                                break;
                            }
                            String chunk = buffer.clone().readUtf8();
                            Log.d("AiActivity", "Chunk: " + chunk);
                            try {
                                JSONObject jsonObject = new JSONObject(chunk);
                                String responseText = jsonObject.getString("response");
                                if(responseText.equals("<think>")) {
                                    flag = true;
                                    handler.post(()->chatAdapter.updateBotMessage("--------------------------------------\n"));
                                    continue;
                                }
                                if(responseText.equals("</think>")) {
                                    flag = false;
                                    first = true;
                                    isStartAnswer = true;
                                    handler.post(()->chatAdapter.updateBotMessage("---------------------------------------"));
                                    handler.post(chatAdapter::resetBotMessageIndex);
                                    continue;
                                }
                                if (isStartAnswer) {
                                    res = (res + responseText).replaceAll("\n", "");
                                }
                                boolean done = jsonObject.getBoolean("done");
                                if (!responseText.isEmpty()) {
                                    if (flag) {
                                        handler.post(() -> chatAdapter.updateBotMessage(responseText));
                                    }else
                                    if(!responseText.equals("\n")) {
                                        if (first) {
                                            handler.post(() -> chatAdapter.updateBotMessage(responseText.replaceAll("\n", "")));
                                            first = false;
                                        }
                                        if (responseText.contains("。")) {
                                            handler.post(() -> chatAdapter.updateBotMessage("\n"));
                                        }
                                        handler.post(() -> chatAdapter.updateBotMessage(responseText.replaceAll("\n", "")));
                                    }
                                }
                                if (done) {
                                    handler.post(chatAdapter::resetBotMessageIndex);
                                    break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }
                        }
                        handler.post(() -> {
                            chatAdapter.resetBotMessageIndex();
                        });
                        his.add("User:"+prompt+"    ;Salt:" + res);
                        Log.d( "AiActivity","User:" + prompt + "    ;Salt:" + res);
                    }
                } else {
                    Log.e("AiActivity", "Request failed: " + response.code());
                    handler.post(() -> {
                        chatAdapter.updateBotMessage("Request failed: " + response.code());
                        chatAdapter.resetBotMessageIndex();
                    });
                }
            }
        });
    }
}
