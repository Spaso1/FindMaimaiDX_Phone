package org.astral.findmaimaiultra.ui.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.astral.findmaimaiultra.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WidgetUpdateService extends IntentService {
    private static final String TAG = "WidgetUpdateService";

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            fetchDataAndUpdateWidget();
        }
    }

    private void fetchDataAndUpdateWidget() {
        Log.d(TAG + "loading", "Fetching data and updating widget");
        OkHttpClient client = new OkHttpClient();
        String url = "http://yourserver.com/api/data"; // 替换为实际的API地址

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error fetching data: " + e.getMessage());
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WidgetUpdateService.this);
                ComponentName thisWidget = new ComponentName(WidgetUpdateService.this, MyWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_2x2);

                    // 将数据列表转换为字符串
                    StringBuilder dataText = new StringBuilder();
                    dataText.append("加载失败");
                    // 设置TextView的文本内容
                    views.setTextViewText(R.id.widget_text, dataText.toString());

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    List<String> dataList = parseData(responseData);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WidgetUpdateService.this);
                    ComponentName thisWidget = new ComponentName(WidgetUpdateService.this, MyWidgetProvider.class);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

                    for (int appWidgetId : appWidgetIds) {
                        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_2x2);

                        // 将数据列表转换为字符串
                        StringBuilder dataText = new StringBuilder();
                        for (String data : dataList) {
                            dataText.append(data).append("\n");
                        }

                        // 设置TextView的文本内容
                        views.setTextViewText(R.id.widget_text, dataText.toString());

                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch data: " + response.code());
                }
            }
        });
    }

    private List<String> parseData(String jsonData) {
        List<String> dataList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String data = jsonObject.getString("data_key"); // 替换为实际的JSON键
                dataList.add(data);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
        }
        return dataList;
    }
}
