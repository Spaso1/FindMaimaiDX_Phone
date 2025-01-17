package org.ast.findmaimaidx.utill;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        if(place.getName().contains("收藏")) {
            holder.nameTextView.setText(place.getName() + "♥");
        }else {
            holder.nameTextView.setText(place.getName());
        }
        holder.provinceTextView.setText(place.getProvince());
        holder.cityTextView.setText(place.getCity());
        holder.areaTextView.setText(place.getArea());
        holder.addressTextView.setText(place.getAddress());
        double rating = (double) ((place.getNum() + place.getNumJ()) * (place.getGood() + 1)) / (place.getGood() + place.getBad() + 1);
        Log.i("rating",rating + "");
        if(rating >= 3) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_sssp));
        }else if (rating < 3 && rating >= 2){
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_sss));
        }else if (rating < 2 && rating >= 1){
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_ss));
        }else if (rating < 1 && rating >= 0.5){
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_s));
        }else if (rating < 0.5 && rating >= 0){
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_s));
        }else {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rank_a));
        }
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
