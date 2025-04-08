package org.astral.findmaimaiultra.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.been.pixiv.model.IllustData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PixivAdapter extends RecyclerView.Adapter<PixivAdapter.ViewHolder> {
    private List<IllustData> dataList;
    private OnItemClickListener listener;

    public PixivAdapter(List<IllustData> dataList) {
        this.dataList = dataList;
    }

    public void update(List<IllustData> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pixiv, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IllustData data = dataList.get(position);
        holder.title.setText(data.getTitle());
        // Load image using a library like Glide or Picasso
        String imageUrl = "http://43.153.174.191:45678/api/v1/pixiv/toTencent?id=" + data.getId();

        if (imageUrl != null) {
            loadImage(holder.backgroundLayout, imageUrl);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView backgroundLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            title = itemView.findViewById(R.id.title);
        }
    }
    private void loadImage(ImageView imageView, String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client =  new OkHttpClient();
        client.newBuilder().connectTimeout(180, TimeUnit.SECONDS);
        client.newBuilder().readTimeout(180, TimeUnit.SECONDS);
        client.newBuilder().writeTimeout(180, TimeUnit.SECONDS);
        Log.d("Web", url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("PixivAdapter", "Failed to load image: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    byte[] imageBytes = response.body().bytes();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (bitmap != null) {
                        imageView.post(() -> imageView.setImageBitmap(bitmap));
                    }
                } else {
                    Log.e("PixivAdapter", "Failed to load image: " + response.code());
                }
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(IllustData illustData);
    }
}
