package org.ast.findmaimaidx.utill;

import android.os.AsyncTask;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.ast.findmaimaidx.been.Release;
import org.json.JSONObject;

public class FetchLatestReleaseTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "FetchLatestReleaseTask";
    private static final String GITHUB_API_URL = "https://github.com/Spaso1/FindMaimaiDX_Phone/releases/latest";

    private OnReleaseFetchedListener listener;

    public interface OnReleaseFetchedListener {
        void onReleaseFetched(Release release);
        void onError(String errorMessage);
    }

    public FetchLatestReleaseTask(OnReleaseFetchedListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String owner = params[0];
        String repo = params[1];

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(String.format(GITHUB_API_URL, owner, repo))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "Error: " + response.code();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result.startsWith("Error: ")) {
            listener.onError(result.substring(6));
        } else {
            try {
                JSONObject json = new JSONObject(result);
                Release release = new Release(
                        json.getString("tag_name"),
                        json.getString("name"),
                        json.getString("html_url"),
                        json.getString("body")
                );
                listener.onReleaseFetched(release);
            } catch (Exception e) {
                listener.onError("JSON parsing error: " + e.getMessage());
            }
        }
    }
}
