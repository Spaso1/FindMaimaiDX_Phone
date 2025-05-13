package org.astral.findmaimaiultra.ui.music;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.adapter.MusicRatingAdapter;
import org.astral.findmaimaiultra.adapter.SongAdapter;
import org.astral.findmaimaiultra.adapter.SuggestMusicRatingAdapter;
import org.astral.findmaimaiultra.been.faker.MaiUser;
import org.astral.findmaimaiultra.been.faker.MusicRating;
import org.astral.findmaimaiultra.been.faker.UserMusicList;
import org.astral.findmaimaiultra.been.lx.Song;
import org.astral.findmaimaiultra.databinding.FragmentMusicBinding;
import org.astral.findmaimaiultra.ui.login.LinkQQBot;
import org.astral.findmaimaiultra.ui.MainActivity;
import org.astral.findmaimaiultra.utill.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicFragment extends Fragment {
    private FragmentMusicBinding binding;
    private SharedPreferences setting;
    private SharedPreferences scorePrefs;
    private RecyclerView recyclerView;
    private MusicRatingAdapter adapter;
    private SuggestMusicRatingAdapter adapterSuggest;
    private DataAnalyzer dataAnalyzer;
    private List<UserMusicList> musicSongsRatings;
    private List<MusicRating> musicRatings = new ArrayList<>();
    private String userId;
    private int iconId;
    private String username;
    private static Map<Integer, Song> songs = new HashMap<>();
    private Map<String, Song> loadedSongs;
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
        SharedPreferences settingProperties = requireActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        username = settingProperties.getString("paikaname", "");
        if (settingProperties.contains("userName")) {
            username = settingProperties.getString("userName", "");
            SharedPreferences.Editor  editorM = setting.edit();
            editorM.putString("paikaname",username);
            editorM.commit();
        }

        iconId = settingProperties.getInt("iconId", 0);

        binding = FragmentMusicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.getRoot().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 一行显示两个

        if (setting.getString("image_uri", null) != null ) {
            try {
                File backgroundFile = FileUtils.getBackground(requireContext(), "background.jpg");

                if (!backgroundFile.exists()) {
                    Toast.makeText(requireContext(), "文件不存在，请先设置背景图片", Toast.LENGTH_SHORT).show();
                    return root;
                }

                Bitmap bitmap = BitmapFactory.decodeFile(backgroundFile.getAbsolutePath());

                if (bitmap != null) {
                    // 获取RecyclerView的尺寸
                    int recyclerViewWidth = 0;
                    int recyclerViewHeight = 0;
                    recyclerViewWidth = recyclerView.getWidth();
                    recyclerViewHeight = recyclerView.getHeight();
                    if (recyclerViewWidth > 0 && recyclerViewHeight > 0) {
                        // 计算缩放比例
                        float scaleWidth = ((float) recyclerViewWidth) / bitmap.getWidth();
                        float scaleHeight = ((float) recyclerViewHeight) / bitmap.getHeight();

                        // 选择较大的缩放比例以保持图片的原始比例
                        float scaleFactor = Math.max(scaleWidth, scaleHeight);

                        // 计算新的宽度和高度
                        int newWidth = (int) (bitmap.getWidth() * scaleFactor);
                        int newHeight = (int) (bitmap.getHeight() * scaleFactor);

                        // 缩放图片
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                        // 计算裁剪区域
                        int x = (scaledBitmap.getWidth() - recyclerViewWidth) / 2;
                        int y = (scaledBitmap.getHeight() - recyclerViewHeight) / 2;

                        // 处理x和y为负数的情况
                        x = Math.max(x, 0);
                        y = Math.max(y, 0);

                        // 裁剪图片
                        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, x, y, recyclerViewWidth, recyclerViewHeight);

                        // 创建一个新的 Bitmap，与裁剪后的 Bitmap 大小相同
                        Bitmap transparentBitmap = Bitmap.createBitmap(croppedBitmap.getWidth(), croppedBitmap.getHeight(), croppedBitmap.getConfig());

                        // 创建一个 Canvas 对象，用于在新的 Bitmap 上绘制
                        Canvas canvas = new Canvas(transparentBitmap);

                        // 创建一个 Paint 对象，并设置透明度
                        Paint paint = new Paint();
                        paint.setAlpha(128); // 设置透明度为 50% (255 * 0.5 = 128)

                        // 将裁剪后的 Bitmap 绘制到新的 Bitmap 上，并应用透明度
                        canvas.drawBitmap(croppedBitmap, 0, 0, paint);

                        // 创建BitmapDrawable并设置其边界为RecyclerView的尺寸
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), transparentBitmap);

                        // 设置recyclerView的背景
                        recyclerView.setBackground(bitmapDrawable);

                    } else {
                        // 如果RecyclerView的尺寸未确定，可以使用ViewTreeObserver来监听尺寸变化
                        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                int recyclerViewWidth = 0;
                                int recyclerViewHeight = 0;
                                recyclerViewWidth = recyclerView.getWidth();
                                recyclerViewHeight = recyclerView.getHeight();

                                // 计算缩放比例
                                float scaleWidth = ((float) recyclerViewWidth) / bitmap.getWidth();
                                float scaleHeight = ((float) recyclerViewHeight) / bitmap.getHeight();

                                // 选择较大的缩放比例以保持图片的原始比例
                                float scaleFactor = Math.max(scaleWidth, scaleHeight);

                                // 计算新的宽度和高度
                                int newWidth = (int) (bitmap.getWidth() * scaleFactor);
                                int newHeight = (int) (bitmap.getHeight() * scaleFactor);

                                // 缩放图片
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                                // 计算裁剪区域
                                int x = (scaledBitmap.getWidth() - recyclerViewWidth) / 2;
                                int y = (scaledBitmap.getHeight() - recyclerViewHeight) / 2;

                                // 处理x和y为负数的情况
                                x = Math.max(x, 0);
                                y = Math.max(y, 0);

                                // 裁剪图片
                                Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, x, y, recyclerViewWidth, recyclerViewHeight);

                                // 创建一个新的 Bitmap，与裁剪后的 Bitmap 大小相同
                                Bitmap transparentBitmap = Bitmap.createBitmap(croppedBitmap.getWidth(), croppedBitmap.getHeight(), croppedBitmap.getConfig());

                                // 创建一个 Canvas 对象，用于在新的 Bitmap 上绘制
                                Canvas canvas = new Canvas(transparentBitmap);

                                // 创建一个 Paint 对象，并设置透明度
                                Paint paint = new Paint();
                                paint.setAlpha(128); // 设置透明度为 50% (255 * 0.5 = 128)

                                // 将裁剪后的 Bitmap 绘制到新的 Bitmap 上，并应用透明度
                                canvas.drawBitmap(croppedBitmap, 0, 0, paint);

                                // 创建BitmapDrawable并设置其边界为RecyclerView的尺寸
                                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), transparentBitmap);
                                recyclerView.setBackground(bitmapDrawable);

                            }
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(requireContext(), "图片加载失败,权限出错!", Toast.LENGTH_SHORT).show();
            }
        }



        adapter = new MusicRatingAdapter(musicRatings);
        adapter.setOnItemClickListener(musicRating -> {
            showMusicDetailDialog(musicRating);
        });

        recyclerView.setAdapter(adapter);
        FloatingActionButton f = binding.fab;
        f.setOnClickListener(view -> {
            showOptionsDialog();
        });

        ImageView user_avatar = binding.useravatar ;
        Glide.with(this)
                .load("https://assets2.lxns.net/maimai/icon/" + iconId +".png")
                .into(user_avatar);
        TextView user_name = binding.username;
        user_name.setText(username);
        if (!(iconId==0)){
            MaterialButton login = binding.login;
            login.setVisibility(View.GONE);
            Intent loginIntent = new Intent(getActivity(), LinkQQBot.class);
            login.setOnClickListener(view -> {
                startActivity(loginIntent);
            });
        }else{
            MaterialButton login = binding.login;
            Intent loginIntent = new Intent(getActivity(), LinkQQBot.class);
            login.setOnClickListener(view -> {
                startActivity(loginIntent);
            });
        }
        dataanlysis();
        return root;
    }

    private void updateScores() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://mais.godserver.cn/api/qq/getAAALLL?qq=" + userId;
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
        builder.setItems(new CharSequence[]{"更新数据", "分数排序", "搜索指定歌曲","推分安排"}, (dialog, which) -> {
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
                case 3:
                    //推分安排
                    setRatingProject();
            }
        });
        builder.show();
    }

    private void setRatingProject() {
        // 创建弹窗
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.rating_project_dialog, null);
        builder.setView(dialogView);

        // 初始化搜索框和列表
        TextInputEditText searchInput = dialogView.findViewById(R.id.search_input);
        RecyclerView songList = dialogView.findViewById(R.id.song_list);
        songList.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 初始化适配器
        List<Song> filteredList = new ArrayList<>(songs.values());
        SongAdapter adapter = new SongAdapter(filteredList);
        songList.setAdapter(adapter);

        // 搜索框监听
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                filteredList.clear();
                for (Song song : songs.values()) {
                    if (song.getTitle().toLowerCase().contains(query) ||
                            String.valueOf(song.getId()).contains(query) ||
                            song.getArtist().toLowerCase().contains(query) ||
                            song.getGenre().toLowerCase().contains(query)) {
                        filteredList.add(song);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 列表项点击事件
        adapter.setOnItemClickListener(song -> {
            showSongDetailDialog(song);
        });

        builder.setPositiveButton("关闭", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showSongDetailDialog(Song song) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_song_detail, null);
        builder.setView(dialogView);

        // 设置歌曲详情
        TextView songTitle = dialogView.findViewById(R.id.song_title);
        TextView songArtist = dialogView.findViewById(R.id.song_artist);
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());

        // 初始化表格
        TableLayout tableLayout = dialogView.findViewById(R.id.song_table);
        for (int i = 0; i < 4; i++) { // 4 行
            TableRow row = new TableRow(requireContext());
            for (int j = 0; j < 5; j++) { // 5 列
                TextView cell = new TextView(requireContext());
                cell.setText("数据 " + (i * 5 + j + 1)); // 示例数据
                cell.setPadding(8, 8, 8, 8);
                row.addView(cell);
            }
            tableLayout.addView(row);
        }

        // 设置额外的 TextView
        TextView extraInfo = dialogView.findViewById(R.id.extra_info);
        extraInfo.setText("额外信息：这里可以显示更多内容");

        // 设置按钮点击事件
        MaterialButton addToPlanButton = dialogView.findViewById(R.id.add_to_plan_button);
        addToPlanButton.setOnClickListener(v -> {
            // 将歌曲添加到计划中
            addToPlan(song);
            Toast.makeText(requireContext(), "已添加到计划", Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("关闭", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addToPlan(Song song) {
        // 实现将歌曲添加到计划的逻辑
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
    private void dataanlysis() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        TextView au = binding.an;
        executor.execute(() -> {
            // Perform data processing in the background
            dataAnalyzer = new DataAnalyzer();


            try {
                InputStream inputStream = requireContext().getAssets().open("musicLike.json");
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                String json = new String(buffer, StandardCharsets.UTF_8);
                dataAnalyzer = new Gson().fromJson(json,DataAnalyzer.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                InputStream inputStream = requireContext().getAssets().open("songs_cache.json");
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                String json = new String(buffer, StandardCharsets.UTF_8);
                Type type = new TypeToken<Map<String, Song>>() {}.getType();
                loadedSongs = new Gson().fromJson(json, type);

                // 手动转换 key 为 Integer 并放入全局 map
                for (Map.Entry<String, Song> entry : loadedSongs.entrySet()) {
                    Integer id = Integer.parseInt(entry.getKey());
                    songs.put(id, entry.getValue());
                    songs.put(id + 10000, entry.getValue()); // 如果你需要 standard 版本
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Collections.sort(musicRatings, (o1, o2) -> Integer.compare(o2.getRating(), o1.getRating()));
            int total = 0;
            List<MusicRating> b50 = musicRatings.subList(0, 50);
            List<MusicRating> b100 = musicRatings.subList(50, 100);
            List<Integer> ids= new ArrayList<>();
            int worstRating = b50.get(b50.size() - 1).getRating();
            int bestRating = b50.get(0).getRating();
            List<MusicRating> suggestMusicRatingList = new ArrayList<>();
            for (int x = 0; x < 50; x++) {
                total += b50.get(x).getRating();
                MusicRating m = b100.get(x);
                int nowache = m.getAchievement();
                int target = 0;
                if (nowache>=1005000) {
                    continue;
                }
                if (nowache >= 1000000 && nowache < 1005000) {
                    target = 1005001;
                } else if (nowache >= 995000 && nowache < 1000000) {
                    target = 1000001;
                } else if (nowache >= 990000 && nowache < 995000) {
                    target = 995001;
                } else if (nowache >= 980000 && nowache < 990000) {
                    target = 990001;
                } else if (nowache >= 970000 && nowache < 980000) {
                    target = 980001;
                }
                double b1 = (double) target / 10000;
                int targetRating = getRatingChart(m.getLevel_info(), b1);
                if (targetRating > worstRating) {
                    m.setExtNum1(target);
                    if (m.getMusicId() > 10000) {
                        ids.add(m.getMusicId()-10000);
                    }else {
                        ids.add(m.getMusicId());
                    }
                    m.setExtNum2(targetRating);
                    suggestMusicRatingList.add(m);
                }
            }
            for (int x = 0; x < 50; x++) {
                MusicRating m = b50.get(x);
                if (m.getLevel_info() > ((double) (total - 1000) / 1000)) {
                    continue;
                }
                int nowache = m.getAchievement();
                int target = 0;
                if (nowache>=1005000) {
                    continue;
                }
                if (nowache >= 1000000 && nowache < 1005000) {
                    target = 1005001;
                } else if (nowache >= 995000 && nowache < 1000000) {
                    target = 1000001;
                } else if (nowache >= 990000 && nowache < 995000) {
                    target = 995001;
                } else if (nowache >= 980000 && nowache < 990000) {
                    target = 990001;
                } else if (nowache >= 970000 && nowache < 980000) {
                    target = 980001;
                }
                double b1 = (double) target / 10000;
                int targetRating = getRatingChart(m.getLevel_info(), b1);
                if (targetRating > worstRating) {
                    m.setExtNum1(target);
                    if (m.getMusicId() > 10000) {
                        ids.add(m.getMusicId()-10000);
                    }else {
                        ids.add(m.getMusicId());
                    }
                    m.setExtNum2(targetRating);
                    suggestMusicRatingList.add(m);
                }
            }

            List<EasySong> easySongs = new ArrayList<>();
            //看看别人打什么
            Log.d("TOP",total + "");
            if (total>=16000) {
                easySongs = dataAnalyzer.getEasySongs().get("16000");
            }else if (total>=15500) {
                easySongs = dataAnalyzer.getEasySongs().get("15500");
            }else if (total>=15000) {
                easySongs = dataAnalyzer.getEasySongs().get("15000");
            }else if (total>=14500) {
                easySongs = dataAnalyzer.getEasySongs().get("14500");
            }else if (total>=14000) {
                easySongs = dataAnalyzer.getEasySongs().get("14000");
            }else if (total>=13000) {
                easySongs = dataAnalyzer.getEasySongs().get("13000");
            }else if (total>=12000) {
                easySongs = dataAnalyzer.getEasySongs().get("12000");
            }else if (total>=11000) {
                easySongs = dataAnalyzer.getEasySongs().get("11000");
            }
            if (easySongs != null) {
                for (EasySong e : easySongs) {
                    if (e.getPercent() > 0.1) {
                        if (ids.contains(e.getId())) {
                            continue;
                        }
                        ;
                        double diff = Objects.requireNonNull(Objects.requireNonNull(songs.get(e.getId())).getDifficulties().get(e.getType()))[e.getLevel()].getLevel_value();
                        double b = 99.0000;
                        for (int i = 0; i < 3; i++) {
                            b = b + 0.5 * (i - 1);
                            int ra = getRatingChart(diff, b);
                            if (ra > worstRating) {
                                MusicRating musicRating = new MusicRating();
                                musicRating.setMusicId(e.getId());
                                musicRating.setMusicName(e.getTitle());
                                musicRating.setExtNum1((int) (b * 10000));
                                musicRating.setExtNum2(ra);
                                musicRating.setRating(0);
                                musicRating.setAchievement(0);
                                musicRating.setLevel_info(diff);
                                musicRating.setType(e.getType());
                                suggestMusicRatingList.add(musicRating);
                                break;
                            }
                        }
                    }
                }
            }


            // Update UI on the main thread
            int finalTotal = total;
            handler.post(() -> {
                RecyclerView suggest = binding.getRoot().findViewById(R.id.suggestion);
                suggest.setLayoutManager(new GridLayoutManager(getContext(), 1));

                au.setText("Rating分析:最低分 " + worstRating + "分,最高分 " + bestRating + ",平均分" + (finalTotal / 50) + "分");

                adapterSuggest = new SuggestMusicRatingAdapter(suggestMusicRatingList);
                suggest.setAdapter(adapterSuggest);
                adapterSuggest.notifyDataSetChanged();
            });
        });
    }
    public int getRatingChart(double a1, double b1) {
        double sys = 22.4;
        if (b1 >= 100.5000) {
            return (int) (a1 * 22.512);
        }
        if (b1 == 100.4999) {
            sys = 22.2;
        } else if (b1 >= 100.0000) {
            sys = 21.6;
        } else if (b1 == 99.9999) {
            sys = 21.4;
        } else if (b1 >= 99.5000) {
            sys = 21.1;
        } else if (b1 >= 99.0000) {
            sys = 20.8;
        } else if (b1 >= 98.0000) {
            sys = 20.3;
        } else if (b1 >= 97.0000) {
            sys = 20.0;
        } else {
            sys = 0;
        }
        return (int) (a1 * sys * b1 / 100);
    }
}
class DataAnalyzer {
    Map<String, List<EasySong>> easySongs = new HashMap<>();

    public Map<String, List<EasySong>> getEasySongs() {
        return easySongs;
    }

    public void setEasySongs(Map<String, List<EasySong>> easySongs) {
        this.easySongs = easySongs;
    }
}
class EasySong {
    private String title;
    private int level;
    private float percent;
    private int id;
    private String type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}