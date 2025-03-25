package org.astral.findmaimaiultra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import jp.wasabeef.glide.transformations.BlurTransformation;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.been.faker.MusicRating;

import java.util.List;

public class MusicRatingAdapter extends RecyclerView.Adapter<MusicRatingAdapter.ViewHolder> {

    private List<MusicRating> musicRatings;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MusicRating musicRating);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {

        this.listener = listener;
    }

    public MusicRatingAdapter(List<MusicRating> musicRatings) {
        this.musicRatings = musicRatings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_rating, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicRating musicRating = musicRatings.get(position);
        holder.musicName.setText(musicRating.getMusicName());
        holder.level.setText("Level " + musicRating.getLevel_info());
        String ac = String.valueOf(musicRating.getAchievement());
        //从后往前第5位加.
        if(ac.length()>4) {
            ac = ac.substring(0, ac.length() - 4) + "." + ac.substring(ac.length() - 4);
        }
        holder.ach.setText(ac);
        int id = musicRating.getMusicId();
        if (id > 10000) {
            id = id - 10000;
        }

        // 假设 musicRating 有一个方法 getCoverImageUrl() 返回图片的URL
        String imageUrl = "https://assets2.lxns.net/maimai/jacket/" + id + ".png";
        if (imageUrl != null) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 使用所有缓存策略
                    .signature(new ObjectKey(musicRating.getMusicId())) // 使用 MusicId 作为签名
                    .transform(new BlurTransformation(15, 1)); // 调整模糊半径为 15，采样因子为 1

            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .apply(options)
                    .into(holder.backgroundLayout);
        }

        // 根据 achievement 数据加载相应的图片
        int achievement = musicRating.getAchievement();
        int achievementImageResId = getAchievementImageResId(achievement);
        if (achievementImageResId != 0) {
            Glide.with(holder.itemView.getContext())
                    .load(achievementImageResId)
                    .into(holder.achievementImage);
        } else {
            holder.achievementImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(musicRating);
            }
        });
    }

    private int getAchievementImageResId(int achievement) {
        // 根据 achievement 值返回相应的图片资源 ID
        // 例如：
        if(achievement > 1005000) {
            return R.drawable.rank_sssp;
        } else if (achievement > 1000000) {
            return R.drawable.rank_sss;
        } else if (achievement > 995000) {
            return R.drawable.rank_ssp;
        } else if (achievement > 990000) {
            return R.drawable.rank_ss;
        } else if (achievement > 980000) {
            return R.drawable.rank_sp;
        } else if (achievement > 970000) {
            return R.drawable.rank_s;
        } else if (achievement > 940000) {
            return R.drawable.rank_aaa;
        } else if (achievement > 900000) {
            return R.drawable.rank_aaa;
        } else if (achievement > 800000) {
            return R.drawable.rank_a;
        }
        return R.drawable.rank_bbb;
    }

    @Override
    public int getItemCount() {
        return musicRatings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView musicName;
        TextView level;
        TextView ach;
        ImageView backgroundLayout;
        ImageView achievementImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            musicName = itemView.findViewById(R.id.musicName);
            level = itemView.findViewById(R.id.level);
            ach = itemView.findViewById(R.id.ach);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            achievementImage = itemView.findViewById(R.id.achievementImage);
        }
    }
}
