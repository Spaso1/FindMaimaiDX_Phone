package org.ast.findmaimaidx.utill;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.util.Log;
import androidx.appcompat.widget.Toolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.MainLaunch;
import org.ast.findmaimaidx.PageActivity;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.DistanceCalculator;
import org.ast.findmaimaidx.been.Market;
import org.ast.findmaimaidx.been.Place;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class FindNearMarket {
    public static String key = "bb0e04ceb735481cf4e461628345f4ec";
    @SuppressLint("StaticFieldLeak")
    public static void findnear(Place place_centor) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                String web = "https://restapi.amap.com/v5/place/around?key=" + key + "&radius=1000&location=" + place_centor.getX() + "," + place_centor.getY() + "&page_size=25&types=060200|060201|060202|060400|060401|060402|060403|060404|060405|060406|060407|060408|060409|060411|060413|060414|060415|";
                System.out.println(web);
                @SuppressLint("StaticFieldLeak") Request request = new Request.Builder()
                        .url(web)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (((Response) response).isSuccessful()) {
                        return response.body().string();
                    } else {
                        return "Error: " + response.code();
                    }
                } catch (Exception e) {
                    Log.e("OkHttp", "Error: " + e.getMessage());
                    return "Error: " + e.getMessage();
                }
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(String result) {
                if (result.contains("pois")) {
                    String b = result.split("\"pois\":")[1];
                    result = b.split("]")[0] + "]";
                    PageActivity.marketList = parseJsonToPlaceList(result);
                    for (Market market : PageActivity.marketList) {
                        Log.d("Market", market.getName());
                    }
                }

            }
        }.execute();
    }
    private static List<Market> parseJsonToPlaceList(String jsonString) {
        Gson gson = new Gson();
        Type placeListType = new TypeToken<List<Market>>() {
        }.getType();
        return gson.fromJson(jsonString, placeListType);
    }
}
