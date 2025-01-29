// AiActivity.java
package org.ast.findmaimaidx.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import org.ast.findmaimaidx.been.ChatMessage;
import org.ast.findmaimaidx.been.Message;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AiActivity extends AppCompatActivity {

    private static final String API_URL = "http://www.godserver.cn:11435/api/generate";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageEditText;
    private Button sendButton;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ailayout);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        handler = new Handler(Looper.getMainLooper());
        client = new OkHttpClient();

        chatAdapter = new ChatAdapter(new ArrayList<>());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        chatAdapter.setOnMessageAddedListener(new ChatAdapter.OnMessageAddedListener() {
            @Override
            public void onMessageAdded() {
                chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
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
    }

    private void sendRequest(String prompt) {
        // 示例请求体
        String json = "{\"model\": \"deepseek-r1:14b\", \"prompt\": \"(这是前置条件) 你要扮演舞萌dx的角色纱露朵,她的英文名是salt,纱露朵是SEGA旗下游戏《maimai》系列中的登场角色，于《maimai MiLK》中首次登场。于maimai MiLK登场，为本作主人公之一。是一只住在满是毛茸茸的国家大吉岭的一只猫，本业是面包师傅(并不重要)，在半年前MURASAKi升级MURASAKi PLUS的时候遇见了兔子拉兹，并且到了她的店里工作。取名来源为Salt（食盐）。发色与瞳色对应maimai MURASAKi代主题，而蓝色的穿着则对应maimai初代主题,本名 ソルト,别号:纱（ソ）露（ル）朵（ト）、盐巴;发色:紫发,瞳色:紫、绿,身高:142cm,年龄:12岁,生日:8月23日,星座:处女座,声优:五十岚裕美,萌点:猫娘、萝莉、盘发、双马尾、渐变瞳、贫乳、无口、弱气、怕羞;所属:CAFE MiLK;亲属或相关人:CAFE MiLK:戚风、拉兹,同系列人物:乙姬、乌龟、鲷鱼(MURASAKi),salt本人特别喜欢说:nia nia~咕噜咕噜~,应该具有日本人的特点,注意:你应该减少思考时间和尽快给出结果(这是用户发送:)" + prompt+"\"}";

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
                                if(!isUseThink) {
                                    if(responseText.equals("<think>")) {
                                        flag = false;
                                    }
                                    if(responseText.equals("</think>")) {
                                        flag = true;
                                        continue;
                                    }
                                }
                                boolean done = jsonObject.getBoolean("done");
                                if (!responseText.isEmpty()) {
                                    if (flag) {
                                        if(!responseText.equals("\n")) {
                                            handler.post(() -> chatAdapter.updateBotMessage(responseText));
                                        }
                                    }
                                }
                                if (done) {
                                    handler.post(chatAdapter::resetBotMessageIndex);
                                    break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                handler.post(() -> {
                                    chatAdapter.resetBotMessageIndex();
                                });
                                break;
                            }
                        }
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
