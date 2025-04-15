package org.astral.findmaimaiultra.adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import org.astral.findmaimaiultra.R;
import org.astral.findmaimaiultra.been.pixiv.jm.Album;
import org.astral.findmaimaiultra.utill.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private Context context;
    private Album album;
    private List<String> imageUrls;
    private List<Integer> nums;
    private static List<Integer> loading = new ArrayList<>();

    public void clearLoad() {
        loading.clear();
    }

    public PhotoAdapter(Context context, List<String> imageUrls, List<Integer> nums, Album a) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.nums = nums;
        this.album = a;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String imageUrl = imageUrls.get(position);
        int num = nums.get(position);
        String fileName = "image_" + album.getAlbum_id() + "_" + position + ".jpg";
        File cacheFile = FileUtils.getCacheDir(context, fileName);
        // 清除之前的图片和状态
        holder.imageView.setImageBitmap(null);
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.imageView.setOnLongClickListener(null);


        Log.d("HHHHHHHHHH", "Loading image at position: " + loading.toString());
        if (cacheFile.exists()) {
            Log.d("HHHHHHHHHH", "Loading cached image at position: " + position);
            // 加载缓存的图片并压缩到屏幕大小
            Glide.with(context)
                    .asBitmap()
                    .load(cacheFile)
                    .override(holder.itemView.getWidth(), holder.itemView.getHeight()) // 压缩图片到屏幕大小
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            Log.d("PhotoAdapter", "Image loaded successfully at position: " + position);
                            if (!loading.contains(position)) {
                                loading.add(position);
                            }
                            holder.imageView.setImageBitmap(resource);
                            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            // 设置长按监听器
                            holder.imageView.setOnLongClickListener(v -> {
                                saveImageToMediaStore(resource, fileName);
                                return true;
                            });
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            Log.d("PhotoAdapter", "Image load cleared at position: " + position);
                        }

                        @Override
                        public void onLoadFailed(Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Log.e("PhotoAdapter", "Image load failed at position: " + position);
                        }
                    });
        } else if ((!loading.contains(position))) {
            // 从网络加载并处理图片
            Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .override(holder.itemView.getWidth(), holder.itemView.getHeight()) // 压缩图片到屏幕大小
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            Log.d("PhotoAdapter", "Image loaded successfully at position: " + position);
                            if (!loading.contains(position)) {
                                loading.add(position);
                            }

                            Bitmap decodedBitmap = decodeImage(resource, num);
                            holder.imageView.setImageBitmap(decodedBitmap);
                            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            // 保存处理后的图片到缓存
                            saveBitmapToCache(decodedBitmap, cacheFile);

                            // 设置长按监听器
                            holder.imageView.setOnLongClickListener(v -> {
                                saveImageToMediaStore(decodedBitmap, fileName);
                                return true;
                            });
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            Log.d("PhotoAdapter", "Image load cleared at position: " + position);
                        }

                        @Override
                        public void onLoadFailed(Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Log.e("PhotoAdapter", "Image load failed at position: " + position);
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo);
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

    private void saveBitmapToCache(Bitmap bitmap, File file) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
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

    private void saveImageToMediaStore(Bitmap bitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FindMaimaiUltra");

        Uri uri = null;
        OutputStream outputStream = null;

        try {
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                    Toast.makeText(context, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (uri != null) {
                context.getContentResolver().delete(uri, null, null);
            }
            Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show();
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

    // 添加删除缓存方法
    public void clearCache() {
        File cacheDir = FileUtils.getCacheDir(context, "");
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }
}
