package org.astral.findmaimaiultra.ui.music;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.adapter.MusicRatingAdapter;
import org.astral.findmaimaiultra.been.faker.MaiUser;
import org.astral.findmaimaiultra.been.faker.MusicRating;
import org.astral.findmaimaiultra.been.faker.UserMusicList;
import org.astral.findmaimaiultra.databinding.FragmentMusicBinding;
import org.astral.findmaimaiultra.ui.MainActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MusicFragment extends Fragment {
    private FragmentMusicBinding binding;
    private SharedPreferences setting;
    private SharedPreferences scorePrefs;
    private RecyclerView recyclerView;
    private MusicRatingAdapter adapter;
    private List<UserMusicList> musicSongsRatings;
    private List<MusicRating> musicRatings = new ArrayList<>();
    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取 SharedPreferences 实例
        setting = requireContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        scorePrefs = requireContext().getSharedPreferences("score", Context.MODE_PRIVATE);
        userId = setting.getString("userId", "未知");
        // 读取音乐评分列表
        musicSongsRatings = loadMusicRatings();
        int totalMusicRatings = 0;
        for (UserMusicList musicSongsRating : musicSongsRatings) {
            musicRatings.addAll(musicSongsRating.getUserMusicDetailList());
            for (MusicRating musicRating : musicSongsRating.getUserMusicDetailList()) {
                totalMusicRatings += musicRating.getRating();
            }
        }
        // 假设这里填充了音乐评分数据
        if (musicRatings.isEmpty()) {
            MusicRating empty = new MusicRating();
            empty.setMusicName("空-请去导入成绩");
            musicRatings.add(empty);
        }else {
            Toolbar toolbar = ((MainActivity) requireActivity()).findViewById(R.id.toolbar);
            toolbar.setTitle("歌曲成绩 - 总共" + musicRatings.size() + "首");
            Toast.makeText(getContext(), "总共" + musicRatings.size() + "首,有效rating:" + totalMusicRatings, Toast.LENGTH_LONG).show();
        }
    }

    private void saveMusicRatings(List<UserMusicList> musicRatings) {
        Gson gson = new Gson();
        String json = gson.toJson(musicRatings);
        SharedPreferences.Editor editor = scorePrefs.edit();
        editor.putString("musicRatings", json);
        editor.apply();
    }

    private List<UserMusicList> loadMusicRatings() {
        Gson gson = new Gson();
        String json = scorePrefs.getString("musicRatings", null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<UserMusicList>>() {}.getType();
        return gson.fromJson(json, type);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MusicViewModel musicViewModel =
                new ViewModelProvider(this).get(MusicViewModel.class);

        binding = FragmentMusicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.getRoot().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 一行显示两个
        adapter = new MusicRatingAdapter(musicRatings);
        adapter.setOnItemClickListener(musicRating -> {
            showMusicDetailDialog(musicRating);
        });
        recyclerView.setAdapter(adapter);
        FloatingActionButton f = binding.fab;
        f.setOnClickListener(view -> {
            showOptionsDialog();
        });
        return root;
    }

    private void updateScores() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://mai.godserver.cn:11451/api/qq/getAAALLL?qq=" + userId;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "");
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    MaiUser maiUser = new Gson().fromJson(json, MaiUser.class);
                    saveMusicRatings(maiUser.getUserMusicList());
                    requireActivity().runOnUiThread(() -> {
                        musicRatings.clear();
                        for (UserMusicList musicSongsRating : maiUser.getUserMusicList()) {
                            musicRatings.addAll(musicSongsRating.getUserMusicDetailList());
                        }
                        adapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 处理失败情况
            }
        });
    }

    private void showMusicDetailDialog(MusicRating musicRating) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.music_dialog, null);
        builder.setView(dialogView);

        ImageView musicImageView = dialogView.findViewById(R.id.dialog_music_image);
        TextView musicNameTextView = dialogView.findViewById(R.id.dialog_music_name);
        TextView musicAchievementTextView = dialogView.findViewById(R.id.dialog_music_achievement);
        TextView musicRatingTextView = dialogView.findViewById(R.id.dialog_music_rating);
        TextView musicLevelInfoTextView = dialogView.findViewById(R.id.dialog_music_level_info);
        ImageView musicTypeImageView = dialogView.findViewById(R.id.dialog_music_type);
        ImageView musicComboStatusTextView = dialogView.findViewById(R.id.dialog_music_combo_status);
        TextView musicPlayCountTextView = dialogView.findViewById(R.id.dialog_music_play_count);
        if (musicRating.getMusicName().equals("空-请去导入成绩")) {
            return;
        }
        // 设置图像曲绘
        int id = musicRating.getMusicId();
        if (id > 10000) {
            id = id - 10000;
        }
        String imageUrl = "https://assets2.lxns.net/maimai/jacket/" + id + ".png";
        Glide.with(this)
                .load(imageUrl)
                .into(musicImageView);

        // 设置详细数据
        musicNameTextView.setText(musicRating.getMusicName());
        String ac = String.valueOf(musicRating.getAchievement());
        if (ac.length() > 4) {
            ac = ac.substring(0, ac.length() - 4) + "." + ac.substring(ac.length() - 4);
        }
        musicAchievementTextView.setText("达成率: " + ac);
        musicRatingTextView.setText("Rating " + String.valueOf(musicRating.getRating()));
        musicLevelInfoTextView.setText("Level " + String.valueOf(musicRating.getLevel_info()));

        // 设置 musicTypeImageView 的图片并等比例缩小到 75dp
        int targetWidth = (int) (75 * getResources().getDisplayMetrics().density); // 75dp 转换为像素
        RequestOptions requestOptions = new RequestOptions()
                .override(targetWidth, targetWidth) // 设置宽度和高度为 75dp 对应的像素值
                .centerInside(); // 确保图片等比例缩放

        if (musicRating.getType().equals("dx")) {
            Glide.with(this)
                    .load(R.drawable.dx)
                    .apply(requestOptions)
                    .into(musicTypeImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.sd)
                    .apply(requestOptions)
                    .into(musicTypeImageView);
        }

        int comboType = musicRating.getComboStatus();
        if (comboType == 1) {
            Glide.with(this)
                    .load(R.drawable.fc)
                    .apply(requestOptions)
                    .into(musicComboStatusTextView);
        } else if (comboType == 2) {
            Glide.with(this)
                    .load(R.drawable.fcp)
                    .apply(requestOptions)
                    .into(musicComboStatusTextView);
        } else if (comboType == 3) {
            Glide.with(this)
                    .load(R.drawable.ap)
                    .apply(requestOptions)
                    .into(musicComboStatusTextView);
        } else if (comboType == 4) {
            Glide.with(this)
                    .load(R.drawable.app)
                    .apply(requestOptions)
                    .into(musicComboStatusTextView);
        }

        musicPlayCountTextView.setText("PC: "+ String.valueOf(musicRating.getPlayCount()));

        builder.setPositiveButton("确定", (dialog, which) -> {
            // 点击确定按钮后的操作
            // 例如：关闭对话框
            dialog.dismiss();
        });

        builder.setNegativeButton("取消", (dialog, which) -> {
            // 点击取消按钮后的操作
            // 例如：关闭对话框
            dialog.dismiss();
        });

        builder.show();
    }

    private void showOptionsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle);
        builder.setTitle("选项");
        builder.setItems(new CharSequence[]{"更新数据", "分数排序", "搜索指定歌曲"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    // 更新数据
                    if (userId.equals("未知")) {
                        new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle)
                                .setMessage("请先绑定机器人")
                                .setPositiveButton("确定", (d, w) -> d.dismiss())
                                .show();
                    } else {
                        new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle)
                                .setMessage("是否更新?")
                                .setPositiveButton("确定", (d, w) -> {
                                    updateScores();
                                    d.dismiss();
                                })
                                .setNegativeButton("cancel", (d, w) -> d.dismiss())
                                .show();
                    }
                    break;
                case 1:
                    // 分数排序
                    sortMusicRatingsByRating();
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    // 搜索指定歌曲
                    showSearchDialog();
                    break;
            }
        });
        builder.show();
    }

    private void sortMusicRatingsByRating() {
        Collections.sort(musicRatings, new Comparator<MusicRating>() {
            @Override
            public int compare(MusicRating o1, MusicRating o2) {
                return Integer.compare(o2.getRating(), o1.getRating()); // 降序排序
            }
        });
    }

    private void showSearchDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.search_dialog, null);
        builder.setView(dialogView);

        TextView searchInput = dialogView.findViewById(R.id.search_input);
        builder.setPositiveButton("搜索", (dialog, which) -> {
            String query = searchInput.getText().toString().trim();
            searchMusicRatings(query);
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void searchMusicRatings(String query) {
        List<MusicRating> filteredList = new ArrayList<>();
        for (MusicRating musicRating : musicRatings) {
            if (musicRating.getMusicName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(musicRating);
            }
        }
        musicRatings.clear();
        musicRatings.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}
