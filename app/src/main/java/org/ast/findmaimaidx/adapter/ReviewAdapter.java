package org.ast.findmaimaidx.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.ast.findmaimaidx.been.PlaceContent;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<PlaceContent> reviews;

    public ReviewAdapter(List<PlaceContent> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceContent review = reviews.get(position);
        if(review.isUsed()) {
            holder.textViewUser.setText(review.getUser_name());
            holder.textViewContent.setText(review.getUser_content());
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void addReview(PlaceContent review) {
        reviews.add(review);
        notifyItemInserted(reviews.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUser;
        TextView textViewContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(android.R.id.text1);
            textViewContent = itemView.findViewById(android.R.id.text2);

            textViewUser.setTextSize(18);
            textViewContent.setTextSize(14);
        }
    }
}
