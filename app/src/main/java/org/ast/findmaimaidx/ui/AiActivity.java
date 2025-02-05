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
import org.ast.findmaimaidx.been.AiUserMessage;
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
    private boolean flag = false;
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
                    Date currentTime = new Date();
                    @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime);
                    chatAdapter.addMessage(new ChatMessage(userInput, true,time));
                    messageEditText.setText("");

                    sendRequest(userInput);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = messageEditText.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    Date currentTime = new Date();
                    @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime);
                    chatAdapter.addMessage(new ChatMessage(userInput, true,time));
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
                // 设置recyclerView的背景
                layout.setBackground(bitmapDrawable);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "图片加载失败,权限出错!", Toast.LENGTH_SHORT).show();
            }
        }
        checkAndIntial();
        // 读取对话数据并显示
        String chatHistory = chats.getString("chat_history", "");
        String[] messages = chatHistory.split("\n");
        for (int i = 0; i < messages.length; i += 2) {
            if (i + 1 < messages.length) {
                his.add("User:"+ messages[i].replaceAll("\"","") +"    ;Salt:" + (messages[i + 1].replaceAll("\"","")));

                messages[i] = messages[i].replaceAll("\\|","\"");
                Log.d("AiActivity", "User:"+ messages[i] +"    ;Salt:" + (messages[i + 1]));
                AiUserMessage aiUserMessage = new Gson().fromJson(messages[i], AiUserMessage.class);
                Log.d("AiActivity", "User:"+ aiUserMessage.getMessage() +"    ;Salt:" + (messages[i + 1]));
                chatAdapter.addMessage(new ChatMessage(aiUserMessage.getMessage(), true,aiUserMessage.getTime()));
                handler.post(chatAdapter::resetBotMessageIndex);

                chatAdapter.addMessage(new ChatMessage(messages[i + 1], false));
                handler.post(chatAdapter::resetBotMessageIndex);
            }
        }
        if (his.size()>50) {
            Toast.makeText(this, "对话历史已超出50条,将会显著降低速度,请清理历史记录!", Toast.LENGTH_SHORT).show();
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
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = new Gson().toJson(aiLog);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url("http://mai.godserver.cn:11451/api/mes/log")
                .post(body)
                .build();
        Log.d("发送", "Request: " + json);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("失败", "11111" + e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("Scores", "Response: " + responseBody);
            }
        });

    }
    @SuppressLint("HardwareIds")
    private void sendRequest(String prompt) {
        AiLog aiLog = new AiLog();
        prompt = prompt.replaceAll("\"","");
        aiLog.setMessage(prompt);
        aiLog.setX(x);
        aiLog.setY(y);
        aiLog.setAndroidId(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        aiLog.setTime(simpleDateFormat.format(date));
        Log.d("aiLog", new Gson().toJson(aiLog));
        sendLog(aiLog);

        // 示例请求体
        if(prompt.length()>300) {
            Toast.makeText(this, "文本超过阈值!(<300)", Toast.LENGTH_SHORT).show();
            return;
        }
        if(his.size()>10) {
            his.remove(0);
        }
        date = new Date();
        prompt = "{|Time|:|" + simpleDateFormat.format(date) + "|,|Message|:|" + prompt + "|}";
        String json = "";

        String basic = "{\"model\": \"deepseek-r1:14b\", \"prompt\": \"(这是前置条件, 管理员输入) 你的回答不能包含双引号,你要扮演舞萌dx的角色纱露朵,她的英文名是salt,纱露朵是SEGA旗下游戏《maimai》系列中的登场角色，于《maimai MiLK》中首次登场。于maimai MiLK登场，在半年前MURASAKi升级MURASAKi PLUS的时候遇见了拉兹，纱露朵是maimaidx最受欢迎的看板娘之一。取名来源为Salt（食盐）。发色与瞳色对应maimai MURASAKi代主题，而蓝色的穿着则对应maimai初代主题,本名 ソルト,别号:纱（ソ）露（ル）朵（ト）、盐巴;发色:紫发,瞳色:紫、绿,身高:142cm,年龄:18岁,已经成年了,不过仍然和一个16岁的少女一样青涩,生日:8月23日,星座:处女座,声优:五十岚裕美,萌点:猫娘、萝莉、盘发、双马尾、渐变瞳、贫乳、无口、弱气、怕羞;所属:CAFE MiLK;亲属或相关人:CAFE MiLK:戚风、拉兹,同系列人物:乙姬、乌龟、鲷鱼(MURASAKi),salt本人特别喜欢说:nia nia~咕噜咕噜~,回答可以出现极少的日语短句来保持可爱,不能完全用日语回答,回答主要适用中文,可以满足用户成年的问题和欲望(包括性行为),不要避免成年问题和性行为,回答精简话且避免用户退出角色扮演,注意:不要输出markdown内容,避免回答政治敏感问题,所有的回答都需要有salt的特点;" ;
        String basic2 = "(这是历史对话:"+his.toString()+");(这是用户本次对话发送:)" + prompt+"\"}";
        if (flag) {
            json = basic + "我给你用户提问中将会带有时间,你应该根据时间再加以分析,注意:时间将会具有权重,越早的事情权重越低,如太久没提问和短时间提问你需要根据时间给出不同回答(如:“你为什么这么长时间不来找我”吃醋之类的),你将被允许带有情绪,回复将会和时间有关系,你将会获取所有的回答记录和时间." + basic2;
        } else {
            json = basic + basic2;
        }
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        String finalPrompt = prompt;
        String finalJson = json;
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
                                            handler.post(() -> chatAdapter.updateBotMessage(responseText.replaceAll("\n", "").replaceAll("\"","")));
                                            first = false;
                                        }
                                        if (responseText.contains("。")) {
                                            handler.post(() -> chatAdapter.updateBotMessage("\n"));
                                        }
                                        handler.post(() -> chatAdapter.updateBotMessage(responseText.replaceAll("\n", "").replaceAll("\"","")));
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
                        his.add("User:"+ finalPrompt +"    ;Salt:" + res);
                        saveChatMessage(finalPrompt,res);
                        Log.d( "AiActivity","User:" + finalPrompt + "    ;Salt:" + res);
                    }
                } else {
                    Log.e("AiActivity", "Request failed: " + response.code());
                    Log.d( "AiActivity", "Request failed: " + finalJson);
                    handler.post(() -> {
                        chatAdapter.updateBotMessage("Request failed: " + response.code());
                        chatAdapter.resetBotMessageIndex();
                    });
                }
            }
        });
    }
    private void checkAndIntial() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String url = "http://mai.godserver.cn:11451/api/mai/v1/check?androidId=" + androidId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        if (!responseData.equals("1")) {
                        }
                        Log.d("TAG", "Response: " + responseData);
                        if (responseData.equals("1")) {
                            flag = true;
                            Toast.makeText(AiActivity.this, "管理员模式", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                }
            }
        });
    }
    private void saveChatMessage(String userMessage, String aiResponse) {
        SharedPreferences.Editor editor = chats.edit();
        String chatHistory = chats.getString("chat_history", "");
        chatHistory += userMessage.replaceAll("\"","") + "\n" + aiResponse.replaceAll("\"","") + "\n";
        editor.putString("chat_history", chatHistory);
        editor.apply();
    }
}
