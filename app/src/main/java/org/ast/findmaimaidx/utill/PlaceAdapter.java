package org.ast.findmaimaidx.utill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> placeList;

    public PlaceAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.nameTextView.setText(place.getName());
        holder.provinceTextView.setText("省: " + place.getProvince());
        holder.cityTextView.setText("市: " + place.getCity());
        holder.areaTextView.setText("区: " + place.getArea());
        holder.addressTextView.setText("详细地址: " + place.getAddress());
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

        PlaceViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            provinceTextView = itemView.findViewById(R.id.provinceTextView);
            cityTextView = itemView.findViewById(R.id.cityTextView);
            areaTextView = itemView.findViewById(R.id.areaTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
        }
    }
}
