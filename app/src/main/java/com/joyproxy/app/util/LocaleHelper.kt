package com.joyproxy.app.util

import android.content.Context
import android.content.res.Configuration
import com.joyproxy.app.config.AppLanguage
import java.util.Locale

object LocaleHelper {
    fun wrap(context: Context, language: AppLanguage): Context {
        val locale = language.toLocale()
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
