package com.joyproxy.app.config

import java.util.Locale

enum class AppLanguage(val tag: String) {
    EN("en"),
    ZH("zh"),
    ;

    fun toLocale(): Locale =
        when (this) {
            EN -> Locale.ENGLISH
            ZH -> Locale.SIMPLIFIED_CHINESE
        }

    companion object {
        val DEFAULT = EN

        fun fromTag(tag: String?): AppLanguage = entries.find { it.tag == tag } ?: DEFAULT
    }
}
