package org.astral.findmaimaiultra.config;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // 设置内存缓存大小
        MemorySizeCalculator.Builder calculator = new MemorySizeCalculator.Builder(context);
        int defaultMemoryCacheSizeBytes = calculator.build().getMemoryCacheSize();
        int customMemoryCacheSizeBytes = defaultMemoryCacheSizeBytes * 2; // 例如，设置为默认值的两倍
        builder.setMemoryCache(new LruResourceCache(customMemoryCacheSizeBytes));

        // 设置磁盘缓存大小
        int diskCacheSizeBytes = 1024 * 1024 * 1024; // 1GB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // 注册组件
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
