package org.astral.findmaimaiultra.adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
    private SparseArray<Bitmap> bitmapCache = new SparseArray<>();

    public void clearLoad() {
        loading.clear();
    }

    public PhotoAdapter(Context context, List<String> imageUrls, List<Integer> nums, Album a) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.nums = nums;
        this.album = a;
    }
    public void updateItem(int position) {
        if (bitmapCache.get(position) != null) {
            notifyItemChanged(position); // 如果已经有 bitmap，直接刷新
        } else {
            String fileName = "image_" + album.getAlbum_id() + "_" + position + ".jpg";
            File cacheFile = FileUtils.getCacheDir(context, fileName);
            if (cacheFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
                if (bitmap != null) {
                    bitmapCache.put(position, bitmap);
                }
            }
            notifyItemChanged(position);
        }
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

        // 先检查内存缓存
        Bitmap cachedBitmap = bitmapCache.get(position);
        if (cachedBitmap != null) {
            Log.d("PhotoAdapter", "Displaying from memory cache: " + position);
            holder.imageView.setImageBitmap(cachedBitmap);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            holder.imageView.setOnLongClickListener(v -> {
                saveImageToMediaStore(cachedBitmap, fileName);
                return true;
            });
            return;
        }

        // 然后检查磁盘缓存
        if (cacheFile.exists()) {
            Log.d("PhotoAdapter", "Loading cached image at position: " + position);
            Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
            if (bitmap != null) {
                bitmapCache.put(position, bitmap);
                holder.imageView.setImageBitmap(bitmap);
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                holder.imageView.setOnLongClickListener(v -> {
                    saveImageToMediaStore(bitmap, fileName);
                    return true;
                });
            } else {
                //holder.imageView.setImageResource(R.drawable.loading);
            }
        } else {
            //holder.imageView.setImageResource(R.drawable.loading);
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
        // 清空内存缓存
        if (bitmapCache != null) {
            for (int i = 0; i < bitmapCache.size(); i++) {
                bitmapCache.remove(i);
            }
            bitmapCache.clear();
        }

        // 删除磁盘缓存文件夹
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
