package com.joyproxy.app.data

import android.content.Context
import com.joyproxy.app.config.AppLanguage

class LanguageRepository(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLanguage(): AppLanguage = AppLanguage.fromTag(prefs.getString(KEY, null))

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY, language.tag).apply()
    }

    companion object {
        private const val PREFS_NAME = "joyproxy_language"
        private const val KEY = "app_language"

        fun getLanguageSync(context: Context): AppLanguage =
            runCatching { LanguageRepository(context).getLanguage() }
                .getOrDefault(AppLanguage.DEFAULT)
    }
}
