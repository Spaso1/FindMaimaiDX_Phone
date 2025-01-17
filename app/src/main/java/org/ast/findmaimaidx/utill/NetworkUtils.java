package org.ast.findmaimaidx.utill;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Callback;
import okhttp3.Response;
import org.ast.findmaimaidx.been.ChartPlay;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkUtils {
    private static final String BASE_URL = "http://mai.godserver.cn:11451/api/chartPlays/charts";

    public static void fetchChartPlays(Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(BASE_URL).build();
        client.newCall(request).enqueue(callback);
    }

    public static List<ChartPlay> parseChartPlays(String responseBody) throws JSONException {
        List<ChartPlay> chartPlayList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray jsonArray = jsonObject.getJSONArray("content");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject chartJson = jsonArray.getJSONObject(i);
            ChartPlay chartPlay = new ChartPlay();
            chartPlay.setSongName(chartJson.getString("songName"));
            chartPlay.setLength(chartJson.getString("length"));
            chartPlay.setDifficulty(chartJson.getString("difficulty"));
            chartPlay.setChartZipUrl(chartJson.getString("chartZipUrl"));
            chartPlay.setLikes(chartJson.getInt("likes"));
            chartPlay.setDownloads(chartJson.getInt("downloads"));
            chartPlay.setAuthor(chartJson.getString("author"));
            chartPlay.setId(chartJson.getInt("id"));
            chartPlay.setUsed(chartJson.getBoolean("used"));
            chartPlayList.add(chartPlay);
        }
        return chartPlayList;
    }
}
