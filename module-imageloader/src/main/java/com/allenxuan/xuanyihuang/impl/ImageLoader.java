package com.allenxuan.xuanyihuang.impl;

import android.content.Context;
import android.widget.ImageView;
import android.widget.Toast;
import com.allenxuan.xuanyihuang.api.imageloader.IImageLoader;
import com.allenxuan.xuanyihuang.bridge.annotation.InterfaceTarget;

@InterfaceTarget(Interface = IImageLoader.class)
public class ImageLoader implements IImageLoader {
    @Override
    public void load(ImageView imageView) {
        Context context = imageView.getContext();
        if(context != null) {
            Toast.makeText(context, "load image", Toast.LENGTH_SHORT).show();
        }
        imageView.setImageResource(android.R.color.black);
    }
}
