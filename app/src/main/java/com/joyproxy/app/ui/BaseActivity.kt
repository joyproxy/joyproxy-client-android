package com.joyproxy.app.ui

import android.content.Context
import androidx.activity.ComponentActivity
import com.joyproxy.app.data.LanguageRepository
import com.joyproxy.app.util.LocaleHelper

abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = LanguageRepository.getLanguageSync(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, language))
    }
}
