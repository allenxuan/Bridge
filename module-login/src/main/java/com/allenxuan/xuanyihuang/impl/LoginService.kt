package com.allenxuan.xuanyihuang.impl

import android.content.Context
import android.content.Intent
import com.allenxuan.xuanyihuang.LoginActivity
import com.allenxuan.xuanyihuang.api.login.ILoginService
import com.allenxuan.xuanyihuang.bridge.annotation.InterfaceTarget

@InterfaceTarget(Interface = ILoginService::class)
class LoginService : ILoginService {
    override fun toLoginActivity(context: Context) {
        context.startActivity(Intent(context, LoginActivity::class.java))
    }
}