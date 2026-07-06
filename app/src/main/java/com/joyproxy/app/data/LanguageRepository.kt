package com.joyproxy.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.joyproxy.app.config.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class LanguageRepository(private val context: Context) {
    private val languageKey = stringPreferencesKey("app_language")

    val language: Flow<AppLanguage> =
        context.appPreferencesStore.data.map { prefs ->
            AppLanguage.fromTag(prefs[languageKey])
        }

    suspend fun setLanguage(language: AppLanguage) {
        context.appPreferencesStore.edit { prefs ->
            prefs[languageKey] = language.tag
        }
    }

    companion object {
        fun getLanguageSync(context: Context): AppLanguage =
            runBlocking {
                LanguageRepository(context).language.first()
            }
    }
}
