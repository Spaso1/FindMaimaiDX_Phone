package org.astral.findmaimaiultra.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.adapter.PhotoAdapter;
import org.astral.findmaimaiultra.been.pixiv.jm.Album;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class JMActivity extends AppCompatActivity {
    private FrameLayout overlay;
    private boolean isOverlayVisible = false;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private PhotoAdapter photoAdapter;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    private Album album;
    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jm_dialog);

        initRecyclerView();
    }
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n", "ResourceType"})
    private void initRecyclerView() {
        Intent intent = getIntent();
        String res = intent.getStringExtra("album");
        Album a = new Gson().fromJson(res, Album.class);
        album = a;
        Toast.makeText(this,"加载中", Toast.LENGTH_SHORT).show();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        photoAdapter = new PhotoAdapter(this, a.getImage_urls(), a.getNums(), a);
        photoAdapter.clearLoad();
        MaterialButton downloadButton = findViewById(R.id.download);
        downloadButton.setOnClickListener(v -> downloadAllImages());
        recyclerView.setAdapter(photoAdapter);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setPeekHeight(dpToPx(80));
        TextView menu = findViewById(R.id.menu);
        menu.setText(a.getName());
        TextView dec = findViewById(R.id.dec);
        dec.setText(a.getAuthors().toString().replaceAll( "\\[","").replaceAll( "]","")
                + " / " + a.getActors().toString().replaceAll( "\"","") .replaceAll( "\\[","").replaceAll( "]","")
                + " \n " + a.getTags().toString().replaceAll( "\"","") .replaceAll( "\\[","").replaceAll( "]","")
                + " \n " + a.getAlbum_id().replaceAll( "\"","").replaceAll( "\\[","").replaceAll( "]",""));
    }

    private void downloadAllImages() {
        String folderName = album.getName();
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "findmaimaiultra"), folderName);
        int totalImages = photoAdapter.getItemCount();
        int[] downloadedCount = {0}; // 使用数组来保存下载计数，以便在匿名内部类中修改

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Download started", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        for (int i = 0; i < album.getImage_urls().size(); i++) {
            String imageUrl = album.getImage_urls().get(i);
            int num = album.getNums().get(i);
            String FileName = "image_" + album.getAlbum_id() + "_" + i + ".jpg";
            File file = new File(folder, FileName);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            Bitmap decodedBitmap = decodeImage(resource, num);
                            downloadedCount[0]++;
                            updateSnackBar(snackbar, downloadedCount[0], totalImages);
                            saveBitmapToFile(decodedBitmap, file);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            downloadedCount[0]++;
                            updateSnackBar(snackbar, downloadedCount[0], totalImages);
                        }
                    });
        }
        snackbar.setText("下载完成!保存在/Download/FindMaimaiUltra/本子名称 目录");
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", v -> snackbar.dismiss());
        Snackbar.make(findViewById(android.R.id.content), "下载完成!保存在/Download/FindMaimaiUltra/本子名称 目录", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", v -> {
                    // 处理 Snackbar 按钮点击事件
                })
                .show();
    }
    private void updateSnackBar(Snackbar snackbar, int current, int total) {
        String message = "Downloaded " + current + "/" + total + " images";
        snackbar.setText(message);
        if (current == total) {
            snackbar.dismiss();
        }
    }
    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (photoAdapter != null) {
            photoAdapter.clearCache();
        }
    }
    private void saveBitmapToFile(Bitmap bitmap, File file) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private Bitmap decodeImage(Bitmap imgSrc, int num) {
        if (num == 0) {
            return imgSrc;
        }

        int w = imgSrc.getWidth();
        int h = imgSrc.getHeight();

        // 创建新的解密图片
        Bitmap imgDecode = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imgDecode);

        int over = h % num;
        for (int i = 0; i < num; i++) {
            int move = h / num;
            int ySrc = h - (move * (i + 1)) - over;
            int yDst = move * i;

            if (i == 0) {
                move += over;
            } else {
                yDst += over;
            }

            Rect srcRect = new Rect(0, ySrc, w, ySrc + move);
            Rect dstRect = new Rect(0, yDst, w, yDst + move);

            canvas.drawBitmap(imgSrc, srcRect, dstRect, null);
        }

        return imgDecode;
    }
}
