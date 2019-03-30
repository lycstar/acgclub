package com.luyucheng.acgclub.glide;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
//    @Override
//    public void applyOptions(@NonNull Context context, GlideBuilder builder) {
//        //        设置缓存大小为20mb
//        int memoryCacheSizeBytes = 1024 * 1024 * 20; // 20mb
//        //        设置内存缓存大小
//        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
//    }

    //    针对V4用户可以提升速度
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
