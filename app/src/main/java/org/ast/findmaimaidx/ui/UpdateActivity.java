package org.ast.findmaimaidx.ui;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import okhttp3.*;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.lx.Lx_chart;
import org.ast.findmaimaidx.been.PlayerData;
import org.ast.findmaimaidx.updater.crawler.Callback;
import org.ast.findmaimaidx.updater.crawler.CrawlerCaller;
import org.ast.findmaimaidx.updater.notification.NotificationUtil;
import org.ast.findmaimaidx.updater.server.HttpServer;
import org.ast.findmaimaidx.updater.server.HttpServerService;
import org.ast.findmaimaidx.updater.ui.DataContext;
import org.ast.findmaimaidx.updater.vpn.core.Constant;
import org.ast.findmaimaidx.updater.vpn.core.LocalVpnService;
import org.ast.findmaimaidx.updater.vpn.core.ProxyConfig;
import org.ast.findmaimaidx.utill.Shuiyu2Luoxue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static org.ast.findmaimaidx.updater.Util.copyText;
import static org.ast.findmaimaidx.updater.Util.getDifficulties;
import static org.ast.findmaimaidx.updater.crawler.CrawlerCaller.writeLog;

public class UpdateActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener,
        LocalVpnService.onStatusChangedListener {

    private static final String TAG = UpdateActivity.class.getSimpleName();
    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;
    private static String GL_HISTORY_LOGS;
    private SwitchCompat switchProxy;
    private TextView textViewLog;
    private ScrollView scrollViewLog;
    private Calendar mCalendar;

    private SharedPreferences mContextSp;
    private Context context = this;
    private void updateTilte() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (LocalVpnService.IsRunning) {
                actionBar.setTitle(getString(R.string.connected));
            } else {
                actionBar.setTitle(getString(R.string.disconnected));
            }
        }
    }

    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);

        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewLog = (TextView) findViewById(R.id.textViewLog);

        assert textViewLog != null;
        textViewLog.setText(GL_HISTORY_LOGS);
        textViewLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        mCalendar = Calendar.getInstance();
        LocalVpnService.addOnStatusChangedListener(this);

        mContextSp = this.getSharedPreferences(
                "updater.data",
                Context.MODE_PRIVATE);

        CrawlerCaller.listener = this;

        loadContextData();

        Button sy2lx = findViewById(R.id.sy2lx);
        sy2lx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建 OkHttpClient 实例
                OkHttpClient client = new OkHttpClient();
                final SharedPreferences sp = getSharedPreferences("setting", Context.MODE_PRIVATE);
                // 原始数据
                String rawData = "{\"username\":\"" + sp.getString("shuiyu_username", "") + "\",\"b50\":true}";
                RequestBody body = RequestBody.create(rawData, MediaType.get("application/json; charset=utf-8"));
                // 创建 Request
                Request request = new Request.Builder()
                        .url("https://www.diving-fish.com/api/maimaidxprober/query/player")
                        .post(body)
                        .build();
                // 使用 AsyncTask 发送请求
                new SendRequestTask(client, request,0).execute();
            }
        });
    }
    private class SendRequestTask extends AsyncTask<Void, Void, String> {
        private OkHttpClient client;
        private Request request;
        private int t;

        public SendRequestTask(OkHttpClient client, Request request,int t) {
            this.client = client;
            this.request = request;
            this.t =t;
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
            if(this.t==0) {
                // 使用Gson进行反序列化
                Gson gson = new Gson();
                PlayerData playerData = gson.fromJson(result, PlayerData.class);
                ArrayList<Lx_chart> lx_charts = Shuiyu2Luoxue.shuiyu2luoxue(playerData);
                // 分割 lx_charts 列表

                // 分割 lx_charts 列表成五个部分
                int size = lx_charts.size();
                int partSize = (int) Math.ceil(size / 2.0);

                List<List<Lx_chart>> parts = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    int fromIndex = i * partSize;
                    int toIndex = Math.min(fromIndex + partSize, size);
                    parts.add(lx_charts.subList(fromIndex, toIndex));
                }

                try {
                    // 处理每个部分
                    for (int i = 0; i < parts.size(); i++) {
                        String rawPart = serializeToJson(gson, parts.get(i));
                        rawPart = "{\"scores\":" + rawPart + "}";
                        Log.d("rawPart", rawPart);
                        OkHttpClient client = new OkHttpClient();
                        RequestBody body = RequestBody.create(rawPart, MediaType.get("application/json; charset=utf-8"));
                        String code = getSharedPreferences("setting", Context.MODE_PRIVATE).getString("luoxue_username","");
                        // 创建 Request
                        Request request = new Request.Builder()
                                .url("https://maimai.lxns.net/api/v0/user/maimai/player/scores")
                                .header("X-User-Token",code) // 添加认证头
                                .post(body)
                                .build();
                        // 使用 AsyncTask 发送请求
                        new SendRequestTask(client, request,1).execute();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(this.t==1) {
                Log.d("out",result);
                Toast.makeText(context, "上传成功,数据已从水鱼传到落雪~", Toast.LENGTH_SHORT).show();
            }
            // 这里raw是发送给落雪查分器的数据,代表着上传歌曲信息
        }
    }
    private void inputAddress() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Http com.bakapiano.maimai.com.bakapiano.maimai.proxy server");
//        final EditText input = new EditText(this);
//        input.setText(ProxyConfig.getHttpProxyServer(this));
//        builder.setView(input);
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String text = input.getText().toString();
//                ProxyConfig.Instance.setProxy(text);
//                ProxyConfig.setHttpProxyServer(MainActivity.this, text);
//            }
//        });
//        builder.setCancelable(false);
//        builder.show();
        @SuppressLint("AuthLeak") String text = "http://user:pass@127.0.0.1:8848";
        ProxyConfig.Instance.setProxy(text);
        ProxyConfig.setHttpProxyServer(UpdateActivity.this, text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTilte();
    }

    String getVersionName() {
        PackageManager packageManager = getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "null package manager is impossible");
            return null;
        }

        try {
            return packageManager.getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "package not found is impossible", e);
            return null;
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLogReceived(String logString) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        logString = String.format("[%1$02d:%2$02d:%3$02d] %4$s\n",
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND),
                logString);

        Log.d(Constant.TAG, logString);

        textViewLog.append(logString);
        GL_HISTORY_LOGS = textViewLog.getText() == null ? "" : textViewLog.getText().toString();
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        switchProxy.setEnabled(true);
        switchProxy.setChecked(isRunning);
        onLogReceived(status);
        updateTilte();
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }
    private static String serializeToJson(Gson gson, List<Lx_chart> charts) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = gson.newJsonWriter(stringWriter);

        try {
            jsonWriter.beginArray();
            for (Lx_chart chart : charts) {
                gson.toJson(chart, Lx_chart.class, jsonWriter);
            }
            jsonWriter.endArray();
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringWriter.toString();
    }
    private final Object switchLock = new Object();

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!switchProxy.isEnabled()) return;
        if (!switchProxy.isPressed()) return;
        saveOptions();
        saveDifficulties();

        if (getDifficulties().isEmpty()) {
            if (isChecked) {
                writeLog("请至少勾选一个难度!");
            }
            switchProxy.setChecked(false);
            return;
        }
        Context context = this;
        if (LocalVpnService.IsRunning != isChecked) {
            switchProxy.setEnabled(false);
            if (isChecked) {
                NotificationUtil.getINSTANCE().setContext(this).startNotification();
                checkProberAccount(result -> {
                    this.runOnUiThread(() -> {
                        if ((Boolean) result) {
//                            getAuthLink(link -> {
                            if (DataContext.CopyUrl) {
                                String link = DataContext.WebHost;
                                // Use local auth server if web host is not set
                                if (link.length() == 0) {
                                    link = "http://127.0.0.2:" + HttpServer.Port + "/" + getRandomString(10);
                                }
                                String finalLink = link;
                                this.runOnUiThread(() -> copyText(context, finalLink));
                            }

                            // Start vpn service
                            Intent intent = LocalVpnService.prepare(context);
                            if (intent == null) {
                                startVPNService();
                                // Jump to wechat app
                                if (DataContext.AutoLaunch) {
                                    getWechatApi();
                                }
                            } else {
                                startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                            }
                            // Start http service
                            startHttpService();
//                            });
                        } else {
                            switchProxy.setChecked(false);
                            switchProxy.setEnabled(true);
                        }
                    });
                });
            } else {
                LocalVpnService.IsRunning = false;
                stopHttpService();
            }
        }
    }

    private void startHttpService() {
        startService(new Intent(this, HttpServerService.class));
    }

    private void stopHttpService() {
        stopService(new Intent(this, HttpServerService.class));
    }

    private void startVPNService() {
        textViewLog.setText("");
        GL_HISTORY_LOGS = null;
        onLogReceived("starting...");
        startService(new Intent(this, LocalVpnService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVPNService();
                // Jump to wechat app
                getWechatApi();
            } else {
                switchProxy.setChecked(false);
                switchProxy.setEnabled(true);
                onLogReceived("canceled.");
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_switch);
        if (menuItem == null) {
            return false;
        }

        switchProxy = (SwitchCompat) menuItem.getActionView();
        if (switchProxy == null) {
            return false;
        }

        switchProxy.setChecked(LocalVpnService.IsRunning);
        switchProxy.setOnCheckedChangeListener(this);

        if (!switchProxy.isChecked()) {
            inputAddress();
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        LocalVpnService.removeOnStatusChangedListener(this);
        super.onDestroy();
    }

    public void saveText(View view) {
        Context context = this;
        checkProberAccount(result -> {
            if ((Boolean) result) {
                saveContextData();
                this.runOnUiThread(() -> {
                    new AlertDialog.Builder(context)
                            .setTitle(getString(R.string.app_name) + " " + getVersionName())
                            .setMessage("查分器账户保存成功")
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                    SharedPreferences sp = getSharedPreferences("setting", Context.MODE_PRIVATE);
                    @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sp.edit();
                    TextView password = findViewById(R.id.password);
                    TextView username = findViewById(R.id.username);
                    editor.putString("shuiyu_password", password.getText().toString());
                    editor.putString("shuiyu_username", username.getText().toString());
                    editor.commit();
                });
            }
        });
    }

    private void showInvalidAccountDialog() {
        this.runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name) + " " + getVersionName())
                    .setMessage("查分账户信息无效")
                    .setPositiveButton(R.string.btn_ok, null)
                    .show();
        });
    }

    private void openWebLink(String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        this.startActivity(intent);
    }

    private void getAuthLink(Callback callback) {
        new Thread() {
            public void run() {
                String link = CrawlerCaller.getWechatAuthUrl();
                callback.onResponse(link);
            }
        }.start();
    }

    private void getLatestVersion(Callback callback) {
        CrawlerCaller.getLatestVersion(result -> {
            String version = (String)result;
            callback.onResponse(version);
        });
    }

    private void checkProberAccount(Callback callback) {
        DataContext.Username = ((TextView) findViewById(R.id.username)).getText().toString();
        DataContext.Password = ((TextView) findViewById(R.id.password)).getText().toString();

        saveOptions();

        saveDifficulties();

        if (DataContext.Username == null || DataContext.Password == null) {
            showInvalidAccountDialog();
            callback.onResponse(false);
            return;
        }
        CrawlerCaller.verifyAccount(DataContext.Username, DataContext.Password, result -> {
            if (!(Boolean) result) showInvalidAccountDialog();
            callback.onResponse(result);
        });
    }

    private void saveDifficulties() {
        DataContext.BasicEnabled = ((CheckBox) findViewById(R.id.basic)).isChecked();
        DataContext.AdvancedEnabled = ((CheckBox) findViewById(R.id.advanced)).isChecked();
        DataContext.ExpertEnabled = ((CheckBox) findViewById(R.id.expert)).isChecked();
        DataContext.MasterEnabled = ((CheckBox) findViewById(R.id.master)).isChecked();
        DataContext.RemasterEnabled = ((CheckBox) findViewById(R.id.remaster)).isChecked();
    }

    private void saveOptions() {
        DataContext.CopyUrl = ((Switch) findViewById(R.id.copyUrl)).isChecked();
        DataContext.AutoLaunch = ((Switch) findViewById(R.id.autoLaunch)).isChecked();
    }


    private void loadContextData() {
        String username = mContextSp.getString("username", null);
        String password = mContextSp.getString("password", null);
        boolean copyUrl = mContextSp.getBoolean("copyUrl", true);
        boolean autoLaunch = mContextSp.getBoolean("autoLaunch", true);

        boolean basicEnabled = mContextSp.getBoolean("basicEnabled", false);
        boolean advancedEnabled = mContextSp.getBoolean("advancedEnabled", false);
        boolean expertEnabled = mContextSp.getBoolean("expertEnabled", true);
        boolean masterEnabled = mContextSp.getBoolean("masterEnabled", true);
        boolean remasterEnabled = mContextSp.getBoolean("remasterEnabled", true);

        String proxyHost = mContextSp.getString("porxyHost","proxy.bakapiano.com");
        String webHost = mContextSp.getString("webHost","");
        int proxyPort = mContextSp.getInt("porxyPort",2569);

        SharedPreferences settingProperties = getSharedPreferences("setting", Context.MODE_PRIVATE);

        SharedPreferences.Editor editorSetting = settingProperties.edit();
        SharedPreferences.Editor editorM = mContextSp.edit();
        if(settingProperties.contains("shuiyu_username")) {
            username = settingProperties.getString("shuiyu_username","");
            editorM.putString("username",username);
            editorM.apply();
        }else if(username != null){
            editorSetting.putString("shuiyu_username",username);
            editorSetting.apply();
        }

        ((TextView) findViewById(R.id.username)).setText(username);
        ((TextView) findViewById(R.id.password)).setText(password);

        ((Switch) findViewById(R.id.copyUrl)).setChecked(copyUrl);
        ((Switch) findViewById(R.id.autoLaunch)).setChecked(autoLaunch);

        ((CheckBox) findViewById(R.id.basic)).setChecked(basicEnabled);
        ((CheckBox) findViewById(R.id.advanced)).setChecked(advancedEnabled);
        ((CheckBox) findViewById(R.id.expert)).setChecked(expertEnabled);
        ((CheckBox) findViewById(R.id.master)).setChecked(masterEnabled);
        ((CheckBox) findViewById(R.id.remaster)).setChecked(remasterEnabled);


        DataContext.Username = username;
        DataContext.Password = password;

        DataContext.CopyUrl = copyUrl;
        DataContext.AutoLaunch = autoLaunch;

        DataContext.BasicEnabled = basicEnabled;
        DataContext.AdvancedEnabled = advancedEnabled;
        DataContext.ExpertEnabled = expertEnabled;
        DataContext.MasterEnabled = masterEnabled;
        DataContext.RemasterEnabled = remasterEnabled;

        DataContext.ProxyPort = proxyPort;
        DataContext.ProxyHost = proxyHost;
        DataContext.WebHost = webHost;

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v -> {
            Toast.makeText(this, "正在跳转至水鱼查分器官网", Toast.LENGTH_SHORT).show();
            String url = "https://www.diving-fish.com/maimaidx/prober/";
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    private void saveContextData() {
        SharedPreferences.Editor editor = mContextSp.edit();
        saveAccountContextData(editor);
        saveOptionsContextData(editor);
        saveDifficultiesContextData(editor);
        editor.apply();
    }

    private static void saveDifficultiesContextData(SharedPreferences.Editor editor) {
        editor.putBoolean("basicEnabled", DataContext.BasicEnabled);
        editor.putBoolean("advancedEnabled", DataContext.AdvancedEnabled);
        editor.putBoolean("expertEnabled", DataContext.ExpertEnabled);
        editor.putBoolean("masterEnabled", DataContext.MasterEnabled);
        editor.putBoolean("remasterEnabled", DataContext.RemasterEnabled);
    }

    private static void saveOptionsContextData(SharedPreferences.Editor editor) {
        editor.putBoolean("copyUrl", DataContext.CopyUrl);
        editor.putBoolean("autoLaunch", DataContext.AutoLaunch);
    }

    private static void saveAccountContextData(SharedPreferences.Editor editor) {
        editor.putString("username", DataContext.Username);
        editor.putString("password", DataContext.Password);
    }

    private void getWechatApi() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
