package org.astral.findmaimaiultra.ui.pixiv;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.adapter.MusicRatingAdapter;
import org.astral.findmaimaiultra.adapter.PixivAdapter;
import org.astral.findmaimaiultra.adapter.PlaceAdapter;
import org.astral.findmaimaiultra.been.*;
import org.astral.findmaimaiultra.been.pixiv.model.IllustData;
import org.astral.findmaimaiultra.been.pixiv.model.PixivResponse;
import org.astral.findmaimaiultra.been.pixiv.model.pages.PagePixivResponse;
import org.astral.findmaimaiultra.been.pixiv.model.pages.photo.Photo;
import org.astral.findmaimaiultra.been.pixiv.model.pages.photo.PhotoResponse;
import org.astral.findmaimaiultra.been.pixiv.model.pages.photo.Urls;
import org.astral.findmaimaiultra.databinding.FragmentHomeBinding;
import org.astral.findmaimaiultra.databinding.FragmentPixivBinding;
import org.astral.findmaimaiultra.ui.MainActivity;
import org.astral.findmaimaiultra.ui.PageActivity;
import org.astral.findmaimaiultra.ui.home.HomeViewModel;
import org.astral.findmaimaiultra.utill.AddressParser;
import org.astral.findmaimaiultra.utill.SharedViewModel;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PixivFragment extends Fragment {
    private RecyclerView recyclerView;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Context context;
    private boolean flag = true;
    private double tagXY[] = new double[2];
    private String tagplace;
    private boolean isFlag = true;
    private SharedPreferences shoucang;
    private SharedPreferences settingProperties;
    private SharedPreferences settingProperties2;
    private LinearLayout path1;
    private LinearLayout path2;
    private SearchView searchView;

    private FragmentPixivBinding binding;
    private SharedViewModel sharedViewModel;
    private PixivAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取 SharedPreferences 实例
        // 在 Application 类中启用 Glide 调试日志

        context = getContext();
        if (context != null) {
            shoucang = context.getSharedPreferences("shoucang_prefs", Context.MODE_PRIVATE);
            settingProperties = context.getSharedPreferences("setting_prefs", Context.MODE_PRIVATE);
            settingProperties2 = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        }
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentPixivBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.recyclerView;
        path1 = binding.path1;
        path2 = binding.path2;
        searchView = binding.searchView;

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (path2.getVisibility() == View.VISIBLE) {
                    // 如果 path2 可见，隐藏 path2 并显示 path1
                    path2.setVisibility(View.GONE);
                    path1.setVisibility(View.VISIBLE);
                } else {
                    // 否则，允许默认的返回行为
                    remove(); // 移除回调，允许默认的返回行为
                    requireActivity().onBackPressed();
                }
            }
        });

        // 示例：读取 SharedPreferences 中的数据
        if (shoucang != null) {
            String savedData = shoucang.getString("key_name", "default_value");
            // 使用 savedData
        }
        adapter = new PixivAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(illustData -> openIllustData(illustData));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        // 设置搜索框的查询监听器
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 显示 Snackbar 提示“正在搜索”
                Snackbar snackbar = Snackbar.make(root, "正在搜索...", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAnchorView(searchView); // 设置 Snackbar 锚定到搜索框
                snackbar.show();

                adapter.update(new ArrayList<>());
                adapter.notifyDataSetChanged();

                // 当用户提交查询时调用 fetchData 方法
                fetchData(query, 1, snackbar);
                // 显示搜索结果布局，隐藏 RecyclerView
                path1.setVisibility(View.GONE);
                path2.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 当用户输入查询文本时可以进行其他操作
                return false;
            }
        });

        return root;
    }

    private void fetchData(String word, int page, Snackbar snackbar) {
        OkHttpClient client = new OkHttpClient();
        //设置超时时间60s
        client.newBuilder().connectTimeout(60, TimeUnit.SECONDS);
        client.newBuilder().readTimeout(60, TimeUnit.SECONDS);
        client.newBuilder().writeTimeout(60, TimeUnit.SECONDS);
        Request request = new Request.Builder()
                .url("http://43.153.174.191:45678/api/v1/pixiv/photo?word=" + word + "&page=" + page)
                .addHeader("Auth", "1234567890qwertyuiop")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(requireContext(), "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PixivFragment", "Request failed: ", e);
                    // 隐藏 Snackbar
                    snackbar.dismiss();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body == null) {
                        throw new IOException("Response body is null");
                    }

                    try (BufferedReader reader = new BufferedReader(body.charStream())) {
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }

                        String responseData = result.toString();
                        if (responseData != null && !responseData.isEmpty()) {
                            Gson gson = new Gson();
                            PixivResponse pixivResponse = gson.fromJson(responseData, PixivResponse.class);

                            if (!pixivResponse.isError() && pixivResponse.getBody() != null && pixivResponse.getBody().getIllustManga() != null) {
                                List<IllustData> dataList = pixivResponse.getBody().getIllustManga().getData();
                                handler.post(() -> {
                                    adapter.update(dataList);
                                    adapter.notifyDataSetChanged();
                                    // 隐藏 Snackbar
                                    snackbar.dismiss();
                                });
                            } else {
                                handler.post(() -> {
                                    Toast.makeText(requireContext(), "数据解析失败", Toast.LENGTH_SHORT).show();
                                    // 隐藏 Snackbar
                                    snackbar.dismiss();
                                });
                            }
                        } else {
                            handler.post(() -> {
                                Toast.makeText(requireContext(), "响应体为空", Toast.LENGTH_SHORT).show();
                                // 隐藏 Snackbar
                                snackbar.dismiss();
                            });
                        }
                    } catch (EOFException e) {
                        // 捕获 EOFException 并记录日志
                        e.printStackTrace();
                        handler.post(() -> {
                            Toast.makeText(requireContext(), "数据读取错误", Toast.LENGTH_SHORT).show();
                            // 隐藏 Snackbar
                            snackbar.dismiss();
                        });
                    }
                } else {
                    handler.post(() -> {
                        Toast.makeText(requireContext(), "请求失败: " + response.code(), Toast.LENGTH_SHORT).show();
                        // 隐藏 Snackbar
                        snackbar.dismiss();
                    });
                }
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    private void openIllustData(IllustData illustData) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(illustData.getTitle());
        Log.d("PixivFragment", "openIllustData: " + illustData.getTitle());
        // 创建对话框
        AlertDialog dialog = builder.create();
        // 使用 pixiv_dialog
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.pixiv_dialog, null);
        dialog.setView(view);
        int id = Integer.parseInt(illustData.getId());
        LinearLayout li = view.findViewById(R.id.li);
        // 设置对话框的尺寸
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);
            window.setAttributes(layoutParams);
        }
        MaterialButton share = view.findViewById(R.id.share);
        share.setOnClickListener(v -> {
            // 获取要打开的链接
            String url = "http://tokyo.1.godserver.cn:45678/api/v1/pixiv/photos?id=" + id; // 假设 getShareUrl() 返回要打开的链接
            Log.d("PixivFragment12121212", "openIllustData: " + url);
            download(url);
        });

        // 设置动画效果
        if (window != null) {
            window.setWindowAnimations(R.style.DialogAnimation);
        }

        OkHttpClient client = new OkHttpClient();
        client.newBuilder().connectTimeout(60, TimeUnit.SECONDS);
        client.newBuilder().readTimeout(60, TimeUnit.SECONDS);
        client.newBuilder().writeTimeout(60, TimeUnit.SECONDS);

        Request request = new Request.Builder()
                .url("http://tokyo.1.godserver.cn:45678/api/v1/pixiv/pageId?id=" + id)
                .addHeader("Auth", "1234567890qwertyuiop")
                .build();
        Request request2 = new Request.Builder()
                .url("http://tokyo.1.godserver.cn:45678/api/v1/pixiv/photoId?id=" + id)
                .addHeader("Auth", "1234567890qwertyuiop")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                Log.d("PixivFragment111111111", "onResponse: " + res);
                if (response.isSuccessful()) {
                    PagePixivResponse photoResponse = new Gson().fromJson(res, PagePixivResponse.class);
                    TextView user = view.findViewById(R.id.user);
                    user.setText(photoResponse.getBody().getUserName());
                    TextView des = view.findViewById(R.id.des);
                    des.setText(photoResponse.getBody().getDescription());

                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(requireContext(), "图片加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
        client.newCall(request2).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                if (response.isSuccessful()) {
                    PhotoResponse photoResponse = new Gson().fromJson(res, PhotoResponse.class);
                    for (Photo photo : photoResponse.getBody()) {
                        ImageView photoView = new ImageView(getContext());
                        String url = "http://43.153.174.191:45678/api/v1/pixiv/photoUrl?href=" + photo.getUrls().getSmall();
                        loadImage(photoView, url,li);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
        // 设置对话框的视图
        builder.setView(view);

        // 显示对话框
        dialog.show();
    }
    private void download(String url) {
        //复制
        ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", url);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(requireContext(), "图片链接已经复制!", Toast.LENGTH_SHORT).show();

        //Intent打开链接
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "无法打开链接", Toast.LENGTH_SHORT).show();
        }
    }
    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(60, TimeUnit.SECONDS)    // 读取超时时间
                .writeTimeout(60, TimeUnit.SECONDS)   // 写入超时时间
                .build();
    }
    private void loadImage(ImageView imageView, String url, LinearLayout li) {
        // 创建自定义的 OkHttpClient
        OkHttpClient okHttpClient = createOkHttpClient();

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .build();

        // 显示加载中的 Snackbar
        Snackbar snackbar = Snackbar.make(requireView(), "加载中", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();

        // 发送请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(requireContext(), "图片加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    snackbar.dismiss();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        handler.post(() -> {
                            imageView.setImageBitmap(bitmap);
                            li.addView(imageView);
                            snackbar.dismiss();
                        });
                    } else {
                        handler.post(() -> {
                            Toast.makeText(requireContext(), "图片加载失败", Toast.LENGTH_SHORT).show();
                            snackbar.dismiss();
                        });
                    }
                } else {
                    handler.post(() -> {
                        Toast.makeText(requireContext(), "请求失败: " + response.code(), Toast.LENGTH_SHORT).show();
                        snackbar.dismiss();
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
