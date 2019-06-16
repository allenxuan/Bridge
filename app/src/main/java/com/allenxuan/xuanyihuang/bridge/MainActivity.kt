package com.allenxuan.xuanyihuang.bridge

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.allenxuan.xuanyihuang.api.imageloader.IImageLoader
import com.allenxuan.xuanyihuang.api.login.ILoginService
import com.allenxuan.xuanyihuang.bridge.core.Bridge

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    private fun initViews(){
        findViewById<View>(R.id.login_button)?.setOnClickListener {
            Bridge.getImpl(ILoginService::class.java)?.toLoginActivity(this)
        }
    }
}
