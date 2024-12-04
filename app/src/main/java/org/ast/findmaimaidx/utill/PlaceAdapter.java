package org.ast.findmaimaidx.utill;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);

        if(place.getName().contains("收藏")) {
            holder.nameTextView.setText(place.getName() + "♥");
        }else {
            holder.nameTextView.setText(place.getName());
        }
        holder.provinceTextView.setText(place.getProvince());
        holder.cityTextView.setText(place.getCity());
        holder.areaTextView.setText(place.getArea());
        holder.addressTextView.setText(place.getAddress());

        // 设置点击监听器
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(place);
            }
        });
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

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            provinceTextView = itemView.findViewById(R.id.provinceTextView);
            cityTextView = itemView.findViewById(R.id.cityTextView);
            areaTextView = itemView.findViewById(R.id.areaTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
        }
    }
}
