package org.ast.findmaimaidx.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.Place;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> placeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Place place);
    }

    public PlaceAdapter(List<Place> placeList, OnItemClickListener listener) {
        this.placeList = placeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(itemView);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        if (place.getIsUse()==0) {
            holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        }
        if (place.getName().contains("收藏")) {
            holder.nameTextView.setText(place.getName() + "♥");
        } else {
            holder.nameTextView.setText(place.getName());
        }
        holder.provinceTextView.setText(place.getProvince());
        holder.cityTextView.setText(place.getCity());
        holder.areaTextView.setText(place.getArea());
        holder.addressTextView.setText(place.getAddress());
        double rating = (double) ((place.getNum() + place.getNumJ()) * (place.getGood() + 1)) / (place.getGood() + place.getBad() + 1);
        Log.i("rating", rating + "");
        if (rating >= 3) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_sssp));
        } else if (rating < 3 && rating >= 2) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_sss));
        } else if (rating < 2 && rating >= 1) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_ss));
        } else if (rating < 1 && rating >= 0.5) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_s));
        } else if (rating < 0.5 && rating >= 0) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_s));
        } else {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_a));
        }
        Log.i("rating", rating + "|" + place.getName());
        // 控制竖线的位置
        controlVerticalLines(holder, place);

        // 设置点击监听器
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(place);
            }
        });
    }

    private void controlVerticalLines(PlaceViewHolder holder, Place place) {
        ConstraintLayout horizontalLinesContainer = holder.itemView.findViewById(R.id.horizontalLinesContainer);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(horizontalLinesContainer);

        double rating = (double) ((place.getNum() + place.getNumJ()) * (place.getGood() + 5)) / (place.getBad() + 5);
        // 根据 rating 计算竖线的位置百分比
        float bias1 = (float) (rating / 3.0); // 示例百分比，假设 rating 最大为 3
        float bias2 = (float) (2 * rating / 3.0); // 示例百分比，假设 rating 最大为 3
        float bias3 = 1.0f; // 第三条竖线始终在最右边

        // 确保 bias 不超过 1.0
        bias1 = Math.min(bias1, 1.0f);
        bias2 = Math.min(bias2, 1.0f);

        // 设置 Guideline 的位置
        constraintSet.setGuidelinePercent(R.id.guideline1, bias1);
        constraintSet.setGuidelinePercent(R.id.guideline2, bias2);
        constraintSet.setGuidelinePercent(R.id.guideline3, bias3);

        if (rating >= 3) {
            // 显示所有竖线
            constraintSet.setVisibility(R.id.verticalLine1, View.VISIBLE);
            constraintSet.setVisibility(R.id.verticalLine2, View.VISIBLE);
            constraintSet.setVisibility(R.id.verticalLine3, View.VISIBLE);
        } else if (rating < 3 && rating >= 2) {
            // 显示前两条竖线
            constraintSet.setVisibility(R.id.verticalLine1, View.VISIBLE);
            constraintSet.setVisibility(R.id.verticalLine2, View.VISIBLE);
            constraintSet.setVisibility(R.id.verticalLine3, View.GONE);
        } else if (rating < 2 && rating >= 1) {
            // 显示第一条竖线
            constraintSet.setVisibility(R.id.verticalLine1, View.VISIBLE);
            constraintSet.setVisibility(R.id.verticalLine2, View.GONE);
            constraintSet.setVisibility(R.id.verticalLine3, View.GONE);
        } else {
            // 不显示竖线
            constraintSet.setVisibility(R.id.verticalLine1, View.GONE);
            constraintSet.setVisibility(R.id.verticalLine2, View.GONE);
            constraintSet.setVisibility(R.id.verticalLine3, View.GONE);
        }

        constraintSet.applyTo(horizontalLinesContainer);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView provinceTextView;
        TextView cityTextView;
        TextView areaTextView;
        TextView addressTextView;
        ImageView imageView;

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            provinceTextView = itemView.findViewById(R.id.provinceTextView);
            cityTextView = itemView.findViewById(R.id.cityTextView);
            areaTextView = itemView.findViewById(R.id.areaTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            imageView = itemView.findViewById(R.id.photoId);
        }
    }
}
