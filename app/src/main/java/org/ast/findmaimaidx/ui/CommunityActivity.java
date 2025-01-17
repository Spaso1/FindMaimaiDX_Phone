package org.ast.findmaimaidx.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.ast.findmaimaidx.been.ChartPlay;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.utill.ChartPlayAdapter;
import org.ast.findmaimaidx.utill.NetworkUtils;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChartPlayAdapter adapter;
    private List<ChartPlay> chartPlayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chartPlayList = new ArrayList<>();
        adapter = new ChartPlayAdapter(this, chartPlayList);
        recyclerView.setAdapter(adapter);

        // Fetch data from server
        fetchDataFromServer();
    }

    private void fetchDataFromServer() {
        NetworkUtils.fetchChartPlays(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        final List<ChartPlay> chartPlays = NetworkUtils.parseChartPlays(response.body().string());
                        runOnUiThread(() -> {
                            chartPlayList.addAll(chartPlays);
                            adapter.notifyDataSetChanged();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
