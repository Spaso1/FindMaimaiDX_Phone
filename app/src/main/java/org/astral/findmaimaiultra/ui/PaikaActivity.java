package org.astral.findmaimaiultra.ui;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class PaikaActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String use_party = "未加入";
    private String use_name;
    private int iconId;
    private int iconResId = iconId;
    private int status;
    private Toolbar toolbar;
    private List<String> players = new ArrayList<>();
    private Handler handler = new Handler();
    private ImageView player1Avatar;
    private ImageView player2Avatar;
    private TextView player1Name;
    private TextView player2Name;
    private boolean isPlaying = false;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private int isNfc = 0;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_paika);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        processNfcIntent(getIntent());

        sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        handler.postDelayed(refreshTask, 3000);

        use_name = sharedPreferences.getString("paikaname", "");
        iconId = sharedPreferences.getInt("iconId", 0);
        iconResId = iconId;
        player1Avatar = findViewById(R.id.player1Avatar);
        player2Avatar = findViewById(R.id.player2Avatar);
        player1Name = findViewById(R.id.player1Name);
        player2Name = findViewById(R.id.player2Name);
        if (use_name.equals("")) {
            //询问并做个名字
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("请输入昵称");
            TextInputEditText textInputEditText = new TextInputEditText(this);
            builder.setView(textInputEditText);
            builder.setPositiveButton("确定", (dialog, which) -> {
                use_name = textInputEditText.getText().toString();
                dialog.dismiss();
            });
            builder.setNegativeButton("取消", null);
            builder.show();
        }

        status = sharedPreferences.getInt("paikastatus", 0);
        toolbar = findViewById(R.id.toolbar);

        if (isNfc == 1) {
            Log.d("partySetting", use_party);

            if (sharedPreferences.getString("use_party", "").equals(use_party)) {
                getData();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (players != null && !players.isEmpty()) {
                            // 如果 players 不为空，执行 doUPlay()
                            doUPlay();
                        } else {
                            // 如果 players 为空，继续延迟 1 秒后检查
                            handler.postDelayed(this, 1000);
                        }
                    }
                }, 1000); // 延迟 1 秒

            }else {
                toolbar.setTitle(use_party + " 房间 " + use_name);
            }
        }else
        if(!sharedPreferences.getString("use_party", "").equals("")) {
            if (!Objects.equals(use_party, "未加入")) {
                if (status == 0) {
                    toolbar.setTitle(use_party + " 房间 " + use_name);
                }else {
                    toolbar.setTitle(use_party + " 房间 " + use_name + " 正在队列");
                }
            }else {
                use_party = sharedPreferences.getString("use_party", "");
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle("重新进入房间");
                builder.setMessage("重新进入房间号?");
                TextInputEditText textInputEditText = new TextInputEditText(this);
                builder.setView(textInputEditText);
                textInputEditText.setText(use_party);
                builder.setPositiveButton("确定", (dialog, which) -> {
                    use_party = textInputEditText.getText().toString();

                    if (status == 0) {
                        toolbar.setTitle(use_party + " 房间 " + use_name);
                    } else {
                        toolbar.setTitle(use_party + " 房间 " + use_name + " 正在队列");
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("use_party", use_party);
                    editor.apply();
                    getData();
                });
                builder.setNegativeButton("取消", null);
                builder.show();
            }
        }else if ((status == 0) && (isNfc ==0)) {
            //询问加入房价
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("进入房间");
            builder.setMessage("进入房间号?");
            TextInputEditText textInputEditText = new TextInputEditText(this);
            builder.setView(textInputEditText);
            builder.setPositiveButton("确定", (dialog, which) -> {
                use_party = textInputEditText.getText().toString();
                toolbar.setTitle(use_party + " 房间 " + use_name);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("use_party", use_party);
                editor.apply();
                getData();
            });
            builder.setNegativeButton("取消", null);
            builder.show();
        }
        Log.d("123456", status + "");
        toolbar.setTitle(use_party + " 房间 " + use_name);
        if (status == 1) {
            toolbar.setTitle(use_party + " 房间 " + use_name + " 正在队列");
        }
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        // 添加测试数据到 TableLayout
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doFab(view);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processNfcIntent(intent);
    }

    private void processNfcIntent(Intent intent) {
        Log.d("Paika123456", "123456");
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            // 获取 NDEF 消息
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                Log.d("Paika123456", "123");

                for (Parcelable rawMessage : rawMessages) {
                    NdefMessage message = (NdefMessage) rawMessage;
                    for (NdefRecord record : message.getRecords()) {
                        // 检查是否是 URI 类型的记录
                        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
                            String uri = parseUriFromNdefRecord(record);
                            use_party = uri.split("paika")[1];
                            isNfc = 1;
                        }
                    }
                }
            }
        }
    }
    private String parseUriFromNdefRecord(NdefRecord record) {
        try {
            byte[] payload = record.getPayload();
            if (payload != null && payload.length > 0) {
                // 跳过第一个字节 (URI 标识符码)
                return new String(payload, 1, payload.length - 1, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void doUPlay() {
        // 使用 MaterialAlertDialogBuilder 创建弹窗
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_uplay, null);
        dialogView.setMinimumWidth(600);
        dialogView.setMinimumHeight(1000);
        builder.setView(dialogView);

        // 获取按钮并设置点击事件
        MaterialButton btnPlay = dialogView.findViewById(R.id.btnPlay);
        AtomicInteger num = new AtomicInteger();

        for (int i = 0; i < players.size();i ++) {
            if(players.get(i).equals(use_name + "()" + iconId)) {
                Log.d("UPlayDialog", "num:" + i);
                num.set(i);
                break;
            }
        }
        // 创建并显示弹窗
        AlertDialog dialog = builder.create();
        btnPlay.setOnClickListener(v -> {
            // 处理“上机”按钮点击事件
            num.set(-1);

            for (int i = 0; i < players.size();i ++) {
                if(players.get(i).equals(use_name + "()" + iconId)) {
                    Log.d("UPlayDialog", "num:" + i);
                    num.set(i);
                    Log.d("UPlayDialog", use_name + "()" + iconId);
                    Log.d("UPlayDialog", players.get(i));
                    break;
                }
            }
            if (num.get() == 2) {
                Log.d("UPlayDialog", "上机 clicked");

                play();
            } else if (num.get() == 3) {
                play3();
            }else if (num.get() == 1 || num.get() ==0) {
                Snackbar.make(v, "正在上机!", Snackbar.LENGTH_SHORT)
                        .setAction("确定", null)
                        .show();
            } else if (players.isEmpty()) {
                Snackbar.make(v, "还在加载队列", Snackbar.LENGTH_SHORT)
                        .setAction("确定", null)
                        .show();            } else {
                Snackbar.make(v, "还在排队!如特殊情况请点击顺位头像请求换位", Snackbar.LENGTH_SHORT)
                        .setAction("确定", null)
                        .show();
            }
            dialog.dismiss();
            // 可以在这里添加更多的逻辑
        });

        if (num.get() ==0 | num.get() == 1) {
            Snackbar.make(findViewById(R.id.back), "正在上机!", Snackbar.LENGTH_SHORT)
                    .setAction("确定", null)
                    .show();
        }else {
            dialog.show();
        }

        // 设置弹窗为全屏大小
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
        }
    }




    private void doFab(View view) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.paika_dialog, null);

        MaterialButton addButton = dialogView.findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            status = 1;
            toolbar.setTitle(use_party + " 房间 " + use_name + " 正在队列");

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("paikastatus", status);
            editor.putString("use_party", use_party);
            editor.commit();

            join();
        });
        MaterialButton leaveButton = dialogView.findViewById(R.id.leaveButton);
        leaveButton.setOnClickListener(v -> {
            status = 0;
            toolbar.setTitle(use_party + " 房间 " + use_name);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("paikastatus", status);
            editor.commit();

            remove();
        });
        MaterialButton leaveRoomButton = dialogView.findViewById(R.id.leaveRoomButton);
        leaveRoomButton.setOnClickListener(v -> {
            status = 0;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("paikastatus", status);
            editor.putString("use_party", "");
            editor.commit();

            unplay();
            Intent intent = new Intent(PaikaActivity.this, MainActivity.class);
            startActivity(intent);
        });
        MaterialButton qrscan = dialogView.findViewById(R.id.qrscan);
        qrscan.setOnClickListener(v -> {
            //扫描二维码,读取为use_party;
            qrScan();
        });

        builder.setView(dialogView);
        builder.show();

    }
    private Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            try {
                if (!use_party.isEmpty()) {
                    getData(); // 调用刷新方法
                }
                handler.postDelayed(this, 3000); // 每隔 2 秒执行一次
            }catch (Exception e) {

            }

        }
    };
    private final ActivityResultLauncher<ScanOptions> qrScanLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    // 获取扫描结果并赋值给 use_party
                    use_party = result.getContents();
                    Log.d("QRScan", "Scanned use_party: " + use_party);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("use_party", use_party);
                    editor.commit();
                    // 更新 UI 或执行其他逻辑
                    toolbar.setTitle(use_party + " 房间 " + use_name);
                    getData(); // 刷新数据
                } else {
                    Log.d("QRScan", "Scan cancelled");
                    Snackbar.make(findViewById(R.id.back), "扫描取消", Snackbar.LENGTH_SHORT)
                            .setAction("确定", null).show();
                }
            }
    );

    // 定义 qrScan 方法
    private void qrScan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("将二维码放入框内扫描"); // 设置提示信息
        options.setOrientationLocked(true); // 锁定屏幕方向为竖屏
        options.setBeepEnabled(true); // 扫描成功时播放提示音
        options.setCaptureActivity(PortraitCaptureActivity.class); // 使用自定义的 CaptureActivity
        options.setTimeout(5000); // 设置超时时间
        qrScanLauncher.launch(options); // 启动扫描
    }


    private void getData() {
        Request request = new Request.Builder()
                .url("https://mais.godserver.cn/api/mai/v1/party?party=" + use_party)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    players = new Gson().fromJson(res, new TypeToken<List<String>>() {
                    }.getType());
                    addDataToTableLayout();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
        Log.d("123456", "getData");
    }
    private void join() {
        if (players.contains(use_name + "()" + iconId)) {
            Snackbar.make(findViewById(R.id.back), "您已经加入该房间", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        Request request = new Request.Builder()
                .url("https://mais.godserver.cn/api/mai/v1/party?party=" + use_party + "&people=" + use_name + "()" + iconId)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Snackbar.make(findViewById(R.id.back), "加入成功", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    getData();
                }
            }
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
        Log.d("123456", "getData");    }
    private void remove() {
        Log.d("123456", "remove");
        Request request = new Request.Builder()
                .url("https://mais.godserver.cn/api/mai/v1/party?party=" + use_party + "&people=" + use_name + "()" + iconId)
                .delete(RequestBody.create("", MediaType.parse("application/json")))
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Snackbar.make(findViewById(R.id.back), "退出成功", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    getData();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
    }
    private void play() {
        Request request = new Request.Builder()
                .url("https://mais.godserver.cn/api/mai/v1/partyPlay?party=" + use_party )
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Snackbar.make(findViewById(R.id.back), "上机成功!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                getData();
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
    }
    private void play3() {
        String changTo = players.get(2);
        change(changTo,1);

    }
    private void unplay() {
        remove();
        status = 0;
        use_party = "";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("paikastatus", status);
        editor.putString("use_party", "");
        editor.commit();
    }
    private void addDataToTableLayout() {
        Context context = this;
        if (players.isEmpty()) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                TableLayout tableLayout = findViewById(R.id.tableLayout);
                //清空
                tableLayout.removeAllViews();
                try {
                    if (players.size() == 0) {
                        Glide.with(context)
                                .load("https://assets2.lxns.net/maimai/icon/0.png")
                                .into(player1Avatar);
                        player1Name.setText("等待玩家");
                        return;
                    }
                    if (players.size() == 1) {
                        Glide.with(context)
                                .load("https://assets2.lxns.net/maimai/icon/" + Integer.parseInt(players.get(0).split("\\(\\)")[1]) +".png")
                                .into(player1Avatar);
                        player1Name.setText(players.get(0).split("\\(\\)")[0]);
                        return;
                    }

                    Glide.with(context)
                            .load("https://assets2.lxns.net/maimai/icon/" + Integer.parseInt(players.get(0).split("\\(\\)")[1]) +".png")
                            .into(player1Avatar);
                    Glide.with(context)
                            .load("https://assets2.lxns.net/maimai/icon/" + Integer.parseInt(players.get(1).split("\\(\\)")[1]) +".png")
                            .into(player2Avatar);
                    player1Name.setText(players.get(0).split("\\(\\)")[0]);
                    player2Name.setText(players.get(1).split("\\(\\)")[0]);

                    for (int i = 2; i < players.size(); i++) {
                        String name = players.get(i).split("\\(\\)")[0];
                        int iconId = Integer.parseInt(players.get(i).split("\\(\\)")[1]);
                        TableRow tableRow = (TableRow) LayoutInflater.from(context).inflate(R.layout.table_row_item, tableLayout, false);
                        int finalI = i;
                        ImageView userAvatar = tableRow.findViewById(R.id.userAvatar);
                        try {
                            Glide.with(context)
                                    .load("https://assets2.lxns.net/maimai/icon/" + iconId +".png")
                                    .into(userAvatar);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                        TextView username = tableRow.findViewById(R.id.username);
                        username.setText(name);
                        tableLayout.addView(tableRow);

                        tableRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                                builder.setTitle("操作");
                                View view = LayoutInflater.from(context).inflate(R.layout.paika_item_dialog, null);
                                builder.setView(view);
                                MaterialButton change = view.findViewById(R.id.change);
                                change.setText("插队");
                                MaterialButton removeButton = view.findViewById(R.id.removeButton);
                                removeButton.setText("移除");
                                MaterialButton fuzhushangji = view.findViewById(R.id.fuzhushangji);
                                fuzhushangji.setText("辅助上机");

                                change.setOnClickListener(v2->{
                                    change(players.get(finalI));
                                });
                                removeButton.setOnClickListener(v2->{
                                    Request request = new Request.Builder()
                                            .url("https://mais.godserver.cn/api/mai/v1/party?party=" + use_party + "&people=" +players.get(finalI))
                                            .delete(RequestBody.create("", MediaType.parse("application/json")))
                                            .build();
                                    OkHttpClient client = new OkHttpClient();
                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                            if (response.isSuccessful()) {
                                                getData();
                                                Snackbar.make(findViewById(R.id.back), "移除成功", Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                                        }
                                    });
                                });
                                fuzhushangji.setOnClickListener(v2->{
                                    if (finalI == 2 ) {
                                        Request request = new Request.Builder()
                                                .url("https://mais.godserver.cn/api/mai/v1/partyPlay?party=" + use_party )
                                                .post(RequestBody.create("", MediaType.parse("application/json")))
                                                .build();
                                        OkHttpClient client = new OkHttpClient();
                                        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    getData();
                                                    Snackbar.make(findViewById(R.id.back), "辅助上机成功", Snackbar.LENGTH_LONG)
                                                            .setAction("Action", null).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                                Log.d("123456", "onFailure: " + e);
                                            }
                                        });
                                    }else {
                                        Snackbar.make(findViewById(R.id.back), "辅助上机失败,位置不满足要求", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }

                                });

                                builder.show();
                            }
                        });
                        // 添加分割线
                        if (finalI < players.size() - 1) {
                            View separator = new View(context);
                            separator.setLayoutParams(new TableLayout.LayoutParams(
                                    TableLayout.LayoutParams.MATCH_PARENT,
                                    1
                            ));
                            separator.setBackgroundColor(ContextCompat.getColor(context, R.color.dividerColor));
                            tableLayout.addView(separator);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }

    private void change(String to) {
        Request request = new Request.Builder()
                .url("https://mais.godserver.cn/api/mai/v1/party?party=" + use_party + "&people=" + use_name + "()" + iconResId + "&changeToPeople=" + to)
                .put(RequestBody.create("", MediaType.parse("application/json")))
                .build();
        OkHttpClient client = new OkHttpClient();
        Log.d("123456", "https://mais.godserver.cn/api/mai/v1/party?party=" + use_party + "&people=" + use_name + "()" + iconResId + "&changeToPeople=" + to);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    getData();
                    Log.d("123456", "onResponse: " + response.body().string());
                    Snackbar.make(findViewById(R.id.back), "换位成功", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
    }
    private void change(String to,int type) {
        Request request = new Request.Builder()
                .url("https://mais.godserver.cn/api/mai/v1/party?party=" + use_party + "&people=" + use_name + "()" + iconResId + "&changeToPeople=" + to)
                .put(RequestBody.create("", MediaType.parse("application/json")))
                .build();
        OkHttpClient client = new OkHttpClient();
        Log.d("123456", "https://mais.godserver.cn/api/mai/v1/party?party=" + use_party + "&people=" + use_name + "()" + iconResId + "&changeToPeople=" + to);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Request request = new Request.Builder()
                            .url("https://mais.godserver.cn/api/mai/v1/partyPlay?party=" + use_party )
                            .post(RequestBody.create("", MediaType.parse("application/json")))
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            Snackbar.make(findViewById(R.id.back), "上机成功!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            getData();
                        }

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
    }

    // 用户类
    private static class User {
        private String name;
        private int avatar;

        public User(String name, int avatar) {
            this.name = name;
            this.avatar = avatar;
        }

        public String getName() {
            return name;
        }

        public int getAvatar() {
            return avatar;
        }
    }
}
