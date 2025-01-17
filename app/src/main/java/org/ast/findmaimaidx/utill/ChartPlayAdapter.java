package org.ast.findmaimaidx.utill;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.ast.findmaimaidx.been.ChartPlay;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.ui.ChartPlayDetailActivity;

import java.util.List;

public class ChartPlayAdapter extends RecyclerView.Adapter<ChartPlayAdapter.ViewHolder> {
    private Context context;
    private List<ChartPlay> chartPlayList;

    public ChartPlayAdapter(Context context, List<ChartPlay> chartPlayList) {
        this.context = context;
        this.chartPlayList = chartPlayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chart_play, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChartPlay chartPlay = chartPlayList.get(position);
        holder.songNameTextView.setText(chartPlay.getSongName());
        holder.difficultyTextView.setText(chartPlay.getDifficulty());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChartPlayDetailActivity.class);
            intent.putExtra("chartPlay", chartPlay);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chartPlayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView songNameTextView;
        TextView difficultyTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songNameTextView = itemView.findViewById(R.id.songNameTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
        }
    }
}
