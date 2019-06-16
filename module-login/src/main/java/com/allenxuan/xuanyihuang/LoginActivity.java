package com.allenxuan.xuanyihuang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import com.allenxuan.xuanyihuang.api.imageloader.IImageLoader;
import com.allenxuan.xuanyihuang.bridge.core.Bridge;
import com.allenxuan.xuanyihuang.module_login.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView imageView = findViewById(R.id.image_view);
        Bridge.getImpl(IImageLoader.class).load(imageView);
    }
}
